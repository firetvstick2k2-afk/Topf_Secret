package de.topfsecret.app

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TopfSecretApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrashLog(thread, throwable)
            } catch (_: Throwable) {
                // Logging must never prevent Android's normal crash handling.
            } finally {
                previousHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun saveCrashLog(thread: Thread, throwable: Throwable) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "TopfSecret_Crash_$timestamp.log"

        val stackTrace = StringWriter().also { writer ->
            throwable.printStackTrace(PrintWriter(writer))
        }.toString()

        val version = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "unbekannt"
        } catch (_: Exception) {
            "unbekannt"
        }

        val logText = """
            Topf Secret Absturzprotokoll
            Zeitpunkt: $timestamp
            App-Version: $version
            Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            Gerät: ${Build.MANUFACTURER} ${Build.MODEL}
            Thread: ${thread.name}

            Fehler:
            $stackTrace
        """.trimIndent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return

            contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use {
                it.write(logText)
            }
        } else {
            // Android 8/9: app-eigener Download-Ordner, ohne zusätzliche Berechtigung.
            val directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
            directory.mkdirs()
            File(directory, fileName).writeText(logText, Charsets.UTF_8)
        }
    }
}
