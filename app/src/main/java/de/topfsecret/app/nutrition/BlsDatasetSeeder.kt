package de.topfsecret.app.nutrition

import android.content.Context
import de.topfsecret.app.data.local.BlsFoodDao
import de.topfsecret.app.data.local.BlsFoodEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

/**
 * Seeds the bundled, official BLS 4.0 JSONL dataset into Room.
 * Raw nutrient triples are retained verbatim as JSON so textual markers such as
 * <LOQ, <LOD and '-' are never converted to numeric zeroes.
 */
object BlsDatasetSeeder {
    const val EXPECTED_FOOD_COUNT = 7140
    private const val BATCH_SIZE = 200
    private val mutex = Mutex()

    suspend fun ensureSeeded(context: Context, dao: BlsFoodDao): Int = mutex.withLock {
        val existing = dao.count()
        if (existing == EXPECTED_FOOD_COUNT) return@withLock existing

        // The table contains only the bundled BLS reference dataset. A partial
        // prior seed is discarded and retried, never treated as complete.
        dao.clearAll()
        var imported = 0
        var batch = ArrayList<BlsFoodEntity>(BATCH_SIZE)
        try {
            context.assets.open("bls_foods_v4.jsonl.gz").use { asset ->
                GZIPInputStream(asset).use { gzip ->
                    BufferedReader(InputStreamReader(gzip, Charsets.UTF_8)).useLines { lines ->
                        lines.forEach { line ->
                            if (line.isBlank()) return@forEach
                            val record = JSONObject(line)
                            val nutrients = record.optJSONObject("n")
                                ?: error("BLS-Datensatz ohne Nährstoffdaten.")
                            fun triple(code: String): JSONArray? = nutrients.optJSONArray(code)
                            fun rawValue(code: String): String? {
                                val value = triple(code)?.opt(0) ?: return null
                                return if (value == JSONObject.NULL) null else value.toString()
                            }
                            fun numeric(code: String): Double? =
                                rawValue(code)?.replace(',', '.')?.toDoubleOrNull()?.takeIf(Double::isFinite)
                            fun origin(code: String): String? =
                                triple(code)?.opt(1)?.toString()?.takeIf { it.isNotBlank() && it != "null" && it != "0" }
                            fun reference(code: String): String? =
                                triple(code)?.opt(2)?.toString()?.takeIf { it.isNotBlank() && it != "null" && it != "0" }

                            val code = record.optString("c")
                            val germanName = record.optString("de")
                            if (code.isBlank() || germanName.isBlank()) error("Ungültiger BLS-Datensatz.")
                            batch += BlsFoodEntity(
                                code = code,
                                germanName = germanName,
                                englishName = record.optString("en").takeIf { it.isNotBlank() && it != "null" },
                                kcalPer100 = numeric("ENERCC"),
                                proteinGPer100 = numeric("PROT625"),
                                carbohydratesGPer100 = numeric("CHO"),
                                fatGPer100 = numeric("FAT"),
                                kcalOrigin = origin("ENERCC"),
                                proteinOrigin = origin("PROT625"),
                                carbohydratesOrigin = origin("CHO"),
                                fatOrigin = origin("FAT"),
                                kcalReference = reference("ENERCC"),
                                proteinReference = reference("PROT625"),
                                carbohydratesReference = reference("FAT"),
                                fatReference = reference("FAT"),
                                nutrientsJson = nutrients.toString(),
                                note = record.optString("note").takeIf { it.isNotBlank() && it != "null" }
                            )
                            if (batch.size >= BATCH_SIZE) {
                                dao.insertAll(batch)
                                imported += batch.size
                                batch = ArrayList(BATCH_SIZE)
                            }
                        }
                    }
                }
            }
            if (batch.isNotEmpty()) {
                dao.insertAll(batch)
                imported += batch.size
            }
            val stored = dao.count()
            check(imported == EXPECTED_FOOD_COUNT && stored == EXPECTED_FOOD_COUNT) {
                "BLS-Import unvollständig: $stored von $EXPECTED_FOOD_COUNT Lebensmitteln."
            }
            stored
        } catch (error: Throwable) {
            dao.clearAll()
            throw error
        }
    }
}
