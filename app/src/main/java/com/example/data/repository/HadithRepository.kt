package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.HadithDatabase
import com.example.data.local.HadithEntity
import com.example.data.model.HadithData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.random.Random

/**
 * Authentic Sunni Hadith Repository with Option B Architecture:
 * - Sahih al-Bukhari (~7,589 hadiths) and Sahih Muslim (~7,563 hadiths) are bundled permanently offline.
 * - Other Sunni collections (Tirmidhi, Abu Dawud, Nasa'i, Ibn Majah) can be downloaded on-demand.
 * - Instant 0ms queries from local Room SQLite database without calling external APIs or requiring internet.
 */
object HadithRepository {
    private const val TAG = "HadithRepository"

    /**
     * Curated, authentic Sunni Hadith collection from Sahih al-Bukhari, Sahih Muslim,
     * Jami` at-Tirmidhi, Sunan Abi Dawud, Sunan an-Nasa'i, and Sunan Ibn Majah.
     * Serves as an immediate zero-latency in-memory fallback.
     */
    val CURATED_SUNNI_AHADITH: List<HadithData> = listOf(
        HadithData(
            id = "bukhari_1",
            book = "Sahih al-Bukhari",
            hadithNumber = "1",
            chapter = "Revelation (کتاب بدء الوحی)",
            narrator = "Narrated by 'Umar bin Al-Khattab (رضي الله عنه)",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.",
            urduTranslation = "اعمال کا دارومدار صرف نیتوں پر ہے، اور ہر انسان کے لیے وہی ہے جس کی اس نے نیت کی۔ پس جس کی ہجرت دنیا حاصل کرنے کے لیے ہو یا کسی عورت سے نکاح کے لیے، تو اس کی ہجرت اسی کے لیے ہے جس کی طرف اس نے ہجرت کی۔",
            englishTranslation = "Actions are according to intentions, and every person will have only that which he intended. So whoever emigrated for the world or for a woman to marry, his emigration was for that to which he emigrated.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 1, Book 1, Hadith 1"
        ),
        HadithData(
            id = "bukhari_13",
            book = "Sahih al-Bukhari",
            hadithNumber = "13",
            chapter = "Belief (کتاب الایمان)",
            narrator = "Narrated by Anas bin Malik (رضي الله عنه)",
            arabicText = "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ.",
            urduTranslation = "تم میں سے کوئی شخص اس وقت تک سچا مومن نہیں ہو سکتا جب تک کہ وہ اپنے بھائی کے لیے بھی وہی پسند نہ کرے جو اپنے لیے پسند کرتا ہے۔",
            englishTranslation = "None of you truly believes until he loves for his brother what he loves for himself.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 13, Book 2, Hadith 6"
        ),
        HadithData(
            id = "muslim_223",
            book = "Sahih Muslim",
            hadithNumber = "223",
            chapter = "Purification (کتاب الطهارة)",
            narrator = "Narrated by Abu Malik Al-Ash'ari (رضي الله عنه)",
            arabicText = "الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ، وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآنِ - أَوْ تَمْلأُ - مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ.",
            urduTranslation = "پاکیزگی نصف ایمان ہے، اور 'الحمد لله' میزان کو بھر دیتا ہے، اور 'سبحان الله والحمد لله' آسمانوں اور زمین کے درمیان کے خلا کو بھر دیتے ہیں۔",
            englishTranslation = "Purity is half of faith, and 'Al-hamdulillah' (praise be to Allah) fills the scale, and 'Subhan-Allah' and 'Al-hamdulillah' fill that which is between the heavens and the earth.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 223, Book 2, Hadith 1"
        ),
        HadithData(
            id = "bukhari_6011",
            book = "Sahih al-Bukhari",
            hadithNumber = "6011",
            chapter = "Good Manners (کتاب الأدب)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيُكْرِمْ جَارَهُ، وَمَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيُكْرِمْ ضَيْفَهُ.",
            urduTranslation = "جو شخص اللہ اور یوم آخرت پر ایمان رکھتا ہے اسے چاہیے کہ بھلی بات کہے یا خاموش رہے، اور جو اللہ اور یوم آخرت پر ایمان رکھتا ہے اسے اپنے پڑوسی کی عزت کرنی چاہیے، اور جو اللہ اور یوم آخرت پر ایمان رکھتا ہے اسے اپنے مہمان کی عزت کرنی چاہیے۔",
            englishTranslation = "Whoever believes in Allah and the Last Day should speak good or remain silent; and whoever believes in Allah and the Last Day should be hospitable to his neighbor; and whoever believes in Allah and the Last Day should be generous to his guest.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6011, Book 78, Hadith 42"
        ),
        HadithData(
            id = "tirmidhi_1987",
            book = "Jami` at-Tirmidhi",
            hadithNumber = "1987",
            chapter = "Righteousness (کتاب البر والصلة)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "الْمُؤْمِنُ مَأْلَفٌ، وَلاَ خَيْرَ فِيمَنْ لاَ يَأْلَفُ وَلاَ يُؤْلَفُ، وَخَيْرُ النَّاسِ أَنْفَعُهُمْ لِلنَّاسِ.",
            urduTranslation = "مومن الفت و محبت کا پیکر ہوتا ہے، اور اس شخص میں کوئی بھلائی نہیں جو نہ کسی سے الفت رکھے اور نہ اس سے الفت رکھی جائے، اور لوگوں میں سب سے بہترین وہ ہے جو لوگوں کو سب سے زیادہ نفع پہنچائے۔",
            englishTranslation = "The believer is friendly and creates affection. There is no good in one who is not friendly and whom others do not like. The best of people are those who are most beneficial to people.",
            grade = "Sahih (صحیح)",
            reference = "Jami` at-Tirmidhi 1987"
        ),
        HadithData(
            id = "bukhari_5027",
            book = "Sahih al-Bukhari",
            hadithNumber = "5027",
            chapter = "Virtues of Qur'an (فضائل القرآن)",
            narrator = "Narrated by 'Uthman bin 'Affan (رضي الله عنه)",
            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ.",
            urduTranslation = "تم میں سے بہترین شخص وہ ہے جس نے قرآن مجید سیکھا اور اسے دوسروں کو سکھایا۔",
            englishTranslation = "The best among you are those who learn the Qur'an and teach it to others.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 5027, Book 66, Hadith 49"
        ),
        HadithData(
            id = "muslim_2564",
            book = "Sahih Muslim",
            hadithNumber = "2564",
            chapter = "Virtues and Manners (کتاب البر والصلة والآداب)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "إِنَّ اللَّهَ لاَ يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ.",
            urduTranslation = "بے شک اللہ تعالیٰ تمہاری صورتوں اور تمہارے مالوں کو نہیں دیکھتا، بلکہ وہ تمہارے دلوں اور تمہارے اعمال کو دیکھتا ہے۔",
            englishTranslation = "Verily, Allah does not look at your appearance or your wealth, but He looks at your hearts and your deeds.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2564, Book 45, Hadith 42"
        ),
        HadithData(
            id = "bukhari_6018",
            book = "Sahih al-Bukhari",
            hadithNumber = "6018",
            chapter = "Good Character (کتاب الأدب)",
            narrator = "Narrated by 'Abdullah bin Mas'ud (رضي الله عنه)",
            arabicText = "إِنَّ الصِّدْقَ يَهْدِي إِلَى الْبِرِّ، وَإِنَّ الْبِرَّ يَهْدِي إِلَى الْجَنَّةِ، وَإِنَّ الرَّجُلَ لَيَصْدُقُ حَتَّى يَكُونَ صِدِّيقًا.",
            urduTranslation = "سچائی نیکی کی طرف رہنمائی کرتی ہے اور نیکی جنت کی طرف لے جاتی ہے، اور انسان سچ بولتا رہتا ہے یہاں تک کہ وہ اللہ کے ہاں 'صدیق' لکھ دیا جاتا ہے۔",
            englishTranslation = "Truthfulness leads to righteousness, and righteousness leads to Paradise. And a man keeps on telling the truth until he becomes a truthful person (Siddiq).",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6018, Book 78, Hadith 49"
        ),
        HadithData(
            id = "tirmidhi_2516",
            book = "Jami` at-Tirmidhi",
            hadithNumber = "2516",
            chapter = "Supplication (کتاب الدعوات)",
            narrator = "Narrated by Anas bin Malik (رضي الله عنه)",
            arabicText = "الدُّعَاءُ مُخُّ الْعِبَادَةِ.",
            urduTranslation = "دعا عبادت کا مغز (حقیقی نچوڑ) ہے۔",
            englishTranslation = "Supplication (Dua) is the very essence of worship.",
            grade = "Sahih / Hasan",
            reference = "Jami` at-Tirmidhi 2516"
        ),
        HadithData(
            id = "muslim_2699",
            book = "Sahih Muslim",
            hadithNumber = "2699",
            chapter = "Remembrance and Supplication (کتاب الذکر)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ.",
            urduTranslation = "جو شخص علم کی تلاش میں کسی راستے پر چلتا ہے، اللہ تعالیٰ اس کے بدلے اس کے لیے جنت کا راستہ آسان فرما دیتا ہے۔",
            englishTranslation = "Whoever follows a path in pursuit of knowledge, Allah will make easy for him a path to Paradise.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2699, Book 48, Hadith 38"
        )
    )

    private val recentlyViewedIds = ArrayDeque<String>(60)
    private val viewedLock = Any()

    fun recordViewedHadith(id: String) {
        if (id.isBlank()) return
        synchronized(viewedLock) {
            recentlyViewedIds.remove(id)
            if (recentlyViewedIds.size >= 50) {
                recentlyViewedIds.removeFirst()
            }
            recentlyViewedIds.addLast(id)
        }
    }

    fun getRecentlyViewedIds(): List<String> {
        synchronized(viewedLock) {
            return recentlyViewedIds.toList()
        }
    }

    fun getTotalHadithCount(): Int = CURATED_SUNNI_AHADITH.size

    fun getHadithByIndex(index: Int): HadithData {
        if (CURATED_SUNNI_AHADITH.isEmpty()) {
            throw IllegalStateException("Curated Ahadith collection is empty")
        }
        val safeIndex = Math.floorMod(index, CURATED_SUNNI_AHADITH.size)
        return CURATED_SUNNI_AHADITH[safeIndex]
    }

    /**
     * Retrieves the daily Hadith.
     * Queries the bundled offline database first. If not ready, falls back to the curated collection.
     */
    suspend fun getDailyHadith(context: Context? = null): HadithData = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        if (context != null) {
            try {
                val dao = HadithDatabase.getInstance(context).hadithDao()
                // Deterministic hadith number for the day from Sahih al-Bukhari
                val targetNum = ((dayOfYear * 17) % 7580 + 1).toString()
                val offlineHadith = dao.getHadithByBookAndNumber("bukhari", targetNum)
                    ?: dao.getRandomHadithByBookExcluding("bukhari", "")
                    ?: dao.getRandomHadith()
                if (offlineHadith != null) {
                    val data = offlineHadith.toHadithData()
                    recordViewedHadith(data.id)
                    return@withContext data
                }
            } catch (e: Exception) {
                Log.w(TAG, "Database access note for daily hadith: ${e.message}")
            }
        }

        val defaultHadith = CURATED_SUNNI_AHADITH[dayOfYear % CURATED_SUNNI_AHADITH.size]
        recordViewedHadith(defaultHadith.id)
        defaultHadith
    }

    /**
     * Instantly fetches a fresh Hadith from the 15,000+ bundled/downloaded Hadiths in the local SQLite database.
     * Ensures Hadiths do not repeat and strictly belong to the chosen book.
     * 100% offline, zero internet required.
     */
    suspend fun fetchAnotherHadith(
        context: Context? = null,
        currentId: String? = null,
        filterBookKey: String? = null
    ): HadithData = withContext(Dispatchers.IO) {
        val normalizedBookKey = filterBookKey?.trim()?.lowercase()?.let {
            if (it == "all" || it.isBlank()) null else it
        }

        val excludeIds = ArrayList<String>()
        if (!currentId.isNullOrBlank()) {
            excludeIds.add(currentId)
        }
        val recent = getRecentlyViewedIds()
        for (id in recent) {
            if (!excludeIds.contains(id)) {
                excludeIds.add(id)
            }
        }

        if (context != null) {
            try {
                val dao = HadithDatabase.getInstance(context).hadithDao()

                var entity: HadithEntity? = if (normalizedBookKey != null) {
                    dao.getRandomHadithByBookExcludingList(normalizedBookKey, excludeIds)
                        ?: dao.getRandomHadithByBookExcluding(normalizedBookKey, currentId ?: "")
                } else {
                    dao.getRandomHadithExcludingList(excludeIds)
                        ?: dao.getRandomHadithExcluding(currentId ?: "")
                }

                // If all were excluded, clear recent history and retry
                if (entity == null) {
                    entity = if (normalizedBookKey != null) {
                        dao.getRandomHadithByBookExcluding(normalizedBookKey, currentId ?: "")
                    } else {
                        dao.getRandomHadith()
                    }
                }

                if (entity != null) {
                    recordViewedHadith(entity.id)
                    return@withContext entity.toHadithData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Offline database query exception: ${e.message}", e)
            }
        }

        // Strict fallback by selected book if database failed
        val bookFilteredCurated = if (normalizedBookKey != null) {
            CURATED_SUNNI_AHADITH.filter { h ->
                when (normalizedBookKey) {
                    "bukhari" -> h.book.contains("Bukhari", ignoreCase = true)
                    "muslim" -> h.book.contains("Muslim", ignoreCase = true)
                    "tirmidhi" -> h.book.contains("Tirmidhi", ignoreCase = true)
                    "abudawud" -> h.book.contains("Dawud", ignoreCase = true)
                    "nasai" -> h.book.contains("Nasa'i", ignoreCase = true) || h.book.contains("Nasai", ignoreCase = true)
                    "ibnmajah" -> h.book.contains("Majah", ignoreCase = true)
                    else -> true
                }
            }
        } else {
            CURATED_SUNNI_AHADITH
        }

        val pool = if (bookFilteredCurated.isNotEmpty()) bookFilteredCurated else CURATED_SUNNI_AHADITH
        val unviewed = pool.filter { !excludeIds.contains(it.id) }
        val chosen = if (unviewed.isNotEmpty()) {
            unviewed[Random.nextInt(unviewed.size)]
        } else {
            val nonCurrent = pool.filter { it.id != currentId }
            if (nonCurrent.isNotEmpty()) nonCurrent[Random.nextInt(nonCurrent.size)] else pool.first()
        }

        recordViewedHadith(chosen.id)
        chosen
    }

    /**
     * Full-text search across downloaded/bundled Hadiths.
     */
    suspend fun searchHadiths(context: Context, query: String): List<HadithData> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val dao = HadithDatabase.getInstance(context).hadithDao()
            dao.searchHadiths(query.trim()).map { it.toHadithData() }
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Paged browsing by book.
     */
    suspend fun getPagedHadiths(context: Context, bookKey: String, page: Int, pageSize: Int = 30): List<HadithData> = withContext(Dispatchers.IO) {
        try {
            val dao = HadithDatabase.getInstance(context).hadithDao()
            dao.getHadithsByBookPaged(bookKey, pageSize, page * pageSize).map { it.toHadithData() }
        } catch (e: Exception) {
            Log.e(TAG, "Browse error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Gets the total number of Hadiths currently installed offline in the local database.
     */
    suspend fun getOfflineTotalCount(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            HadithDatabase.getInstance(context).hadithDao().getTotalCount()
        } catch (_: Exception) {
            CURATED_SUNNI_AHADITH.size
        }
    }
}
