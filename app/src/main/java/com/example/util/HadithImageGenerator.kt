package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.model.HadithData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object HadithImageGenerator {

    /**
     * Renders a HadithData card into an image Bitmap and saves it to the device's Pictures/AlnoorIslami gallery.
     * Returns the Uri of the saved image.
     */
    suspend fun saveHadithCardAsImage(context: Context, hadith: HadithData): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createHadithBitmap(hadith)
            saveBitmapToGallery(context, bitmap, "Hadith_${hadith.book.replace(" ", "_")}_${hadith.hadithNumber}")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates and shares the Hadith image directly to WhatsApp/other apps.
     */
    suspend fun shareHadithAsImage(context: Context, hadith: HadithData): Boolean = withContext(Dispatchers.IO) {
        try {
            val bitmap = createHadithBitmap(hadith)
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "hadith_share_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Daily Hadith: ${hadith.book}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📖 *Daily Hadith - ${hadith.book} (${hadith.reference})*\n\n${hadith.arabicText}\n\n*Urdu:*\n${hadith.urduTranslation}\n\n*English:*\n${hadith.englishTranslation}\n\n_Shared via Alnoor Islami App_"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Hadith Image")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createHadithBitmap(hadith: HadithData): Bitmap {
        val width = 1080
        // Calculate estimated height dynamically based on text lengths
        val padding = 64
        val contentWidth = width - (padding * 2)

        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800") // Gold
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0BEC5") // Silver-grey
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val arabicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            textSize = 38f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val urduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4EDDA") // Soft islamic green-white
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val englishPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8F9FA")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val arabicLayout = createStaticLayout(hadith.arabicText, arabicPaint, contentWidth, Layout.Alignment.ALIGN_CENTER)
        val urduLayout = createStaticLayout(hadith.urduTranslation, urduPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL)
        val englishLayout = createStaticLayout(hadith.englishTranslation, englishPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL)

        val totalHeight = padding +
                80 + // App Header
                70 + // Book & Ref
                60 + // Divider
                arabicLayout.height + 50 +
                40 + // Section Label Urdu
                urduLayout.height + 50 +
                40 + // Section Label English
                englishLayout.height + 60 +
                70 + // Footer badge
                padding

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Dark Islamic Emerald Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#062319") // Deep Emerald
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // Draw Decorative Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37") // Gold border
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(24f, 24f, (width - 24).toFloat(), (totalHeight - 24).toFloat(), 32f, 32f, borderPaint)

        // Inner subtle border
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(36f, 36f, (width - 36).toFloat(), (totalHeight - 36).toFloat(), 24f, 24f, innerBorderPaint)

        var currentY = padding.toFloat() + 20f

        // Draw Top App Branding
        canvas.drawText("AL NOOR ISLAMI • DAILY HADITH", width / 2f, currentY + 30f, headerPaint)
        currentY += 60f

        // Draw Book & Reference
        val refText = "${hadith.book} • Hadith #${hadith.hadithNumber} (${hadith.grade})"
        canvas.drawText(refText, width / 2f, currentY + 24f, subHeaderPaint)
        currentY += 60f

        // Draw Gold Separator Line
        val linePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 3f
        }
        canvas.drawLine(width / 4f, currentY, (width * 3 / 4f), currentY, linePaint)
        currentY += 40f

        // Draw Arabic Text
        canvas.save()
        canvas.translate(padding.toFloat(), currentY)
        arabicLayout.draw(canvas)
        canvas.restore()
        currentY += arabicLayout.height + 40f

        // Divider
        val subtleLinePaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            strokeWidth = 2f
        }
        canvas.drawLine(padding.toFloat(), currentY, (width - padding).toFloat(), currentY, subtleLinePaint)
        currentY += 30f

        // Urdu Translation Header
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("اردو ترجمہ (Urdu Translation):", padding.toFloat(), currentY, labelPaint)
        currentY += 36f

        // Urdu Text
        canvas.save()
        canvas.translate(padding.toFloat(), currentY)
        urduLayout.draw(canvas)
        canvas.restore()
        currentY += urduLayout.height + 40f

        // Divider
        canvas.drawLine(padding.toFloat(), currentY, (width - padding).toFloat(), currentY, subtleLinePaint)
        currentY += 30f

        // English Translation Header
        canvas.drawText("English Translation:", padding.toFloat(), currentY, labelPaint)
        currentY += 36f

        // English Text
        canvas.save()
        canvas.translate(padding.toFloat(), currentY)
        englishLayout.draw(canvas)
        canvas.restore()
        currentY += englishLayout.height + 40f

        // Footer App Stamp
        canvas.drawText("Alnoor International Trust • www.alnoorislami.com", width / 2f, currentY + 30f, footerPaint)

        return bitmap
    }

    private fun createStaticLayout(text: String, paint: TextPaint, width: Int, align: Layout.Alignment): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(align)
                .setLineSpacing(6f, 1.25f)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, align, 1.25f, 6f, true)
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val fullFileName = "${fileName}_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AlnoorIslami")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/AlnoorIslami"
                val dir = File(imagesDir)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, fullFileName)
                outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                imageUri = Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            outputStream?.close()
        }
        return imageUri
    }
}
