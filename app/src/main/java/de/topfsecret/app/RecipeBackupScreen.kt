package de.topfsecret.app

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.topfsecret.app.data.local.RecipeBackup
import de.topfsecret.app.data.local.RecipeDao
import kotlinx.coroutines.launch

@Composable
fun RecipeBackupScreen(context: Context, recipeDao: RecipeDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) scope.launch {
            busy = true
            message = "Backup wird erstellt …"
            runCatching { RecipeBackup.export(context, uri, recipeDao) }
                .onSuccess { count -> message = "Fertig! $count Rezepte wurden einschließlich verfügbarer Bilder gesichert." }
                .onFailure { error -> message = "Backup fehlgeschlagen: ${error.message ?: "Unbekannter Fehler"}" }
            busy = false
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) scope.launch {
            busy = true
            message = "Backup wird geprüft und importiert …"
            runCatching { RecipeBackup.import(context, uri, recipeDao) }
                .onSuccess { count -> message = "Import abgeschlossen: $count Rezepte wurden hinzugefügt. Bestehende Rezepte wurden nicht überschrieben." }
                .onFailure { error -> message = "Import fehlgeschlagen: ${error.message ?: "Unbekannter Fehler"}" }
            busy = false
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.Top
    ) {
        TextButton(onClick = onBack) { Text("← Zurück") }
        Text("BACKUP & WIEDERHERSTELLUNG", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        Text("Deine Rezepte bleiben bei dir", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))
        Text("Sichere deine Rezeptsammlung als ZIP-Datei. Die Sicherung enthält Rezepttexte, Zutaten, Zubereitung, Kategorien, Favoriten, Nährwerte und lokal gespeicherte Rezeptbilder.")
        Spacer(Modifier.height(20.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Backup erstellen", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Speichere die Datei zum Beispiel auf Google Drive, deinem PC oder einer SD-Karte.")
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { exportLauncher.launch("TopfSecret-Backup.zip") },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Backup exportieren") }
            }
        }
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Text("Backup importieren", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text("Wähle eine zuvor exportierte Topf-Secret-Backup.zip aus. Die enthaltenen Rezepte werden zusätzlich übernommen; vorhandene Einträge bleiben erhalten.")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Backup auswählen") }
            }
        }
        if (message.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        if (busy) {
            Spacer(Modifier.height(10.dp))
            Text("Bitte warten …")
        }
    }
}
