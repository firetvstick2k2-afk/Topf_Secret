package de.topfsecret.app

import android.util.Base64
import org.json.JSONObject
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

/**
 * Verifies offline Topf Secret Lifetime licence tokens issued by Licence Studio.
 *
 * Token format: TSL1.<base64url-payload>.<base64url-ECDSA-SHA256-signature>
 * The public key is the X.509 SubjectPublicKeyInfo exported by Android Keystore.
 */
internal object LicenseVerifier {
    private const val PUBLIC_KEY_BASE64_URL =
        "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEBokANbM6NqN4zxM5iXcFpbRoJrasqXfcp8hPA31xImPDt0NZkOJChJz3f0BbydjgNcMo0qw9EQUSk_ym1vW0dA"

    fun isValidLifetimeToken(token: String): Boolean = runCatching {
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
        require(payload.optString("type") == "lifetime")
        require(payload.optLong("issuedAt", -1L) > 0L)
        require(payload.optString("customerName").isNotBlank())
        UUID.fromString(payload.getString("id"))
        true
    }.getOrDefault(false)
}
