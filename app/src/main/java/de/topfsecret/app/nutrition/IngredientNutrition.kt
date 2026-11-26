package de.topfsecret.app.nutrition

import java.util.Locale

/** Converts common German recipe measures without assuming that ml equals g. */
object IngredientMeasureConverter {
    data class ConvertedAmount(val value: Double, val basis: Basis, val estimated: Boolean = false)
    enum class Basis { GRAMS, MILLILITRES }
    private val densityGramsPerMl = mapOf("wasser" to 1.0, "milch" to 1.03, "sahne" to 1.0, "olivenöl" to 0.92, "sonnenblumenöl" to 0.92, "öl" to 0.92, "honig" to 1.42, "sirup" to 1.30, "joghurt" to 1.03, "essig" to 1.01, "brühe" to 1.0, "tomatenpassata" to 1.04)

    fun convert(amount: Double, unit: String): ConvertedAmount? {
        if (!amount.isFinite() || amount < 0.0) return null
        return when (unit.trim().lowercase(Locale.GERMAN).replace(".", "")) {
            "mg" -> ConvertedAmount(amount / 1000.0, Basis.GRAMS)
            "g", "gramm" -> ConvertedAmount(amount, Basis.GRAMS)
            "kg" -> ConvertedAmount(amount * 1000.0, Basis.GRAMS)
            "ml" -> ConvertedAmount(amount, Basis.MILLILITRES)
            "cl" -> ConvertedAmount(amount * 10.0, Basis.MILLILITRES)
            "dl" -> ConvertedAmount(amount * 100.0, Basis.MILLILITRES)
            "l", "liter", "litre" -> ConvertedAmount(amount * 1000.0, Basis.MILLILITRES)
            "tl", "teelöffel" -> ConvertedAmount(amount * 5.0, Basis.MILLILITRES, true)
            "el", "esslöffel" -> ConvertedAmount(amount * 15.0, Basis.MILLILITRES, true)
            "tasse", "tassen" -> ConvertedAmount(amount * 200.0, Basis.MILLILITRES, true)
            else -> null
        }
    }

    fun volumeToGrams(millilitres: Double, ingredientName: String): ConvertedAmount? {
        val name = ingredientName.lowercase(Locale.GERMAN)
        val density = densityGramsPerMl.entries.firstOrNull { name.contains(it.key) }?.value ?: return null
        return ConvertedAmount(millilitres * density, Basis.GRAMS, true)
    }

    fun parseAmount(raw: String): Pair<Double, String>? {
        val match = Regex("""^\s*(\d+(?:[.,]\d+)?)\s*([\p{L}.]+)\s*$""").matchEntire(raw) ?: return null
        val value = match.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        return value to match.groupValues[2]
    }
}

data class NutritionPer100(val caloriesKcal: Double?, val carbohydratesG: Double?, val proteinG: Double?, val fatG: Double?, val basis: IngredientMeasureConverter.Basis) {
    data class Totals(val caloriesKcal: Double = 0.0, val carbohydratesG: Double = 0.0, val proteinG: Double = 0.0, val fatG: Double = 0.0, val calculatedIngredients: Int = 0, val uncalculatedIngredients: Int = 0, val estimatedMeasures: Boolean = false)

    fun forAmount(amount: Double, unit: String, ingredientName: String): Totals? {
        val converted = IngredientMeasureConverter.convert(amount, unit) ?: return null
        val normalized = if (basis == IngredientMeasureConverter.Basis.GRAMS) {
            if (converted.basis == IngredientMeasureConverter.Basis.GRAMS) converted else IngredientMeasureConverter.volumeToGrams(converted.value, ingredientName) ?: return null
        } else {
            if (converted.basis == IngredientMeasureConverter.Basis.MILLILITRES) converted else return null
        }
        val factor = normalized.value / 100.0
        return Totals((caloriesKcal ?: 0.0) * factor, (carbohydratesG ?: 0.0) * factor, (proteinG ?: 0.0) * factor, (fatG ?: 0.0) * factor, calculatedIngredients = 1, estimatedMeasures = converted.estimated || normalized.estimated)
    }
}

fun List<NutritionPer100.Totals>.sumNutrition(): NutritionPer100.Totals = fold(NutritionPer100.Totals()) { total, item ->
    total.copy(caloriesKcal = total.caloriesKcal + item.caloriesKcal, carbohydratesG = total.carbohydratesG + item.carbohydratesG, proteinG = total.proteinG + item.proteinG, fatG = total.fatG + item.fatG, calculatedIngredients = total.calculatedIngredients + item.calculatedIngredients, uncalculatedIngredients = total.uncalculatedIngredients + item.uncalculatedIngredients, estimatedMeasures = total.estimatedMeasures || item.estimatedMeasures)
}
