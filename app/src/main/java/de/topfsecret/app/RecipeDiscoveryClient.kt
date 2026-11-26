package de.topfsecret.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class DiscoveryRecipe(
    val id: String,
    val title: String,
    val imageUrl: String,
    val category: String,
    val area: String,
    val sourceUrl: String
)

object RecipeDiscoveryClient {
    private const val BASE = "https://www.themealdb.com/api/json/v1/1/"

    suspend fun search(query: String): List<DiscoveryRecipe> = request("search.php?s=${java.net.URLEncoder.encode(query, "UTF-8")}")
    suspend fun byCategory(category: String): List<DiscoveryRecipe> = request("filter.php?c=${java.net.URLEncoder.encode(category, "UTF-8")}")
    suspend fun popular(): List<DiscoveryRecipe> = request("filter.php?c=Chicken")

    private suspend fun request(path: String): List<DiscoveryRecipe> = withContext(Dispatchers.IO) {
        val connection = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 12_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            require(connection.responseCode in 200..299) { "Die Rezeptdatenbank ist gerade nicht erreichbar." }
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val meals = json.optJSONArray("meals") ?: return@withContext emptyList()
            (0 until meals.length()).mapNotNull { i ->
                val item = meals.optJSONObject(i) ?: return@mapNotNull null
                val id = item.optString("idMeal")
                val title = item.optString("strMeal")
                val image = item.optString("strMealThumb")
                if (id.isBlank() || title.isBlank() || image.isBlank()) null
                else DiscoveryRecipe(
                    id = id,
                    title = title,
                    imageUrl = image,
                    category = item.optString("strCategory"),
                    area = item.optString("strArea"),
                    sourceUrl = item.optString("strSource")
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun details(id: String): ImportedRecipe = withContext(Dispatchers.IO) {
        require(id.matches(Regex("""\d+"""))) { "Ungültige TheMealDB-Rezeptnummer." }
        val connection = (URL(BASE + "lookup.php?i=$id").openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 12_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            require(connection.responseCode in 200..299) { "Das vollständige Rezept ist gerade nicht erreichbar." }
            val root = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val meal = root.optJSONArray("meals")?.optJSONObject(0)
                ?: throw IllegalArgumentException("Dieses Rezept wurde nicht gefunden.")
            val ingredients = (1..20).mapNotNull { index ->
                val name = meal.optString("strIngredient$index").trim()
                if (name.isBlank() || name.equals("null", true)) null else {
                    val measure = meal.optString("strMeasure$index").trim().takeUnless { it.isBlank() || it.equals("null", true) }
                    listOfNotNull(measure, name).joinToString(" ")
                }
            }
            require(ingredients.isNotEmpty()) { "Für dieses Rezept wurden keine Zutaten geliefert." }
            val instructions = meal.optString("strInstructions").replace("\r", "")
                .split(Regex("""\n+""")).map { it.trim() }.filter(String::isNotBlank)
            val category = meal.optString("strCategory").ifBlank { "TheMealDB" }
            val area = meal.optString("strArea").trim()
            val source = meal.optString("strSource").takeIf { it.startsWith("http://") || it.startsWith("https://") }
                ?: "https://www.themealdb.com/meal/$id"
            ImportedRecipe(
                title = meal.optString("strMeal").trim(),
                description = listOf(category, area).filter(String::isNotBlank).joinToString(" · "),
                category = category,
                servings = 2,
                preparationMinutes = null,
                cookingMinutes = null,
                ingredients = ingredients,
                steps = instructions,
                sourceUrl = source,
                imageUrl = meal.optString("strMealThumb").takeIf { it.startsWith("http://") || it.startsWith("https://") },
                imageUrls = listOfNotNull(meal.optString("strMealThumb").takeIf { it.startsWith("http://") || it.startsWith("https://") })
            )
        } finally {
            connection.disconnect()
        }
    }
}
