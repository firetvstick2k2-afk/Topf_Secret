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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import java.util.Locale

private const val ADMIN_KEY_SHA256 = "625a5d8ffcc7d51ed5ec6703877b3d4c30a2232ee1a2c34434b7ad3f055ada1f"
private const val ACTIVATION_PREFS = "topf_secret_activation"
private const val ADMIN_ACTIVATED_KEY = "admin_unlocked"
private const val LIFETIME_TOKEN_KEY = "lifetime_license_token"

val LocalCustomerLicenseInfo = androidx.compose.runtime.compositionLocalOf<CustomerLicenseInfo?> { null }

@Composable
fun ActivationGate(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(ACTIVATION_PREFS, android.content.Context.MODE_PRIVATE)
    }
    val storedLifetimeToken = remember(preferences) {
        preferences.getString(LIFETIME_TOKEN_KEY, null)
    }
    var customerLicenseInfo by remember {
        mutableStateOf(
            if (BuildConfig.ADMIN_ONLY) null
            else storedLifetimeToken?.let(LicenseVerifier::verifyCustomerToken)
        )
    }
    var isActivated by remember {
        mutableStateOf(
            if (BuildConfig.ADMIN_ONLY) preferences.getBoolean(ADMIN_ACTIVATED_KEY, false)
            else customerLicenseInfo != null
        )
    }
    var enteredSerial by rememberSaveable { mutableStateOf("") }
    var enteredAdminKey by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }

    if (isActivated) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalCustomerLicenseInfo provides if (BuildConfig.ADMIN_ONLY) null else customerLicenseInfo
        ) { content() }
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
                if (BuildConfig.ADMIN_ONLY) "Privater Admin-Zugang" else "Euer Kochbuch wartet!",
                color = Color(0xFF302019),
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                if (BuildConfig.ADMIN_ONLY)
                    "Diese Version wird mit dem Admin-Key freigeschaltet."
                else
                    "Gebt eure Seriennummer ein, um Topf Secret freizuschalten.",
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
                    if (BuildConfig.ADMIN_ONLY) {
                        OutlinedTextField(
                            value = enteredAdminKey,
                            onValueChange = {
                                enteredAdminKey = it
                                showError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Admin-Key") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            visualTransformation = PasswordVisualTransformation()
                        )
                        if (showError) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Der Admin-Key stimmt leider nicht.",
                                color = Color(0xFFB3261E),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val candidate = enteredAdminKey.trim().uppercase(Locale.ROOT)
                                val actualHash = MessageDigest.getInstance("SHA-256")
                                    .digest(candidate.toByteArray(Charsets.UTF_8))
                                    .joinToString("") { "%02x".format(it) }
                                if (MessageDigest.isEqual(
                                        actualHash.toByteArray(Charsets.US_ASCII),
                                        ADMIN_KEY_SHA256.toByteArray(Charsets.US_ASCII)
                                    )
                                ) {
                                    preferences.edit()
                                        .putBoolean(ADMIN_ACTIVATED_KEY, true)
                                        .apply()
                                    isActivated = true
                                    enteredAdminKey = ""
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enteredAdminKey.isNotBlank()
                        ) {
                            Text("Admin-Zugang freischalten")
                        }
                    } else {
                        OutlinedTextField(
                            value = enteredSerial,
                            onValueChange = {
                                enteredSerial = it
                                showError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Seriennummer") },
                            placeholder = { Text("TSL1.…") },
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                        )
                        if (showError) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Diese Seriennummer ist ungültig. Bitte prüft den vollständigen Lizenzschlüssel.",
                                color = Color(0xFFB3261E),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val candidate = enteredSerial.trim()
                                val verifiedLicense = LicenseVerifier.verifyCustomerToken(candidate)
                                if (verifiedLicense != null) {
                                    preferences.edit().putString(LIFETIME_TOKEN_KEY, candidate).apply()
                                    customerLicenseInfo = verifiedLicense
                                    isActivated = true
                                    enteredSerial = ""
                                } else {
                                    showError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = enteredSerial.isNotBlank()
                        ) {
                            Text("Topf Secret freischalten")
                        }
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
