package com.example.data.model

enum class UserRole(val label: String, val badge: String) {
    STANDARD_USER("Community Member", "User"),
    ADMIN("Administrator (Muhtamim)", "Admin (Full CRUD)")
}

data class LiveStreamItem(
    val id: String,
    val title: String,
    val speaker: String,
    val isLiveNow: Boolean = false,
    val scheduledTime: String,
    val youtubeVideoId: String,
    val channelUrl: String = "https://www.youtube.com/@AlnoorislamiMushahidat/streams",
    val channelHandle: String = "@AlnoorislamiMushahidat",
    val description: String,
    val viewersCount: Int = 0,
    val topicCategory: String = "Weekly Khutbah"
)

data class CommunityEvent(
    val id: String,
    val title: String,
    val dateGregorian: String,
    val dateHijri: String,
    val time: String,
    val venue: String,
    val address: String,
    val description: String,
    val category: String, // Khatam Sharif, Juma, Milad, Conference, Iftar, Dars-e-Quran
    val speaker: String,
    val isRsvpEnabled: Boolean = true,
    val rsvpCount: Int = 0,
    val isUserRsvp: Boolean = false,
    val isReminderSet: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val posterUrl: String = ""
)

data class PhotoAlbum(
    val id: String,
    val title: String,
    val description: String,
    val photoCount: Int,
    val coverUrl: String,
    val date: String
)

data class GalleryPhoto(
    val id: String,
    val albumId: String,
    val title: String,
    val caption: String,
    val date: String,
    val imageUrl: String
)

enum class GalleryAssetType(val label: String) {
    IMAGE("Image / Photo"),
    PDF("PDF Document"),
    DOCUMENT("General Document")
}

data class GalleryAsset(
    val id: String,
    val title: String,
    val category: String = "Community Events",
    val fileType: String = "IMAGE", // "IMAGE", "PDF", "DOCUMENT"
    val fileExtension: String = "jpg",
    val fileSize: String = "2.4 MB",
    val fileUrl: String = "",
    val thumbnailUrl: String = "",
    val uploadedDate: String = "Today",
    val date: String = "Today",
    val description: String = "",
    val type: String = "IMAGE",
    val uploadedBy: String = "Admin (Muhtamim)"
)

data class IslamicBook(
    val id: String,
    val title: String,
    val author: String,
    val category: String, // Quran Tafseer, Hadith, Khatam Guide, Fiqh, Duas & Adhkar
    val pagesCount: Int,
    val language: String,
    val description: String,
    val contentPreview: String,
    val fileType: String = "PDF", // "PDF" or "IMAGE"
    val fileUrl: String = "",
    val fileSize: String = "4.2 MB",
    val hasAudioRecitation: Boolean = false,
    val audioUrl: String = "",
    val isBookmarked: Boolean = false
)

data class PrayerTime(
    val name: String,
    val arabicName: String,
    val adhanTime: String,
    val iqamahTime: String,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
)

data class PrayerTimesData(
    val dateGregorian: String,
    val dateHijri: String,
    val locationName: String,
    val countryName: String = "Auto-Detecting Location...",
    val fajr: String,
    val fajrIqamah: String,
    val sunrise: String,
    val dhuhr: String,
    val dhuhrIqamah: String,
    val asr: String,
    val asrIqamah: String,
    val maghrib: String,
    val maghribIqamah: String,
    val isha: String,
    val ishaIqamah: String,
    val tahajjud: String,
    val qiblaDirectionDeg: Float = 67.5f,
    val calculationMethod: String = "Islamic Society of North America (ISNA)",
    val isAutoDetected: Boolean = true
)

data class MediaArchiveItem(
    val id: String,
    val title: String,
    val type: String = "Mahafil Sharif",
    val speaker: String = "Hazrat Sahib",
    val reciter: String = "Hazrat Sahib",
    val duration: String = "45:00",
    val audioUrl: String = "https://www.youtube.com/@AlnoorislamiMushahidat",
    val audioStreamUrl: String = "",
    val date: String = "Recent",
    val description: String = "",
    val isFavorite: Boolean = false
)

data class YouTubePlaylist(
    val id: String,
    val title: String,
    val playlistUrl: String,
    val playlistId: String = "",
    val videoCount: Int = 0,
    val description: String = "",
    val dateAdded: String = "Recent",
    val channelTitle: String = "Alnoor Islamic Live",
    val channelHandle: String = "@AlnoorislamiMushahidat",
    val videos: List<MediaArchiveItem> = emptyList(),
    val items: List<MediaArchiveItem> = emptyList()
)

data class ImportantNoticePopup(
    val id: String = "primary_popup",
    val title: String,
    val message: String,
    val issuingDepartment: String = "Markazi Committee",
    val imageUrl: String = "",
    val showOnLogin: Boolean = true,
    val isActive: Boolean = true,
    val description: String = "",
    val datePublished: String = "Today"
)

data class DaroodSubmission(
    val id: String,
    val userNameOrNumber: String,
    val count: Long,
    val timestamp: String
)

data class DaroodState(
    val dailyCount: Long = 0,
    val monthlyCommunityTotal: Long = 1425890,
    val personalLifetimeCount: Long = 12500,
    val dailyStreak: Int = 14,
    val targetGoal: Int = 500,
    val isVibrationEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val submissions: List<DaroodSubmission> = emptyList(),
    val grandTotal: Long = 1425890
)

enum class NoticePriority {
    URGENT,
    IMPORTANT,
    GENERAL
}

data class NoticeItem(
    val id: String,
    val title: String,
    val content: String,
    val priority: NoticePriority,
    val date: String,
    val isPinned: Boolean = false,
    val department: String = "Alnoor Central Committee"
)

enum class MessageStatus {
    PENDING,
    RESOLVED
}

enum class MessageCategory(val title: String) {
    MASLA_FATWA("Islamic Ruling / Masla Inquiry"),
    DUA_REQUEST("Special Dua Request"),
    EVENT_INQUIRY("Event / Programme Query"),
    GENERAL("General Message to Admin")
}

data class AdminMessage(
    val id: String,
    val senderName: String,
    val senderContact: String,
    val category: MessageCategory,
    val subject: String,
    val message: String,
    val timestamp: String,
    val status: MessageStatus = MessageStatus.PENDING,
    val isRead: Boolean = false,
    val adminReply: String? = null,
    val internalNotes: String? = null
)

enum class UserGender(val label: String) {
    MALE("Male"),
    FEMALE("Female")
}

data class RegisteredUser(
    val userId: String,
    val fullName: String,
    val email: String,
    val whatsappNumber: String,
    val gender: UserGender,
    val password: String,
    val role: UserRole = UserRole.STANDARD_USER,
    val registeredAt: String = "Today",
    val status: String = "Active"
) {
    val id: String get() = userId
    val registeredAtFormatted: String get() = registeredAt
}

data class ActionCardConfig(
    val cardKey: String,
    val defaultTitle: String,
    val customTitle: String = "",
    val defaultSubtitle: String,
    val customSubtitle: String = "",
    val isVisibleToMembers: Boolean = true,
    val orderIndex: Int = 0
) {
    val displayTitle: String
        get() = if (customTitle.isNotBlank()) customTitle.trim() else defaultTitle

    val displaySubtitle: String
        get() = if (customSubtitle.isNotBlank()) customSubtitle.trim() else defaultSubtitle
}

data class AppVersionInfo(
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0.0",
    val minSupportedVersionCode: Int = 1,
    val isForcedUpdate: Boolean = false,
    val apkDownloadUrl: String = "https://github.com/AlnoorIslami/alnoor-islamic-app/releases/latest/download/app-release.apk",
    val releaseNotes: String = "• Important performance and cloud sync upgrades\n• Support for login notice posters and interactive popups\n• General stability improvements",
    val releaseDate: String = "August 2026",
    val apkSizeMb: String = "19.5 MB"
)


