package de.topfsecret.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecipeEntity::class, IngredientEntity::class, PreparationStepEntity::class, LocalFoodProductEntity::class], version = 3, exportSchema = true)
abstract class TopfSecretDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun localFoodProductDao(): LocalFoodProductDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN barcode TEXT")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN caloriesPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN carbohydratesPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN proteinPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN fatPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN nutritionBasis TEXT NOT NULL DEFAULT '100g'")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN nutritionSource TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ingredients_barcode ON ingredients(barcode)")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS local_food_products (
                        barcode TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        brand TEXT,
                        caloriesKcalPer100 REAL,
                        carbohydratesGPer100 REAL,
                        proteinGPer100 REAL,
                        fatGPer100 REAL,
                        sourceUrl TEXT NOT NULL,
                        sourceName TEXT NOT NULL,
                        quality TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        @Volatile private var INSTANCE: TopfSecretDatabase? = null
        fun getInstance(context: Context): TopfSecretDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, TopfSecretDatabase::class.java, "topf_secret.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { INSTANCE = it }
        }
    }
}
