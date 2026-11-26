package de.topfsecret.app.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RecoveryCodeGeneratorTest {

    @Test
    fun generatedCodeHasSixGroupsOfFourSymbols() {
        val code = RecoveryCodeGenerator.generate()

        assertEquals(29, code.length)
        assertEquals(6, code.split("-").size)
        assertEquals(6, code.split("-").count { it.length == 4 })
        assertNotNull(RecoveryCodeGenerator.normalize(code))
    }

    @Test
    fun generatedCodesUseOnlyCrockfordBase32Alphabet() {
        repeat(100) {
            val normalized = RecoveryCodeGenerator.normalize(RecoveryCodeGenerator.generate())
            assertNotNull(normalized)
            assertEquals(24, normalized!!.length)
            assertNull(RecoveryCodeGenerator.normalize(normalized.replace('0', 'O')))
        }
    }

    @Test
    fun normalizeAcceptsSpacesAndLowercase() {
        val code = RecoveryCodeGenerator.generate()
        val spacedLowercase = code.lowercase().replace("-", "  ")

        assertEquals(
            code.replace("-", ""),
            RecoveryCodeGenerator.normalize(spacedLowercase)
        )
    }

    @Test
    fun normalizeRejectsWrongLengthAndAmbiguousCharacters() {
        assertNull(RecoveryCodeGenerator.normalize("ABC"))
        assertNull(RecoveryCodeGenerator.normalize("OOOO-OOOO-OOOO-OOOO-OOOO-OOOO"))
        assertNull(RecoveryCodeGenerator.normalize("IIII-IIII-IIII-IIII-IIII-IIII"))
    }

    @Test
    fun formatReturnsCanonicalGroups() {
        val raw = "0123456789ABCDEFGHJKMNPQ"

        assertEquals("0123-4567-89AB-CDEG-HJKM-NPQ", RecoveryCodeGenerator.format(raw))
    }
}
