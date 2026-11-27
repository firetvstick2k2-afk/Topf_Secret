package de.topfsecret.app

import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

data class CustomerLicenseInfo(
    val type: String,
    val issuedAt: Long,
    val expiresAt: Long?,
    val durationValue: Int?,
    val durationUnit: String?
) {
    val typeLabel: String
        get() = when (type) {
            "lifetime" -> "Lifetime"
            "monthly" -> "1 Monat"
            "quarterly" -> "3 Monate"
            "half_yearly" -> "6 Monate"
            "yearly" -> "1 Jahr"
            "custom" -> {
                val amount = durationValue ?: 0
                val unit = when (durationUnit) {
                    "days" -> if (amount == 1) "Tag" else "Tage"
                    "months" -> if (amount == 1) "Monat" else "Monate"
                    "years" -> if (amount == 1) "Jahr" else "Jahre"
                    else -> "Einheiten"
                }
                "Individuelle Lizenz ($amount $unit)"
            }
            else -> "Unbekannt"
        }
}

/**
 * Verifies offline Topf Secret licence tokens issued by Licence Studio.
 *
 * Token format: TSL1.<base64url-payload>.<base64url-ECDSA-SHA256-signature>
 * The public key is the X.509 SubjectPublicKeyInfo of the Licence Studio main key.
 */
internal object LicenseVerifier {
    private const val PUBLIC_KEY_BASE64_URL =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEO_8xiWBZLIQw--gBpf1Touzg5jPDJLbLHbHiRUlakcvVTgdEcqSBf3-wsU9pzvsF9yCpKZfz4lWWB4bj2LGnQ"

    fun isValidLifetimeToken(token: String): Boolean = verifyCustomerToken(token) != null

    fun verifyCustomerToken(token: String): CustomerLicenseInfo? = runCatching {
        val parts = token.trim().split('.')
        require(parts.size == 3 && parts[0] == "TSL1")
        val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val signatureBytes = Base64.decode(parts[2], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)

        val publicKeyBytes = Base64.decode(
            PUBLIC_KEY_BASE64_URL,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val publicKey = KeyFactory.getInstance("EC")
            .generatePublic(X509EncodedKeySpec(publicKeyBytes))

        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(publicKey)
        verifier.update(payloadBytes)
        require(verifier.verify(signatureBytes))

        val payload = JSONObject(payloadBytes.toString(Charsets.UTF_8))
        require(payload.optInt("version", -1) == 1)
        require(payload.optString("product") == "topf_secret")
        val type = payload.optString("type")
        require(type in setOf("lifetime", "monthly", "quarterly", "half_yearly", "yearly", "custom"))
        val issuedAt = payload.optLong("issuedAt", -1L)
        require(issuedAt > 0L)
        require(payload.optString("customerName").isNotBlank())
        UUID.fromString(payload.getString("id"))

        if (type == "lifetime") {
            require(!payload.has("expiresAt"))
            CustomerLicenseInfo(type, issuedAt, null, null, null)
        } else {
            val expiresAt = payload.optLong("expiresAt", -1L)
            require(expiresAt > issuedAt && expiresAt > System.currentTimeMillis() / 1000L)
            val durationValue = payload.optInt("durationValue", 0)
            require(durationValue in 1..3650)
            val durationUnit = payload.optString("durationUnit")
            require(durationUnit in setOf("days", "months", "years"))
            CustomerLicenseInfo(type, issuedAt, expiresAt, durationValue, durationUnit)
        }
    }.getOrNull()
}
