package de.topfsecret.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import java.util.Locale

private const val ADMIN_KEY_SHA256 = "f62cb41ded03fafc42d8fe807f3dbf22bcbc3b9cbe981e99f1682319e52fefc7"
private const val ACTIVATION_PREFS = "topf_secret_activation"
private const val ACTIVATED_KEY = "admin_unlocked"

@Composable
fun ActivationGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(ACTIVATION_PREFS, android.content.Context.MODE_PRIVATE)
    }
    var isActivated by remember { mutableStateOf(preferences.getBoolean(ACTIVATED_KEY, false)) }
    var enteredKey by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    if (isActivated) {
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🍲", fontSize = 62.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                "TOPF SECRET",
                color = Color(0xFFE87524),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Euer Kochbuch wartet!",
                color = Color(0xFF302019),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Gebt euren persönlichen Admin-Freischalt-Key ein, um Topf Secret zu öffnen.",
                color = Color(0xFF806E62),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9))
            ) {
                Column(Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = enteredKey,
                        onValueChange = {
                            enteredKey = it
                            showError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Freischalt-Key") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (showError) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Der Key stimmt leider nicht. Bitte prüft eure Eingabe.",
                            color = Color(0xFFB3261E),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val candidate = enteredKey.trim().uppercase(Locale.ROOT)
                            val actualHash = MessageDigest.getInstance("SHA-256")
                                .digest(candidate.toByteArray(Charsets.UTF_8))
                                .joinToString("") { "%02x".format(it) }
                            if (MessageDigest.isEqual(
                                    actualHash.toByteArray(Charsets.US_ASCII),
                                    ADMIN_KEY_SHA256.toByteArray(Charsets.US_ASCII)
                                )
                            ) {
                                preferences.edit().putBoolean(ACTIVATED_KEY, true).apply()
                                isActivated = true
                                enteredKey = ""
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enteredKey.isNotBlank()
                    ) {
                        Text("Topf Secret freischalten")
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Die Freischaltung bleibt auf diesem Gerät gespeichert.",
                color = Color(0xFF806E62),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
