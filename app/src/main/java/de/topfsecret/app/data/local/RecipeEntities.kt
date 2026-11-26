package de.topfsecret.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "recipes", indices = [Index(value = ["title"]), Index(value = ["category"]), Index(value = ["isFavorite"])])
data class RecipeEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val description: String = "", val category: String = "", val servings: Int = 2, val preparationMinutes: Int? = null, val cookingMinutes: Int? = null, val imageUri: String? = null, val sourceUrl: String? = null, val notes: String = "", val caloriesPerServing: Double? = null, val carbohydratesPerServing: Double? = null, val proteinPerServing: Double? = null, val fatPerServing: Double? = null, val isFavorite: Boolean = false, val createdAt: Long = System.currentTimeMillis(), val updatedAt: Long = System.currentTimeMillis())

@Entity(tableName = "ingredients", foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)], indices = [Index("recipeId"), Index("barcode")])
data class IngredientEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val recipeId: Long, val position: Int, val name: String, val amount: String = "", val note: String = "", val barcode: String? = null, val caloriesPer100: Double? = null, val carbohydratesPer100: Double? = null, val proteinPer100: Double? = null, val fatPer100: Double? = null, @ColumnInfo(defaultValue = "'100g'") val nutritionBasis: String = "100g", val nutritionSource: String? = null)

@Entity(tableName = "preparation_steps", foreignKeys = [ForeignKey(entity = RecipeEntity::class, parentColumns = ["id"], childColumns = ["recipeId"], onDelete = ForeignKey.CASCADE)], indices = [Index("recipeId")])
data class PreparationStepEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val recipeId: Long, val position: Int, val instruction: String)

data class RecipeWithDetails(@Embedded val recipe: RecipeEntity, @Relation(parentColumn = "id", entityColumn = "recipeId") val ingredients: List<IngredientEntity>, @Relation(parentColumn = "id", entityColumn = "recipeId") val steps: List<PreparationStepEntity>)
