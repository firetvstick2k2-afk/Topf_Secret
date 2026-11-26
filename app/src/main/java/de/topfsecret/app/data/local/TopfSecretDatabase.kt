package de.topfsecret.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecipeEntity::class, IngredientEntity::class, PreparationStepEntity::class], version = 2, exportSchema = true)
abstract class TopfSecretDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

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
        @Volatile private var INSTANCE: TopfSecretDatabase? = null
        fun getInstance(context: Context): TopfSecretDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, TopfSecretDatabase::class.java, "topf_secret.db")
                .addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
        }
    }
}
