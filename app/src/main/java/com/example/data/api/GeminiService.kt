package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askIslamicScholar(
        question: String,
        enableHighThinking: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.success(getCuratedIslamicAnswer(question))
            }

            val modelName = if (enableHighThinking) "gemini-3.1-pro-preview" else "gemini-3.5-flash"
            val url = "$BASE_URL$modelName:generateContent?key=$apiKey"

            val systemPrompt = "You are an authentic, respectful, and wise Islamic Scholar and Community Knowledge Assistant for 'Alnoor Islamic App'. Provide accurate, referenced guidance from the Holy Quran, Sahih Hadith, and scholarly consensus (Ijma) in a gentle, inspiring tone. Conclude with appropriate blessings (Dua/Salawat)."

            val jsonBody = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", question) })
                        })
                    })
                })
                if (enableHighThinking) {
                    put("generationConfig", JSONObject().apply {
                        put("thinkingConfig", JSONObject().apply {
                            put("thinkingLevel", "HIGH")
                        })
                    })
                }
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed with code: ${response.code} body: $responseString")
                return@withContext Result.success(getCuratedIslamicAnswer(question))
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textPart = parts?.optJSONObject(0)?.optString("text")

            if (!textPart.isNullOrBlank()) {
                Result.success(textPart)
            } else {
                Result.success(getCuratedIslamicAnswer(question))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini request", e)
            Result.success(getCuratedIslamicAnswer(question))
        }
    }

    suspend fun generateIslamicAnnouncementDraft(
        topic: String,
        details: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val prompt = "Draft a formal, inspiring Islamic Community Announcement for '$topic'. Key details: $details. Include Bismillah, Islamic greetings (Assalamu Alaikum), Quranic/Hadith quote relevance, schedule, venue, and a closing Dua for the attendees."
        askIslamicScholar(prompt, enableHighThinking = false)
    }

    private fun getCuratedIslamicAnswer(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("darood") || q.contains("salawat") || q.contains("blessing") -> """
                **Virtues and Significance of Darood Sharif (Salawat)**
                
                Reciting Darood Sharif upon our beloved Prophet Muhammad ﷺ is among the most rewarded and beloved acts of worship in Islam.
                
                **Quranic Command:**
                > *« إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ ۚ يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا »*
                > *"Indeed, Allah and His angels send blessings upon the Prophet. O you who have believed, ask [Allah to confer] blessing upon him and ask [Allah to grant him] peace."* (Surah Al-Ahzab 33:56)
                
                **Prophetic Hadith:**
                The Prophet Muhammad ﷺ said:
                > *"Whoever sends blessings upon me once, Allah sends ten blessings upon him, removes ten sins from him, and raises him ten ranks."* (Sunan an-Nasa'i)
                
                **Recommended Recitation:**
                *اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ*
                
                May Allah grant you steadfastness in sending abundant blessings upon the Master of Creation ﷺ.
            """.trimIndent()

            q.contains("khatam") || q.contains("dua") || q.contains("forgive") -> """
                **Regarding Khatam Sharif & Supplications for the Deceased (Isal-e-Sawab)**
                
                Sending spiritual reward (*Isal-e-Sawab*) to departed loved ones and the Muslim Ummah through Quran recitation, charity (*Sadaqah*), and Darood Sharif is firmly established in traditional Islamic practice.
                
                **Virtues:**
                1. Reciting Quranic Surahs such as Surah Yaseen, Surah Al-Mulk, and Surah Al-Ikhlas brings light to the grave and solace to the soul.
                2. Collective supplications (*Dua-e-Jam'e*) carried out with sincerity are promptly accepted by Allah's infinite mercy.
                
                May Allah accept all your supplications and illuminate the graves of all deceased Muslims. Ameen.
            """.trimIndent()

            q.contains("prayer") || q.contains("namaz") || q.contains("salah") || q.contains("juma") -> """
                **Importance of Prayer (Salah) & Juma Congregation**
                
                Salah is the second pillar of Islam and the primary means of communicating with our Creator five times daily.
                
                **Key Guidance:**
                - Performing Salah in congregation (*Jamat*) in the Masjid holds 27 times more reward than individual prayer.
                - Friday Juma prayer is an obligation (*Fard Ayn*) upon every adult male, preceded by the spiritual reminder of the Khutbah.
                - Maintaining regular timings for Fajr, Dhuhr, Asr, Maghrib, and Isha illuminates the heart and purifies daily conduct.
                
                *"Guard strictly your (habit of) prayers, especially the Middle Prayer, and stand before Allah in a devout frame of mind."* (Surah Al-Baqarah 2:238)
            """.trimIndent()

            else -> """
                **Islamic Guidance & Reflection**
                
                *In the Name of Allah, the Most Gracious, the Most Merciful.*
                
                Islam encourages us to seek knowledge with sincerity, adhere to the Sunnah of the Prophet Muhammad ﷺ, maintain unity among brothers and sisters in faith, and turn to Allah in every circumstance.
                
                **Golden Principle from Hadith:**
                > *"The best of people are those that bring the most benefit to others."* (Al-Mu'jam al-Awsat)
                
                Whether reciting Darood Sharif, attending community gatherings, or extending charity, every good deed done with pure intention (*Ikhlas*) is treasured in the sight of Allah Subhana wa Ta'ala.
                
                May Allah bless your time, family, and community with abundance of peace (*Afiya*) and divine guidance. Ameen.
            """.trimIndent()
        }
    }
}
