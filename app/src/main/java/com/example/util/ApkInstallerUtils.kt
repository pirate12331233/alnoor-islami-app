package com.example.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
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
     * Converts shareable links (such as Google Drive or Dropbox preview links) into direct download links.
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

        // Dropbox: convert share link to direct download
        if (trimmed.contains("dropbox.com") && trimmed.contains("dl=0")) {
            return trimmed.replace("dl=0", "dl=1")
        }

        return trimmed
    }

    /**
     * Checks if the app currently has permission to install unknown apps on Android 8.0+ (Oreo).
     */
    fun canInstallUnknownApps(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Opens Android System Settings to allow installing unknown apps from Alnoor App.
     */
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Toast.makeText(context, "Please enable 'Install unknown apps' for Alnoor in Settings", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Resolves a FileProvider content URI safely by testing matching authorities.
     */
    fun getFileProviderUri(context: Context, file: File): Uri {
        val authoritiesToTry = listOf(
            "${context.packageName}.fileprovider",
            "com.aistudio.alnoorislamic.app.fileprovider",
            "com.aistudio.alnoorislamic.app.mrdbts.fileprovider"
        )
        for (auth in authoritiesToTry) {
            try {
                return FileProvider.getUriForFile(context, auth, file)
            } catch (_: Exception) {}
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Verifies that the given file exists, has a non-trivial size, and starts with ZIP magic bytes (PK).
     */
    fun isValidApkFile(file: File): Boolean {
        if (!file.exists() || file.length() < 50_000) return false
        return try {
            java.io.FileInputStream(file).use { fis ->
                val magic = ByteArray(2)
                val read = fis.read(magic)
                read == 2 && magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Scans for an already downloaded update APK on the device storage.
     * Helpful when users download via browser and it gets stuck at 100% or finishes in Downloads folder.
     */
    fun findExistingDownloadedApk(context: Context): File? {
        try {
            // 1. Check app's external downloads directory
            val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (appDownloadsDir != null && appDownloadsDir.exists()) {
                val files = appDownloadsDir.listFiles()?.filter {
                    it.name.endsWith(".apk", ignoreCase = true) && it.length() > 5_000_000 && isValidApkFile(it)
                }?.sortedByDescending { it.lastModified() }
                if (!files.isNullOrEmpty()) {
                    return files.first()
                }
            }

            // 2. Check public Downloads directory
            val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDownloads != null && publicDownloads.exists()) {
                val candidates = publicDownloads.listFiles()?.filter {
                    (it.name.contains("app-release", ignoreCase = true) ||
                     it.name.contains("alnoor", ignoreCase = true) ||
                     it.name.contains("update", ignoreCase = true)) &&
                    it.name.endsWith(".apk", ignoreCase = true) &&
                    it.length() > 5_000_000 &&
                    isValidApkFile(it)
                }?.sortedByDescending { it.lastModified() }
                if (!candidates.isNullOrEmpty()) {
                    return candidates.first()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not scan Downloads directory: ${e.message}")
        }
        return null
    }

    /**
     * Opens the device system Downloads screen so the user can easily tap and install the downloaded APK.
     */
    fun openSystemDownloadsFolder(context: Context) {
        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/vnd.android.package-archive"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Please open your phone's 'Downloads' app to tap the downloaded file", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Downloads the APK in background using Android's native DownloadManager.
     * Completely bypasses browser blocks, Brave Shields, and in-app thread limitations.
     */
    fun downloadViaSystemDownloadManager(
        context: Context,
        downloadUrl: String,
        title: String = "Alnoor Islamic Update"
    ): Long {
        return try {
            val resolvedUrl = resolveDirectDownloadUrl(downloadUrl)
            val uri = Uri.parse(resolvedUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle(title)
                setDescription("Downloading Alnoor Islamic App update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setMimeType("application/vnd.android.package-archive")
                try {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "alnoor_update.apk")
                } catch (e: Exception) {
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "alnoor_update.apk")
                }
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)
            Toast.makeText(context, "Update downloading in system notification bar. Swipe down to check!", Toast.LENGTH_LONG).show()
            downloadId
        } catch (e: Exception) {
            Log.e(TAG, "System DownloadManager failed: ${e.message}")
            openInBrowser(context, downloadUrl)
            -1L
        }
    }

    /**
     * Downloads the latest APK file directly with smooth, throttled progress callback,
     * then triggers native installation.
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
                    .addHeader("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")
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
                // Use standard external files Downloads dir so PackageInstaller has full read access
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val apkFile = File(downloadDir, "alnoor_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                withContext(Dispatchers.Main) {
                    onStatusMessage("Downloading APK update file...")
                }

                val inputStream = body.byteStream()
                val outputStream = FileOutputStream(apkFile)
                // 64 KB buffer for high streaming performance
                val buffer = ByteArray(64 * 1024)
                var bytesRead: Int
                var totalBytesRead: Long = 0
                var lastUiUpdateTime = 0L
                var lastUiProgress = 0f

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    val now = System.currentTimeMillis()
                    val progress = if (contentLength > 0) {
                        (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0.05f, 0.99f)
                    } else {
                        0.5f
                    }

                    // Throttle UI updates to once every 150ms or on 2% change to prevent thread lockup
                    if (now - lastUiUpdateTime > 150 || progress - lastUiProgress >= 0.02f) {
                        lastUiUpdateTime = now
                        lastUiProgress = progress
                        val mbDownloaded = totalBytesRead / (1024 * 1024f)
                        val msg = if (contentLength > 0) {
                            val mbTotal = contentLength / (1024 * 1024f)
                            val pct = (progress * 100).toInt()
                            String.format("Downloading: %.1f MB / %.1f MB (%d%%)", mbDownloaded, mbTotal, pct)
                        } else {
                            String.format("Downloading: %.1f MB downloaded...", mbDownloaded)
                        }
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                            onStatusMessage(msg)
                        }
                    }
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                // Allow OS and PackageInstaller to read the file
                apkFile.setReadable(true, false)

                // Verify file integrity: All valid APKs are ZIP files starting with magic bytes PK (0x50, 0x4B)
                val isValidApk = isValidApkFile(apkFile)

                if (!isValidApk) {
                    apkFile.delete()
                    withContext(Dispatchers.Main) {
                        onError("Downloaded file is incomplete or corrupted. Please try System Download Manager or Browser link.")
                        openInBrowser(context, downloadUrl)
                    }
                    return@withContext
                }

                withContext(Dispatchers.Main) {
                    onProgress(1.0f)
                    onStatusMessage("Download complete! Launching package installer...")
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
     * Prompts the Android OS package installer using FileProvider, with Unknown Sources permission handling.
     */
    fun installApk(context: Context, apkFile: File, onNeedPermission: (() -> Unit)? = null): Boolean {
        try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "APK file not found on device", Toast.LENGTH_SHORT).show()
                return false
            }

            if (!isValidApkFile(apkFile)) {
                Toast.makeText(context, "APK file is incomplete or corrupted", Toast.LENGTH_LONG).show()
                return false
            }

            // Check Unknown Sources permission on Android 8.0+
            if (!canInstallUnknownApps(context)) {
                Toast.makeText(
                    context,
                    "Please enable 'Allow from this source' for Alnoor App to install updates",
                    Toast.LENGTH_LONG
                ).show()
                if (onNeedPermission != null) {
                    onNeedPermission()
                } else {
                    openInstallPermissionSettings(context)
                }
                return false
            }

            val apkUri = getFileProviderUri(context, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            }

            // Explicitly grant URI permission to all potential installer activities
            val resolvedActivities = context.packageManager.queryIntentActivities(
                installIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resolvedActivities) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer: ${e.message}", e)
            Toast.makeText(context, "Could not open installer: ${e.localizedMessage}. Please install from Downloads.", Toast.LENGTH_LONG).show()
            return false
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
