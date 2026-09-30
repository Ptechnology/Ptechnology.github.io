package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultBibleWords
import com.example.game.WordUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Palavras Bíblicas", appName)
    }

    @Test
    fun `test word masking rules`() {
        // Palavra até 5 letras -> revela 1 letra
        val shortWord = "ADÃO"
        val revealedShort = WordUtils.computeInitialRevealedIndices(shortWord)
        assertEquals(1, revealedShort.size)

        // Palavra até 10 letras -> revela 2 letras
        val midWord = "ABRAÃO"
        val revealedMid = WordUtils.computeInitialRevealedIndices(midWord)
        assertEquals(2, revealedMid.size)

        // Palavra com mais de 10 letras -> revela 3 letras
        val longWord = "MELQUISEDEQUE"
        val revealedLong = WordUtils.computeInitialRevealedIndices(longWord)
        assertEquals(3, revealedLong.size)
    }

    @Test
    fun `test text normalization`() {
        assertEquals("JOSUE", WordUtils.normalizeText("Josué"))
        assertEquals("ABRAAO", WordUtils.normalizeText("Abraão"))
        assertEquals("GENESIS", WordUtils.normalizeText("Gênesis"))
    }

    @Test
    fun `test room code generation`() {
        val code = WordUtils.generateRoomCode()
        assertEquals(5, code.length)
        assertTrue(code.all { it.isLetterOrDigit() })
    }

    @Test
    fun `test unique words in catalogue`() {
        val words = DefaultBibleWords.list.map { it.word }
        val uniqueWords = words.toSet()
        assertEquals(words.size, uniqueWords.size)
    }
}
