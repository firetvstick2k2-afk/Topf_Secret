package de.topfsecret.app.data

import de.topfsecret.app.data.local.IngredientEntity
import de.topfsecret.app.data.local.PreparationStepEntity
import de.topfsecret.app.data.local.RecipeDao
import de.topfsecret.app.data.local.RecipeEntity
import de.topfsecret.app.data.local.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

class RecipeRepository(
    private val recipeDao: RecipeDao
) {
    fun observeRecipes(): Flow<List<RecipeWithDetails>> =
        recipeDao.observeRecipes()

    fun observeRecipe(recipeId: Long): Flow<RecipeWithDetails?> =
        recipeDao.observeRecipe(recipeId)

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredientLines: List<String>,
        stepLines: List<String>
    ): Long {
        val ingredients = ingredientLines
            .map(String::trim)
            .filter(String::isNotEmpty)
            .mapIndexed { index, line ->
                IngredientEntity(
                    recipeId = recipe.id,
                    position = index,
                    name = line
                )
            }

        val steps = stepLines
            .map(String::trim)
            .filter(String::isNotEmpty)
            .mapIndexed { index, line ->
                PreparationStepEntity(
                    recipeId = recipe.id,
                    position = index,
                    instruction = line
                )
            }

        return recipeDao.saveRecipe(recipe, ingredients, steps)
    }

    suspend fun deleteRecipe(recipeId: Long) {
        recipeDao.deleteRecipe(recipeId)
    }

    suspend fun setFavorite(recipe: RecipeEntity, isFavorite: Boolean) {
        recipeDao.updateRecipe(
            recipe.copy(
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
