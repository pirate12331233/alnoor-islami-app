package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DaroodRecordEntity::class,
        DaroodSubmissionEntity::class,
        SavedBookmarkEntity::class,
        UserInquiryEntity::class,
        RegisteredUserEntity::class,
        IslamicBookEntity::class,
        PhotoAlbumEntity::class,
        GalleryPhotoEntity::class,
        GalleryAssetEntity::class,
        CommunityEventEntity::class,
        NoticeItemEntity::class,
        YouTubePlaylistEntity::class,
        ImportantNoticePopupEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daroodDao(): DaroodDao
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun inquiriesDao(): InquiriesDao
    abstract fun usersDao(): UsersDao
    abstract fun booksDao(): BooksDao
    abstract fun galleryDao(): GalleryDao
    abstract fun galleryAssetsDao(): GalleryAssetsDao
    abstract fun eventsDao(): EventsDao
    abstract fun noticesDao(): NoticesDao
    abstract fun playlistsDao(): PlaylistsDao
    abstract fun importantNoticeDao(): ImportantNoticeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alnoor_islamic_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
