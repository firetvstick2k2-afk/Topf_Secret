package de.topfsecret.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
class SqlCipherRoomSmokeTest {

    @Test
    fun roomDatabaseIsEncryptedAndCanBeReopenedWithSameKey() = runBlocking {
        System.loadLibrary("sqlcipher")
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "sqlcipher_room_smoke_test.db"
        context.deleteDatabase(databaseName)

        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()

        val testKey = ByteArray(32).also(SecureRandom()::nextBytes)
        val factory = SupportOpenHelperFactory(testKey)

        try {
            var database = Room.databaseBuilder(
                context,
                TopfSecretDatabase::class.java,
                databaseFile.absolutePath
            )
                .openHelperFactory(factory)
                .build()

            val recipeId = database.recipeDao().insertRecipe(
                RecipeEntity(
                    title = "SQLCipher smoke test recipe",
                    description = "This text must not be stored in plaintext."
                )
            )
            assertEquals(1L, recipeId)
            assertEquals(
                "SQLCipher smoke test recipe",
                database.recipeDao().observeRecipes().first().single().recipe.title
            )
            database.close()

            val encryptedBytes = File(databaseFile.absolutePath).readBytes()
            val sqliteHeader = "SQLite format 3".toByteArray(Charsets.US_ASCII)
            assertFalse(
                "The database file must not have the plaintext SQLite header",
                encryptedBytes.take(sqliteHeader.size).toByteArray().contentEquals(sqliteHeader)
            )

            database = Room.databaseBuilder(
                context,
                TopfSecretDatabase::class.java,
                databaseFile.absolutePath
            )
                .openHelperFactory(SupportOpenHelperFactory(testKey))
                .build()

            assertEquals(
                "SQLCipher smoke test recipe",
                database.recipeDao().observeRecipes().first().single().recipe.title
            )
            database.close()
        } finally {
            context.deleteDatabase(databaseName)
            testKey.fill(0)
        }
    }
}
