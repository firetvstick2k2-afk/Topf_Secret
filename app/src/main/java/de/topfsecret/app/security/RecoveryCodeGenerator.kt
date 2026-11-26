package de.topfsecret.app.security

import java.security.SecureRandom
import java.util.Locale

/**
 * Creates and validates the one-time profile recovery code.
 *
 * The generated value contains 120 bits from SecureRandom and is intended
 * to be shown once during profile setup. Callers must never persist or log
 * the returned plaintext code.
 */
object RecoveryCodeGenerator {
    private const val ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private const val SYMBOL_COUNT = 24
    private const val GROUP_SIZE = 4
    private const val RAW_BYTE_COUNT = 15

    private val secureRandom = SecureRandom()

    fun generate(): String {
        val randomBytes = ByteArray(RAW_BYTE_COUNT)
        secureRandom.nextBytes(randomBytes)

        val encoded = StringBuilder(SYMBOL_COUNT)
        var buffer = 0
        var bitsInBuffer = 0

        for (byte in randomBytes) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xff)
            bitsInBuffer += 8

            while (bitsInBuffer >= 5) {
                bitsInBuffer -= 5
                val index = (buffer ushr bitsInBuffer) and 0x1f
                encoded.append(ALPHABET[index])
            }
        }

        return encoded.toString()
            .chunked(GROUP_SIZE)
            .joinToString("-")
    }

    /**
     * Removes presentation separators and validates the exact 24-symbol
     * Crockford Base32 alphabet. Returns the canonical ungrouped form.
     */
    fun normalize(input: String): String? {
        val normalized = input
            .uppercase(Locale.ROOT)
            .filterNot { it == '-' || it.isWhitespace() }

        if (normalized.length != SYMBOL_COUNT) return null
        if (normalized.any { it !in ALPHABET }) return null

        return normalized
    }

    fun format(normalizedCode: String): String? {
        val valid = normalize(normalizedCode) ?: return null
        return valid.chunked(GROUP_SIZE).joinToString("-")
    }
}
