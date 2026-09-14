package com.example.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ApkInstallerUtils {
    private const val TAG = "ApkInstallerUtils"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Converts shareable links (such as Google Drive preview links) into direct download links.
     */
    fun resolveDirectDownloadUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        // Google Drive: https://drive.google.com/file/d/<FILE_ID>/view... -> direct download link
        if (trimmed.contains("drive.google.com")) {
            val fileIdPattern = Regex("/file/d/([a-zA-Z0-9_-]+)")
            val match = fileIdPattern.find(trimmed)
            if (match != null) {
                val fileId = match.groupValues[1]
                return "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
            }
            val idParamPattern = Regex("[?&]id=([a-zA-Z0-9_-]+)")
            val idMatch = idParamPattern.find(trimmed)
            if (idMatch != null) {
                val fileId = idMatch.groupValues[1]
                return "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
            }
        }
        return trimmed
    }

    /**
     * Downloads the latest APK file directly with progress callback, then triggers native installation.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit,
        onStatusMessage: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                if (downloadUrl.isBlank()) {
                    withContext(Dispatchers.Main) {
                        onError("Download URL is empty. Please contact the administrator.")
                    }
                    return@withContext
                }

                val resolvedUrl = resolveDirectDownloadUrl(downloadUrl)

                withContext(Dispatchers.Main) {
                    onStatusMessage("Connecting to cloud download server...")
                    onProgress(0.05f)
                }

                val request = Request.Builder()
                    .url(resolvedUrl)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onError("Server returned error code ${response.code}. Opening direct download browser...")
                        openInBrowser(context, downloadUrl)
                    }
                    return@withContext
                }

                val contentType = response.header("Content-Type") ?: ""
                if (contentType.contains("text/html", ignoreCase = true)) {
                    // Google Drive or file host showed an HTML warning/preview page instead of sending raw bytes
                    withContext(Dispatchers.Main) {
                        onError("Host returned a web page instead of APK binary. Opening in web browser to download...")
                        openInBrowser(context, downloadUrl)
                    }
                    return@withContext
                }

                val body = response.body
                if (body == null) {
                    withContext(Dispatchers.Main) {
                        onError("Empty response body from server. Opening download link...")
                        openInBrowser(context, downloadUrl)
                    }
                    return@withContext
                }

                val contentLength = body.contentLength()
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val apkFile = File(cacheDir, "alnoor_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                withContext(Dispatchers.Main) {
                    onStatusMessage("Downloading APK update file...")
                }

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var totalBytesRead: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        val progress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                            val mbDownloaded = totalBytesRead / (1024 * 1024f)
                            val mbTotal = contentLength / (1024 * 1024f)
                            onStatusMessage(String.format("Downloading: %.1f MB / %.1f MB (%.0f%%)", mbDownloaded, mbTotal, progress * 100))
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Verify file integrity: All APKs are ZIP files starting with magic bytes PK (0x50, 0x4B)
                val isValidApk = apkFile.length() > 50_000 && try {
                    java.io.FileInputStream(apkFile).use { fis ->
                        val magic = ByteArray(2)
                        val readCount = fis.read(magic)
                        readCount == 2 && magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte()
                    }
                } catch (e: Exception) {
                    false
                }

                if (!isValidApk) {
                    apkFile.delete()
                    withContext(Dispatchers.Main) {
                        onError("File received is not a valid APK package (web page or incomplete download). Opening browser...")
                        openInBrowser(context, downloadUrl)
                    }
                    return@withContext
                }

                withContext(Dispatchers.Main) {
                    onStatusMessage("Download complete! Launching package installer...")
                    onProgress(1.0f)
                    onSuccess()
                    installApk(context, apkFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading APK: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError("Download issue: ${e.localizedMessage ?: "Unknown error"}. Opening download browser link...")
                    openInBrowser(context, downloadUrl)
                }
            }
        }
    }

    /**
     * Prompts the Android OS package installer using FileProvider.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "APK file not found on device", Toast.LENGTH_SHORT).show()
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer: ${e.message}", e)
            Toast.makeText(context, "Please grant permission to install updates or use the browser link", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Fallback to open the APK download URL in device web browser / Chrome.
     */
    fun openInBrowser(context: Context, url: String) {
        try {
            val validUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                "https://$url"
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser for URL $url: ${e.message}")
            Toast.makeText(context, "Unable to open browser: $url", Toast.LENGTH_SHORT).show()
        }
    }
}
