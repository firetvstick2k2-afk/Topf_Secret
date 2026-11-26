package de.topfsecret.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.Html
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

private data class CommonsDishImage(
    val title: String,
    val thumbnailUrl: String,
    val descriptionUrl: String,
    val license: String,
    val artist: String,
    val attribution: String
)

@Composable
internal fun OnlineDishImageSearch(
    initialQuery: String,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<CommonsDishImage>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!downloading) onDismiss() },
        title = { Text("Gerichtefoto online suchen") },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Suche nach einem Gericht. Die Treffer stammen aus Wikimedia Commons.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Gericht") },
                    enabled = !loading && !downloading
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            results = emptyList()
                            try {
                                results = searchCommonsDishImages(query)
                                if (results.isEmpty()) error = "Keine passenden Bilder mit auslesbaren Lizenzangaben gefunden."
                            } catch (_: Exception) {
                                error = "Die Bildersuche ist gerade nicht erreichbar. Bitte später erneut versuchen."
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = query.isNotBlank() && !loading && !downloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Bilder suchen")
                }
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                results.forEach { result ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CommonsThumbnail(result.thumbnailUrl, Modifier.size(92.dp))
                            Column(Modifier.weight(1f)) {
                                Text(result.title.removePrefix("File:"), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                Text("Lizenz: " + result.license, style = MaterialTheme.typography.labelSmall)
                                if (result.artist.isNotBlank()) Text("Urheber: " + result.artist, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            downloading = true
                                            error = null
                                            try {
                                                val saved = downloadCommonsImage(context, result)
                                                if (saved != null) onImageSelected(saved)
                                                else error = "Das Bild konnte nicht heruntergeladen werden."
                                            } catch (_: Exception) {
                                                error = "Das Bild konnte nicht heruntergeladen werden."
                                            } finally {
                                                downloading = false
                                            }
                                        }
                                    },
                                    enabled = !downloading
                                ) { Text(if (downloading) "Lädt …" else "Dieses Bild verwenden") }
                            }
                        }
                    }
                }
                if (downloading) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Bild wird lokal gespeichert …", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text(
                    "Bitte Lizenz und Urheberangabe beachten. Die Bildquelle wird zusammen mit dem Bild lokal gespeichert.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, enabled = !downloading) { Text("Schließen") } }
    )
}

@Composable
private fun CommonsThumbnail(url: String, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, url) {
        value = withContext(Dispatchers.IO) { loadCommonsThumbnail(url) }
    }
    if (bitmap != null) {
        Image(bitmap!!.asImageBitmap(), contentDescription = "Gefundenes Gerichtefoto", modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Surface(modifier, shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
            Box(contentAlignment = Alignment.Center) { Text("🍽️") }
        }
    }
}

private suspend fun searchCommonsDishImages(query: String): List<CommonsDishImage> = withContext(Dispatchers.IO) {
    val encoded = URLEncoder.encode(query.trim(), "UTF-8")
    val api = "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search&gsrnamespace=6&gsrlimit=16&gsrsearch=$encoded&prop=imageinfo&iiprop=url%7Cextmetadata&iiurlwidth=640&iiextmetadatalanguage=de&iiextmetadatafilter=Artist%7CLicenseShortName%7CLicenseUrl%7CAttribution"
    val json = JSONObject(readCommonsUrl(api))
    val pages = json.optJSONObject("query")?.optJSONObject("pages") ?: return@withContext emptyList()
    val output = mutableListOf<CommonsDishImage>()
    val keys = pages.keys()
    while (keys.hasNext()) {
        val page = pages.optJSONObject(keys.next()) ?: continue
        val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
        val thumb = info.optString("thumburl")
        val pageUrl = info.optString("descriptionurl")
        val metadata = info.optJSONObject("extmetadata") ?: JSONObject()
        val license = metadata.value("LicenseShortName")
        val artist = metadata.value("Artist")
        val attribution = metadata.value("Attribution")
        if (thumb.isBlank() || pageUrl.isBlank() || license.isBlank()) continue
        output += CommonsDishImage(
            title = page.optString("title"),
            thumbnailUrl = thumb,
            descriptionUrl = pageUrl,
            license = license,
            artist = artist,
            attribution = attribution
        )
    }
    output
}

private fun JSONObject.value(key: String): String {
    val raw = optJSONObject(key)?.optString("value").orEmpty()
    return Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}

private fun readCommonsUrl(address: String): String {
    val connection = URL(address).openConnection() as HttpURLConnection
    connection.connectTimeout = 12_000
    connection.readTimeout = 15_000
    connection.setRequestProperty("User-Agent", "TopfSecret/0.6 (https://github.com/firetvstick2k2-afk/Topf_Secret)")
    return connection.inputStream.bufferedReader().use { it.readText() }.also { connection.disconnect() }
}

private fun loadCommonsThumbnail(address: String): Bitmap? = runCatching {
    val connection = URL(address).openConnection() as HttpURLConnection
    connection.connectTimeout = 10_000
    connection.readTimeout = 15_000
    connection.setRequestProperty("User-Agent", "TopfSecret/0.6 (https://github.com/firetvstick2k2-afk/Topf_Secret)")
    connection.inputStream.use { BitmapFactory.decodeStream(it) }.also { connection.disconnect() }
}.getOrNull()

private suspend fun downloadCommonsImage(context: Context, image: CommonsDishImage): String? = withContext(Dispatchers.IO) {
    runCatching {
        val directory = File(context.filesDir, "recipe_images").apply { mkdirs() }
        val destination = File(directory, "online-${UUID.randomUUID()}.jpg")
        val connection = URL(image.thumbnailUrl).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 20_000
        connection.setRequestProperty("User-Agent", "TopfSecret/0.6 (https://github.com/firetvstick2k2-afk/Topf_Secret)")
        connection.inputStream.use { input -> destination.outputStream().use { output -> input.copyTo(output) } }
        connection.disconnect()
        if (!destination.exists() || destination.length() < 1024L) {
            destination.delete()
            return@withContext null
        }
        val sourceFile = File(directory, destination.name + ".source.txt")
        sourceFile.writeText(
            "Source: Wikimedia Commons\n" +
                "File: ${image.title}\n" +
                "Description page: ${image.descriptionUrl}\n" +
                "Thumbnail URL: ${image.thumbnailUrl}\n" +
                "License: ${image.license}\n" +
                "Artist: ${image.artist}\n" +
                "Attribution: ${image.attribution}\n"
        )
        Uri.fromFile(destination).toString()
    }.getOrNull()
}
