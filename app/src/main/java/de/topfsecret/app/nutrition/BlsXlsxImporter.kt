package de.topfsecret.app.nutrition

import android.content.Context
import android.net.Uri
import android.util.Xml
import de.topfsecret.app.data.local.BlsFoodDao
import de.topfsecret.app.data.local.BlsFoodEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.util.zip.ZipFile

/**
 * Imports the official BLS 4.0 XLSX main workbook selected by the user.
 * Only the four macro fields currently used by recipe calculations are stored;
 * their original per-value origin and reference fields are retained.
 */
object BlsXlsxImporter {
    suspend fun importWorkbook(context: Context, uri: Uri, dao: BlsFoodDao): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(context.cacheDir, "bls-import-${System.currentTimeMillis()}.xlsx")
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    } ?: error("Die ausgewählte Datei konnte nicht geöffnet werden.")
                    ZipFile(file).use { zip ->
                        val strings = zip.getEntry("xl/sharedStrings.xml")?.let { entry ->
                            zip.getInputStream(entry).use(::readSharedStrings)
                        } ?: emptyList()
                        val sheet = zip.getEntry("xl/worksheets/sheet1.xml")
                            ?: error("Die Excel-Datei enthält kein erstes Tabellenblatt.")
                        var header: Map<Int, String>? = null
                        var codeCol = 0
                        var nameCol = 1
                        var englishCol = 2
                        var nutrientCols = emptyMap<String, Int>()
                        var batch = mutableListOf<BlsFoodEntity>()
                        var imported = 0
                        zip.getInputStream(sheet).use { stream ->
                            val parser = Xml.newPullParser()
                            parser.setInput(stream, "UTF-8")
                            var event = parser.eventType
                            while (event != XmlPullParser.END_DOCUMENT) {
                                if (event == XmlPullParser.START_TAG && parser.name == "row") {
                                    val row = readRow(parser, strings)
                                    if (header == null) {
                                        val candidate = row.mapValues { it.value.trim() }
                                        val found = candidate.entries.firstOrNull { it.value.equals("BLS Code", true) }?.key
                                        if (found != null) {
                                            codeCol = found
                                            nameCol = candidate.entries.firstOrNull { it.value.equals("Lebensmittelbezeichnung", true) }?.key
                                                ?: error("Die deutsche Lebensmittelspalte fehlt.")
                                            englishCol = candidate.entries.firstOrNull { it.value.equals("Food name", true) }?.key ?: 2
                                            nutrientCols = mapOf(
                                                "ENERCC" to findNutrientColumn(candidate, "ENERCC"),
                                                "PROT625" to findNutrientColumn(candidate, "PROT625"),
                                                "CHO" to findNutrientColumn(candidate, "CHO"),
                                                "FAT" to findNutrientColumn(candidate, "FAT")
                                            )
                                            header = candidate
                                        }
                                    } else {
                                        val code = row[codeCol].orEmpty().trim()
                                        val name = row[nameCol].orEmpty().trim()
                                        if (code.isNotBlank() && name.isNotBlank()) {
                                            fun value(codeKey: String): Double? =
                                                nutrientCols[codeKey]?.let { row[it]?.replace(',', '.')?.toDoubleOrNull()?.takeIf(Double::isFinite) }
                                            fun origin(codeKey: String): String? = nutrientCols[codeKey]?.let { row[it + 1]?.trim()?.takeIf(String::isNotBlank) }
                                            fun reference(codeKey: String): String? = nutrientCols[codeKey]?.let { row[it + 2]?.trim()?.takeIf(String::isNotBlank) }
                                            batch += BlsFoodEntity(
                                                code = code,
                                                germanName = name,
                                                englishName = row[englishCol]?.takeIf(String::isNotBlank),
                                                kcalPer100 = value("ENERCC"),
                                                proteinGPer100 = value("PROT625"),
                                                carbohydratesGPer100 = value("CHO"),
                                                fatGPer100 = value("FAT"),
                                                kcalOrigin = origin("ENERCC"),
                                                proteinOrigin = origin("PROT625"),
                                                carbohydratesOrigin = origin("CHO"),
                                                fatOrigin = origin("FAT"),
                                                kcalReference = reference("ENERCC"),
                                                proteinReference = reference("PROT625"),
                                                carbohydratesReference = reference("CHO"),
                                                fatReference = reference("FAT")
                                            )
                                            if (batch.size >= 250) {
                                                dao.insertAll(batch)
                                                imported += batch.size
                                                batch = mutableListOf()
                                            }
                                        }
                                    }
                                }
                                event = parser.next()
                            }
                        }
                        if (header == null) error("Das Tabellenblatt ist nicht als BLS-4.0-Hauptdatei erkennbar.")
                        if (batch.isNotEmpty()) {
                            dao.insertAll(batch)
                            imported += batch.size
                        }
                        if (imported == 0) error("Es wurden keine Lebensmittel importiert.")
                        imported
                    }
                } finally {
                    file.delete()
                }
            }
        }

    private fun findNutrientColumn(header: Map<Int, String>, code: String): Int =
        header.entries.firstOrNull { it.value.trim().startsWith("$code ", ignoreCase = true) }?.key
            ?: error("Die Nährstoffspalte $code fehlt in der BLS-Datei.")

    private fun readSharedStrings(parser: java.io.InputStream): List<String> {
        val xml = Xml.newPullParser()
        xml.setInput(parser, "UTF-8")
        val result = mutableListOf<String>()
        var event = xml.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && xml.name == "si") {
                val text = StringBuilder()
                var depth = 1
                while (depth > 0) {
                    when (xml.next()) {
                        XmlPullParser.START_TAG -> if (xml.name == "t") text.append(xml.nextText()) else depth++
                        XmlPullParser.END_TAG -> if (xml.name == "si") depth-- else if (xml.name != "t") depth--
                    }
                }
                result += text.toString()
            }
            event = xml.next()
        }
        return result
    }

    private fun readRow(parser: XmlPullParser, shared: List<String>): Map<Int, String> {
        val values = mutableMapOf<Int, String>()
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "row") depth++
                    if (parser.name == "c") {
                        val ref = parser.getAttributeValue(null, "r").orEmpty()
                        val type = parser.getAttributeValue(null, "t")
                        val column = columnIndex(ref)
                        var value = ""
                        var cellDepth = 1
                        while (cellDepth > 0) {
                            when (parser.next()) {
                                XmlPullParser.START_TAG -> {
                                    if (parser.name == "v" || parser.name == "t") value = parser.nextText()
                                    else if (parser.name == "is") cellDepth++
                                }
                                XmlPullParser.END_TAG -> if (parser.name == "c") cellDepth--
                            }
                        }
                        if (column >= 0) {
                            values[column] = if (type == "s") shared.getOrNull(value.toIntOrNull() ?: -1).orEmpty() else value
                        }
                    }
                }
                XmlPullParser.END_TAG -> if (parser.name == "row") depth--
            }
        }
        return values
    }

    private fun columnIndex(reference: String): Int {
        val letters = reference.takeWhile(Char::isLetter).uppercase()
        if (letters.isEmpty()) return -1
        var value = 0
        letters.forEach { value = value * 26 + (it - 'A' + 1) }
        return value - 1
    }
}
