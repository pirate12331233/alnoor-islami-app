package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HadithEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HadithDatabase : RoomDatabase() {
    abstract fun hadithDao(): HadithDao

    companion object {
        const val DB_NAME = "hadiths_sunni.db"

        @Volatile
        private var INSTANCE: HadithDatabase? = null

        fun resetInstance() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (_: Exception) {}
                INSTANCE = null
            }
        }

        fun getInstance(context: Context): HadithDatabase {
            val existing = INSTANCE
            if (existing != null && existing.isOpen && HadithDatabaseManager.isDatabaseValid(context)) {
                return existing
            }
            return synchronized(this) {
                if (INSTANCE != null && INSTANCE!!.isOpen && HadithDatabaseManager.isDatabaseValid(context)) {
                    return INSTANCE!!
                }

                // If DB file on disk is invalid or stub, close previous instance if any
                if (!HadithDatabaseManager.isDatabaseValid(context)) {
                    try {
                        INSTANCE?.close()
                    } catch (_: Exception) {}
                    INSTANCE = null
                }

                // Pre-extraction check from assets
                HadithDatabaseManager.ensureDatabaseExtracted(context)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HadithDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
