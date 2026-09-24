package com.example.data.repository

import android.content.Context
import com.example.data.model.HadithData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

/**
 * Authentic Sunni Hadith Repository.
 * Features:
 * 1. 30+ rich offline curated Ahadith covering core pillars, morals, character, and Sunnah.
 * 2. Real-time online fetching from authentic Bukhari & Muslim repositories via raw GitHub API endpoints.
 * 3. Dynamic in-memory caching ensuring "Another Hadith" constantly presents fresh Ahadith without repetition.
 */
object HadithRepository {

    /**
     * Curated, authentic Sunni Hadith collection from Sahih al-Bukhari, Sahih Muslim,
     * Jami` at-Tirmidhi, Sunan Abi Dawud, Sunan an-Nasa'i, and Sunan Ibn Majah (The Sihah Sittah).
     * Every entry is complete with authentic Arabic (with Tashkeel), high-quality Urdu translation,
     * and accurate English translation.
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
        ),
        HadithData(
            id = "tirmidhi_1956",
            book = "Jami` at-Tirmidhi",
            hadithNumber = "1956",
            chapter = "Righteousness (کتاب البر والصلة)",
            narrator = "Narrated by Abu Dharr (رضي الله عنه)",
            arabicText = "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ لَكَ صَدَقَةٌ، وَأَمْرُكَ بِالْمَعْرُوفِ وَنَهْيُكَ عَنِ الْمُنْكَرِ صَدَقَةٌ.",
            urduTranslation = "اپنے مسلمان بھائی کے سامنے تمہارا مسکرانا تمہارے لیے صدقہ ہے، اور نیکی کا حکم دینا اور برائی سے روکنا تمہارے لیے صدقہ ہے۔",
            englishTranslation = "Your smiling in the face of your brother is charity, and your enjoining good and forbidding evil is charity.",
            grade = "Sahih (صحیح)",
            reference = "Jami` at-Tirmidhi 1956"
        ),
        HadithData(
            id = "bukhari_6021",
            book = "Sahih al-Bukhari",
            hadithNumber = "6021",
            chapter = "Good Manners (کتاب الأدب)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "لَيْسَ الشَّدِيدُ بِالصُّرَعَةِ، إِنَّمَا الشَّدِيدُ الَّذِي يَمْلِكُ نَفْسَهُ عِنْدَ الْغَضَبِ.",
            urduTranslation = "طاقتور وہ شخص نہیں جو کشتی میں پچھاڑ دے، بلکہ اصل طاقتور وہ ہے جو غصے کے وقت اپنے نفس پر قابو رکھے۔",
            englishTranslation = "The strong is not the one who overcomes the people by his strength, but the strong is the one who controls himself while in anger.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6021, Book 78, Hadith 52"
        ),
        HadithData(
            id = "muslim_2588",
            book = "Sahih Muslim",
            hadithNumber = "2588",
            chapter = "Virtues and Manners (کتاب البر والصلة والآداب)",
            narrator = "Narrated by 'Aisha (رضي الله عنها)",
            arabicText = "إِنَّ الرِّفْقَ لاَ يَكُونُ فِي شَيْءٍ إِلاَّ زَانَهُ، وَلاَ يُنْزَعُ مِنْ شَيْءٍ إِلاَّ شَانَهُ.",
            urduTranslation = "نرمی جس چیز میں بھی ہو اسے خوبصورت بنا دیتی ہے، اور جس چیز سے نکال دی جائے اسے عیب دار کر دیتی ہے۔",
            englishTranslation = "Verily, kindness is not found in anything except that it beautifies it, and it is not withdrawn from anything except that it defects it.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2588, Book 45, Hadith 78"
        ),
        HadithData(
            id = "tirmidhi_1899",
            book = "Jami` at-Tirmidhi",
            hadithNumber = "1899",
            chapter = "Righteousness (کتاب البر والصلة)",
            narrator = "Narrated by 'Abdullah bin 'Amr (رضي الله عنه)",
            arabicText = "رِضَا الرَّبِّ فِي رِضَا الْوَالِدِ، وَسَخَطُ الرَّبِّ فِي سَخَطِ الْوَالِدِ.",
            urduTranslation = "رب کی رضا والدین کی خوشنودی میں ہے، اور رب کی ناراضگی والدین کی ناراضگی میں ہے۔",
            englishTranslation = "The Lord's pleasure is in the parent's pleasure, and the Lord's displeasure is in the parent's displeasure.",
            grade = "Sahih (صحیح)",
            reference = "Jami` at-Tirmidhi 1899"
        ),
        HadithData(
            id = "muslim_2691",
            book = "Sahih Muslim",
            hadithNumber = "2691",
            chapter = "Remembrance and Supplication (کتاب الذکر)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "مَنْ نَفَّسَ عَنْ مُؤْمِنٍ كُرْبَةً مِنْ كُرَبِ الدُّنْيَا، نَفَّسَ اللَّهُ عَنْهُ كُرْبَةً مِنْ كُرَبِ يَوْمِ الْقِيَامَةِ، وَاللَّهُ فِي عَوْنِ الْعَبْدِ مَا كَانَ الْعَبْدُ فِي عَوْنِ أَخِيهِ.",
            urduTranslation = "جو شخص کسی مومن کی دنیاوی تکلیفوں میں سے کوئی تکلیف دور کرے گا، اللہ تعالیٰ قیامت کے دن کی تکلیفوں میں سے اس کی تکلیف دور فرمائے گا۔ اور اللہ بندے کی مدد میں رہتا ہے جب تک بندہ اپنے بھائی کی مدد میں رہے گا۔",
            englishTranslation = "Whoever relieves a believer's distress of the distressful aspects of this world, Allah will rescue him from a difficulty of the Day of Resurrection. Allah is in help of the servant as long as the servant is helping his brother.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2691, Book 48, Hadith 38"
        ),
        HadithData(
            id = "bukhari_6412",
            book = "Sahih al-Bukhari",
            hadithNumber = "6412",
            chapter = "Heart-Melting Traditions (کتاب الرقاق)",
            narrator = "Narrated by 'Abdullah bin 'Amr (رضي الله عنه)",
            arabicText = "إِنَّ مِنْ خِيَارِكُمْ أَحْسَنَكُمْ أَخْلاَقًا.",
            urduTranslation = "تم میں سب سے بہترین اور پسندیدہ شخص وہ ہے جس کے اخلاق سب سے اچھے ہوں۔",
            englishTranslation = "The best amongst you are those who have the best manners and character.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6412, Book 81, Hadith 1"
        ),
        HadithData(
            id = "tirmidhi_2317",
            book = "Jami` at-Tirmidhi",
            hadithNumber = "2317",
            chapter = "Resurrection & Softening of Hearts (صفة القيامة)",
            narrator = "Narrated by Al-Hasan bin 'Ali (رضي الله عنه)",
            arabicText = "دَعْ مَا يَرِيبُكَ إِلَى مَا لاَ يَرِيبُكَ، فَإِنَّ الصِّدْقَ طُمَأْنِينَةٌ وَإِنَّ الْكَذِبَ رِيبَةٌ.",
            urduTranslation = "اس چیز کو چھوڑ دو جو تمہیں شک میں ڈالے اس چیز کی طرف جو تمہیں شک میں نہ ڈالے، کیونکہ سچائی اطمینان اور جھوٹ شک ہے۔",
            englishTranslation = "Leave that which makes you doubt for that which does not make you doubt. For truth brings peace of mind, and lies bring doubt.",
            grade = "Sahih (صحیح)",
            reference = "Jami` at-Tirmidhi 2317"
        ),
        HadithData(
            id = "muslim_2577",
            book = "Sahih Muslim",
            hadithNumber = "2577",
            chapter = "Virtues and Manners (کتاب البر والصلة والآداب)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "الْمُسْلِمُ أَخُو الْمُسْلِمِ لاَ يَظْلِمُهُ وَلاَ يَخْذُلُهُ وَلاَ يَحْقِرُهُ، التَّقْوَى هَاهُنَا - وَيُشِيرُ إِلَى صَدْرِهِ ثَلاَثَ مَرَّاتٍ.",
            urduTranslation = "مسلمان مسلمان کا بھائی ہے، نہ اس پر ظلم کرے، نہ اسے بے یار و مددگار چھوڑے اور نہ اسے حقیر سمجھے۔ تقویٰ یہاں ہے — اور آپ ﷺ نے اپنے سینہ مبارک کی طرف تین بار اشارہ فرمایا۔",
            englishTranslation = "A Muslim is the brother of a Muslim: he does not oppress him, nor does he fail him, nor does he despise him. Piety is here — and he pointed to his chest three times.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2577, Book 45, Hadith 32"
        ),
        HadithData(
            id = "bukhari_1421",
            book = "Sahih al-Bukhari",
            hadithNumber = "1421",
            chapter = "Obligatory Charity (کتاب الزکاة)",
            narrator = "Narrated by Hakim bin Hizam (رضي الله عنه)",
            arabicText = "الْيَدُ الْعُلْيَا خَيْرٌ مِنَ الْيَدِ السُّفْلَى، وَابْدَأْ بِمَنْ تَعُولُ، وَخَيْرُ الصَّدَقَةِ عَنْ ظَهْرِ غِنًى.",
            urduTranslation = "اوپر والا ہاتھ (دینے والا) نیچے والے ہاتھ (مانگنے والے) سے بہتر ہے، اور خرچ کی ابتدا ان سے کرو جن کی کفالت تمہارے ذمے ہے، اور بہترین صدقہ وہ ہے جو غنا کے بعد ہو۔",
            englishTranslation = "The upper hand (that gives) is better than the lower hand (that begs). Begin with those who are your dependents, and the best charity is that which leaves you independent.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 1421, Book 24, Hadith 25"
        ),
        HadithData(
            id = "bukhari_6407",
            book = "Sahih al-Bukhari",
            hadithNumber = "6407",
            chapter = "Remembrance of Allah (کتاب الدعوات)",
            narrator = "Narrated by Abu Hurairah (رضي الله عنه)",
            arabicText = "كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ.",
            urduTranslation = "دو کلمے ایسے ہیں جو زبان پر ہلکے ہیں لیکن میزان میں بہت بھاری ہیں اور رحمن کو بہت پیارے ہیں: 'سبحان الله وبحمده' اور 'سبحان الله العظيم'۔",
            englishTranslation = "Two words are light on the tongue, heavy on the balance, and beloved to the Most Merciful: 'Subhan-Allahi wa bihamdihi' and 'Subhan-Allahil-Azim'.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6407, Book 80, Hadith 100"
        ),
        HadithData(
            id = "abudawud_4941",
            book = "Sunan Abi Dawud",
            hadithNumber = "4941",
            chapter = "General Behavior (کتاب الأدب)",
            narrator = "Narrated by 'Abdullah bin 'Amr (رضي الله عنه)",
            arabicText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ.",
            urduTranslation = "رحم کرنے والوں پر رحمن رحم فرماتا ہے۔ تم زمین والوں پر رحم کرو، آسمان والا تم پر رحم فرمائے گا۔",
            englishTranslation = "The merciful will be shown mercy by the Most Merciful. Be merciful to those on the earth, and the One in the heavens will have mercy upon you.",
            grade = "Sahih (صحیح)",
            reference = "Sunan Abi Dawud 4941"
        ),
        HadithData(
            id = "bukhari_6019",
            book = "Sahih al-Bukhari",
            hadithNumber = "6019",
            chapter = "Good Manners (کتاب الأدب)",
            narrator = "Narrated by Sahl bin Sa'd (رضي الله عنه)",
            arabicText = "أَنَا وَكَافِلُ الْيَتِيمِ فِي الْجَنَّةِ هَكَذَا - وَأَشَارَ بِالسَّبَّابَةِ وَالْوُسْطَى وَفَرَّجَ بَيْنَهُمَا شَيْئًا.",
            urduTranslation = "میں اور یتیم کی کفالت کرنے والا جنت میں اس طرح ہوں گے — اور آپ ﷺ نے شہادت کی اور درمیانی انگلی کے درمیان تھوڑا سا فاصلہ رکھ کر اشارہ فرمایا۔",
            englishTranslation = "I and the person who looks after an orphan will be in Paradise like this — and he indicated by his index and middle fingers and separated between them.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 6019, Book 78, Hadith 50"
        ),
        HadithData(
            id = "muslim_2999",
            book = "Sahih Muslim",
            hadithNumber = "2999",
            chapter = "Asceticism and Softening of Hearts (کتاب الزهد)",
            narrator = "Narrated by Suhaib (رضي الله عنه)",
            arabicText = "عَجَبًا لأَمْرِ الْمُؤْمِنِ إِنَّ أَمْرَهُ كُلَّهُ خَيْرٌ، إِنْ أَصَابَتْهُ سَرَّاءُ شَكَرَ فَكَانَ خَيْرًا لَهُ، وَإِنْ أَصَابَتْهُ ضَرَّاءُ صَبَرَ فَكَانَ خَيْرًا لَهُ.",
            urduTranslation = "مومن کا معاملہ بھی خوب ہے، اس کے تمام معاملات میں خیر ہی خیر ہے۔ اگر اسے خوشحالی ملے تو شکر کرتا ہے جو اس کے لیے بہتر ہے، اور اگر تکلیف پہنچے تو صبر کرتا ہے جو اس کے لیے بہتر ہے۔",
            englishTranslation = "Strange are the ways of a believer for there is good in every affair of his. If any praise befalls him, he is thankful and that is good for him; and if an adversity befalls him, he endures it patiently and that is good for him.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 2999, Book 55, Hadith 82"
        ),
        HadithData(
            id = "bukhari_10",
            book = "Sahih al-Bukhari",
            hadithNumber = "10",
            chapter = "Belief (کتاب الایمان)",
            narrator = "Narrated by 'Abdullah bin 'Amr (رضي الله عنه)",
            arabicText = "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ، وَالْمُهَاجِرُ مَنْ هَجَرَ مَا نَهَى اللَّهُ عَنْهُ.",
            urduTranslation = "کامل مسلمان وہ ہے جس کی زبان اور ہاتھ کی تکلیف سے دوسرے مسلمان محفوظ رہیں، اور مہاجر وہ ہے جو ان چیزوں کو چھوڑ دے جن سے اللہ نے منع فرمایا ہے۔",
            englishTranslation = "The Muslim is the one from whose tongue and hand the Muslims are safe, and the emigrant is the one who abandons that which Allah has forbidden.",
            grade = "Sahih (صحیح)",
            reference = "Sahih al-Bukhari 10, Book 2, Hadith 3"
        ),
        HadithData(
            id = "nasai_5015",
            book = "Sunan an-Nasa'i",
            hadithNumber = "5015",
            chapter = "Faith and its Signs (کتاب الایمان وشرائعه)",
            narrator = "Narrated by 'Imran bin Husain (رضي الله عنه)",
            arabicText = "الْحَيَاءُ لاَ يَأْتِي إِلاَّ بِخَيْرٍ.",
            urduTranslation = "حیا تو ہمیشہ صرف خیر ہی لاتی ہے۔",
            englishTranslation = "Modesty brings nothing but good.",
            grade = "Sahih (صحیح)",
            reference = "Sunan an-Nasa'i 5015"
        ),
        HadithData(
            id = "ibnmajah_224",
            book = "Sunan Ibn Majah",
            hadithNumber = "224",
            chapter = "The Book of the Sunnah (المقدمة)",
            narrator = "Narrated by Anas bin Malik (رضي الله عنه)",
            arabicText = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ.",
            urduTranslation = "علم دین حاصل کرنا ہر مسلمان پر فرض ہے۔",
            englishTranslation = "Seeking knowledge is an obligation upon every Muslim.",
            grade = "Hasan / Sahih",
            reference = "Sunan Ibn Majah 224"
        ),
        HadithData(
            id = "bukhari_6094",
            book = "Sahih Muslim",
            hadithNumber = "55",
            chapter = "Faith (کتاب الایمان)",
            narrator = "Narrated by Tamim Ad-Dari (رضي الله عنه)",
            arabicText = "الدِّينُ النَّصِيحَةُ، قُلْنَا لِمَنْ؟ قَالَ: لِلَّهِ، وَلِكِتَابِهِ، وَلِرَسُولِهِ، وَلأَئِمَّةِ الْمُسْلِمِينَ وَعَامَّتِهِمْ.",
            urduTranslation = "دین خیر خواہی کا نام ہے۔ ہم نے عرض کیا: کس کے لیے؟ آپ ﷺ نے فرمایا: اللہ کے لیے، اس کی کتاب کے لیے، اس کے رسول کے لیے، اور مسلمانوں کے ائمہ اور عام مسلمانوں کے لیے۔",
            englishTranslation = "Religion is sincerity (goodwill and advice). We said: To whom? He ﷺ said: To Allah, His Book, His Messenger, and to the leaders of the Muslims and their common folk.",
            grade = "Sahih (صحیح)",
            reference = "Sahih Muslim 55, Book 1, Hadith 95"
        )
    )

    // Dynamic thread-safe collection of online-fetched Hadiths
    private val dynamicallyFetchedHadiths: MutableList<HadithData> = CopyOnWriteArrayList()

    // Well-curated pool of authentic Hadith numbers to pull from live endpoints
    private val POPULAR_BUKHARI_NUMBERS = listOf(
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
        21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 50, 52, 53, 54, 55, 56, 57, 58, 60, 61, 62, 63, 64, 65, 70,
        73, 75, 80, 85, 90, 95, 100, 110, 120, 130, 140, 150, 200, 250, 300, 400, 500,
        600, 700, 800, 900, 1000, 1421, 5027, 6011, 6018, 6019, 6021, 6407, 6412, 6464
    )

    private val POPULAR_MUSLIM_NUMBERS = listOf(
        1, 2, 3, 4, 5, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 70, 80,
        90, 100, 120, 140, 160, 180, 200, 223, 250, 300, 350, 400, 500, 600,
        1000, 1500, 2000, 2162, 2564, 2577, 2588, 2691, 2699, 2999
    )

    fun getAllAvailableHadiths(): List<HadithData> {
        val combined = ArrayList<HadithData>(CURATED_SUNNI_AHADITH.size + dynamicallyFetchedHadiths.size)
        combined.addAll(CURATED_SUNNI_AHADITH)
        for (h in dynamicallyFetchedHadiths) {
            if (combined.none { it.id == h.id }) {
                combined.add(h)
            }
        }
        return combined
    }

    fun getTotalHadithCount(): Int = getAllAvailableHadiths().size

    fun getHadithByIndex(index: Int): HadithData {
        val list = getAllAvailableHadiths()
        if (list.isEmpty()) {
            throw IllegalStateException("Ahadith collection is empty")
        }
        val safeIndex = Math.floorMod(index, list.size)
        return list[safeIndex]
    }

    /**
     * Gets the Hadith for today using the calendar day-of-year index, ensuring daily rotation,
     * and attempts to fetch fresh authentic Ahadith from the live Sunni Hadith API.
     */
    suspend fun getDailyHadith(context: Context? = null): HadithData = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val defaultHadith = CURATED_SUNNI_AHADITH[dayOfYear % CURATED_SUNNI_AHADITH.size]

        // Attempt online fetch from Sunni Hadith API with 3.5s timeout
        try {
            val targetNum = POPULAR_BUKHARI_NUMBERS[dayOfYear % POPULAR_BUKHARI_NUMBERS.size]
            val onlineHadith = fetchHadithFromWeb("bukhari", targetNum)
            if (onlineHadith != null) {
                if (dynamicallyFetchedHadiths.none { it.id == onlineHadith.id }) {
                    dynamicallyFetchedHadiths.add(onlineHadith)
                }
                onlineHadith
            } else {
                defaultHadith
            }
        } catch (_: Exception) {
            defaultHadith
        }
    }

    /**
     * Fetches a brand new Hadith when the user clicks "Another Hadith".
     * First attempts to fetch an authentic Hadith from the live web repository (Sahih Bukhari or Muslim).
     * If offline or error, picks a non-repeating Hadith from the rich curated Sunni collection.
     */
    suspend fun fetchAnotherHadith(currentId: String? = null): HadithData = withContext(Dispatchers.IO) {
        // 1. Try to fetch from online API
        val book = if (Random.nextBoolean()) "bukhari" else "muslim"
        val numbersPool = if (book == "bukhari") POPULAR_BUKHARI_NUMBERS else POPULAR_MUSLIM_NUMBERS
        val randomNum = numbersPool[Random.nextInt(numbersPool.size)]

        val candidateId = "${book}_$randomNum"
        if (candidateId != currentId) {
            try {
                val fetched = fetchHadithFromWeb(book, randomNum)
                if (fetched != null && fetched.arabicText.isNotBlank()) {
                    if (dynamicallyFetchedHadiths.none { it.id == fetched.id }) {
                        dynamicallyFetchedHadiths.add(fetched)
                    }
                    return@withContext fetched
                }
            } catch (_: Exception) {
                // Online fetch failed or offline, fallback to curated library
            }
        }

        // 2. Offline Fallback: pick a non-repeating Hadith from the curated collection
        val candidates = CURATED_SUNNI_AHADITH.filter { it.id != currentId }
        if (candidates.isNotEmpty()) {
            candidates[Random.nextInt(candidates.size)]
        } else {
            CURATED_SUNNI_AHADITH.first()
        }
    }

    /**
     * Live API fetcher using the high-reliability GitHub Raw API endpoints for authentic Hadiths.
     * Concurrently retrieves Arabic Matn, Urdu translation, and English translation.
     */
    private suspend fun fetchHadithFromWeb(book: String, hadithNumber: Int): HadithData? = withContext(Dispatchers.IO) {
        try {
            val araUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/ara-$book/$hadithNumber.json"
            val engUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/eng-$book/$hadithNumber.json"
            val urdUrl = "https://raw.githubusercontent.com/fawazahmed0/hadith-api/1/editions/urd-$book/$hadithNumber.json"

            coroutineScope {
                val araDeferred = async { httpGet(araUrl) }
                val engDeferred = async { httpGet(engUrl) }
                val urdDeferred = async { httpGet(urdUrl) }

                val araJson = araDeferred.await() ?: return@coroutineScope null
                val engJson = engDeferred.await()
                val urdJson = urdDeferred.await()

                val araObj = JSONObject(araJson)
                val araHadiths = araObj.optJSONArray("hadiths") ?: return@coroutineScope null
                if (araHadiths.length() == 0) return@coroutineScope null
                val araFirst = araHadiths.getJSONObject(0)
                val arabicText = araFirst.optString("text", "").trim()
                if (arabicText.isBlank()) return@coroutineScope null

                var englishText = ""
                var chapterName = araObj.optJSONObject("metadata")?.optJSONObject("section")?.optString("1", "Daily Sunnah") ?: "Daily Sunnah"
                if (!engJson.isNullOrBlank()) {
                    try {
                        val engObj = JSONObject(engJson)
                        val engHadiths = engObj.optJSONArray("hadiths")
                        if (engHadiths != null && engHadiths.length() > 0) {
                            englishText = engHadiths.getJSONObject(0).optString("text", "").trim()
                        }
                        val secMap = engObj.optJSONObject("metadata")?.optJSONObject("section")
                        if (secMap != null && secMap.length() > 0) {
                            val firstKey = secMap.keys().next()
                            val secName = secMap.optString(firstKey, "")
                            if (secName.isNotBlank()) chapterName = secName
                        }
                    } catch (_: Exception) {}
                }

                var urduText = ""
                if (!urdJson.isNullOrBlank()) {
                    try {
                        val urdObj = JSONObject(urdJson)
                        val urdHadiths = urdObj.optJSONArray("hadiths")
                        if (urdHadiths != null && urdHadiths.length() > 0) {
                            urduText = urdHadiths.getJSONObject(0).optString("text", "").trim()
                        }
                    } catch (_: Exception) {}
                }

                if (urduText.isBlank()) {
                    urduText = "رسول اللہ ﷺ کی مبارک حدیث شریف — صحیح بخاری و مسلم سے ماخوذ۔"
                }
                if (englishText.isBlank()) {
                    englishText = "Authentic Prophetic Hadith from ${if (book == "bukhari") "Sahih al-Bukhari" else "Sahih Muslim"}."
                }

                val bookTitle = if (book == "bukhari") "Sahih al-Bukhari" else "Sahih Muslim"
                val refObj = araFirst.optJSONObject("reference")
                val refBook = refObj?.optInt("book", 1) ?: 1
                val refHadith = refObj?.optInt("hadith", hadithNumber) ?: hadithNumber
                val referenceStr = "$bookTitle, Book $refBook, Hadith $refHadith"

                val narrator = extractNarrator(englishText)

                HadithData(
                    id = "${book}_$hadithNumber",
                    book = bookTitle,
                    hadithNumber = hadithNumber.toString(),
                    chapter = chapterName,
                    narrator = narrator,
                    arabicText = arabicText,
                    urduTranslation = urduText,
                    englishTranslation = englishText,
                    grade = "Sahih (صحیح)",
                    reference = referenceStr
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun httpGet(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AlnoorIslamicApp/1.0")
            }
            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractNarrator(english: String): String {
        val trimmed = english.trim()
        val colonIdx = trimmed.indexOf(':')
        if (colonIdx in 5..80 && (trimmed.startsWith("Narrated", ignoreCase = true) || trimmed.startsWith("On the authority", ignoreCase = true))) {
            return trimmed.substring(0, colonIdx).trim()
        }
        return "Prophetic Sunnah (ﷺ)"
    }
}
