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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ApkInstallerUtils {
    private const val TAG = "ApkInstallerUtils"

    // Generous timeouts to tolerate slow or congested international network routes (up to 3 minutes read timeout)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(180, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
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
     * Verifies that the given file exists, has a plausible update size (>= 10 MB),
     * starts with ZIP magic bytes (PK), and can be parsed by Android's package archive parser.
     */
    fun isValidApkFile(file: File, context: Context? = null): Boolean {
        if (!file.exists() || file.length() < 10_000_000L) return false // Update APK is ~19 MB, never < 10 MB
        val startsWithZip = try {
            java.io.FileInputStream(file).use { fis ->
                val magic = ByteArray(2)
                val read = fis.read(magic)
                read == 2 && magic[0] == 0x50.toByte() && magic[1] == 0x4B.toByte()
            }
        } catch (e: Exception) {
            false
        }
        if (!startsWithZip) return false

        // Comprehensive verification: Check that Android OS package parser can parse the archive
        if (context != null) {
            return try {
                val pm = context.packageManager
                val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageArchiveInfo(file.absolutePath, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageArchiveInfo(file.absolutePath, 0)
                }
                info != null && !info.packageName.isNullOrBlank()
            } catch (e: Exception) {
                false
            }
        }
        return true
    }

    /**
     * Scans for an already downloaded update APK on the device storage.
     * Cleans up any partial/incomplete files (such as interrupted downloads) so users are never misled.
     */
    fun findExistingDownloadedApk(context: Context): File? {
        try {
            // 1. Check app's external downloads directory
            val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (appDownloadsDir != null && appDownloadsDir.exists()) {
                val allApkFiles = appDownloadsDir.listFiles()?.filter {
                    it.name.endsWith(".apk", ignoreCase = true)
                } ?: emptyList()

                // Remove broken/partial downloads (e.g. 4.7 MB interrupted files)
                for (file in allApkFiles) {
                    if (!isValidApkFile(file, context)) {
                        Log.i(TAG, "Cleaning up partial/corrupt APK from app storage: ${file.name} (${file.length()} bytes)")
                        file.delete()
                    }
                }

                val files = appDownloadsDir.listFiles()?.filter {
                    it.name.endsWith(".apk", ignoreCase = true) && isValidApkFile(it, context)
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
                    isValidApkFile(it, context)
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
     * Downloads the latest APK file directly with resumable HTTP range support,
     * automatic retries for flaky connections, smooth progress callback,
     * and native installation.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        fallbackUrl: String = "",
        onProgress: (Float) -> Unit,
        onStatusMessage: (String) -> Unit,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val urlsToTry = mutableListOf<String>()
            if (downloadUrl.isNotBlank()) urlsToTry.add(downloadUrl)
            if (fallbackUrl.isNotBlank() && fallbackUrl != downloadUrl) urlsToTry.add(fallbackUrl)

            if (urlsToTry.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onError("Download URL is empty. Please contact the administrator.")
                }
                return@withContext
            }

            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val partFile = File(downloadDir, "alnoor_update.apk.part")
            val targetApkFile = File(downloadDir, "alnoor_update.apk")

            for ((urlIndex, currentRawUrl) in urlsToTry.withIndex()) {
                val resolvedUrl = resolveDirectDownloadUrl(currentRawUrl)
                val isFallback = urlIndex > 0
                val maxRetries = if (isFallback) 2 else 3

                if (isFallback) {
                    withContext(Dispatchers.Main) {
                        onStatusMessage("Primary download mirror congested. Switching to backup mirror...")
                    }
                    delay(1500)
                }

                var downloadSuccess = false

                for (attempt in 1..maxRetries) {
                    var outputStream: FileOutputStream? = null
                    var inputStream: java.io.InputStream? = null
                    var response: okhttp3.Response? = null

                    try {
                        val existingBytes = if (partFile.exists()) partFile.length() else 0L

                        withContext(Dispatchers.Main) {
                            val mirrorLabel = if (isFallback) " (Backup mirror)" else ""
                            if (existingBytes > 0) {
                                val mb = existingBytes / (1024 * 1024f)
                                onStatusMessage("Resuming download from ${String.format("%.1f MB", mb)}$mirrorLabel (Attempt $attempt/$maxRetries)...")
                            } else {
                                onStatusMessage("Connecting to cloud download server$mirrorLabel (Attempt $attempt/$maxRetries)...")
                                onProgress(0.05f)
                            }
                        }

                        val requestBuilder = Request.Builder()
                            .url(resolvedUrl)
                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                            .addHeader("Accept", "application/vnd.android.package-archive, application/octet-stream, */*")

                        if (existingBytes > 0) {
                            requestBuilder.addHeader("Range", "bytes=$existingBytes-")
                        }

                        response = httpClient.newCall(requestBuilder.build()).execute()

                        // If HTTP 416 (Range Not Satisfiable), previous partial file is invalid; reset and retry fresh
                        if (response.code == 416) {
                            response.close()
                            partFile.delete()
                            continue
                        }

                        if (!response.isSuccessful && response.code != 206) {
                            response.close()
                            if (attempt == maxRetries) {
                                break // Exit retry loop for this URL and try fallback if available
                            }
                            delay(2000)
                            continue
                        }

                        val contentType = response.header("Content-Type") ?: ""
                        if (contentType.contains("text/html", ignoreCase = true)) {
                            response.close()
                            if (urlIndex == urlsToTry.lastIndex) {
                                withContext(Dispatchers.Main) {
                                    onError("Server returned a web page instead of APK binary. Opening in web browser to download...")
                                    openInBrowser(context, currentRawUrl)
                                }
                                return@withContext
                            }
                            break
                        }

                        val body = response.body
                        if (body == null) {
                            response.close()
                            if (attempt == maxRetries) break
                            delay(2000)
                            continue
                        }

                        val isPartial = (response.code == 206)
                        val appendToFile = isPartial && existingBytes > 0

                        val totalContentLength: Long = if (isPartial) {
                            val contentRange = response.header("Content-Range")
                            val rangeTotal = contentRange?.substringAfterLast('/', "")?.toLongOrNull()
                            rangeTotal ?: (existingBytes + body.contentLength())
                        } else {
                            body.contentLength()
                        }

                        outputStream = FileOutputStream(partFile, appendToFile)
                        inputStream = body.byteStream()

                        val buffer = ByteArray(64 * 1024)
                        var bytesRead: Int
                        var currentTotalBytes = if (appendToFile) existingBytes else 0L
                        var lastUiUpdateTime = 0L
                        var lastUiProgress = 0f

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            currentTotalBytes += bytesRead

                            val now = System.currentTimeMillis()
                            val progress = if (totalContentLength > 0) {
                                (currentTotalBytes.toFloat() / totalContentLength.toFloat()).coerceIn(0.05f, 0.99f)
                            } else {
                                0.5f
                            }

                            if (now - lastUiUpdateTime > 150 || progress - lastUiProgress >= 0.02f) {
                                lastUiUpdateTime = now
                                lastUiProgress = progress
                                val mbDownloaded = currentTotalBytes / (1024 * 1024f)
                                val msg = if (totalContentLength > 0) {
                                    val mbTotal = totalContentLength / (1024 * 1024f)
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
                        outputStream = null
                        inputStream.close()
                        inputStream = null
                        response.close()
                        response = null

                        if (targetApkFile.exists()) {
                            targetApkFile.delete()
                        }
                        val renameSuccess = partFile.renameTo(targetApkFile)
                        val finalApk = if (renameSuccess) targetApkFile else partFile

                        finalApk.setReadable(true, false)

                        val isValid = isValidApkFile(finalApk, context)
                        if (!isValid) {
                            Log.w(TAG, "Completed file failed APK verification. File size: ${finalApk.length()}")
                            finalApk.delete()
                            if (attempt == maxRetries) break
                            delay(2000)
                            continue
                        }

                        downloadSuccess = true
                        withContext(Dispatchers.Main) {
                            onProgress(1.0f)
                            onStatusMessage("Download complete! Launching package installer...")
                            onSuccess()
                            installApk(context, finalApk)
                        }
                        return@withContext

                    } catch (e: Exception) {
                        Log.w(TAG, "Download attempt $attempt on $resolvedUrl encountered: ${e.message}", e)
                        try { outputStream?.flush(); outputStream?.close() } catch (_: Exception) {}
                        try { inputStream?.close() } catch (_: Exception) {}
                        try { response?.close() } catch (_: Exception) {}

                        val savedMb = if (partFile.exists()) partFile.length() / (1024 * 1024f) else 0f
                        if (attempt < maxRetries) {
                            withContext(Dispatchers.Main) {
                                onStatusMessage("Connection stalled (${String.format("%.1f MB", savedMb)} saved). Resuming update...")
                            }
                            delay(2500)
                        } else if (urlIndex == urlsToTry.lastIndex) {
                            withContext(Dispatchers.Main) {
                                val reason = if (e is java.net.SocketTimeoutException) {
                                    "Connection timed out due to slow network"
                                } else {
                                    e.localizedMessage ?: "Network error"
                                }
                                onError("Download issue: $reason. ${String.format("%.1f MB", savedMb)} saved. Tap 'Download & Install' to resume, or choose Notification Bar/Browser below.")
                            }
                        }
                    }
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

            if (!isValidApkFile(apkFile, context)) {
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
