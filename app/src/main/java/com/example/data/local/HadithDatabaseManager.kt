package com.example.data.local

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PushbackInputStream
import java.util.zip.GZIPInputStream

object HadithDatabaseManager {
    private const val TAG = "HadithDbManager"
    private const val MIN_VALID_DB_BYTES = 45L * 1024L * 1024L // ~45 MB minimum valid size

    private val ASSET_CANDIDATES = listOf(
        "databases/sahihain_library.db",
        "databases/sahihain_library.db.gz",
        "sahihain_library.db",
        "sahihain_library.db.gz"
    )

    private val lock = Any()

    fun isDatabaseValid(context: Context): Boolean {
        val dbFile = context.getDatabasePath(HadithDatabase.DB_NAME)
        return dbFile.exists() && dbFile.length() >= MIN_VALID_DB_BYTES
    }

    fun ensureDatabaseExtracted(context: Context) {
        synchronized(lock) {
            val dbFile = context.getDatabasePath(HadithDatabase.DB_NAME)
            if (isDatabaseValid(context)) {
                Log.d(TAG, "Bundled Hadith database is already present and valid (${dbFile.length() / (1024 * 1024)} MB).")
                return
            }

            Log.i(TAG, "Database not found or incomplete (current size: ${dbFile.length()} bytes). Starting extraction...")

            // Clean up any stale or empty DB and WAL files from previous runs
            try {
                if (dbFile.exists()) dbFile.delete()
                File(dbFile.path + "-wal").delete()
                File(dbFile.path + "-shm").delete()
                File(dbFile.path + "-journal").delete()
            } catch (e: Exception) {
                Log.w(TAG, "Error cleaning stale database files: ${e.message}")
            }

            dbFile.parentFile?.mkdirs()

            // Find valid asset
            var chosenAsset: String? = null
            for (candidate in ASSET_CANDIDATES) {
                try {
                    context.assets.open(candidate).use {
                        chosenAsset = candidate
                    }
                    if (chosenAsset != null) break
                } catch (_: Exception) {
                    // Not found, try next
                }
            }

            if (chosenAsset == null) {
                Log.e(TAG, "No bundled Hadith database asset found among candidates: $ASSET_CANDIDATES")
                return
            }

            Log.i(TAG, "Found bundled Hadith asset: $chosenAsset. Extracting to ${dbFile.absolutePath}...")

            val tempFile = File(dbFile.parentFile, "${HadithDatabase.DB_NAME}.tmp")
            try {
                if (tempFile.exists()) tempFile.delete()

                context.assets.open(chosenAsset!!).use { rawIn ->
                    val streamToRead = wrapIfGzip(rawIn)
                    streamToRead.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            val buffer = ByteArray(128 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    }
                }

                if (tempFile.length() >= MIN_VALID_DB_BYTES) {
                    if (tempFile.renameTo(dbFile)) {
                        Log.i(TAG, "Hadith database extraction successful! Size: ${dbFile.length() / (1024 * 1024)} MB")
                    } else {
                        // If renameTo fails (cross-volume), copy then delete
                        tempFile.copyTo(dbFile, overwrite = true)
                        tempFile.delete()
                        Log.i(TAG, "Hadith database copied successfully! Size: ${dbFile.length() / (1024 * 1024)} MB")
                    }
                } else {
                    Log.e(TAG, "Extracted database size ${tempFile.length()} is smaller than expected $MIN_VALID_DB_BYTES bytes")
                    tempFile.delete()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fatal error extracting Hadith database: ${e.message}", e)
                try {
                    if (tempFile.exists()) tempFile.delete()
                } catch (_: Exception) {}
            }
        }
    }

    private fun wrapIfGzip(input: InputStream): InputStream {
        val pushback = PushbackInputStream(input, 2)
        val header = ByteArray(2)
        val read = pushback.read(header)
        if (read == 2) {
            pushback.unread(header, 0, read)
            val isGzip = (header[0] == 0x1f.toByte()) && (header[1] == 0x8b.toByte())
            if (isGzip) {
                return GZIPInputStream(pushback)
            }
        } else if (read > 0) {
            pushback.unread(header, 0, read)
        }
        return pushback
    }
}
