package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "darood_records")
data class DaroodRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val count: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "darood_submissions")
data class DaroodSubmissionEntity(
    @PrimaryKey val id: String,
    val userNameOrNumber: String,
    val count: Long,
    val timestamp: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_bookmarks")
data class SavedBookmarkEntity(
    @PrimaryKey val itemId: String,
    val itemType: String, // BOOK, MEDIA, EVENT
    val title: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_inquiries")
data class UserInquiryEntity(
    @PrimaryKey val id: String,
    val senderName: String,
    val senderContact: String,
    val category: String,
    val subject: String,
    val message: String,
    val timestamp: String,
    val status: String,
    val reply: String? = null,
    val isRead: Boolean = false,
    val internalNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "registered_users")
data class RegisteredUserEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val email: String,
    val whatsappNumber: String,
    val gender: String, // "Male" or "Female"
    val password: String,
    val role: String, // "STANDARD_USER" or "ADMIN"
    val registeredAt: String,
    val status: String = "Active"
)

@Entity(tableName = "islamic_books")
data class IslamicBookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val category: String,
    val pagesCount: Int,
    val language: String,
    val description: String,
    val contentPreview: String,
    val fileType: String,
    val fileUrl: String,
    val fileSize: String,
    val hasAudioRecitation: Boolean,
    val audioUrl: String,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "photo_albums")
data class PhotoAlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val photoCount: Int,
    val coverUrl: String,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_photos")
data class GalleryPhotoEntity(
    @PrimaryKey val id: String,
    val albumId: String,
    val title: String,
    val caption: String,
    val date: String,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gallery_assets")
data class GalleryAssetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val fileType: String,
    val fileExtension: String,
    val fileSize: String,
    val fileUrl: String,
    val thumbnailUrl: String,
    val uploadedDate: String,
    val date: String,
    val description: String,
    val type: String = "IMAGE",
    val uploadedBy: String = "Admin (Muhtamim)",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_events")
data class CommunityEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val dateGregorian: String,
    val dateHijri: String,
    val time: String,
    val venue: String,
    val address: String,
    val description: String,
    val category: String,
    val speaker: String,
    val isRsvpEnabled: Boolean = true,
    val rsvpCount: Int = 0,
    val isUserRsvp: Boolean = false,
    val isReminderSet: Boolean = false,
    val reminderMinutesBefore: Int = 15,
    val posterUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notice_items")
data class NoticeItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val priority: String, // URGENT, IMPORTANT, GENERAL
    val date: String,
    val isPinned: Boolean = false,
    val department: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "youtube_playlists")
data class YouTubePlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val playlistUrl: String,
    val playlistId: String,
    val videoCount: Int,
    val description: String,
    val dateAdded: String,
    val channelTitle: String,
    val channelHandle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "important_notice_popups")
data class ImportantNoticePopupEntity(
    @PrimaryKey val id: String = "important_popup",
    val title: String,
    val message: String,
    val issuingDepartment: String = "Alnoor Central Management",
    val imageUrl: String = "",
    val showOnLogin: Boolean = true,
    val isActive: Boolean = true,
    val description: String = "",
    val datePublished: String = "Today",
    val updatedAt: Long = System.currentTimeMillis()
)
