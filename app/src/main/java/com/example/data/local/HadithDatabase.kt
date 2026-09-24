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

        fun getInstance(context: Context): HadithDatabase {
            return INSTANCE ?: synchronized(this) {
                // Pre-extraction check from assets if database file is not yet unpacked
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
