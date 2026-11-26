package de.topfsecret.app.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
