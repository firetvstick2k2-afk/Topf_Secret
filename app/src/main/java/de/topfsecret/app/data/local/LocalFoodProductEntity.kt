package de.topfsecret.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

@Entity(tableName = "local_food_products")
data class LocalFoodProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brand: String? = null,
    val caloriesKcalPer100: Double? = null,
    val carbohydratesGPer100: Double? = null,
    val proteinGPer100: Double? = null,
    val fatGPer100: Double? = null,
    val sourceUrl: String,
    val sourceName: String = "Open Food Facts",
    val quality: String = "VERIFIED_LOCAL",
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface LocalFoodProductDao {
    @Query("SELECT * FROM local_food_products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): LocalFoodProductEntity?

    @Query("SELECT * FROM local_food_products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name COLLATE NOCASE LIMIT 30")
    suspend fun search(query: String): List<LocalFoodProductEntity>

    @Upsert
    suspend fun save(product: LocalFoodProductEntity)
}
