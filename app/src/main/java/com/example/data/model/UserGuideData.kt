package com.example.data.model

data class QuickFaqItem(
    val question: String,
    val answer: String,
    val category: String
)

object UserGuideProvider {

    val QUICK_FAQS = listOf(
        QuickFaqItem(
            question = "How do I watch Live Jumu'ah sermons and broadcasts?",
            answer = "Tap 'Live' in the bottom navigation bar or tap the live banner on the Home Dashboard. When a broadcast is in progress, the video will automatically buffer. You can toggle between Full-Screen video, Picture-in-Picture mode, or Audio-Only mode for data saving.",
            category = "Live Broadcasts"
        ),
        QuickFaqItem(
            question = "How do I participate in a community Khatam Sharif?",
            answer = "Navigate to the Khatam Sharif section from the Home screen or Action Cards. Tap 'Reserve / Read Juz' to select an unallocated Para/Juz. Once you complete your recitation, tap 'Mark as Completed' so the overall community completion progress bar updates in real time.",
            category = "Khatam Sharif"
        ),
        QuickFaqItem(
            question = "Can I read Quran and Islamic books offline without internet?",
            answer = "Yes! All 114 Surahs of the Holy Quran, daily Duas, and downloaded or pre-loaded Islamic publications (including this manual) are stored locally in the offline database and can be read anytime without an active internet connection.",
            category = "Offline Mode"
        ),
        QuickFaqItem(
            question = "How do I make donations via Bank Transfer or Sadaqah?",
            answer = "Tap the 'Donate' card on the Home dashboard. You will see official Trust verified bank account details (IBAN, Account Number, Sort Code) with one-tap copy buttons. You can also specify the intent (Zakat, Sadaqah, Alnoor Islami Expansion) and submit payment confirmation receipts.",
            category = "Donations"
        ),
        QuickFaqItem(
            question = "How do I change prayer times calculation method?",
            answer = "Go to 'App Settings' -> 'Prayer Times & Notifications'. The app defaults to the University of Islamic Sciences, Karachi (Hanafi Asr method). You can view Fajr, Dhuhr, Asr, Maghrib, and Isha with Jama'ah times.",
            category = "Prayer Times"
        ),
        QuickFaqItem(
            question = "How do I contact the Trust office or request a Dua / Prayer?",
            answer = "Tap 'Messages' or 'Helpline'. You can send a direct message via WhatsApp, initiate a direct telephone call, or submit an official prayer request form for upcoming gatherings and Khatam Sharif dedications.",
            category = "Helpline & Support"
        ),
        QuickFaqItem(
            question = "How can I share or download PDF documents and publications?",
            answer = "When viewing any book or document in the Islamic Digital Library (or this User Guide), tap 'Share PDF' at the bottom to send the file via WhatsApp, Email, or Bluetooth, or tap 'Open in PDF App' to view in Adobe Acrobat or Google Drive.",
            category = "Library & PDFs"
        )
    )

    val FULL_MANUAL_TEXT = """
ALNOOR ISLAMI COMMUNITY MOBILE APPLICATION
OFFICIAL MEMBER USER GUIDE & OPERATION MANUAL
Document Version: 3.2 | Published by Alnoor Management Board & Research Bureau
================================================================================

بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
In the name of Allah, the Most Gracious, the Most Merciful.
Assalamu Alaikum wa Rahmatullahi wa Barakatuh.

Welcome to the official Alnoor Trust Community mobile application. This comprehensive manual provides detailed instructions on how to use every feature, service, and spiritual resource available in the app. Keep this guide saved on your device for offline reference or share it with family members.

--------------------------------------------------------------------------------
TABLE OF CONTENTS (ENGLISH SECTION)
--------------------------------------------------------------------------------
1.  Introduction & App Overview
2.  Home Dashboard & Quick Action Cards
3.  Live Video Broadcasts & Sermon Streaming
4.  Daily Prayer Times, Jama'ah Schedule & Qibla Direction
5.  Holy Quran Majeed, Translations & Audio Recitations
6.  Khatam Sharif Registration & Community Progress Tracker
7.  Official Announcements, Event Schedules & Bulletins
8.  Islamic Digital Library & PDF Document Reader
9.  Photo Gallery & Community Event Archives
10. Online Donations, Sadaqah Jariyah & Zakat
11. Alnoor Islami Helpline, WhatsApp Support & Dua Requests
12. Member Account, Profile & Role Privileges (Admin vs Member)
13. App Settings, Theme Customization & Offline Cache
14. Frequently Asked Questions (FAQ) & Troubleshooting

================================================================================
CHAPTER 1: INTRODUCTION & APP OVERVIEW
================================================================================
[SCREENSHOT: CHAPTER 1 - APPLICATION ARCHITECTURE & OVERVIEW]

The Alnoor Trust Community mobile application is an integrated digital platform designed to keep community members spiritually connected to Alnoor Islami, informed about religious events, and engaged in collective acts of worship.

Key Capabilities:
• 100% Offline-Ready: Access Quran, daily supplications, prayer timetables, and library books even without mobile data or Wi-Fi.
• Real-Time Cloud Synchronization: Announcements, live broadcasts, and Khatam Sharif progress automatically synchronize with cloud servers when connected.
• Built-in Document Reader: High-fidelity PDF engine renders Islamic books and guides directly in the app without requiring third-party PDF viewers.
• Multi-Theme Support: Switch seamlessly between Emerald Green, Midnight Navy, Warm Sepia, and High-Contrast modes for comfortable day and night reading.

================================================================================
CHAPTER 2: HOME DASHBOARD & QUICK ACTION CARDS
================================================================================
[SCREENSHOT: CHAPTER 2 - HOME DASHBOARD & ACTION CARDS]

Upon opening the app, the Home Dashboard presents a centralized view of everything happening at Alnoor Islami.

Elements on the Home Screen:
1. Alnoor Islami Header Banner: Displays today's Islamic Hijri date, Gregorian date, and current Alnoor Islami operational status.
2. Next Prayer Countdown: Shows the time remaining until the next obligatory prayer and congregational Jama'ah time.
3. Live Broadcast Indicator: When a religious lecture, Taraweeh, or Jumu'ah sermon is on air, a pulsating gold badge appears. Tapping this banner launches the live player immediately.
4. Quick Action Cards: Grid of shortcuts providing one-tap navigation to:
   • Live Streaming
   • Daily Prayer Times
   • Quran Majeed
   • Khatam Sharif
   • Community Notices
   • Islamic Digital Library
   • Photo Gallery
   • Donations & Sadaqah
   • Community Helpline
5. Important Notice Banner: Displays breaking announcements, Ramadan schedules, or Eid prayers directly below the header.

Tip: Admins can customize, reorder, or hide specific Action Cards via the Action Cards Manager in the Admin menu.

================================================================================
CHAPTER 3: LIVE VIDEO BROADCASTS & SERMON STREAMING
================================================================================
[SCREENSHOT: CHAPTER 3 - LIVE STREAM & AUDIO PLAYER]

Never miss a congregational prayer or spiritual gathering. The Live Stream section broadcasts real-time high-definition video and audio directly from the Alnoor Islami Mihrab and Minbar.

How to Use the Live Player:
1. Tap 'Live' in the bottom navigation bar or tap the live banner on the Home Dashboard.
2. The player will automatically connect to the active stream (HLS / M3U8 or YouTube Live).
3. Playback Controls:
   • Play / Pause: Tap the center of the video screen.
   • Fullscreen Mode: Tap the expand icon in the bottom-right corner of the video.
   • Audio-Only Mode: When driving or conserving cellular data, tap 'Audio Stream' to switch off video while maintaining crystal-clear audio recitation.
   • Volume & Mute: Use your device volume buttons or the in-player volume slider.
4. Stream Status: If Alnoor Islami is currently off-air, the player displays the upcoming broadcast schedule and links to recorded archives.

================================================================================
CHAPTER 4: DAILY PRAYER TIMES, JAMA'AH & QIBLA DIRECTION
================================================================================
[SCREENSHOT: CHAPTER 4 - PRAYER TIMETABLE & QIBLA COMPASS]

Accurate prayer timetables calculated specifically for Alnoor Islami and surrounding regions.

Features:
• 5 Obligatory Prayers: Fajr, Dhuhr, Asr, Maghrib, and Isha.
• Additional Times: Sunrise (Shurooq), Sunset, Tahajjud, and Suhoor/Iftar timings during Ramadan.
• Adhan vs Jama'ah: Both the astronomical beginning time (Adhan) and the actual congregational prayer time at Alnoor Islami (Iqamah/Jama'ah) are clearly listed.
• Calculation Standard: Follows the University of Islamic Sciences, Karachi (Hanafi Asr calculation).
• Qibla Compass: Built-in sensor-based compass pointing accurately toward the Holy Kaaba in Makkah al-Mukarramah. Ensure your device GPS/Location is enabled for precise directional alignment.

Setting Prayer Notifications:
Open App Settings -> Prayer Times & Notifications to enable or adjust Adhan reminders for each prayer.

================================================================================
CHAPTER 5: HOLY QURAN MAJEED, TRANSLATIONS & RECITATIONS
================================================================================
[SCREENSHOT: CHAPTER 5 - HOLY QURAN MUSHAF READER & AUDIO]

A complete, respectful digital Quran reader for daily Tilawat and study.

Capabilities:
• 114 Surahs Complete: Instant access to all Makki and Madani Surahs.
• Clear Arabic Script: High-contrast, authentic Uthmani / Indo-Pak Arabic calligraphy.
• English Translation & Transliteration: Side-by-side or line-by-line translation for non-Arabic speakers.
• Audio Recitation: Tap the speaker icon on any Surah to listen to verse-by-verse recitation by world-renowned Qaris.
• Bookmark & Favorite: Tap the bookmark icon to save your last-read Ayah so you can resume recitation anytime.
• Adjustable Font Size: Increase or decrease Arabic and translation text sizes using the Zoom controls in the top toolbar.

================================================================================
CHAPTER 6: KHATAM SHARIF REGISTRATION & PROGRESS TRACKER
================================================================================
[SCREENSHOT: CHAPTER 6 - KHATAM SHARIF 30 JUZ SELECTION]

The Khatam Sharif module enables the community to collectively complete recitations of the Holy Quran for religious occasions, deceased family members (Esal-e-Sawab), and communal blessings.

How to Participate:
1. Open the Khatam Sharif section from the Home screen.
2. View the Active Khatams list (e.g., Monthly Trust Khatam, Ramadan Khatam, Special Memorial Khatam).
3. Tap 'Join Recitation / Select Juz'.
4. A grid of all 30 Paras (Juz) is displayed:
   • Green / Gold: Available for reservation.
   • Grey / Locked: Already reserved by another brother or sister.
5. Tap an available Juz to reserve it in your name.
6. Once you finish reciting the Juz, return to this screen and tap 'Mark as Completed'.
7. The communal progress bar updates immediately, celebrating when all 30 Paras are completed for the collective Dua.

================================================================================
CHAPTER 7: OFFICIAL ANNOUNCEMENTS & IMPORTANT NOTICES
================================================================================
[SCREENSHOT: CHAPTER 7 - OFFICIAL ANNOUNCEMENTS & NOTICES]

Stay informed with real-time official notices from the Alnoor Islami Shura and Management Board.

Notice Categories:
• Breaking News & Urgent Alerts (pinned with a red priority banner).
• Community Events & Religious Gatherings (Jumu'ah timings, Milad-un-Nabi, Khatam gatherings, Janaza announcements).
• Ramadan & Eid Schedules (Moon sighting declarations, Eid prayer batches).
• Educational Courses & Madrasah admissions.

Attachment Downloads:
Many announcements include official PDF circulars or event flyers. Tap 'Download Notice PDF' to save or share the official flyer via WhatsApp.

================================================================================
CHAPTER 8: ISLAMIC DIGITAL LIBRARY & PDF DOCUMENT READER
================================================================================
[SCREENSHOT: CHAPTER 8 - DIGITAL LIBRARY & PDF VIEWER]

A curated library of authentic Islamic publications, prayer books, Fiqh manuals, and Trust publications.

Built-in Features:
• Offline Reading: All featured publications are pre-packaged or locally cached.
• Dual Reading Modes:
  - Original PDF Pages: Renders authentic, high-resolution pages using Android's native PDF engine. Supports pinch-to-zoom and multi-page scrolling.
  - Text Reader Mode: Converts text into an eye-comfort clean reader layout with customizable font sizes.
• Reading Themes: Choose between 'Emerald Night' (dark theme), 'Warm Sepia' (eye-friendly book paper), or 'Day Paper' (high-contrast white).
• Share with Family: Tap 'Share PDF' to directly share the physical PDF file via WhatsApp, Telegram, Gmail, or Bluetooth.
• External Viewer: Tap 'Open in PDF App' to launch the document inside Adobe Acrobat, Google Drive, or your device's default reader.
• Bookmarks: Star your favorite books to quickly locate them in the 'Bookmarked' filter tab.

================================================================================
CHAPTER 9: PHOTO GALLERY & COMMUNITY EVENT ARCHIVES
================================================================================
[SCREENSHOT: CHAPTER 9 - PHOTO GALLERY & EVENT ALBUMS]

Browse high-resolution photographs of Alnoor Islami construction milestones, community Eid celebrations, youth programs, and annual conferences.

• Albums: Organized by event and date for intuitive browsing.
• Full-Screen Viewer: Tap any photo to view in high definition with pinch-to-zoom support.
• Save to Device: Tap 'Save Image' or 'Share' to store memorable moments on your phone gallery.

================================================================================
CHAPTER 10: ONLINE DONATIONS, SADAQAH JARIYAH & ZAKAT
================================================================================
[SCREENSHOT: CHAPTER 10 - DONATION PORTAL & BANK DETAILS]

Support the House of Allah with transparency and ease.

Donation Channels:
1. Official Bank Transfers:
   • Account Name: Alnoor Islamic Trust
   • Bank Name: Alnoor Community Bank
   • Account Number & IBAN: Provided with one-tap copy buttons.
   • Sort Code / Swift Code: Clear details for domestic and international transfers.
2. Donation Categories:
   • General Alnoor Islami Maintenance & Utilities
   • Sadaqah Jariyah (Alnoor Islami Expansion & Building Fund)
   • Zakat-ul-Maal & Zakat-ul-Fitr (Distributed strictly to eligible beneficiaries)
   • Madrasah & Student Sponsorship Fund
   • Daily Iftar & Community Kitchen
3. Submission of Payment Proof:
   After transferring funds, you can upload your transaction reference or receipt via the app for official tax-deductible receipt issuance.

================================================================================
CHAPTER 11: ALNOOR ISLAMI HELPLINE, WHATSAPP & DUA REQUESTS
================================================================================
[SCREENSHOT: CHAPTER 11 - ALNOOR ISLAMI HELPLINE & DUA FORM]

Direct access to Alnoor Islami administration, resident scholars (Imams), and funeral support services.

Ways to Connect:
• One-Tap WhatsApp: Launches WhatsApp directly connected to the official Trust helpline number.
• Telephone Hotline: Initiates an immediate phone call to the Alnoor Islami reception desk.
• Submit Dua Request: Submit names of sick or deceased family members to be included in the congregational supplications after Jumu'ah prayer.
• Janaza (Funeral) Emergency: Dedicated 24/7 emergency contact details for immediate funeral arrangements, Ghusl, and burial coordination.

================================================================================
CHAPTER 12: USER ROLES & PRIVILEGES (MEMBER VS ADMIN)
================================================================================
[SCREENSHOT: CHAPTER 12 - ROLES & ACCESS PRIVILEGES]

The app features secure role-based access control:

Standard Member Privileges:
• Access all spiritual, informational, and multimedia resources.
• Reserve Khatam Sharif Paras and track personal recitation.
• Receive push notifications and download official documents.
• Submit Dua requests, volunteer registrations, and donation confirmations.

Administrator Privileges:
• Create, edit, and pin Community Announcements and urgent alerts.
• Upload new PDF books and visual guides to the Digital Library.
• Create photo albums and upload event photography.
• Manage Khatam Sharif campaigns and moderate community submissions.
• Customize Home Dashboard Action Cards.

To access Admin features, administrators log in using their verified credentials.

================================================================================
CHAPTER 13: APP SETTINGS, THEMES & OFFLINE CACHE
================================================================================
[SCREENSHOT: CHAPTER 13 - APP SETTINGS & VISUAL THEMES]

Customize your app experience in the App Settings screen:

1. Color Themes:
   • Emerald Green (Official default with Islamic gold accents)
   • Midnight Navy (Cool dark theme ideal for low-light environments)
   • Warm Sepia (Gentle cream tone inspired by classical manuscripts)
   • Pure Black AMOLED (True black for battery conservation on OLED screens)
2. Member User Guide:
   • Tap 'Read In-App Guide' to open this manual anytime offline.
   • Tap 'Share / Download PDF' to export this manual as a standard PDF file.
3. Cache Maintenance:
   • Tap 'Clear Cache' to free up temporary storage used by audio streams and image thumbnails without losing saved bookmarks or user data.

================================================================================
CHAPTER 14: FREQUENTLY ASKED QUESTIONS (FAQ)
================================================================================
[SCREENSHOT: CHAPTER 14 - FREQUENTLY ASKED QUESTIONS]

Q: The live stream is buffering or not playing audio?
A: Ensure your internet connection is stable. If video is stuttering on cellular data, switch to 'Audio-Only' mode in the player toolbar.

Q: Why are prayer times slightly different from my other app?
A: Alnoor App displays the exact Jama'ah (congregational) times practiced inside Alnoor Islami, using the University of Islamic Sciences Karachi calculation. Check if your other app is set to an alternate calculation method (such as ISNA or Umm al-Qura).

Q: Can I share announcements and books with friends on WhatsApp?
A: Yes. Every announcement, book, and photo has a dedicated 'Share' button that formats a neat WhatsApp message with download links and document attachments.

Q: How do I change my registered phone number or email?
A: Visit Alnoor Islami Administration or send a message via the Helpline tab with your existing account details.

May Allah Subhanahu wa Ta'ala accept all our worship, bless our families, and keep our community united in righteousness and faith. Ameen.

================================================================================
================================================================================
اردو ورژن: النور اسلامی کمیونٹی موبائل ایپلیکیشن
سرکاری ممبر گائیڈ و مکمل طریقہ کار (رہنمائے صارفین)
================================================================================
================================================================================

بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ
شروع اللہ کے نام سے جو بڑا مہربان نہایت رحم والا ہے۔
السلام علیکم ورحمۃ اللہ وبرکاتہ!

النور اسلامی ٹرسٹ کمیونٹی کی آفیشل موبائل ایپلی کیشن میں خوش آمدید۔ یہ جامع ہدایت نامہ آپ کو ایپ کے تمام فیچرز، سہولیات اور روحانی وسائل کے استعمال کا طریقہ کار سکھانے کے لیے ترتیب دیا گیا ہے۔ یہ فائل آپ کے فون میں بغیر انٹرنیٹ (آف لائن) بھی دستیاب رہے گی۔

--------------------------------------------------------------------------------
فہرست مضامین (اردو سیکشن)
--------------------------------------------------------------------------------
باب 1: تعارف اور ایپ کا عمومی جائزہ
باب 2: ہوم ڈیش بورڈ اور ایکشن کارڈز
باب 3: لائیو ویڈیو اور جمعہ بیانات کی نشریات
باب 4: نماز کے اوقات، جماعت کا شیڈول اور قبلہ رخ
باب 5: قرآن پاک، ترجمہ اور آڈیو تلاوت
باب 6: ختم شریف میں شرکت اور 30 پاروں کی بکنگ
باب 7: سرکاری اعلانات اور اہم نوٹسز
باب 8: اسلامی ڈیجیٹل لائبریری اور پی ڈی ایف ریڈر
باب 9: تصویری گیلری اور تقاریب کی تصاویر
باب 10: آن لائن عطیات، صدقہ جاریہ اور زکوٰۃ
باب 11: النور اسلامی ہیلپ لائن اور دعاؤں کی درخواست
باب 12: صارفین کے اختیارات (ممبر بمقابلہ ایڈمن)
باب 13: ایپ سیٹنگز، تھیمز اور کیشے صفائی
باب 14: عام پوچھے جانے والے سوالات (FAQ)

================================================================================
باب 1: تعارف اور ایپ کا عمومی جائزہ
================================================================================
[اسکرین شاٹ: باب 1 - ایپلی کیشن کا ڈھانچہ اور تعارف]

النور اسلامی ٹرسٹ کی یہ موبائل ایپلی کیشن اہل محلہ اور نمازیوں کو النور اسلامی سے روحانی طور پر وابستہ رکھنے، دینی تقاریب سے باخبر رکھنے اور عبادات میں اجتماعی شرکت کے لیے بنائی گئی ہے۔

نمایاں خصوصیات:
• سو فیصد آف لائن دستیابی: قرآن مجید، دعائیں، نماز کے اوقات اور لائبریری کی کتب بغیر انٹرنیٹ بھی پڑھی جا سکتی ہیں۔
• کلاؤڈ سنکرونائزیشن: انٹرنیٹ ملتے ہی اعلانات، لائیو نشریات اور ختم شریف کا ڈیٹا خودکار اپ ڈیٹ ہو جاتا ہے۔
• ان بلٹ پی ڈی ایف ریڈر: کتب و گائیڈ پڑھنے کے لیے کسی دوسرے بیرونی پی ڈی ایف سافٹ ویئر کی ضرورت نہیں۔
• مختلف رنگین تھیمز: ایمرلڈ سبز، نیوی بلیو، سیپیا اور ڈارک موڈ۔

================================================================================
باب 2: ہوم ڈیش بورڈ اور ایکشن کارڈز
================================================================================
[اسکرین شاٹ: باب 2 - ہوم اسکرین اور ایکشن بٹن]

ایپ کھولتے ہی ہوم اسکرین پر النور اسلامی کی تمام سرگرمیوں کا خلاصہ سامنے آ جاتا ہے:
1. النور اسلامی کا ہیڈر بینر: موجودہ ہجری تاریخ، شمسی تاریخ اور النور اسلامی کی حالت۔
2. اگلی نماز کا الٹی گنتی ٹائمر: اگلی فرض نماز اور جماعت کا وقت سیکنڈز میں دکھاتا ہے۔
3. لائیو نشریات کا بٹن: جب النور اسلامی سے بیان یا نماز لائیو ہو تو چمکتا ہوا سنہرا بینر ظاہر ہوتا ہے۔
4. فوری ایکشن کارڈز:
   • لائیو نشریات
   • نماز کے اوقات
   • قرآن مجید
   • ختم شریف
   • اعلانات و نوٹسز
   • اسلامی لائبریری
   • فوٹو گیلری
   • عطیات و صدقات
   • کمیونٹی ہیلپ لائن
5. اہم نوٹس بینر: رمضان، عیدین اور فوری اعلانات۔

================================================================================
باب 3: لائیو ویڈیو اور جمعہ بیانات کی نشریات
================================================================================
[اسکرین شاٹ: باب 3 - لائیو ویڈیو اسٹریمنگ اور آڈیو پلیئر]

گھر بیٹھے النور اسلامی کے محراب و منبر سے براہ راست فل ایچ ڈی ویڈیو اور آڈیو نشریات سنیں:
1. نیچے والی پٹی سے 'Live' دبائیں یا ہوم اسکرین پر لائیو بینر پر کلک کریں۔
2. ویڈیو پلیئر فوری لائیو نشریات سے جڑ جائے گا۔
3. کنٹرولز:
   • ویڈیو پر ٹیپ کر کے پلے / پاز کریں۔
   • فل اسکرین پر دیکھنے کے لیے پھیلانے والا آئیکن دبائیں۔
   • سفر کے دوران یا کم انٹرنیٹ پر 'Audio Only' موڈ آن کریں جس سے صرف آواز صاف سنائی دے گی اور انٹرنیٹ ڈیٹا بچے گا۔

================================================================================
باب 4: نماز کے اوقات، جماعت کا شیڈول اور قبلہ رخ
================================================================================
[اسکرین شاٹ: باب 4 - نماز ٹائم ٹیبل اور قبلہ کمپاس]

النور اسلامی اور گردونواح کے لیے تصدیق شدہ مستند نظام الاوقات:
• 5 فرض نمازیں: فجر، ظہر، عصر، مغرب اور عشاء۔
• اضافی اوقات: طلوع آفتاب، غروب، تہجد، اور سحر و افطار کے اوقات۔
• اذان و جماعت: ابتدائے وقت (اذان) اور النور اسلامی میں باجماعت ادائیگی کا وقت الگ الگ واضح ہے۔
• حساب کا معیار: جامعہ علوم اسلامیہ علامہ بنوری ٹاؤن کراچی کا حنفی فارمولا۔
• قبلہ رخ کمپاس: اندرونی سینسر کے ذریعے کعبہ شریف کی بالکل درست سمت دکھاتا ہے۔

================================================================================
باب 5: قرآن پاک، ترجمہ اور آڈیو تلاوت
================================================================================
[اسکرین شاٹ: باب 5 - قرآن مجید کا مصحف اور تلاوت]

روزانہ تلاوت کلام پاک اور فہم دین کے لیے مکمل ڈیجیٹل قرآن پاک:
• 114 سورتیں مکمل دستیاب (مکی و مدنی سورتیں)۔
• واضح اور خوبصورت عثمانی و انڈو پاک عربی خطاطی۔
• انگریزی و اردو ترجمہ ساتھ ساتھ۔
• معروف قراء کرام کی آواز میں آیت بہ آیت آڈیو تلاوت سننے کی سہولت۔
• آخری پڑھی گئی آیت پر بک مارک لگانے کی سہولت۔
• عربی اور ترجمے کا فونٹ چھوٹا یا بڑا کرنے کے بٹن۔

================================================================================
باب 6: ختم شریف میں شرکت اور 30 پاروں کی بکنگ
================================================================================
[اسکرین شاٹ: باب 6 - ختم شریف 30 پاروں کی بکنگ]

ایصال ثواب، بابرکت ایام اور دعاؤں کے لیے باہمی تلاوت قرآن:
1. ہوم اسکرین سے 'ختم شریف' سیکشن کھولیں۔
2. جاری مہم (مثلاً ماہانہ ٹرسٹ ختم شریف، رمضان ختم) کا انتخاب کریں۔
3. 'Join Recitation / Select Juz' پر کلک کریں۔
4. 30 پاروں کا گرڈ کھلے گا:
   • سبز / سنہرا پارہ: خالی ہے، آپ اپنے نام پر منتخب کر سکتے ہیں۔
   • سرمئی پارہ: کسی دوسرے بھائی یا بہن نے پہلے سے ریزرو کر لیا ہے۔
5. منتخب پارے کی تلاوت مکمل کرنے کے بعد 'Mark as Completed' پر کلک کریں۔
6. کل کمیونٹی کا پروگریس بار فوری اپ ڈیٹ ہو جائے گا۔

================================================================================
باب 7: سرکاری اعلانات اور اہم نوٹسز
================================================================================
[اسکرین شاٹ: باب 7 - سرکاری نوٹسز اور پی ڈی ایف ڈاؤن لوڈ]

النور اسلامی انتظامیہ اور شوریٰ کے مصدقہ اعلانات:
• فوری اور ہنگامی الرٹس (سرخ رنگ کے نمایاں بینر کے ساتھ)۔
• دینی اجتماعات، میلاد النبی، محافل، نماز جنازہ کے اعلانات۔
• رویت ہلال، عیدین کے اوقات اور رمضان المبارک کا ٹائم ٹیبل۔
• پی ڈی ایف فائل ڈاؤن لوڈ کرنے اور واٹس ایپ پر آگے شیئر کرنے کی سہولت۔

================================================================================
باب 8: اسلامی ڈیجیٹل لائبریری اور پی ڈی ایف ریڈر
================================================================================
[اسکرین شاٹ: باب 8 - ڈیجیٹل لائبریری اور پی ڈی ایف ریڈر]

دینی و اصلاحی کتب، ختم شریف کی کتابچے اور احکام و مسائل:
• مکمل آف لائن ریڈنگ: تمام کتب بغیر انٹرنیٹ بھی فون پر کھولی جا سکتی ہیں۔
• 2 مطالعہ کے طریقے:
  - اصل پی ڈی ایف صفحات (زوم اور پیج اسکرولنگ کے ساتھ)
  - ٹیکسٹ ریڈر موڈ (آنکھوں کے لیے آرام دہ، فونٹ ایڈجسٹمنٹ کے ساتھ)
• ریڈنگ تھیمز: نائٹ موڈ، سیپیا اور وائٹ موڈ۔
• واٹس ایپ یا دوستوں کو کتاب کی پی ڈی ایف فائل شیئر کرنے کی سہولت۔
• ایڈوب ایکروبیٹ یا گوگل پی ڈی ایف میں کھولنے کی سہولت۔

================================================================================
باب 9: تصویری گیلری اور تقاریب کی تصاویر
================================================================================
[اسکرین شاٹ: باب 9 - تصویری گیلری اور البمز]

النور اسلامی کی تعمیراتی پیش رفت، عیدین کے اجتماعات اور دینی کانفرنسز کی تصاویر:
• تقریب اور تاریخ کے حساب سے منظم البمز۔
• فل اسکرین ایچ ڈی میں دیکھنے اور محفوظ کرنے کی سہولت۔

================================================================================
باب 10: آن لائن عطیات، صدقہ جاریہ اور زکوٰۃ
================================================================================
[اسکرین شاٹ: باب 10 - بینک اکاونٹ اور عطیات کا نظام]

اللہ کے گھر کی تعمیر و ترقی اور مستحقین کی امداد شفاف طریقے سے:
1. سرکاری بینک اکاؤنٹ تفصیلات:
   • اکاؤنٹ کا نام: النور اسلامی ٹرسٹ
   • بینک: النور کمیونٹی بینک
   • اکاؤنٹ نمبر اور IBAN کے ساتھ ایک کلک میں کاپی کرنے کا بٹن۔
2. عطیات کی مدات:
   • النور اسلامی کا عمومی انتظام و بجلی پانی
   • صدقہ جاریہ (النور اسلامی کی توسیع و تعمیر)
   • زکوٰۃ مال و فطرانہ (مستحقین کے لیے مخصوص)
   • مدرسہ اور طلبہ کے تعلیمی اخراجات
3. رسید بھیجنے کی سہولت: رقم ٹرانسفر کے بعد رسید ایپ کے ذریعے جمع کرائی جا سکتی ہے۔

================================================================================
باب 11: النور اسلامی ہیلپ لائن اور دعاؤں کی درخواست
================================================================================
[اسکرین شاٹ: باب 11 - النور اسلامی ہیلپ لائن اور دعا فارم]

انتظامیہ اور علمائے کرام سے براہ راست رابطہ:
• ون ٹیپ واٹس ایپ: ایک کلک سے النور اسلامی کے سرکاری واٹس ایپ پر رابطہ کریں۔
• ٹیلی فون کال: براہ راست النور اسلامی کے استقبالیہ پر رابطہ کریں۔
• دعائے خیر کی درخواست: بیماروں کی شفایابی یا مرحومین کے ایصال ثواب کے لیے نام جمع کرائیں جن کے لیے جمعہ کے بعد دعا کی جاتی ہے۔
• جنازہ ایمرجنسی: تجہیز و تکفین، غسل اور تدفین کے لیے 24 گھنٹے دستیاب رابطہ۔

================================================================================
باب 12: صارفین کے اختیارات (ممبر بمقابلہ ایڈمن)
================================================================================
[اسکرین شاٹ: باب 12 - صارفین کے اختیارات اور رول]

عام ممبر کے اختیارات:
• تمام روحانی مواد، قرآن، بیانات اور لائبریری کتب کا مطالعہ۔
• ختم شریف میں پارے کی بکنگ۔
• نوٹسز دیکھنا اور دعائیہ درخواستیں بھیجنا۔

ایڈمنسٹریٹر کے اختیارات:
• سرکاری اعلانات اور ہنگامی الرٹس جاری کرنا۔
• لائبریری میں نئی پی ڈی ایف کتب اپ لوڈ کرنا۔
• فوٹو گیلری کا انتظام اور ختم شریف مہم چلانا۔

================================================================================
باب 13: ایپ سیٹنگز، تھیمز اور کیشے صفائی
================================================================================
[اسکرین شاٹ: باب 13 - ایپ سیٹنگز اور تھیمز]

1. خوبصورت تھیمز کا انتخاب:
   • ایمرلڈ سبز (اسلامی سنہری رنگ کے ساتھ)
   • مڈ نائٹ نیوی (رات کے وقت کے لیے پرسکون رنگ)
   • وارم سیپیا (کتابی ورق کی مانند ہلکا کریمی رنگ)
   • امولیڈ بلیک (بیٹری بچانے کے لیے مکمل سیاہ)
2. ممبر یوزر گائیڈ:
   • 'Read In-App Guide' دبا کر یہ کتابچہ کسی بھی وقت پڑھیں۔
   • 'Share PDF' کے ذریعے مکمل کتابچہ پی ڈی ایف میں برآمد کریں۔
3. کیشے صفائی (Clear Cache): آڈیو اور تصاویری عارضی فائلیں صاف کر کے فون کی میموری خالی کریں۔

================================================================================
باب 14: عام پوچھے جانے والے سوالات (FAQ)
================================================================================
[اسکرین شاٹ: باب 14 - عام پوچھے جانے والے سوالات]

سوال: لائیو نشریات اٹک رہی ہیں یا انٹرنیٹ کم ہے؟
جواب: پلیئر میں 'Audio Only' موڈ آن کر لیں، اس سے ویڈیو بند ہو کر صرف صاف آواز چلے گی اور ڈیٹا نہیں رکے گا۔

سوال: نماز کے اوقات دوسری ایپ سے مختلف کیوں ہیں؟
جواب: النور اسلامی کی ایپ النور اسلامی کے مروجہ باجماعت اوقات اور جامعہ بنوری ٹاؤن کے حساب کے مطابق چلتی ہے۔

سوال: کیا کتب اور قرآن پاک انٹرنیٹ کے بغیر پڑھے جا سکتے ہیں؟
جواب: جی ہاں! تمام سورتیں، دعائیں اور یہ یوزر گائیڈ فون میں محفوظ رہتی ہیں اور بغیر انٹرنیٹ کھل جاتی ہیں۔

اللہ تبارک و تعالی ہماری عبادات کو شرف قبولیت بخشے، ہمارے اہل خانہ پر رحمتیں نازل فرمائے اور امت مسلمہ کو متحد اور بیدار رکھے۔ آمین یا رب العالمین۔
================================================================================
""".trimIndent()

    val OFFICIAL_USER_GUIDE_BOOK = IslamicBook(
        id = "bk-official-user-guide",
        title = "Alnoor Islami Community App — Official Member User Guide & Manual",
        author = "Alnoor Islami Management Board",
        category = "Trust Publications & Manuals",
        pagesCount = 24,
        language = "English & Urdu (Complete Dual Version)",
        description = "Comprehensive dual-language (English & Urdu) user manual and complete operational guide for the Alnoor Islami Community mobile application. Includes screenshots and visual diagrams for each chapter, covering Live Broadcasts, Prayer Times, Quran Majeed, Khatam Sharif, Announcements, Digital Library, Donations, and Support.",
        contentPreview = FULL_MANUAL_TEXT,
        fileType = "PDF",
        fileUrl = "local://alnoor_member_user_guide.pdf",
        fileSize = "2.8 MB",
        hasAudioRecitation = false,
        isBookmarked = true
    )
}
