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
            answer = "Tap the 'Donate' card on the Home dashboard. You will see official Trust verified bank account details (IBAN, Account Number, Sort Code) with one-tap copy buttons. You can also specify the intent (Zakat, Sadaqah, Mosque Expansion) and submit payment confirmation receipts.",
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
TABLE OF CONTENTS
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
11. Mosque Helpline, WhatsApp Support & Dua Requests
12. Member Account, Profile & Role Privileges (Admin vs Member)
13. App Settings, Theme Customization & Offline Cache
14. Frequently Asked Questions (FAQ) & Troubleshooting
15. Official Mosque Directory & Support Contacts

================================================================================
CHAPTER 1: INTRODUCTION & APP OVERVIEW
================================================================================
The Alnoor Trust Community mobile application is an integrated digital platform designed to keep community members spiritually connected to the Mosque, informed about religious events, and engaged in collective acts of worship.

Key Capabilities:
• 100% Offline-Ready: Access Quran, daily supplications, prayer timetables, and library books even without mobile data or Wi-Fi.
• Real-Time Cloud Synchronization: Announcements, live broadcasts, and Khatam Sharif progress automatically synchronize with cloud servers when connected.
• Built-in Document Reader: High-fidelity PDF engine renders Islamic books and guides directly in the app without requiring third-party PDF viewers.
• Multi-Theme Support: Switch seamlessly between Emerald Green, Midnight Navy, Warm Sepia, and High-Contrast modes for comfortable day and night reading.

================================================================================
CHAPTER 2: HOME DASHBOARD & QUICK ACTION CARDS
================================================================================
Upon opening the app, the Home Dashboard presents a centralized view of everything happening at Alnoor Mosque.

Elements on the Home Screen:
1. Mosque Header Banner: Displays today's Islamic Hijri date, Gregorian date, and current mosque operational status.
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
Never miss a congregational prayer or spiritual gathering. The Live Stream section broadcasts real-time high-definition video and audio directly from the Mosque Mihrab and Minbar.

How to Use the Live Player:
1. Tap 'Live' in the bottom navigation bar or tap the live banner on the Home Dashboard.
2. The player will automatically connect to the active stream (HLS / M3U8 or YouTube Live).
3. Playback Controls:
   • Play / Pause: Tap the center of the video screen.
   • Fullscreen Mode: Tap the expand icon in the bottom-right corner of the video.
   • Audio-Only Mode: When driving or conserving cellular data, tap 'Audio Stream' to switch off video while maintaining crystal-clear audio recitation.
   • Volume & Mute: Use your device volume buttons or the in-player volume slider.
4. Stream Status: If the mosque is currently off-air, the player displays the upcoming broadcast schedule and links to recorded archives.

================================================================================
CHAPTER 4: DAILY PRAYER TIMES, JAMA'AH & QIBLA DIRECTION
================================================================================
Accurate prayer timetables calculated specifically for the Mosque and surrounding regions.

Features:
• 5 Obligatory Prayers: Fajr, Dhuhr, Asr, Maghrib, and Isha.
• Additional Times: Sunrise (Shurooq), Sunset, Tahajjud, and Suhoor/Iftar timings during Ramadan.
• Adhan vs Jama'ah: Both the astronomical beginning time (Adhan) and the actual congregational prayer time at the Mosque (Iqamah/Jama'ah) are clearly listed.
• Calculation Standard: Follows the University of Islamic Sciences, Karachi (Hanafi Asr calculation).
• Qibla Compass: Built-in sensor-based compass pointing accurately toward the Holy Kaaba in Makkah al-Mukarramah. Ensure your device GPS/Location is enabled for precise directional alignment.

Setting Prayer Notifications:
Open App Settings -> Prayer Times & Notifications to enable or adjust Adhan reminders for each prayer.

================================================================================
CHAPTER 5: HOLY QURAN MAJEED, TRANSLATIONS & RECITATIONS
================================================================================
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
Stay informed with real-time official notices from the Mosque Shura and Management Board.

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
Browse high-resolution photographs of Mosque construction milestones, community Eid celebrations, youth programs, and annual conferences.

• Albums: Organized by event and date for intuitive browsing.
• Full-Screen Viewer: Tap any photo to view in high definition with pinch-to-zoom support.
• Save to Device: Tap 'Save Image' or 'Share' to store memorable moments on your phone gallery.

================================================================================
CHAPTER 10: ONLINE DONATIONS, SADAQAH JARIYAH & ZAKAT
================================================================================
Support the House of Allah with transparency and ease.

Donation Channels:
1. Official Bank Transfers:
   • Account Name: Alnoor Islamic Trust
   • Bank Name: Alnoor Community Bank
   • Account Number & IBAN: Provided with one-tap copy buttons.
   • Sort Code / Swift Code: Clear details for domestic and international transfers.
2. Donation Categories:
   • General Mosque Maintenance & Utilities
   • Sadaqah Jariyah (Mosque Expansion & Building Fund)
   • Zakat-ul-Maal & Zakat-ul-Fitr (Distributed strictly to eligible beneficiaries)
   • Madrasah & Student Sponsorship Fund
   • Daily Iftar & Community Kitchen
3. Submission of Payment Proof:
   After transferring funds, you can upload your transaction reference or receipt via the app for official tax-deductible receipt issuance.

================================================================================
CHAPTER 11: MOSQUE HELPLINE, WHATSAPP & DUA REQUESTS
================================================================================
Direct access to Mosque administration, resident scholars (Imams), and funeral support services.

Ways to Connect:
• One-Tap WhatsApp: Launches WhatsApp directly connected to the official Trust helpline number.
• Telephone Hotline: Initiates an immediate phone call to the mosque reception desk.
• Submit Dua Request: Submit names of sick or deceased family members to be included in the congregational supplications after Jumu'ah prayer.
• Janaza (Funeral) Emergency: Dedicated 24/7 emergency contact details for immediate funeral arrangements, Ghusl, and burial coordination.

================================================================================
CHAPTER 12: USER ROLES & PRIVILEGES (MEMBER VS ADMIN)
================================================================================
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
Q: The live stream is buffering or not playing audio?
A: Ensure your internet connection is stable. If video is stuttering on cellular data, switch to 'Audio-Only' mode in the player toolbar.

Q: Why are prayer times slightly different from my other app?
A: Alnoor App displays the exact Jama'ah (congregational) times practiced inside the Mosque, using the University of Islamic Sciences Karachi calculation. Check if your other app is set to an alternate calculation method (such as ISNA or Umm al-Qura).

Q: Can I share announcements and books with friends on WhatsApp?
A: Yes. Every announcement, book, and photo has a dedicated 'Share' button that formats a neat WhatsApp message with download links and document attachments.

Q: How do I change my registered phone number or email?
A: Visit Mosque Administration or send a message via the Helpline tab with your existing account details.

================================================================================
CHAPTER 15: OFFICIAL MOSQUE DIRECTORY & CONTACTS
================================================================================
Alnoor Islamic Trust & Community Center
• Main Office Address: 124 Noorani Way, Community Center Complex
• General Inquiries Telephone: +1 (800) 555-NOOR / +1 (800) 555-6667
• Official WhatsApp Helpline: +1 (800) 555-6667
• Administration Email: info@alnoor-trust.org
• Web Portal: https://www.alnoor-trust.org

May Allah Subhanahu wa Ta'ala accept all our worship, bless our families, and keep our community united in righteousness and faith. Ameen.
================================================================================
""".trimIndent()

    val OFFICIAL_USER_GUIDE_BOOK = IslamicBook(
        id = "bk-official-user-guide",
        title = "Alnoor Trust Community App — Official Member User Guide & Manual",
        author = "Alnoor Trust Management Board",
        category = "Trust Publications & Manuals",
        pagesCount = 18,
        language = "English / Urdu / Arabic",
        description = "Comprehensive user manual and complete operational guide for the Alnoor Trust Community mobile application. Includes step-by-step instructions for Live Broadcasts, Prayer Times, Quran Majeed, Khatam Sharif, Announcements, Digital Library, Donations, and Support.",
        contentPreview = FULL_MANUAL_TEXT,
        fileType = "PDF",
        fileUrl = "local://alnoor_member_user_guide.pdf",
        fileSize = "2.1 MB",
        hasAudioRecitation = false,
        isBookmarked = true
    )
}
