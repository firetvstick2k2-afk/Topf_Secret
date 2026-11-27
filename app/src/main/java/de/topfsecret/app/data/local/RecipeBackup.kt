package de.topfsecret.app.data.local

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object RecipeBackup {
    private const val MANIFEST = "recipes.json"
    private const val MAX_BACKUP_BYTES = 100L * 1024L * 1024L

    suspend fun export(context: Context, destination: Uri, dao: RecipeDao): Int {
        val recipes = dao.getAllRecipesWithDetails()
        val manifest = JSONObject().put("format", "topf-secret-backup").put("version", 1)
        val items = JSONArray()
        val imageEntries = linkedMapOf<String, ByteArray>()
        recipes.forEach { details ->
            val recipe = details.recipe
            val item = JSONObject()
                .put("title", recipe.title).put("description", recipe.description)
                .put("category", recipe.category).put("servings", recipe.servings)
                .putNullable("preparationMinutes", recipe.preparationMinutes)
                .putNullable("cookingMinutes", recipe.cookingMinutes)
                .putNullable("sourceUrl", recipe.sourceUrl).put("notes", recipe.notes)
                .putNullable("caloriesPerServing", recipe.caloriesPerServing)
                .putNullable("carbohydratesPerServing", recipe.carbohydratesPerServing)
                .putNullable("proteinPerServing", recipe.proteinPerServing)
                .putNullable("fatPerServing", recipe.fatPerServing)
                .put("isFavorite", recipe.isFavorite)
                .put("createdAt", recipe.createdAt).put("updatedAt", recipe.updatedAt)
                .putNullable("sugarsPerServing", recipe.sugarsPerServing)
                .putNullable("fiberPerServing", recipe.fiberPerServing)
                .putNullable("saltPerServing", recipe.saltPerServing)
                .putNullable("saturatedFatPerServing", recipe.saturatedFatPerServing)
            val imageFile = managedImageFile(context, recipe.imageUri)
            if (imageFile != null && imageFile.isFile && imageFile.length() <= 8L * 1024L * 1024L) {
                val entryName = "images/${recipe.id}-${imageFile.name.replace(Regex("[^A-Za-z0-9._-]"), "_")}"
                imageEntries[entryName] = imageFile.readBytes()
                item.put("imageEntry", entryName)
                val source = File(imageFile.parentFile, imageFile.name + ".source.txt")
                if (source.isFile && source.length() <= 32_768) {
                    imageEntries["sources/${recipe.id}.txt"] = source.readBytes()
                }
            }
            val ingredients = JSONArray()
            details.ingredients.sortedBy { it.position }.forEach { ingredient ->
                ingredients.put(JSONObject()
                    .put("position", ingredient.position).put("name", ingredient.name)
                    .put("amount", ingredient.amount).put("note", ingredient.note)
                    .putNullable("barcode", ingredient.barcode)
                    .putNullable("caloriesPer100", ingredient.caloriesPer100)
                    .putNullable("carbohydratesPer100", ingredient.carbohydratesPer100)
                    .putNullable("proteinPer100", ingredient.proteinPer100)
                    .putNullable("fatPer100", ingredient.fatPer100)
                    .put("nutritionBasis", ingredient.nutritionBasis)
                    .putNullable("nutritionSource", ingredient.nutritionSource)
                    .putNullable("sugarsPer100", ingredient.sugarsPer100)
                    .putNullable("fiberPer100", ingredient.fiberPer100)
                    .putNullable("saltPer100", ingredient.saltPer100)
                    .putNullable("saturatedFatPer100", ingredient.saturatedFatPer100))
            }
            val steps = JSONArray()
            details.steps.sortedBy { it.position }.forEach { step ->
                steps.put(JSONObject().put("position", step.position).put("instruction", step.instruction))
            }
            item.put("ingredients", ingredients).put("steps", steps)
            items.put(item)
        }
        manifest.put("recipes", items)
        context.contentResolver.openOutputStream(destination, "w")?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(MANIFEST))
                zip.write(manifest.toString().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                imageEntries.forEach { (name, bytes) ->
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(bytes)
                    zip.closeEntry()
                }
            }
        } ?: throw IllegalStateException("Die Backup-Datei konnte nicht angelegt werden.")
        return recipes.size
    }

    suspend fun import(context: Context, source: Uri, dao: RecipeDao): Int {
        val entries = linkedMapOf<String, ByteArray>()
        var total = 0L
        context.contentResolver.openInputStream(source)?.use { input ->
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    require(!entry.isDirectory && !entry.name.startsWith("/") && !entry.name.contains("..")) {
                        "Die Backup-Datei enthält einen ungültigen Dateipfad."
                    }
                    val bytes = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val count = zip.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_BACKUP_BYTES) { "Das Backup ist größer als 100 MB." }
                        bytes.write(buffer, 0, count)
                    }
                    require(!entries.containsKey(entry.name)) { "Die Backup-Datei enthält doppelte Dateieinträge." }
                    entries[entry.name] = bytes.toByteArray()
                    zip.closeEntry()
                }
            }
        } ?: throw IllegalStateException("Die Backup-Datei konnte nicht geöffnet werden.")

        val jsonBytes = entries[MANIFEST]
            ?: throw IllegalArgumentException("Keine gültige Topf-Secret-Sicherung gefunden.")
        val root = JSONObject(String(jsonBytes, Charsets.UTF_8))
        require(root.optString("format") == "topf-secret-backup" && root.optInt("version") == 1) {
            "Dieses Backup-Format wird nicht unterstützt."
        }

        val recipes = root.optJSONArray("recipes") ?: JSONArray()
        val importedFiles = mutableListOf<File>()
        val importRecords = mutableListOf<RecipeImportRecord>()
        val seenKeys = mutableSetOf<Pair<String, String?>>()

        try {
            for (index in 0 until recipes.length()) {
                val item = recipes.getJSONObject(index)
                val title = item.optString("title").trim()
                require(title.isNotBlank()) { "Ein Rezept im Backup hat keinen Namen." }
                val sourceUrl = item.optNullableString("sourceUrl")
                val duplicateKey = title.lowercase(java.util.Locale.ROOT) to sourceUrl
                if (!seenKeys.add(duplicateKey)) continue
                if (dao.recipeExists(title, sourceUrl)) continue

                val imageEntry = item.optString("imageEntry")
                    .takeIf { it.isNotBlank() && it != "null" }
                val imageUri = imageEntry?.let { name ->
                    require(name.startsWith("images/") && !name.contains("..")) {
                        "Ein Rezeptbild hat einen ungültigen Dateipfad."
                    }
                    val bytes = entries[name]
                        ?: throw IllegalArgumentException("Ein Rezeptbild im Backup fehlt.")
                    val directory = File(context.filesDir, "recipe_images").apply {
                        if (!exists() && !mkdirs()) {
                            throw IllegalStateException("Der Rezeptbild-Ordner konnte nicht angelegt werden.")
                        }
                    }
                    val file = File(directory, "restore-${UUID.randomUUID()}.img")
                    importedFiles.add(file)
                    file.writeBytes(bytes)

                    val oldId = name.substringAfter("images/").substringBefore("-")
                    entries["sources/$oldId.txt"]?.let { sourceBytes ->
                        val sidecar = File(directory, file.name + ".source.txt")
                        importedFiles.add(sidecar)
                        sidecar.writeBytes(sourceBytes)
                    }
                    Uri.fromFile(file).toString()
                }

                val recipe = RecipeEntity(
                    title = title,
                    description = item.optString("description"),
                    category = item.optString("category"),
                    servings = item.optInt("servings", 2).coerceAtLeast(1),
                    preparationMinutes = item.nullableInt("preparationMinutes"),
                    cookingMinutes = item.nullableInt("cookingMinutes"),
                    imageUri = imageUri,
                    sourceUrl = sourceUrl,
                    notes = item.optString("notes"),
                    caloriesPerServing = item.nullableDouble("caloriesPerServing"),
                    carbohydratesPerServing = item.nullableDouble("carbohydratesPerServing"),
                    proteinPerServing = item.nullableDouble("proteinPerServing"),
                    fatPerServing = item.nullableDouble("fatPerServing"),
                    isFavorite = item.optBoolean("isFavorite"),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
                    sugarsPerServing = item.nullableDouble("sugarsPerServing"),
                    fiberPerServing = item.nullableDouble("fiberPerServing"),
                    saltPerServing = item.nullableDouble("saltPerServing"),
                    saturatedFatPerServing = item.nullableDouble("saturatedFatPerServing")
                )
                val ingredientArray = item.optJSONArray("ingredients") ?: JSONArray()
                val ingredients = (0 until ingredientArray.length()).map { ingredientIndex ->
                    val value = ingredientArray.getJSONObject(ingredientIndex)
                    IngredientEntity(
                        recipeId = 0,
                        position = value.optInt("position", ingredientIndex),
                        name = value.optString("name"),
                        amount = value.optString("amount"),
                        note = value.optString("note"),
                        barcode = value.optNullableString("barcode"),
                        caloriesPer100 = value.nullableDouble("caloriesPer100"),
                        carbohydratesPer100 = value.nullableDouble("carbohydratesPer100"),
                        proteinPer100 = value.nullableDouble("proteinPer100"),
                        fatPer100 = value.nullableDouble("fatPer100"),
                        nutritionBasis = value.optString("nutritionBasis", "100g"),
                        nutritionSource = value.optNullableString("nutritionSource"),
                        sugarsPer100 = value.nullableDouble("sugarsPer100"),
                        fiberPer100 = value.nullableDouble("fiberPer100"),
                        saltPer100 = value.nullableDouble("saltPer100"),
                        saturatedFatPer100 = value.nullableDouble("saturatedFatPer100")
                    )
                }
                val stepArray = item.optJSONArray("steps") ?: JSONArray()
                val steps = (0 until stepArray.length()).map { stepIndex ->
                    val value = stepArray.getJSONObject(stepIndex)
                    PreparationStepEntity(
                        recipeId = 0,
                        position = value.optInt("position", stepIndex),
                        instruction = value.optString("instruction")
                    )
                }
                importRecords.add(RecipeImportRecord(recipe, ingredients, steps))
            }

            // All database rows are committed together or rolled back together.
            dao.importRecipesWithDetails(importRecords)
            return importRecords.size
        } catch (error: Exception) {
            importedFiles.forEach { file -> file.delete() }
            throw error
        }
    }

    private fun managedImageFile(context: Context, uriString: String?): File? = runCatching {
        if (uriString.isNullOrBlank()) return null
        val uri = Uri.parse(uriString)
        if (uri.scheme != "file") return null
        val file = File(uri.path ?: return null).canonicalFile
        val directory = File(context.filesDir, "recipe_images").canonicalFile
        file.takeIf { it.parentFile == directory }
    }.getOrNull()

    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject =
        put(key, value ?: JSONObject.NULL)

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it != "null" }

    private fun JSONObject.nullableDouble(key: String): Double? =
        if (isNull(key) || !has(key)) null else optDouble(key).takeIf { it.isFinite() }

    private fun JSONObject.nullableInt(key: String): Int? =
        if (isNull(key) || !has(key)) null else optInt(key)
}
