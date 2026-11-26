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
}
