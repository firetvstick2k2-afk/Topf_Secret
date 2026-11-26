package de.topfsecret.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ImportedRecipe(
    val title: String,
    val description: String,
    val category: String,
    val servings: Int,
    val preparationMinutes: Int?,
    val cookingMinutes: Int?,
    val ingredients: List<String>,
    val steps: List<String>,
    val sourceUrl: String,
    val caloriesPerServing: Double? = null,
    val carbohydratesPerServing: Double? = null,
    val sugarsPerServing: Double? = null,
    val fiberPerServing: Double? = null,
    val proteinPerServing: Double? = null,
    val fatPerServing: Double? = null,
    val saturatedFatPerServing: Double? = null,
    val saltPerServing: Double? = null
)

object OnlineRecipeImporter {
    suspend fun importFromUrl(rawUrl: String): ImportedRecipe = withContext(Dispatchers.IO) {
        val parsed = URL(rawUrl.trim())
        require(parsed.protocol == "https" || parsed.protocol == "http") { "Bitte einen gültigen http- oder https-Rezeptlink eingeben." }
        require(!parsed.host.isNullOrBlank()) { "Der Rezeptlink ist ungültig." }
        val connection = (parsed.openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 12_000
            readTimeout = 15_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "TopfSecret/0.7 (recipe metadata import)")
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
        }
        try {
            connection.connect()
            require(connection.responseCode in 200..299) { "Die Webseite antwortet mit HTTP ${connection.responseCode}." }
            val finalUrl = connection.url.toString()
            val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val output = StringBuilder()
                val buffer = CharArray(8192)
                var total = 0
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= 1_500_000) { "Die Webseite ist zu groß für den Rezeptimport." }
                    output.append(buffer, 0, count)
                }
                output.toString()
            }
            parseRecipeJsonLd(body, finalUrl)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRecipeJsonLd(html: String, sourceUrl: String): ImportedRecipe {
        val scripts = Regex(
            """<script[^>]*type\s*=\s*["']application/ld\+json["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).findAll(html)
        for (script in scripts) {
            val raw = script.groupValues[1].trim()
            val root = runCatching { JSONObject(raw) }.getOrNull()
            if (root != null) {
                findRecipe(root)?.let { return toImportedRecipe(it, sourceUrl) }
            }
            val array = runCatching { JSONArray(raw) }.getOrNull()
            if (array != null) {
                for (index in 0 until array.length()) {
                    val value = array.optJSONObject(index) ?: continue
                    findRecipe(value)?.let { return toImportedRecipe(it, sourceUrl) }
                }
            }
        }
        throw IllegalArgumentException("Auf dieser Seite wurden keine strukturierten Rezeptdaten gefunden. Nicht jede Webseite stellt Zutaten und Zubereitung maschinenlesbar bereit.")
    }

    private fun findRecipe(value: JSONObject): JSONObject? {
        val type = value.opt("@type")
        val isRecipe = when (type) {
            is String -> type.substringAfterLast("/").equals("Recipe", true)
            is JSONArray -> (0 until type.length()).any { type.optString(it).substringAfterLast("/").equals("Recipe", true) }
            else -> false
        }
        if (isRecipe) return value
        value.optJSONArray("@graph")?.let { graph ->
            for (i in 0 until graph.length()) graph.optJSONObject(i)?.let { findRecipe(it)?.let { found -> return found } }
        }
        value.optJSONArray("itemListElement")?.let { items ->
            for (i in 0 until items.length()) items.optJSONObject(i)?.let { findRecipe(it)?.let { found -> return found } }
        }
        return null
    }

    private fun toImportedRecipe(json: JSONObject, sourceUrl: String): ImportedRecipe {
        val title = json.optString("name").trim()
        require(title.isNotBlank()) { "Die Rezeptseite enthält keinen Rezeptnamen." }
        val ingredientsJson = json.optJSONArray("recipeIngredient")
            ?: throw IllegalArgumentException("Die Rezeptseite stellt keine Zutatenliste bereit.")
        val ingredients = (0 until ingredientsJson.length()).map { ingredientsJson.optString(it).trim() }.filter(String::isNotBlank)
        require(ingredients.isNotEmpty()) { "Die Rezeptseite enthält keine lesbaren Zutaten." }
        val instructions = mutableListOf<String>()
        fun readInstructions(value: Any?) {
            when (value) {
                is String -> value.trim().takeIf(String::isNotBlank)?.let(instructions::add)
                is JSONArray -> for (i in 0 until value.length()) readInstructions(value.opt(i))
                is JSONObject -> {
                    val text = value.optString("text")
                    if (text.isNotBlank()) instructions.add(text.trim())
                    else readInstructions(value.opt("itemListElement"))
                }
            }
        }
        readInstructions(json.opt("recipeInstructions"))
        val yieldText = when (val yield = json.opt("recipeYield")) {
            is JSONArray -> yield.optString(0)
            else -> yield?.toString().orEmpty()
        }
        val servings = Regex("""\d+""").find(yieldText)?.value?.toIntOrNull()?.coerceIn(1, 100) ?: 2
        val nutrition = json.optJSONObject("nutrition")
        fun nutrient(vararg keys: String): Double? {
            if (nutrition == null) return null
            for (key in keys) {
                val value = nutrition.opt(key)
                val parsed = parseNutritionValue(value) ?: continue
                return parsed
            }
            return null
        }
        val calories = nutrient("calories")
        val sodium = nutrient("sodiumContent")
        val salt = nutrient("saltContent") ?: sodium?.let { it * 2.5 }
        return ImportedRecipe(
            title = title,
            description = json.optString("description").trim(),
            category = json.optString("recipeCategory").ifBlank { "Importiert" },
            servings = servings,
            preparationMinutes = parseDuration(json.optString("prepTime")),
            cookingMinutes = parseDuration(json.optString("cookTime")),
            ingredients = ingredients,
            steps = instructions,
            sourceUrl = sourceUrl,
            caloriesPerServing = calories,
            carbohydratesPerServing = nutrient("carbohydrateContent"),
            sugarsPerServing = nutrient("sugarContent"),
            fiberPerServing = nutrient("fiberContent"),
            proteinPerServing = nutrient("proteinContent"),
            fatPerServing = nutrient("fatContent"),
            saturatedFatPerServing = nutrient("saturatedFatContent"),
            saltPerServing = salt
        )
    }

    private fun parseNutritionValue(value: Any?): Double? {
        val raw = when (value) {
            is Number -> value.toString()
            is String -> value
            is JSONObject -> value.optString("value")
            else -> return null
        }
        val number = Regex("""-?\d+(?:[.,]\d+)?""").find(raw)?.value
            ?.replace(',', '.')?.toDoubleOrNull() ?: return null
        val unit = raw.lowercase()
        val grams = when {
            Regex("""\bmg\b""").containsMatchIn(unit) -> number / 1000.0
            Regex("""\bkg\b""").containsMatchIn(unit) -> number * 1000.0
            Regex("""\b(g|gram|grams)\b""").containsMatchIn(unit) -> number
            else -> number
        }
        return grams.takeIf { it.isFinite() && it >= 0.0 }
    }

    private fun parseDuration(value: String): Int? {
        if (value.isBlank()) return null
        val match = Regex("""P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?)?""", RegexOption.IGNORE_CASE).matchEntire(value) ?: return null
        val days = match.groupValues[1].toIntOrNull() ?: 0
        val hours = match.groupValues[2].toIntOrNull() ?: 0
        val minutes = match.groupValues[3].toIntOrNull() ?: 0
        return (days * 1440 + hours * 60 + minutes).takeIf { it > 0 }
    }
}
