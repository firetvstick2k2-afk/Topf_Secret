package de.topfsecret.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RecipeImportRecord(
    val recipe: RecipeEntity,
    val ingredients: List<IngredientEntity>,
    val steps: List<PreparationStepEntity>
)

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY isFavorite DESC, title COLLATE NOCASE ASC")
    fun observeRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    fun observeRecipe(recipeId: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY title COLLATE NOCASE ASC")
    fun observeRecipesInCategory(category: String): Flow<List<RecipeWithDetails>>


    @Transaction
    @Query("SELECT * FROM recipes ORDER BY id ASC")
    suspend fun getAllRecipesWithDetails(): List<RecipeWithDetails>

    @Query("SELECT EXISTS(SELECT 1 FROM recipes WHERE title = :title COLLATE NOCASE AND ((sourceUrl = :sourceUrl) OR (sourceUrl IS NULL AND :sourceUrl IS NULL)))")
    suspend fun recipeExists(title: String, sourceUrl: String?): Boolean

    @Transaction
    suspend fun importRecipeWithDetails(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<PreparationStepEntity>
    ): Long {
        val newId = insertRecipe(recipe.copy(id = 0))
        if (ingredients.isNotEmpty()) {
            insertIngredients(ingredients.mapIndexed { index, item ->
                item.copy(id = 0, recipeId = newId, position = index)
            })
        }
        if (steps.isNotEmpty()) {
            insertSteps(steps.mapIndexed { index, item ->
                item.copy(id = 0, recipeId = newId, position = index)
            })
        }
        return newId
    }

    @Transaction
    suspend fun importRecipesWithDetails(records: List<RecipeImportRecord>): Int {
        records.forEach { record ->
            val newId = insertRecipe(record.recipe.copy(id = 0))
            if (record.ingredients.isNotEmpty()) {
                insertIngredients(record.ingredients.mapIndexed { index, item ->
                    item.copy(id = 0, recipeId = newId, position = index)
                })
            }
            if (record.steps.isNotEmpty()) {
                insertSteps(record.steps.mapIndexed { index, step ->
                    step.copy(id = 0, recipeId = newId, position = index)
                })
            }
        }
        return records.size
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Insert
    suspend fun insertSteps(steps: List<PreparationStepEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredients(recipeId: Long)

    @Query("DELETE FROM preparation_steps WHERE recipeId = :recipeId")
    suspend fun deleteSteps(recipeId: Long)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipe(recipeId: Long)

    @Transaction
    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<PreparationStepEntity>
    ): Long {
        val recipeId = if (recipe.id == 0L) {
            insertRecipe(recipe)
        } else {
            updateRecipe(recipe.copy(updatedAt = System.currentTimeMillis()))
            recipe.id
        }

        deleteIngredients(recipeId)
        deleteSteps(recipeId)

        if (ingredients.isNotEmpty()) {
            insertIngredients(ingredients.mapIndexed { index, ingredient ->
                ingredient.copy(id = 0, recipeId = recipeId, position = index)
            })
        }
        if (steps.isNotEmpty()) {
            insertSteps(steps.mapIndexed { index, step ->
                step.copy(id = 0, recipeId = recipeId, position = index)
            })
        }
        return recipeId
    }
}
