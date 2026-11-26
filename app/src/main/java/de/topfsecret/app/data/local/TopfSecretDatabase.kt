package de.topfsecret.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecipeEntity::class, IngredientEntity::class, PreparationStepEntity::class, LocalFoodProductEntity::class, BlsFoodEntity::class], version = 7, exportSchema = true)
abstract class TopfSecretDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun localFoodProductDao(): LocalFoodProductDao
    abstract fun blsFoodDao(): BlsFoodDao

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
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bls_foods (
                        code TEXT NOT NULL PRIMARY KEY,
                        germanName TEXT NOT NULL,
                        englishName TEXT,
                        kcalPer100 REAL,
                        proteinGPer100 REAL,
                        carbohydratesGPer100 REAL,
                        fatGPer100 REAL,
                        kcalOrigin TEXT,
                        proteinOrigin TEXT,
                        carbohydratesOrigin TEXT,
                        fatOrigin TEXT,
                        kcalReference TEXT,
                        proteinReference TEXT,
                        carbohydratesReference TEXT,
                        fatReference TEXT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bls_foods_germanName ON bls_foods(germanName)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE bls_foods ADD COLUMN nutrientsJson TEXT NOT NULL DEFAULT '{}' ")
                db.execSQL("ALTER TABLE bls_foods ADD COLUMN note TEXT")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ingredients ADD COLUMN sugarsPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN fiberPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN saltPer100 REAL")
                db.execSQL("ALTER TABLE ingredients ADD COLUMN saturatedFatPer100 REAL")
                db.execSQL("ALTER TABLE local_food_products ADD COLUMN sugarsGPer100 REAL")
                db.execSQL("ALTER TABLE local_food_products ADD COLUMN fiberGPer100 REAL")
                db.execSQL("ALTER TABLE local_food_products ADD COLUMN saltGPer100 REAL")
                db.execSQL("ALTER TABLE local_food_products ADD COLUMN saturatedFatGPer100 REAL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN sugarsPerServing REAL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN fiberPerServing REAL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN saltPerServing REAL")
                db.execSQL("ALTER TABLE recipes ADD COLUMN saturatedFatPerServing REAL")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_food_products ADD COLUMN nutritionBasis TEXT NOT NULL DEFAULT '100g'")
            }
        }
        @Volatile private var INSTANCE: TopfSecretDatabase? = null
        fun getInstance(context: Context): TopfSecretDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, TopfSecretDatabase::class.java, "topf_secret.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7).build().also { INSTANCE = it }
        }
    }
}
