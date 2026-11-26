package de.topfsecret.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RecipeEntity::class,
        IngredientEntity::class,
        PreparationStepEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class TopfSecretDatabase : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: TopfSecretDatabase? = null

        fun getInstance(context: Context): TopfSecretDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TopfSecretDatabase::class.java,
                    "topf_secret.db"
                ).build().also { INSTANCE = it }
            }
    }
}
