package de.topfsecret.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Index
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "bls_foods", indices = [Index(value = ["germanName"])])
data class BlsFoodEntity(
    @PrimaryKey val code: String,
    val germanName: String,
    val englishName: String?,
    val kcalPer100: Double?,
    val proteinGPer100: Double?,
    val carbohydratesGPer100: Double?,
    val fatGPer100: Double?,
    val kcalOrigin: String?,
    val proteinOrigin: String?,
    val carbohydratesOrigin: String?,
    val fatOrigin: String?,
    val kcalReference: String?,
    val proteinReference: String?,
    val carbohydratesReference: String?,
    val fatReference: String?
)

@Dao
interface BlsFoodDao {
    @Query("SELECT * FROM bls_foods WHERE germanName LIKE '%' || :query || '%' OR englishName LIKE '%' || :query || '%' ORDER BY germanName COLLATE NOCASE LIMIT 40")
    suspend fun search(query: String): List<BlsFoodEntity>

    @Query("SELECT COUNT(*) FROM bls_foods")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BlsFoodEntity>)

    @Query("DELETE FROM bls_foods")
    suspend fun clearAll()
}
