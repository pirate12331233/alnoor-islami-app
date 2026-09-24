package com.example.data.local

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

object HadithDatabaseManager {
    private const val TAG = "HadithDbManager"
    const val ASSET_GZ_NAME = "databases/sahihain_library.db.gz"

    fun ensureDatabaseExtracted(context: Context) {
        val dbFile = context.getDatabasePath(HadithDatabase.DB_NAME)
        if (dbFile.exists() && dbFile.length() > 1024 * 1024) {
            // Already extracted and valid
            return
        }

        try {
            dbFile.parentFile?.mkdirs()
            val assetNames = context.assets.list("databases") ?: emptyArray()
            if (!assetNames.contains("sahihain_library.db.gz")) {
                Log.i(TAG, "Bundled asset database not present yet; skipping pre-extraction.")
                return
            }

            Log.d(TAG, "Extracting bundled Sahihain database from APK assets to ${dbFile.absolutePath}...")
            context.assets.open(ASSET_GZ_NAME).use { assetIn ->
                GZIPInputStream(assetIn).use { gzipIn ->
                    FileOutputStream(dbFile).use { fileOut ->
                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        while (gzipIn.read(buffer).also { bytesRead = it } != -1) {
                            fileOut.write(buffer, 0, bytesRead)
                        }
                        fileOut.flush()
                    }
                }
            }
            Log.d(TAG, "Extraction complete! Database size: ${dbFile.length() / (1024 * 1024)} MB")
        } catch (e: Exception) {
            Log.e(TAG, "Failed extracting bundled Hadith database: ${e.message}", e)
        }
    }
}
