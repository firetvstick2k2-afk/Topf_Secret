package de.topfsecret.app.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientMeasureConverterTest {
    @Test fun convertsMassUnitsToGrams() {
        assertEquals(0.5, IngredientMeasureConverter.convert(500.0, "mg")?.value ?: -1.0, 0.0001)
        assertEquals(1000.0, IngredientMeasureConverter.convert(1.0, "kg")?.value ?: -1.0, 0.0001)
        assertEquals(250.0, IngredientMeasureConverter.convert(250.0, "g")?.value ?: -1.0, 0.0001)
    }

    @Test fun convertsVolumeUnitsToMillilitres() {
        assertEquals(1000.0, IngredientMeasureConverter.convert(1.0, "l")?.value ?: -1.0, 0.0001)
        assertEquals(250.0, IngredientMeasureConverter.convert(2.5, "dl")?.value ?: -1.0, 0.0001)
        assertEquals(30.0, IngredientMeasureConverter.convert(2.0, "EL")?.value ?: -1.0, 0.0001)
        assertEquals(5.0, IngredientMeasureConverter.convert(1.0, "TL")?.value ?: -1.0, 0.0001)
    }

    @Test fun volumeToMassNeedsKnownFoodDensity() {
        assertEquals(92.0, IngredientMeasureConverter.volumeToGrams(100.0, "Olivenöl")?.value ?: -1.0, 0.0001)
        assertNull(IngredientMeasureConverter.volumeToGrams(100.0, "Mehl"))
    }

    @Test fun parsesGermanDecimalAmount() {
        val parsed = IngredientMeasureConverter.parseAmount("1,5 EL")
        assertEquals(1.5, parsed?.first ?: -1.0, 0.0001)
        assertEquals("EL", parsed?.second)
    }
    
    @Test fun calculatesSugarFiberSaltAndSaturatedFat() {
        val result = NutritionPer100(
            caloriesKcal = 200.0,
            carbohydratesG = 30.0,
            proteinG = 5.0,
            fatG = 8.0,
            basis = IngredientMeasureConverter.Basis.GRAMS,
            sugarsG = 10.0,
            fiberG = 4.0,
            saltG = 1.2,
            saturatedFatG = 2.0
        ).forAmount(50.0, "g", "Testlebensmittel")

        assertEquals(5.0, result?.sugarsG ?: -1.0, 0.0001)
        assertEquals(2.0, result?.fiberG ?: -1.0, 0.0001)
        assertEquals(0.6, result?.saltG ?: -1.0, 0.0001)
        assertEquals(1.0, result?.saturatedFatG ?: -1.0, 0.0001)
        assertTrue(result?.sugarsComplete == true)
    }

    @Test fun missingNutrientsStayMarkedAsUnavailable() {
        val result = NutritionPer100(
            caloriesKcal = 100.0,
            carbohydratesG = 20.0,
            proteinG = 2.0,
            fatG = 1.0,
            basis = IngredientMeasureConverter.Basis.GRAMS
        ).forAmount(100.0, "g", "Testlebensmittel")

        assertFalse(result?.sugarsComplete ?: true)
        assertFalse(result?.fiberComplete ?: true)
        assertFalse(result?.saltComplete ?: true)
        assertFalse(result?.saturatedFatComplete ?: true)
    }

    @Test fun piecesCannotBeConvertedWithoutAWeight() {
        val nutrition = NutritionPer100(100.0, 20.0, 3.0, 2.0, IngredientMeasureConverter.Basis.GRAMS)
        assertNull(nutrition.forAmount(4.0, "Stück", "Semmelknödel"))
    }
}

