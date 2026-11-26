package de.topfsecret.app.nutrition

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class OpenFoodFactsProduct(
    val barcode: String,
    val name: String,
    val brand: String?,
    val caloriesKcalPer100: Double?,
    val carbohydratesGPer100: Double?,
    val proteinGPer100: Double?,
    val fatGPer100: Double?,
    val sourceUrl: String
) {
    val hasNutrition: Boolean
        get() = caloriesKcalPer100 != null || carbohydratesGPer100 != null ||
            proteinGPer100 != null || fatGPer100 != null
}

object OpenFoodFactsClient {
    suspend fun lookupBarcode(barcode: String): Result<OpenFoodFactsProduct> =
        withContext(Dispatchers.IO) {
            runCatching {
                require(barcode.matches(Regex("[0-9]{8,14}"))) { "Ungültiger Barcode." }
                val url = URL(
                    "https://world.openfoodfacts.org/api/v3/product/$barcode" +
                        "?fields=code,product_name,product_name_de,brands,nutriments"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 12_000
                    setRequestProperty(
                        "User-Agent",
                        "TopfSecret/0.6.0 (https://github.com/firetvstick2k2-afk/Topf_Secret)"
                    )
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val status = connection.responseCode
                    if (status == 404) error("Dieser Barcode ist bei Open Food Facts nicht bekannt.")
                    if (status !in 200..299) error("Open Food Facts ist gerade nicht erreichbar (HTTP $status).")
                    val json = connection.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(json)
                    val product = root.optJSONObject("product")
                        ?: error("Zu diesem Barcode wurden keine Produktdaten gefunden.")
                    val name = product.optString("product_name_de")
                        .takeIf { it.isNotBlank() && it != "null" }
                        ?: product.optString("product_name").takeIf { it.isNotBlank() && it != "null" }
                        ?: error("Der Barcode ist bekannt, aber der Produktname fehlt.")
                    val nutrients = product.optJSONObject("nutriments") ?: JSONObject()
                    fun number(vararg keys: String): Double? {
                        for (key in keys) {
                            if (nutrients.has(key) && !nutrients.isNull(key)) {
                                val value = nutrients.optDouble(key, Double.NaN)
                                if (value.isFinite() && value >= 0.0) return value
                            }
                        }
                        return null
                    }
                    val kcal = number("energy-kcal_100g")
                        ?: number("energy-kj_100g")?.div(4.184)
                    OpenFoodFactsProduct(
                        barcode = barcode,
                        name = name.trim(),
                        brand = product.optString("brands").takeIf { it.isNotBlank() && it != "null" },
                        caloriesKcalPer100 = kcal,
                        carbohydratesGPer100 = number("carbohydrates_100g"),
                        proteinGPer100 = number("proteins_100g"),
                        fatGPer100 = number("fat_100g"),
                        sourceUrl = "https://world.openfoodfacts.org/product/$barcode"
                    )
                } finally {
                    connection.disconnect()
                }
            }
        }
    suspend fun searchProducts(query: String, limit: Int = 20): Result<List<OpenFoodFactsProduct>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val term = query.trim()
                require(term.length >= 2) { "Bitte mindestens zwei Suchzeichen eingeben." }
                val encoded = java.net.URLEncoder.encode(term, Charsets.UTF_8.name())
                val url = URL(
                    "https://world.openfoodfacts.org/cgi/search.pl?action=process&search_terms=$encoded" +
                        "&json=1&page_size=${limit.coerceIn(1, 30)}" +
                        "&fields=code,product_name,product_name_de,brands,nutriments"
                )
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 15_000
                    setRequestProperty("User-Agent", "TopfSecret/0.6.0 (https://github.com/firetvstick2k2-afk/Topf_Secret)")
                    setRequestProperty("Accept", "application/json")
                }
                try {
                    val status = connection.responseCode
                    if (status !in 200..299) error("Produktsuche nicht erreichbar (HTTP $status).")
                    val root = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                    val products = root.optJSONArray("products") ?: org.json.JSONArray()
                    (0 until products.length()).mapNotNull { index ->
                        val item = products.optJSONObject(index) ?: return@mapNotNull null
                        val code = item.optString("code").takeIf { it.matches(Regex("[0-9]{8,14}")) }
                            ?: return@mapNotNull null
                        val name = item.optString("product_name_de").takeIf { it.isNotBlank() && it != "null" }
                            ?: item.optString("product_name").takeIf { it.isNotBlank() && it != "null" }
                            ?: return@mapNotNull null
                        val nutrients = item.optJSONObject("nutriments") ?: JSONObject()
                        fun number(key: String): Double? {
                            if (!nutrients.has(key) || nutrients.isNull(key)) return null
                            val value = nutrients.optDouble(key, Double.NaN)
                            return value.takeIf { it.isFinite() && it >= 0.0 }
                        }
                        OpenFoodFactsProduct(
                            barcode = code,
                            name = name.trim(),
                            brand = item.optString("brands").takeIf { it.isNotBlank() && it != "null" },
                            caloriesKcalPer100 = number("energy-kcal_100g") ?: number("energy-kj_100g")?.div(4.184),
                            carbohydratesGPer100 = number("carbohydrates_100g"),
                            proteinGPer100 = number("proteins_100g"),
                            fatGPer100 = number("fat_100g"),
                            sourceUrl = "https://world.openfoodfacts.org/product/$code"
                        )
                    }.distinctBy { it.barcode }
                } finally {
                    connection.disconnect()
                }
            }
        }

}
