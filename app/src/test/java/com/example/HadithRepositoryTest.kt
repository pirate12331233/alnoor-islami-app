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

    @Test
    fun testSmartMatnExtractorFiltersSanadChains() {
        val testArabicWithSanad = "حَدَّثَنَا مُوسَى بْنُ إِسْمَاعِيلَ، قَالَ حَدَّثَنَا أَبُو عَوَانَةَ، قَالَ حَدَّثَنَا مُوسَى بْنُ أَبِي عَائِشَةَ، قَالَ حَدَّثَنَا سَعِيدُ بْنُ جُبَيْرٍ، عَنِ ابْنِ عَبَّاسٍ، قَالَ كَانَ رَسُولُ اللَّهِ صلى الله عليه وسلم يُعَالِجُ مِنَ التَّنْزِيلِ شِدَّةً"
        val cleanArabic = com.example.util.HadithMatnExtractor.extractMatnArabic(testArabicWithSanad)
        assertFalse(cleanArabic.startsWith("حَدَّثَنَا مُوسَى"))
        assertTrue(cleanArabic.contains("رَسُولُ اللَّهِ"))

        val testUrduWithSanad = "موسیٰ بن اسماعیل نے ہم سے حدیث بیان کی، ان کو ابوعوانہ نے خبر دی، ان سے موسیٰ ابن ابی عائشہ نے بیان کی، ان سے سعید بن جبیر نے انہوں نے ابن عباس سے سنا کہ رسول اللہ صلی اللہ علیہ وسلم نے فرمایا اعمال کا دارومدار نیتوں پر ہے۔"
        val cleanUrdu = com.example.util.HadithMatnExtractor.extractMafhoomUrdu(testUrduWithSanad)
        assertFalse(cleanUrdu.startsWith("موسیٰ بن اسماعیل نے"))
        assertTrue(cleanUrdu.contains("رسول اللہ صلی اللہ علیہ وسلم نے فرمایا"))

        val testEnglishWithSanad = "Narrated Said bin Jubair: Ibn Abbas reported that Allah's Messenger (ﷺ) said, 'Actions are judged by motives.'"
        val cleanEnglish = com.example.util.HadithMatnExtractor.extractMatnEnglish(testEnglishWithSanad)
        assertFalse(cleanEnglish.startsWith("Narrated Said bin Jubair"))
    }
}

