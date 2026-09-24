package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.HadithDatabase
import com.example.data.local.HadithEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class HadithBookInfo(
    val key: String,
    val titleEnglish: String,
    val titleArabic: String,
    val approximateCount: Int,
    val isBundled: Boolean,
    val estimatedDownloadMb: String
)

sealed class BookDownloadState {
    object Idle : BookDownloadState()
    data class Downloading(val bookKey: String, val progress: Float, val statusMessage: String) : BookDownloadState()
    data class Success(val bookKey: String, val count: Int) : BookDownloadState()
    data class Error(val bookKey: String, val errorMessage: String) : BookDownloadState()
}

object HadithDownloadManager {
    private const val TAG = "HadithDownloadManager"

    val SUNNI_HADITH_BOOKS = listOf(
        HadithBookInfo(
            key = "bukhari",
            titleEnglish = "Sahih al-Bukhari",
            titleArabic = "صحيح البخاري",
            approximateCount = 7589,
            isBundled = true,
            estimatedDownloadMb = "Bundled"
        ),
        HadithBookInfo(
            key = "muslim",
            titleEnglish = "Sahih Muslim",
            titleArabic = "صحيح مسلم",
            approximateCount = 7563,
            isBundled = true,
            estimatedDownloadMb = "Bundled"
        ),
        HadithBookInfo(
            key = "tirmidhi",
            titleEnglish = "Jami` at-Tirmidhi",
            titleArabic = "جامع الترمذي",
            approximateCount = 3956,
            isBundled = false,
            estimatedDownloadMb = "5.2 MB"
        ),
        HadithBookInfo(
            key = "abudawud",
            titleEnglish = "Sunan Abi Dawud",
            titleArabic = "سنن أبي داود",
            approximateCount = 5274,
            isBundled = false,
            estimatedDownloadMb = "6.8 MB"
        ),
        HadithBookInfo(
            key = "nasai",
            titleEnglish = "Sunan an-Nasa'i",
            titleArabic = "سنن النسائي",
            approximateCount = 5758,
            isBundled = false,
            estimatedDownloadMb = "7.2 MB"
        ),
        HadithBookInfo(
            key = "ibnmajah",
            titleEnglish = "Sunan Ibn Majah",
            titleArabic = "سنن ابن ماجه",
            approximateCount = 4341,
            isBundled = false,
            estimatedDownloadMb = "5.5 MB"
        )
    )

    private val _downloadState = MutableStateFlow<BookDownloadState>(BookDownloadState.Idle)
    val downloadState: StateFlow<BookDownloadState> = _downloadState.asStateFlow()

    suspend fun isBookDownloaded(context: Context, bookKey: String): Boolean = withContext(Dispatchers.IO) {
        val dao = HadithDatabase.getInstance(context).hadithDao()
        val count = dao.getCountByBook(bookKey)
        count > 0
    }

    suspend fun getBookCount(context: Context, bookKey: String): Int = withContext(Dispatchers.IO) {
        HadithDatabase.getInstance(context).hadithDao().getCountByBook(bookKey)
    }

    suspend fun downloadBook(context: Context, bookKey: String): Result<Int> = withContext(Dispatchers.IO) {
        val bookInfo = SUNNI_HADITH_BOOKS.firstOrNull { it.key == bookKey }
            ?: return@withContext Result.failure(IllegalArgumentException("Unknown book: $bookKey"))

        _downloadState.value = BookDownloadState.Downloading(bookKey, 0.05f, "Connecting to repository...")

        try {
            val araUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-$bookKey.min.json"
            val engUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/eng-$bookKey.min.json"
            val urdUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/urd-$bookKey.min.json"

            coroutineScope {
                _downloadState.value = BookDownloadState.Downloading(bookKey, 0.15f, "Downloading Arabic text...")
                val araDef = async { fetchJsonString(araUrl) }

                _downloadState.value = BookDownloadState.Downloading(bookKey, 0.25f, "Downloading English translation...")
                val engDef = async { fetchJsonString(engUrl) }

                _downloadState.value = BookDownloadState.Downloading(bookKey, 0.35f, "Downloading Urdu translation...")
                val urdDef = async { fetchJsonString(urdUrl) }

                val araJson = araDef.await() ?: throw IllegalStateException("Failed to download Arabic texts for $bookKey")
                val engJson = engDef.await()
                val urdJson = urdDef.await()

                _downloadState.value = BookDownloadState.Downloading(bookKey, 0.55f, "Parsing texts and chapters...")

                val araObj = JSONObject(araJson)
                val engObj = if (!engJson.isNullOrBlank()) JSONObject(engJson) else null
                val urdObj = if (!urdJson.isNullOrBlank()) JSONObject(urdJson) else null

                val engMap = mutableMapOf<Int, String>()
                val sections = mutableMapOf<String, String>()
                if (engObj != null) {
                    val hadithsArr = engObj.optJSONArray("hadiths")
                    if (hadithsArr != null) {
                        for (i in 0 until hadithsArr.length()) {
                            val h = hadithsArr.getJSONObject(i)
                            engMap[h.optInt("hadithnumber")] = h.optString("text", "")
                        }
                    }
                    val secObj = engObj.optJSONObject("metadata")?.optJSONObject("section")
                    if (secObj != null) {
                        val keys = secObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            sections[k] = secObj.optString(k, "")
                        }
                    }
                }

                val urdMap = mutableMapOf<Int, String>()
                if (urdObj != null) {
                    val hadithsArr = urdObj.optJSONArray("hadiths")
                    if (hadithsArr != null) {
                        for (i in 0 until hadithsArr.length()) {
                            val h = hadithsArr.getJSONObject(i)
                            urdMap[h.optInt("hadithnumber")] = h.optString("text", "")
                        }
                    }
                }

                val araHadiths = araObj.optJSONArray("hadiths") ?: throw IllegalStateException("No hadiths found in Arabic data")
                val total = araHadiths.length()
                val entities = ArrayList<HadithEntity>(total)

                for (i in 0 until total) {
                    val h = araHadiths.getJSONObject(i)
                    val num = h.optInt("hadithnumber")
                    val ref = h.optJSONObject("reference")
                    val bookNum = ref?.optString("book", "1") ?: "1"
                    val hadithInBook = ref?.optString("hadith", num.toString()) ?: num.toString()

                    val chapter = sections[bookNum] ?: "Sunnah & Traditions"
                    val eng = engMap[num] ?: ""
                    val urd = urdMap[num] ?: ""
                    val ara = h.optString("text", "").trim()

                    var narrator = "Prophetic Sunnah (ﷺ)"
                    if (eng.isNotBlank()) {
                        val idx = eng.indexOf(':')
                        if (idx in 5..80 && (eng.startsWith("Narrated", ignoreCase = true) || eng.startsWith("On the authority", ignoreCase = true))) {
                            narrator = eng.substring(0, idx).trim()
                        }
                    }

                    val grade = if (bookKey == "tirmidhi" || bookKey == "abudawud" || bookKey == "nasai" || bookKey == "ibnmajah") {
                        val gradesArr = h.optJSONArray("grades")
                        if (gradesArr != null && gradesArr.length() > 0) {
                            val gObj = gradesArr.optJSONObject(0)
                            gObj?.optString("grade", "Sahih / Hasan") ?: "Sahih / Hasan"
                        } else {
                            "Hasan / Sahih"
                        }
                    } else {
                        "Sahih (صحیح)"
                    }

                    val reference = "${bookInfo.titleEnglish} $num, Book $bookNum, Hadith $hadithInBook"

                    entities.add(
                        HadithEntity(
                            id = "${bookKey}_$num",
                            bookKey = bookKey,
                            book = bookInfo.titleEnglish,
                            hadithNumber = num.toString(),
                            chapter = chapter,
                            narrator = narrator,
                            arabicText = ara,
                            urduTranslation = if (urd.isNotBlank()) urd else "رسول اللہ ﷺ کی مبارک حدیث شریف — ${bookInfo.titleEnglish} سے ماخوذ۔",
                            englishTranslation = if (eng.isNotBlank()) eng else "Prophetic Hadith from ${bookInfo.titleEnglish}.",
                            grade = grade,
                            reference = reference,
                            isBundled = false
                        )
                    )
                }

                _downloadState.value = BookDownloadState.Downloading(bookKey, 0.80f, "Saving to offline database...")

                val dao = HadithDatabase.getInstance(context).hadithDao()
                // Batch insert in chunks of 500
                val batchSize = 500
                var inserted = 0
                for (chunk in entities.chunked(batchSize)) {
                    dao.insertHadiths(chunk)
                    inserted += chunk.size
                    val progress = 0.80f + (0.20f * (inserted.toFloat() / entities.size))
                    _downloadState.value = BookDownloadState.Downloading(bookKey, progress, "Saved $inserted / ${entities.size} Hadiths...")
                }

                _downloadState.value = BookDownloadState.Success(bookKey, entities.size)
                Log.d(TAG, "Successfully downloaded and stored ${entities.size} hadiths for $bookKey")
                Result.success(entities.size)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for $bookKey: ${e.message}", e)
            _downloadState.value = BookDownloadState.Error(bookKey, e.message ?: "Download failed")
            Result.failure(e)
        }
    }

    suspend fun deleteBook(context: Context, bookKey: String): Boolean = withContext(Dispatchers.IO) {
        val book = SUNNI_HADITH_BOOKS.firstOrNull { it.key == bookKey }
        if (book?.isBundled == true) {
            // Cannot delete permanently bundled books
            return@withContext false
        }
        val dao = HadithDatabase.getInstance(context).hadithDao()
        dao.deleteDownloadedBook(bookKey)
        true
    }

    private fun fetchJsonString(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 12000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AlnoorApp/1.0")
            }
            if (conn.responseCode == 200) {
                BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "HTTP fetch failed for $urlStr: ${e.message}")
            null
        }
    }
}
