package de.topfsecret.app.data

import de.topfsecret.app.data.local.IngredientEntity
import de.topfsecret.app.data.local.PreparationStepEntity
import de.topfsecret.app.data.local.RecipeDao
import de.topfsecret.app.data.local.RecipeEntity
import de.topfsecret.app.data.local.RecipeWithDetails
import de.topfsecret.app.nutrition.OpenFoodFactsProduct
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {
    fun observeRecipes(): Flow<List<RecipeWithDetails>> = recipeDao.observeRecipes()
    fun observeRecipe(recipeId: Long): Flow<RecipeWithDetails?> = recipeDao.observeRecipe(recipeId)

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredientLines: List<String>,
        stepLines: List<String>,
        nutritionByIngredientName: Map<String, OpenFoodFactsProduct> = emptyMap()
    ): Long {
        val ingredients = ingredientLines.map(String::trim).filter(String::isNotEmpty).mapIndexed { index, line ->
            val match = Regex("""^(\d+(?:[.,]\d+)?\s*(?:kg|g|mg|l|ml|cl|dl|EL|TL|Esslöffel|Teelöffel|Tasse(?:n)?|Stk\.?|Stück|Prise|Pck\.?|Packung|Bund|Dose|Becher)?)(?:\s+)(.+)$""", RegexOption.IGNORE_CASE).matchEntire(line)
            val amount = match?.groupValues?.get(1)?.trim().orEmpty()
            val name = match?.groupValues?.get(2)?.trim() ?: line
            val product = nutritionByIngredientName[name]
            IngredientEntity(
                recipeId = recipe.id,
                position = index,
                amount = amount,
                name = name,
                barcode = product?.barcode,
                caloriesPer100 = product?.caloriesKcalPer100,
                carbohydratesPer100 = product?.carbohydratesGPer100,
                proteinPer100 = product?.proteinGPer100,
                fatPer100 = product?.fatGPer100,
                sugarsPer100 = product?.sugarsGPer100,
                fiberPer100 = product?.fiberGPer100,
                saltPer100 = product?.saltGPer100,
                saturatedFatPer100 = product?.saturatedFatGPer100,
                nutritionBasis = "100g",
                nutritionSource = product?.sourceUrl
            )
        }
        val steps = stepLines.map(String::trim).filter(String::isNotEmpty).mapIndexed { index, line ->
            PreparationStepEntity(recipeId = recipe.id, position = index, instruction = line)
        }
        return recipeDao.saveRecipe(recipe, ingredients, steps)
    }

    suspend fun deleteRecipe(recipeId: Long) { recipeDao.deleteRecipe(recipeId) }
    suspend fun setFavorite(recipe: RecipeEntity, isFavorite: Boolean) {
        recipeDao.updateRecipe(recipe.copy(isFavorite = isFavorite, updatedAt = System.currentTimeMillis()))
    }
}
