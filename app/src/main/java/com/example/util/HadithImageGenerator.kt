package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.R
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
            val bitmap = createHadithBitmap(context, hadith)
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
            val bitmap = createHadithBitmap(context, hadith)
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
                    "📖 *Daily Hadith - ${hadith.book} (${hadith.reference})*\n\n${hadith.arabicText}\n\n*Urdu:*\n${hadith.urduTranslation}\n\n*English:*\n${hadith.englishTranslation}\n\n_Alnoor International Trust_\nWhatsApp: +92-333-2434114 | Email: info@alnoorislami.pk\n_Shared via Alnoor Islami App_"
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

    private fun createHadithBitmap(context: Context, hadith: HadithData): Bitmap {
        val width = 1080
        val targetHeight = 1920 // Universal 9:16 Vertical Story / Status Standard (1080 x 1920)
        val horizontalPadding = 76
        val contentWidth = width - (horizontalPadding * 2)

        // Measure text at base 9:16 font scale to determine vertical fitting
        val baseArabicSize = 54f
        val baseUrduSize = 44f
        val baseEnglishSize = 36f
        val baseHeaderSize = 42f
        val baseSubHeaderSize = 32f
        val baseLabelSize = 30f
        val baseFooterOrgSize = 32f
        val baseFooterContactSize = 25f

        // Initial measurement pass to see if text requires scaling down for exceptionally long Hadiths
        val testArabicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = baseArabicSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val testUrduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = baseUrduSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val testEnglishPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = baseEnglishSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val testArabicLayout = createStaticLayout(hadith.arabicText, testArabicPaint, contentWidth, Layout.Alignment.ALIGN_CENTER, 8f, 1.35f)
        val testUrduLayout = createStaticLayout(hadith.urduTranslation, testUrduPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 6f, 1.3f)
        val testEnglishLayout = createStaticLayout(hadith.englishTranslation, testEnglishPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 6f, 1.25f)

        val fixedOverheadBase = 520f // Top header, subheader, labels, dividers, and footer
        val rawContentHeight = fixedOverheadBase + testArabicLayout.height + testUrduLayout.height + testEnglishLayout.height

        // Calculate dynamic scale factor: 1.0f for normal/short Hadiths, scaling down gracefully for long Hadiths
        val scale = if (rawContentHeight > targetHeight - 120f) {
            ((targetHeight - 120f) / rawContentHeight).coerceIn(0.72f, 1.0f)
        } else {
            1.0f
        }

        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800") // Gold
            textSize = baseHeaderSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0BEC5") // Silver-grey
            textSize = baseSubHeaderSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val arabicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF") // Crisp White
            textSize = baseArabicSize * scale
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val urduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4EDDA") // Soft islamic green-white
            textSize = baseUrduSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val englishPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8F9FA")
            textSize = baseEnglishSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800")
            textSize = baseLabelSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val footerOrgPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = baseFooterOrgSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val footerContactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2D39A")
            textSize = baseFooterContactSize * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Layouts with final scaled paints
        val arabicLayout = createStaticLayout(hadith.arabicText, arabicPaint, contentWidth, Layout.Alignment.ALIGN_CENTER, 8f * scale, 1.35f)
        val urduLayout = createStaticLayout(hadith.urduTranslation, urduPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 6f * scale, 1.3f)
        val englishLayout = createStaticLayout(hadith.englishTranslation, englishPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 6f * scale, 1.25f)

        // Calculate heights & distribute vertical space evenly across the 9:16 frame
        val headerAreaHeight = 170f * scale
        val labelAreaHeight = (42f * scale) * 2
        val footerAreaHeight = 150f * scale
        val totalTextHeight = arabicLayout.height + urduLayout.height + englishLayout.height
        val minRequiredHeight = headerAreaHeight + labelAreaHeight + footerAreaHeight + totalTextHeight + 200f

        // Ensure canvas is at least 1920 (9:16), or expand if an enormous Hadith exceeds even with scaling
        val totalHeight = maxOf(targetHeight, minRequiredHeight.toInt())

        // Calculate flexible vertical spacing between sections to achieve balanced vertical alignment
        val remainingVerticalSpace = (totalHeight - (headerAreaHeight + labelAreaHeight + footerAreaHeight + totalTextHeight)).coerceAtLeast(100f)
        val sectionGap = (remainingVerticalSpace / 6f).coerceIn(24f, 65f)
        val topMargin = ((remainingVerticalSpace - (sectionGap * 4f)) / 2f).coerceIn(40f, 90f)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Dark Islamic Emerald Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#062319") // Deep Emerald
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Decode Logo and Draw Light Watermark in Center of the 9:16 Background
        val logoRaw = getLogoBitmap(context)
        if (logoRaw != null) {
            val transparentLogo = createWatermarkBitmap(logoRaw)
            val watermarkSize = (width * 0.72f).toInt()
            val wmLeft = (width - watermarkSize) / 2f
            val wmTop = (totalHeight - watermarkSize) / 2f
            val destRect = RectF(wmLeft, wmTop, wmLeft + watermarkSize, wmTop + watermarkSize)

            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                alpha = 18 // ~7% opacity: very light and subtle, maintains high text legibility
            }
            canvas.drawBitmap(transparentLogo, null, destRect, watermarkPaint)
        }

        // 3. Draw Decorative Outer Gold Border (9:16 full-bleed rounded frame)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37") // Royal Gold border
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(28f, 28f, (width - 28).toFloat(), (totalHeight - 28).toFloat(), 36f, 36f, borderPaint)

        // 4. Inner subtle border
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(42f, 42f, (width - 42).toFloat(), (totalHeight - 42).toFloat(), 26f, 26f, innerBorderPaint)

        // 5. Draw Circular Alnoor Logo Medallion in Top-Right Corner
        if (logoRaw != null) {
            val cornerLogoSize = 114f
            val cornerRight = width - 58f
            val cornerTop = 54f
            val cornerLeft = cornerRight - cornerLogoSize
            val cornerBottom = cornerTop + cornerLogoSize
            val centerX = (cornerLeft + cornerRight) / 2f
            val centerY = (cornerTop + cornerBottom) / 2f
            val radius = cornerLogoSize / 2f

            // White circular medallion base for optimal contrast & clarity on dark emerald background
            val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, radius, discPaint)

            // Logo image drawn cleanly inside the circular medallion
            val logoInset = 5f
            val logoRect = RectF(cornerLeft + logoInset, cornerTop + logoInset, cornerRight - logoInset, cornerBottom - logoInset)
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
            }
            canvas.drawBitmap(logoRaw, null, logoRect, logoPaint)

            // Outer gold rim around the medallion
            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }
            canvas.drawCircle(centerX, centerY, radius, rimPaint)
        }

        var currentY = topMargin + 30f

        // Draw Top App Branding
        canvas.drawText("AL NOOR ISLAMI • DAILY HADITH", width / 2f, currentY + 32f, headerPaint)
        currentY += 66f * scale

        // Draw Book & Reference
        val refText = "${hadith.book} • Hadith #${hadith.hadithNumber} (${hadith.grade})"
        canvas.drawText(refText, width / 2f, currentY + 24f, subHeaderPaint)
        currentY += 56f * scale

        // Draw Gold Separator Line
        val linePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 3.5f
        }
        canvas.drawLine(width / 3.5f, currentY, width * 2.5f / 3.5f, currentY, linePaint)
        currentY += sectionGap

        // Draw Arabic Text
        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), currentY)
        arabicLayout.draw(canvas)
        canvas.restore()
        currentY += arabicLayout.height + sectionGap

        // Subtle Divider Line
        val subtleLinePaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            strokeWidth = 2.5f
        }
        canvas.drawLine(horizontalPadding.toFloat(), currentY, (width - horizontalPadding).toFloat(), currentY, subtleLinePaint)
        currentY += (sectionGap * 0.75f)

        // Urdu Translation Header
        canvas.drawText("اردو ترجمہ (Urdu Translation):", horizontalPadding.toFloat(), currentY, labelPaint)
        currentY += 40f * scale

        // Urdu Text
        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), currentY)
        urduLayout.draw(canvas)
        canvas.restore()
        currentY += urduLayout.height + sectionGap

        // Subtle Divider Line
        canvas.drawLine(horizontalPadding.toFloat(), currentY, (width - horizontalPadding).toFloat(), currentY, subtleLinePaint)
        currentY += (sectionGap * 0.75f)

        // English Translation Header
        canvas.drawText("English Translation:", horizontalPadding.toFloat(), currentY, labelPaint)
        currentY += 40f * scale

        // English Text
        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), currentY)
        englishLayout.draw(canvas)
        canvas.restore()
        currentY += englishLayout.height + sectionGap

        // Mini gold divider line before footer
        canvas.drawLine(width / 3.2f, currentY, width * 2.2f / 3.2f, currentY, linePaint)
        currentY += 42f * scale

        // Footer App Stamp: Organization Name
        canvas.drawText("Alnoor International Trust", width / 2f, currentY, footerOrgPaint)
        currentY += 40f * scale

        // Footer App Stamp: WhatsApp & Email
        canvas.drawText("WhatsApp: +92-333-2434114   •   Email: info@alnoorislami.pk", width / 2f, currentY, footerContactPaint)

        return bitmap
    }

    private fun getLogoBitmap(context: Context): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.app_logo_transparent)
                ?: BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
        } catch (e: Exception) {
            try {
                BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun createWatermarkBitmap(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val outBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)
        for (i in pixels.indices) {
            val color = pixels[i]
            val a = Color.alpha(color)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)
            // If pixel is pure white or near-white background, make transparent
            if (r > 240 && g > 240 && b > 240) {
                pixels[i] = 0
            } else if (a > 0) {
                pixels[i] = Color.argb(a, r, g, b)
            }
        }
        outBitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return outBitmap
    }

    private fun createStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        align: Layout.Alignment,
        lineSpacingAdd: Float = 6f,
        lineSpacingMult: Float = 1.25f
    ): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(align)
                .setLineSpacing(lineSpacingAdd, lineSpacingMult)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, align, lineSpacingMult, lineSpacingAdd, true)
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
