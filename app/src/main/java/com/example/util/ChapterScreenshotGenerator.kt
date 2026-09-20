package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * Generates crisp, lightweight, high-fidelity vector UI preview cards and screenshot diagrams
 * for each chapter of the Alnoor Islami Community App.
 * These can be embedded directly both in the in-app document viewer and rendered into PDF pages.
 */
object ChapterScreenshotGenerator {

    /**
     * Generates a 600x340 bitmap showing a mock visual preview / screenshot diagram
     * of the specific screen related to the chapter.
     */
    fun createChapterPreviewBitmap(chapterNumber: Int): Bitmap {
        val width = 640
        val height = 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Palette
        val darkBg = Color.rgb(7, 27, 20)
        val cardBg = Color.rgb(14, 39, 31)
        val emeraldHeader = Color.rgb(6, 68, 51)
        val emeraldLight = Color.rgb(19, 150, 113)
        val goldAccent = Color.rgb(245, 158, 11)
        val goldLight = Color.rgb(253, 230, 138)
        val textWhite = Color.rgb(241, 245, 242)
        val textMuted = Color.rgb(156, 163, 175)

        // Background canvas
        val bgPaint = Paint().apply { color = darkBg }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Outer device mockup frame
        val framePaint = Paint().apply {
            color = Color.rgb(24, 59, 48)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val outerRect = RectF(12f, 12f, (width - 12).toFloat(), (height - 12).toFloat())
        canvas.drawRoundRect(outerRect, 20f, 20f, framePaint)

        // Top Status / App Bar
        val appBarPaint = Paint().apply { color = emeraldHeader }
        canvas.drawRect(12f, 12f, (width - 12).toFloat(), 64f, appBarPaint)

        val titlePaint = Paint().apply {
            color = goldLight
            textSize = 17f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subtitlePaint = Paint().apply {
            color = textMuted
            textSize = 12f
            isAntiAlias = true
        }
        val accentPaint = Paint().apply {
            color = goldAccent
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val whitePaint = Paint().apply {
            color = textWhite
            textSize = 13f
            isAntiAlias = true
        }
        val cardPaint = Paint().apply {
            color = cardBg
            isAntiAlias = true
        }
        val cardBorderPaint = Paint().apply {
            color = Color.rgb(34, 78, 63)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }

        // Draw top bar text
        canvas.drawText("ALNOOR ISLAMI  •  MOBILE APP SCREENSHOT", 30f, 42f, titlePaint)
        canvas.drawText("SCREEN PREVIEW", (width - 160).toFloat(), 42f, accentPaint)

        when (chapterNumber) {
            1 -> {
                // Chapter 1: Introduction & App Overview
                canvas.drawText("Chapter 1: Application Architecture & Overview", 30f, 96f, accentPaint)
                canvas.drawText("100% Offline Database • Real-Time Cloud Sync • Multi-Theme", 30f, 118f, subtitlePaint)

                // 3 Pillar Cards
                val pillarWidth = (width - 80f) / 3f
                for (i in 0 until 3) {
                    val x = 30f + i * (pillarWidth + 10f)
                    val r = RectF(x, 140f, x + pillarWidth, 310f)
                    canvas.drawRoundRect(r, 14f, 14f, cardPaint)
                    canvas.drawRoundRect(r, 14f, 14f, cardBorderPaint)
                }

                canvas.drawText("Offline Ready", 45f, 175f, titlePaint)
                canvas.drawText("• Quran 114 Surahs", 45f, 210f, whitePaint)
                canvas.drawText("• Prayer Timetables", 45f, 235f, whitePaint)
                canvas.drawText("• Digital Library", 45f, 260f, whitePaint)
                canvas.drawText("• Khatam Archive", 45f, 285f, whitePaint)

                val x2 = 30f + (pillarWidth + 10f)
                canvas.drawText("Live Sync", x2 + 15f, 175f, titlePaint)
                canvas.drawText("• Live Broadcasts", x2 + 15f, 210f, whitePaint)
                canvas.drawText("• Breaking Notices", x2 + 15f, 235f, whitePaint)
                canvas.drawText("• Khatam Progress", x2 + 15f, 260f, whitePaint)
                canvas.drawText("• Dua Requests", x2 + 15f, 285f, whitePaint)

                val x3 = 30f + 2 * (pillarWidth + 10f)
                canvas.drawText("Polished UI", x3 + 15f, 175f, titlePaint)
                canvas.drawText("• Emerald / Gold", x3 + 15f, 210f, whitePaint)
                canvas.drawText("• Navy Blue Dark", x3 + 15f, 235f, whitePaint)
                canvas.drawText("• Warm Sepia Mode", x3 + 15f, 260f, whitePaint)
                canvas.drawText("• AMOLED Black", x3 + 15f, 285f, whitePaint)
            }
            2 -> {
                // Chapter 2: Home Dashboard & Quick Action Cards
                canvas.drawText("Chapter 2: Home Dashboard & Quick Action Cards", 30f, 96f, accentPaint)
                canvas.drawText("Top Countdown Timer • Live Stream Banner • 9 Quick Action Cards", 30f, 118f, subtitlePaint)

                // Top Mosque Header & Next Prayer Banner
                val headerRect = RectF(30f, 135f, (width - 30).toFloat(), 195f)
                canvas.drawRoundRect(headerRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(headerRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("ALNOOR ISLAMI  •  DHUHR IN 01:24:10", 45f, 162f, titlePaint)
                canvas.drawText("Next Jama'ah: 01:30 PM  |  Hijri: 24 Rabi al-Awwal 1448 AH", 45f, 182f, subtitlePaint)

                // Action cards 4 in a row
                val actionWidth = (width - 90f) / 4f
                val cardLabels = listOf("Live Stream", "Prayer Times", "Quran Majeed", "Khatam Sharif")
                for (i in 0 until 4) {
                    val x = 30f + i * (actionWidth + 10f)
                    val r = RectF(x, 210f, x + actionWidth, 310f)
                    canvas.drawRoundRect(r, 12f, 12f, cardPaint)
                    canvas.drawRoundRect(r, 12f, 12f, cardBorderPaint)

                    val dotPaint = Paint().apply { color = if (i == 0) Color.rgb(239, 68, 68) else goldAccent }
                    canvas.drawCircle(x + actionWidth / 2f, 245f, 14f, dotPaint)

                    val labelPaint = Paint().apply {
                        color = textWhite
                        textSize = 12f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    canvas.drawText(cardLabels[i], x + actionWidth / 2f, 285f, labelPaint)
                }
            }
            3 -> {
                // Chapter 3: Live Video Broadcasts & Sermon Streaming
                canvas.drawText("Chapter 3: Live Video Broadcast & Audio Player", 30f, 96f, accentPaint)
                canvas.drawText("HLS / YouTube Streaming • Picture-in-Picture • Audio-Only Mode", 30f, 118f, subtitlePaint)

                // Video viewport
                val videoRect = RectF(30f, 135f, 400f, 315f)
                val videoPaint = Paint().apply { color = Color.BLACK }
                canvas.drawRoundRect(videoRect, 12f, 12f, videoPaint)
                canvas.drawRoundRect(videoRect, 12f, 12f, cardBorderPaint)

                // Red LIVE indicator
                val liveBadge = Paint().apply { color = Color.rgb(220, 38, 38) }
                canvas.drawRoundRect(RectF(45f, 150f, 115f, 175f), 6f, 6f, liveBadge)
                val liveText = Paint().apply { color = Color.WHITE; textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
                canvas.drawText("● LIVE", 58f, 168f, liveText)

                // Play icon in center
                val playPaint = Paint().apply { color = goldAccent }
                canvas.drawCircle(215f, 225f, 26f, playPaint)

                // Side controls panel
                val sideRect = RectF(415f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(sideRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(sideRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("Controls & Audio", 430f, 168f, titlePaint)
                canvas.drawText("✓ Fullscreen Mode", 430f, 205f, whitePaint)
                canvas.drawText("✓ Audio-Only (Save Data)", 430f, 235f, whitePaint)
                canvas.drawText("✓ High Quality (1080p)", 430f, 265f, whitePaint)
                canvas.drawText("✓ Recent Archive Replay", 430f, 295f, whitePaint)
            }
            4 -> {
                // Chapter 4: Daily Prayer Times, Jama'ah Schedule & Qibla Direction
                canvas.drawText("Chapter 4: Daily Prayer Times & Qibla Compass", 30f, 96f, accentPaint)
                canvas.drawText("Adhan vs Jama'ah Timetable • Hanafi Asr Method • GPS Qibla", 30f, 118f, subtitlePaint)

                // Prayer table
                val tableRect = RectF(30f, 135f, 430f, 315f)
                canvas.drawRoundRect(tableRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(tableRect, 12f, 12f, cardBorderPaint)

                val prayers = listOf("Fajr: 05:10 AM (Jama'ah: 05:40 AM)", "Dhuhr: 01:15 PM (Jama'ah: 01:30 PM)", "Asr: 04:45 PM (Jama'ah: 05:00 PM)", "Maghrib: 06:42 PM (Jama'ah: 06:45 PM)", "Isha: 08:00 PM (Jama'ah: 08:20 PM)")
                for (i in prayers.indices) {
                    val y = 168f + i * 28f
                    canvas.drawText(prayers[i], 45f, y, if (i == 1) accentPaint else whitePaint)
                }

                // Qibla Compass box
                val qiblaRect = RectF(445f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(qiblaRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(qiblaRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("Qibla Compass", 460f, 168f, titlePaint)
                canvas.drawCircle(530f, 235f, 40f, cardBorderPaint)
                canvas.drawText("KAABA", 510f, 240f, accentPaint)
                canvas.drawText("Sensor Active", 460f, 298f, subtitlePaint)
            }
            5 -> {
                // Chapter 5: Holy Quran Majeed, Translations & Audio Recitations
                canvas.drawText("Chapter 5: Holy Quran Majeed & Audio Recitation", 30f, 96f, accentPaint)
                canvas.drawText("114 Surahs Complete • Clear Arabic Calligraphy • Audio Verse by Verse", 30f, 118f, subtitlePaint)

                val quranRect = RectF(30f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(quranRect, 14f, 14f, cardPaint)
                canvas.drawRoundRect(quranRect, 14f, 14f, cardBorderPaint)

                val arabicPaint = Paint().apply {
                    color = goldLight
                    textSize = 22f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", width / 2f, 185f, arabicPaint)
                canvas.drawText("الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ (١) الرَّحْمَٰنِ الرَّحِيمِ (٢)", width / 2f, 220f, arabicPaint)

                canvas.drawText("1. In the name of Allah, the Entirely Merciful, the Especially Merciful.", 50f, 260f, whitePaint)
                canvas.drawText("2. [All] praise is [due] to Allah, Lord of the worlds.  |  Reciter: Mishary Rashid", 50f, 285f, subtitlePaint)
            }
            6 -> {
                // Chapter 6: Khatam Sharif Registration & Progress Tracker
                canvas.drawText("Chapter 6: Community Khatam Sharif & Para Reservation", 30f, 96f, accentPaint)
                canvas.drawText("Interactive 30 Juz Grid • Instant Booking • Collective Completion Progress", 30f, 118f, subtitlePaint)

                // Progress Bar
                val barBg = RectF(30f, 135f, (width - 30).toFloat(), 165f)
                canvas.drawRoundRect(barBg, 8f, 8f, cardPaint)
                val barFill = RectF(30f, 135f, (width * 0.72f), 165f)
                val fillPaint = Paint().apply { color = goldAccent }
                canvas.drawRoundRect(barFill, 8f, 8f, fillPaint)
                canvas.drawText("Community Progress: 24 of 30 Juz Completed (80%)", 45f, 155f, titlePaint)

                // 30 Juz Grid Mockup (3 rows of 10 blocks)
                val blockW = (width - 70f) / 10f
                for (row in 0 until 3) {
                    for (col in 0 until 10) {
                        val num = row * 10 + col + 1
                        val x = 30f + col * (blockW + 1f)
                        val y = 180f + row * 40f
                        val r = RectF(x, y, x + blockW - 2f, y + 34f)
                        val blockPaint = Paint().apply {
                            color = when {
                                num <= 24 -> emeraldLight
                                num in 25..27 -> goldAccent
                                else -> Color.rgb(40, 50, 60)
                            }
                        }
                        canvas.drawRoundRect(r, 6f, 6f, blockPaint)
                        val numPaint = Paint().apply { color = Color.WHITE; textSize = 11f; textAlign = Paint.Align.CENTER; isFakeBoldText = true }
                        canvas.drawText("J$num", x + blockW / 2f, y + 21f, numPaint)
                    }
                }
            }
            7 -> {
                // Chapter 7: Official Announcements, Event Schedules & Bulletins
                canvas.drawText("Chapter 7: Official Announcements & Important Notices", 30f, 96f, accentPaint)
                canvas.drawText("Real-Time Bulletins • Urgent Alerts • Official Circular PDF Downloads", 30f, 118f, subtitlePaint)

                val noticeCard1 = RectF(30f, 135f, (width - 30).toFloat(), 215f)
                canvas.drawRoundRect(noticeCard1, 12f, 12f, cardPaint)
                canvas.drawRoundRect(noticeCard1, 12f, 12f, cardBorderPaint)
                val urgentBadge = Paint().apply { color = Color.rgb(220, 38, 38) }
                canvas.drawRoundRect(RectF(45f, 148f, 135f, 172f), 6f, 6f, urgentBadge)
                canvas.drawText("URGENT", 55f, 165f, whitePaint)
                canvas.drawText("Ramadan Moon Sighting Official Announcement", 150f, 166f, titlePaint)
                canvas.drawText("Taraweeh prayers begin tonight following Isha at 08:30 PM. Download full schedule PDF.", 45f, 195f, subtitlePaint)

                val noticeCard2 = RectF(30f, 228f, (width - 30).toFloat(), 308f)
                canvas.drawRoundRect(noticeCard2, 12f, 12f, cardPaint)
                canvas.drawRoundRect(noticeCard2, 12f, 12f, cardBorderPaint)
                canvas.drawText("Jumu'ah Congregational Prayer Timing Update", 45f, 258f, titlePaint)
                canvas.drawText("First Khutbah: 01:15 PM  |  Second Khutbah: 02:00 PM  |  Tap to share via WhatsApp", 45f, 285f, subtitlePaint)
            }
            8 -> {
                // Chapter 8: Islamic Digital Library & PDF Document Reader
                canvas.drawText("Chapter 8: Islamic Digital Library & PDF Document Reader", 30f, 96f, accentPaint)
                canvas.drawText("Built-in Native PDF Viewer • Text Mode • Zoom • Day/Sepia/Night Theme", 30f, 118f, subtitlePaint)

                val readerRect = RectF(30f, 135f, 380f, 315f)
                canvas.drawRoundRect(readerRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(readerRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("Khatam Sharif Booklet (PDF)", 45f, 165f, titlePaint)
                canvas.drawText("Page 1 of 18  •  Verified Publication", 45f, 185f, subtitlePaint)
                canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 45f, 220f, accentPaint)
                canvas.drawText("Detailed supplications, Darood Sharif,", 45f, 250f, whitePaint)
                canvas.drawText("and spiritual litanies of the Trust.", 45f, 275f, whitePaint)

                // Tool panel
                val toolRect = RectF(395f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(toolRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(toolRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("Reading Options", 410f, 165f, titlePaint)
                canvas.drawText("• View as Native PDF Pages", 410f, 198f, whitePaint)
                canvas.drawText("• Clean Text Reader Layout", 410f, 228f, whitePaint)
                canvas.drawText("• Sepia / Night / AMOLED", 410f, 258f, whitePaint)
                canvas.drawText("• Share PDF via WhatsApp", 410f, 288f, whitePaint)
            }
            9 -> {
                // Chapter 9: Photo Gallery & Community Event Archives
                canvas.drawText("Chapter 9: Photo Gallery & Event Archives", 30f, 96f, accentPaint)
                canvas.drawText("High-Res Photo Albums • Fullscreen Zoom • Save & Share Memories", 30f, 118f, subtitlePaint)

                val albW = (width - 80f) / 3f
                val albumTitles = listOf("Eid Mubarak 1447", "New Library Opening", "Youth Conference")
                for (i in 0 until 3) {
                    val x = 30f + i * (albW + 10f)
                    val r = RectF(x, 135f, x + albW, 315f)
                    canvas.drawRoundRect(r, 12f, 12f, cardPaint)
                    canvas.drawRoundRect(r, 12f, 12f, cardBorderPaint)

                    // Photo placeholder box
                    val photoBox = RectF(x + 10f, 145f, x + albW - 10f, 240f)
                    val phPaint = Paint().apply { color = emeraldHeader }
                    canvas.drawRoundRect(photoBox, 8f, 8f, phPaint)
                    canvas.drawText("PHOTO", x + albW / 2f - 24f, 198f, accentPaint)

                    canvas.drawText(albumTitles[i], x + 12f, 268f, titlePaint)
                    canvas.drawText("24 Photos", x + 12f, 292f, subtitlePaint)
                }
            }
            10 -> {
                // Chapter 10: Online Donations, Sadaqah Jariyah & Zakat
                canvas.drawText("Chapter 10: Online Donations, Sadaqah & Zakat", 30f, 96f, accentPaint)
                canvas.drawText("Verified Trust Accounts • One-Tap Copy IBAN • Tax Receipts", 30f, 118f, subtitlePaint)

                val donRect = RectF(30f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(donRect, 14f, 14f, cardPaint)
                canvas.drawRoundRect(donRect, 14f, 14f, cardBorderPaint)

                canvas.drawText("Alnoor Islami Trust — Official Bank Account", 50f, 170f, titlePaint)
                canvas.drawText("Bank: Community Islamic Bank  |  Sort Code: 20-40-60", 50f, 198f, whitePaint)
                canvas.drawText("IBAN / Account Number: GB29 ALNOOR 0001 2345 6789 00", 50f, 228f, accentPaint)
                canvas.drawText("Purpose Options: Zakat-ul-Maal  •  Sadaqah Jariyah  •  Alnoor Islami Fund  •  Madrasah", 50f, 258f, whitePaint)
                canvas.drawText("Receipt Issuance: Upload transfer reference receipt in-app for automated confirmation.", 50f, 288f, subtitlePaint)
            }
            11 -> {
                // Chapter 11: Alnoor Islami Helpline, WhatsApp Support & Dua Requests
                canvas.drawText("Chapter 11: Alnoor Islami Helpline & WhatsApp Support", 30f, 96f, accentPaint)
                canvas.drawText("One-Tap WhatsApp • Direct Phone Hotline • Dua Requests Submission", 30f, 118f, subtitlePaint)

                val helpWidth = (width - 70f) / 2f
                val h1 = RectF(30f, 135f, 30f + helpWidth, 315f)
                canvas.drawRoundRect(h1, 12f, 12f, cardPaint)
                canvas.drawRoundRect(h1, 12f, 12f, cardBorderPaint)
                canvas.drawText("Helpline Channels", 45f, 170f, titlePaint)
                canvas.drawText("• WhatsApp Support Desk", 45f, 208f, whitePaint)
                canvas.drawText("• Direct Imam Reception Line", 45f, 238f, whitePaint)
                canvas.drawText("• 24/7 Janaza Emergency Support", 45f, 268f, accentPaint)
                canvas.drawText("Response within minutes", 45f, 295f, subtitlePaint)

                val h2 = RectF(30f + helpWidth + 10f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(h2, 12f, 12f, cardPaint)
                canvas.drawRoundRect(h2, 12f, 12f, cardBorderPaint)
                canvas.drawText("Dua Request Form", 30f + helpWidth + 25f, 170f, titlePaint)
                canvas.drawText("Submit names for communal supplication", 30f + helpWidth + 25f, 208f, whitePaint)
                canvas.drawText("following Jumu'ah congregational prayer", 30f + helpWidth + 25f, 235f, whitePaint)
                canvas.drawText("Dedicated Khatam Sharif intentions", 30f + helpWidth + 25f, 265f, whitePaint)
                canvas.drawText("Alnoor Islami Imam Desk", 30f + helpWidth + 25f, 295f, subtitlePaint)
            }
            12 -> {
                // Chapter 12: Member Account, Profile & Role Privileges
                canvas.drawText("Chapter 12: User Roles & Access Privileges", 30f, 96f, accentPaint)
                canvas.drawText("Member Access vs Administrator Management Control", 30f, 118f, subtitlePaint)

                val rW = (width - 70f) / 2f
                val r1 = RectF(30f, 135f, 30f + rW, 315f)
                canvas.drawRoundRect(r1, 12f, 12f, cardPaint)
                canvas.drawRoundRect(r1, 12f, 12f, cardBorderPaint)
                canvas.drawText("Standard Member", 45f, 170f, titlePaint)
                canvas.drawText("✓ View Live Video Broadcasts", 45f, 205f, whitePaint)
                canvas.drawText("✓ Read Quran & Islamic Books", 45f, 232f, whitePaint)
                canvas.drawText("✓ Reserve Khatam Sharif Juz", 45f, 259f, whitePaint)
                canvas.drawText("✓ Submit Dua & Feedback", 45f, 286f, whitePaint)

                val r2 = RectF(30f + rW + 10f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(r2, 12f, 12f, cardPaint)
                canvas.drawRoundRect(r2, 12f, 12f, cardBorderPaint)
                canvas.drawText("Trust Administrator", 30f + rW + 25f, 170f, accentPaint)
                canvas.drawText("★ Post Official Announcements", 30f + rW + 25f, 205f, whitePaint)
                canvas.drawText("★ Upload PDF Books to Library", 30f + rW + 25f, 232f, whitePaint)
                canvas.drawText("★ Manage Photo Galleries", 30f + rW + 25f, 259f, whitePaint)
                canvas.drawText("★ Manage Dashboard Action Cards", 30f + rW + 25f, 286f, whitePaint)
            }
            13 -> {
                // Chapter 13: App Settings, Theme Customization & Offline Cache
                canvas.drawText("Chapter 13: App Settings & Visual Themes", 30f, 96f, accentPaint)
                canvas.drawText("Theme Selection • In-App Manual • PDF Export • Storage Maintenance", 30f, 118f, subtitlePaint)

                val tW = (width - 80f) / 4f
                val themes = listOf("Emerald Gold", "Midnight Navy", "Warm Sepia", "AMOLED Black")
                val themeColors = listOf(Color.rgb(6, 68, 51), Color.rgb(16, 31, 66), Color.rgb(251, 240, 217), Color.BLACK)

                for (i in 0 until 4) {
                    val x = 30f + i * (tW + 10f)
                    val r = RectF(x, 135f, x + tW, 255f)
                    val tp = Paint().apply { color = themeColors[i] }
                    canvas.drawRoundRect(r, 10f, 10f, tp)
                    canvas.drawRoundRect(r, 10f, 10f, cardBorderPaint)

                    val tlPaint = Paint().apply {
                        color = if (i == 2) Color.BLACK else Color.WHITE
                        textSize = 11f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    canvas.drawText(themes[i], x + tW / 2f, 240f, tlPaint)
                }

                val btmRect = RectF(30f, 268f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(btmRect, 8f, 8f, cardPaint)
                canvas.drawRoundRect(btmRect, 8f, 8f, cardBorderPaint)
                canvas.drawText("Storage Cache: 1-Tap 'Clear Cache' clears audio buffer without losing saved bookmarks.", 45f, 296f, subtitlePaint)
            }
            14 -> {
                // Chapter 14: Frequently Asked Questions & Troubleshooting
                canvas.drawText("Chapter 14: Community FAQs & Troubleshooting", 30f, 96f, accentPaint)
                canvas.drawText("Instant Answers to 7 Common Questions • Audio Fallback • Share Tips", 30f, 118f, subtitlePaint)

                val faqRect = RectF(30f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(faqRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(faqRect, 12f, 12f, cardBorderPaint)

                canvas.drawText("Q1: Live stream buffering on mobile data?", 45f, 168f, titlePaint)
                canvas.drawText("A: Switch player to 'Audio-Only' mode in toolbar for 90% data savings.", 45f, 190f, whitePaint)

                canvas.drawText("Q2: Why are prayer times slightly different?", 45f, 222f, titlePaint)
                canvas.drawText("A: Alnoor Islami uses Karachi University Hanafi calculation standard.", 45f, 244f, whitePaint)

                canvas.drawText("Q3: Can I read Holy Quran and books without Internet?", 45f, 276f, titlePaint)
                canvas.drawText("A: Yes! All 114 Surahs and library publications are stored 100% offline.", 45f, 298f, whitePaint)
            }
            else -> {
                // Default fallback
                canvas.drawText("Alnoor Islami Community App Preview", 30f, 96f, accentPaint)
                val fallbackRect = RectF(30f, 135f, (width - 30).toFloat(), 315f)
                canvas.drawRoundRect(fallbackRect, 12f, 12f, cardPaint)
                canvas.drawRoundRect(fallbackRect, 12f, 12f, cardBorderPaint)
                canvas.drawText("Official Document Publication & Operational Guide", 50f, 210f, titlePaint)
                canvas.drawText("Alnoor Islami Trust Management Board", 50f, 245f, subtitlePaint)
            }
        }

        return bitmap
    }
}
