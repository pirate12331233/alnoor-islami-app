package com.example

import com.example.data.repository.HadithRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithRepositoryTest {

    @Test
    fun testCuratedAhadithHasEssentialSunniCollections() {
        val ahadith = HadithRepository.CURATED_SUNNI_AHADITH
        assertTrue(ahadith.isNotEmpty())

        // Verify all entries contain valid Arabic, Urdu and English translations
        ahadith.forEach { hadith ->
            assertFalse("Arabic text cannot be blank for ${hadith.id}", hadith.arabicText.isBlank())
            assertFalse("Urdu translation cannot be blank for ${hadith.id}", hadith.urduTranslation.isBlank())
            assertFalse("English translation cannot be blank for ${hadith.id}", hadith.englishTranslation.isBlank())
            assertFalse("Book cannot be blank for ${hadith.id}", hadith.book.isBlank())
        }

        // Check for presence of primary Sunni books: Bukhari, Muslim, Tirmidhi
        val books = ahadith.map { it.book }.toSet()
        assertTrue(books.any { it.contains("Bukhari", ignoreCase = true) })
        assertTrue(books.any { it.contains("Muslim", ignoreCase = true) })
        assertTrue(books.any { it.contains("Tirmidhi", ignoreCase = true) })
    }

    @Test
    fun testGetDailyHadithReturnsValidItem() = runBlocking {
        val daily = HadithRepository.getDailyHadith(null)
        assertNotNull(daily)
        assertTrue(daily.arabicText.isNotBlank())
        assertTrue(daily.urduTranslation.isNotBlank())
        assertTrue(daily.englishTranslation.isNotBlank())
    }
}
