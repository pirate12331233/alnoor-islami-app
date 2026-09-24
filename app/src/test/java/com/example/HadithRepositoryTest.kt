package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.HadithRepository
import com.example.util.HadithImageGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

    @Test
    fun testGetHadithByIndexAndTotalCount() {
        val total = HadithRepository.getTotalHadithCount()
        assertTrue(total > 0)
        val first = HadithRepository.getHadithByIndex(0)
        assertNotNull(first)
        val wrapped = HadithRepository.getHadithByIndex(total)
        assertEquals(first.id, wrapped.id)
    }

    @Test
    fun testOfflineDatabaseExtractionAndQuery() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val total = HadithRepository.getOfflineTotalCount(context)
        println("TEST: Total offline hadith count = $total")
        assertTrue("Offline database must have over 15,000 hadiths (found: $total)", total > 15000)

        // Test Bukhari selection and uniqueness
        val bukhariSeen = mutableSetOf<String>()
        var lastBukhariId: String? = null
        for (i in 1..5) {
            val h = HadithRepository.fetchAnotherHadith(context, lastBukhariId, "bukhari")
            println("TEST: Bukhari #$i = ${h.book}, #${h.hadithNumber} (id=${h.id})")
            assertEquals("Sahih al-Bukhari", h.book)
            assertFalse("Hadith should not repeat in consecutive requests: ${h.id}", bukhariSeen.contains(h.id))
            bukhariSeen.add(h.id)
            lastBukhariId = h.id
        }

        // Test Muslim selection and uniqueness
        val muslimSeen = mutableSetOf<String>()
        var lastMuslimId: String? = null
        for (i in 1..5) {
            val h = HadithRepository.fetchAnotherHadith(context, lastMuslimId, "muslim")
            println("TEST: Muslim #$i = ${h.book}, #${h.hadithNumber} (id=${h.id})")
            assertEquals("Sahih Muslim", h.book)
            assertFalse("Hadith should not repeat in consecutive requests: ${h.id}", muslimSeen.contains(h.id))
            muslimSeen.add(h.id)
            lastMuslimId = h.id
        }
    }

    @Test
    fun testHadithImageGenerationWithWatermarkAndContacts() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hadith = HadithRepository.getHadithByIndex(0)
        val success = HadithImageGenerator.shareHadithAsImage(context, hadith)
        assertTrue("Hadith image generation and sharing should succeed", success)
    }
}

