package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DaroodDao {
    @Query("SELECT * FROM darood_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DaroodRecordEntity>>

    @Query("SELECT SUM(count) FROM darood_records")
    fun getTotalLifetimeCount(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DaroodRecordEntity)

    @Query("SELECT * FROM darood_submissions ORDER BY createdAt DESC")
    fun getAllSubmissions(): Flow<List<DaroodSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: DaroodSubmissionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubmissions(submissions: List<DaroodSubmissionEntity>)

    @Query("SELECT COUNT(*) FROM darood_submissions")
    suspend fun getSubmissionsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissionsReplacing(submissions: List<DaroodSubmissionEntity>)

    @Query("DELETE FROM darood_records")
    suspend fun clearAll()

    @Query("DELETE FROM darood_submissions")
    suspend fun clearAllSubmissions()

    @androidx.room.Transaction
    suspend fun syncSubmissionsWithCloud(submissions: List<DaroodSubmissionEntity>) {
        clearAllSubmissions()
        if (submissions.isNotEmpty()) {
            insertSubmissionsReplacing(submissions)
        }
    }
}

@Dao
interface BookmarksDao {
    @Query("SELECT * FROM saved_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<SavedBookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_bookmarks WHERE itemId = :id)")
    fun isBookmarked(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(bookmark: SavedBookmarkEntity)

    @Query("DELETE FROM saved_bookmarks WHERE itemId = :id")
    suspend fun removeBookmark(id: String)
}

@Dao
interface InquiriesDao {
    @Query("SELECT * FROM user_inquiries ORDER BY createdAt ASC")
    fun getAllInquiries(): Flow<List<UserInquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiry(inquiry: UserInquiryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInquiries(inquiries: List<UserInquiryEntity>)

    @Query("SELECT COUNT(*) FROM user_inquiries")
    suspend fun getInquiriesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInquiriesReplacing(inquiries: List<UserInquiryEntity>)

    @Query("DELETE FROM user_inquiries")
    suspend fun clearAll()

    @Query("UPDATE user_inquiries SET status = :status, reply = :reply, isRead = 1 WHERE id = :id")
    suspend fun updateReply(id: String, status: String, reply: String)

    @Query("UPDATE user_inquiries SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: String, isRead: Boolean)

    @Query("UPDATE user_inquiries SET isRead = :isRead WHERE threadId = :threadId OR senderContact = :contact")
    suspend fun updateThreadReadStatus(threadId: String, contact: String, isRead: Boolean)

    @Query("UPDATE user_inquiries SET internalNotes = :notes WHERE id = :id")
    suspend fun updateInternalNotes(id: String, notes: String)

    @Query("DELETE FROM user_inquiries WHERE id = :id")
    suspend fun deleteInquiry(id: String)

    @Query("DELETE FROM user_inquiries WHERE threadId = :threadId OR senderContact = :contact")
    suspend fun deleteThread(threadId: String, contact: String)

    @Query("SELECT * FROM user_inquiries")
    suspend fun getAllInquiriesSync(): List<UserInquiryEntity>

    @Query("DELETE FROM user_inquiries WHERE id IN (:ids)")
    suspend fun deleteInquiriesByIds(ids: List<String>)

    @androidx.room.Transaction
    suspend fun syncInquiriesWithCloud(inquiries: List<UserInquiryEntity>) {
        if (inquiries.isEmpty()) return
        val existingList = getAllInquiriesSync()
        val existingMap = existingList.associateBy { it.id }
        val readIds = com.example.AlnoorApp.instance?.let { com.example.util.ReadStatusTracker.getReadIds(it) } ?: emptySet()

        val mergedList = inquiries.map { incoming ->
            val existing = existingMap[incoming.id]
            val isLocallyRead = (existing?.isRead == true) || (incoming.id in readIds)
            val effectiveRead = incoming.isRead || isLocallyRead
            incoming.copy(
                isRead = effectiveRead,
                internalNotes = incoming.internalNotes ?: existing?.internalNotes
            )
        }

        val incomingIds = inquiries.map { it.id }.toSet()
        val idsToDelete = existingList.filter { it.id !in incomingIds && !it.id.startsWith("local_") }.map { it.id }
        if (idsToDelete.isNotEmpty()) {
            deleteInquiriesByIds(idsToDelete)
        }

        insertInquiriesReplacing(mergedList)
    }
}

@Dao
interface UsersDao {
    @Query("SELECT * FROM registered_users ORDER BY registeredAt DESC")
    fun getAllUsers(): Flow<List<RegisteredUserEntity>>

    @Query("SELECT * FROM registered_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): RegisteredUserEntity?

    @Query("SELECT * FROM registered_users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): RegisteredUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: RegisteredUserEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<RegisteredUserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsersReplacing(users: List<RegisteredUserEntity>)

    @Query("SELECT COUNT(*) FROM registered_users")
    suspend fun getUsersCount(): Int

    @Query("UPDATE registered_users SET password = :newPassword WHERE userId = :userId")
    suspend fun updatePassword(userId: String, newPassword: String)

    @Query("UPDATE registered_users SET role = :newRole WHERE userId = :userId")
    suspend fun updateRole(userId: String, newRole: String)

    @Query("DELETE FROM registered_users WHERE userId = :userId")
    suspend fun deleteUser(userId: String)

    @Query("DELETE FROM registered_users")
    suspend fun clearAll()

    @androidx.room.Transaction
    suspend fun syncUsersWithCloud(users: List<RegisteredUserEntity>) {
        clearAll()
        if (users.isNotEmpty()) {
            insertUsersReplacing(users)
        }
    }
}

@Dao
interface BooksDao {
    @Query("SELECT * FROM islamic_books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<IslamicBookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: IslamicBookEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBooks(books: List<IslamicBookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooksReplacing(books: List<IslamicBookEntity>)

    @Query("DELETE FROM islamic_books")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM islamic_books")
    suspend fun getBooksCount(): Int

    @Update
    suspend fun updateBook(book: IslamicBookEntity)

    @Query("UPDATE islamic_books SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)

    @Query("DELETE FROM islamic_books WHERE id = :id")
    suspend fun deleteBook(id: String)

    @androidx.room.Transaction
    suspend fun syncBooksWithCloud(books: List<IslamicBookEntity>) {
        clearAll()
        if (books.isNotEmpty()) {
            insertBooksReplacing(books)
        }
    }
}

@Dao
interface GalleryDao {
    @Query("SELECT * FROM photo_albums ORDER BY createdAt DESC")
    fun getAllAlbums(): Flow<List<PhotoAlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: PhotoAlbumEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbums(albums: List<PhotoAlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbumsReplacing(albums: List<PhotoAlbumEntity>)

    @Query("DELETE FROM photo_albums")
    suspend fun clearAllAlbums()

    @Query("SELECT COUNT(*) FROM photo_albums")
    suspend fun getAlbumsCount(): Int

    @Query("SELECT * FROM gallery_photos ORDER BY createdAt DESC")
    fun getAllPhotos(): Flow<List<GalleryPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: GalleryPhotoEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPhotos(photos: List<GalleryPhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotosReplacing(photos: List<GalleryPhotoEntity>)

    @Query("DELETE FROM gallery_photos")
    suspend fun clearAllPhotos()

    @Query("SELECT COUNT(*) FROM gallery_photos")
    suspend fun getPhotosCount(): Int

    @Query("DELETE FROM gallery_photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)

    @Query("DELETE FROM photo_albums WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: String)

    @Query("UPDATE photo_albums SET photoCount = (SELECT COUNT(*) FROM gallery_photos WHERE albumId = :albumId) WHERE id = :albumId")
    suspend fun recalculatePhotoCount(albumId: String)

    @androidx.room.Transaction
    suspend fun syncAlbumsWithCloud(albums: List<PhotoAlbumEntity>) {
        clearAllAlbums()
        if (albums.isNotEmpty()) {
            insertAlbumsReplacing(albums)
        }
    }

    @androidx.room.Transaction
    suspend fun syncPhotosWithCloud(photos: List<GalleryPhotoEntity>) {
        clearAllPhotos()
        if (photos.isNotEmpty()) {
            insertPhotosReplacing(photos)
        }
    }
}

@Dao
interface GalleryAssetsDao {
    @Query("SELECT * FROM gallery_assets ORDER BY createdAt DESC")
    fun getAllAssets(): Flow<List<GalleryAssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: GalleryAssetEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAssets(assets: List<GalleryAssetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetsReplacing(assets: List<GalleryAssetEntity>)

    @Query("DELETE FROM gallery_assets")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM gallery_assets")
    suspend fun getAssetsCount(): Int

    @Query("DELETE FROM gallery_assets WHERE id = :assetId")
    suspend fun deleteAsset(assetId: String)

    @androidx.room.Transaction
    suspend fun syncAssetsWithCloud(assets: List<GalleryAssetEntity>) {
        clearAll()
        if (assets.isNotEmpty()) {
            insertAssetsReplacing(assets)
        }
    }
}

@Dao
interface EventsDao {
    @Query("SELECT * FROM community_events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<CommunityEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CommunityEventEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<CommunityEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventsReplacing(events: List<CommunityEventEntity>)

    @Query("DELETE FROM community_events")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM community_events")
    suspend fun getEventsCount(): Int

    @Update
    suspend fun updateEvent(event: CommunityEventEntity)

    @Query("UPDATE community_events SET isUserRsvp = :isRsvp, rsvpCount = :rsvpCount WHERE id = :id")
    suspend fun updateRsvp(id: String, isRsvp: Boolean, rsvpCount: Int)

    @Query("UPDATE community_events SET isReminderSet = :isReminder, reminderMinutesBefore = :minutes WHERE id = :id")
    suspend fun updateReminder(id: String, isReminder: Boolean, minutes: Int)

    @Query("SELECT id FROM community_events")
    suspend fun getAllEventIds(): List<String>

    @Query("SELECT * FROM community_events")
    suspend fun getExistingEventsList(): List<CommunityEventEntity>

    @Query("DELETE FROM community_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    @androidx.room.Transaction
    suspend fun syncEventsWithCloud(events: List<CommunityEventEntity>) {
        clearAll()
        if (events.isNotEmpty()) {
            insertEventsReplacing(events)
        }
    }
}

@Dao
interface NoticesDao {
    @Query("SELECT * FROM notice_items ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotices(): Flow<List<NoticeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotices(notices: List<NoticeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoticesReplacing(notices: List<NoticeItemEntity>)

    @Query("DELETE FROM notice_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM notice_items")
    suspend fun getNoticesCount(): Int

    @Query("UPDATE notice_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("SELECT id FROM notice_items")
    suspend fun getAllNoticeIds(): List<String>

    @Query("SELECT * FROM notice_items")
    suspend fun getExistingNoticesList(): List<NoticeItemEntity>

    @Query("DELETE FROM notice_items WHERE id = :id")
    suspend fun deleteNotice(id: String)

    @androidx.room.Transaction
    suspend fun syncNoticesWithCloud(notices: List<NoticeItemEntity>) {
        clearAll()
        if (notices.isNotEmpty()) {
            insertNoticesReplacing(notices)
        }
    }
}

@Dao
interface PlaylistsDao {
    @Query("SELECT * FROM youtube_playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<YouTubePlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: YouTubePlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylists(playlists: List<YouTubePlaylistEntity>)

    @Query("SELECT COUNT(*) FROM youtube_playlists")
    suspend fun getPlaylistsCount(): Int

    @Query("DELETE FROM youtube_playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)
}

@Dao
interface ImportantNoticeDao {
    @Query("SELECT * FROM important_notice_popups ORDER BY updatedAt DESC LIMIT 1")
    fun getPopupFlow(): Flow<ImportantNoticePopupEntity?>

    @Query("SELECT * FROM important_notice_popups ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getPopupDirect(): ImportantNoticePopupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePopup(popup: ImportantNoticePopupEntity)

    @Query("SELECT COUNT(*) FROM important_notice_popups")
    suspend fun getCount(): Int

    @Query("DELETE FROM important_notice_popups")
    suspend fun clearPopup()
}
