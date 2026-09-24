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
     * Renders a Hadith card into a 9:16 vertical image Bitmap and saves it to the device Gallery.
     * Contains only Urdu and English translations with dynamic font sizing to properly utilize
     * the 9:16 vertical space without showing Arabic text.
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
     * Creates and shares the Hadith image (Urdu + English only) to WhatsApp/social media.
     * Accompanying text also includes only Urdu and English.
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

            val cleanUrdu = HadithMatnExtractor.extractMafhoomUrdu(hadith.urduTranslation)
            val cleanEnglish = HadithMatnExtractor.extractMatnEnglish(hadith.englishTranslation)

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Daily Hadith: ${hadith.book}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📖 *Daily Hadith - ${hadith.book} (${hadith.reference.ifBlank { "Hadith #${hadith.hadithNumber}" }})*\n\n" +
                            "*اردو ترجمہ (مفہوم):*\n$cleanUrdu\n\n" +
                            "*English Translation:*\n$cleanEnglish\n\n" +
                            "— *Grade:* ${hadith.grade}\n" +
                            "_Alnoor International Trust_\n" +
                            "WhatsApp: +92-333-2434114 | Email: info@alnoorislami.pk\n" +
                            "_Shared via Alnoor Islami App_"
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

    /**
     * Generates a 9:16 aspect ratio vertical image card (1080 x 1920) displaying only Urdu and English.
     * Features automatic font-size fitting and dynamic vertical spacing to fully utilize
     * available canvas space regardless of whether the Hadith is short, medium, or long.
     */
    private fun createHadithBitmap(context: Context, hadith: HadithData): Bitmap {
        val width = 1080
        val targetHeight = 1920 // Universal 9:16 Vertical Story standard (1080 x 1920)
        val horizontalPadding = 80
        val contentWidth = width - (horizontalPadding * 2)

        // Extract clean Mafhoom / core message for crisp reading
        val urduText = HadithMatnExtractor.extractMafhoomUrdu(hadith.urduTranslation)
        val englishText = HadithMatnExtractor.extractMatnEnglish(hadith.englishTranslation)

        // Baseline font sizes
        val baseHeaderSize = 42f
        val baseSubHeaderSize = 31f
        val baseLabelSize = 32f
        val baseFooterOrgSize = 32f
        val baseFooterContactSize = 25f

        // Intelligent Auto-fitting algorithm for Urdu and English to fill the 9:16 canvas:
        // We find the optimal scale factor `scale` such that total content height comfortably
        // fills between 1000px and 1380px of available space inside the 1920px frame.
        var bestScale = 1.0f
        var bestUrduSize = 48f
        var bestEnglishSize = 38f

        // Target content height inside the 9:16 canvas: ~1100f
        // Test scale range from 0.65f (very long hadith) up to 1.55f (short hadith)
        var lowScale = 0.65f
        var highScale = 1.55f
        val fixedOverhead = 480f // Top header + book title + labels + dividers + footer

        for (step in 0..7) {
            val midScale = (lowScale + highScale) / 2f
            val testUrduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 48f * midScale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val testEnglishPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 38f * midScale
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val testUrduLayout = createStaticLayout(urduText, testUrduPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 8f * midScale, 1.35f)
            val testEnglishLayout = createStaticLayout(englishText, testEnglishPaint, contentWidth, Layout.Alignment.ALIGN_NORMAL, 6f * midScale, 1.3f)
            val testTotal = fixedOverhead + testUrduLayout.height + testEnglishLayout.height + (160f * midScale)

            if (testTotal > targetHeight - 140f) {
                // Too large, scale down
                highScale = midScale
            } else if (testTotal < targetHeight - 480f) {
                // Too small, scale up to utilize empty space
                lowScale = midScale
            } else {
                bestScale = midScale
                break
            }
            bestScale = midScale
        }

        bestScale = bestScale.coerceIn(0.68f, 1.50f)
        bestUrduSize = (48f * bestScale).coerceIn(34f, 72f)
        bestEnglishSize = (38f * bestScale).coerceIn(26f, 54f)

        // Create styled paints with the calculated optimal sizes
        val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800") // Gold
            textSize = (baseHeaderSize * bestScale.coerceIn(0.85f, 1.25f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D1D5DB") // Light silver-grey
            textSize = (baseSubHeaderSize * bestScale.coerceIn(0.85f, 1.2f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }

        val urduPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8F5E9") // Soft luminous Islamic emerald-white
            textSize = bestUrduSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        val englishPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F3F4F6") // Crisp soft white
            textSize = bestEnglishSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val urduLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800") // Gold label
            textSize = (baseLabelSize * bestScale.coerceIn(0.9f, 1.25f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val englishLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6B800")
            textSize = (baseLabelSize * 0.95f * bestScale.coerceIn(0.9f, 1.25f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val footerOrgPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37")
            textSize = (baseFooterOrgSize * bestScale.coerceIn(0.85f, 1.2f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val footerContactPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2D39A")
            textSize = (baseFooterContactSize * bestScale.coerceIn(0.85f, 1.15f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Final text layouts
        val urduLayout = createStaticLayout(
            urduText,
            urduPaint,
            contentWidth,
            Layout.Alignment.ALIGN_NORMAL,
            8f * bestScale,
            1.35f
        )

        val englishLayout = createStaticLayout(
            englishText,
            englishPaint,
            contentWidth,
            Layout.Alignment.ALIGN_NORMAL,
            6f * bestScale,
            1.30f
        )

        // Compute vertical layout distribution
        val headerAreaHeight = 190f * bestScale.coerceIn(0.85f, 1.2f)
        val urduLabelHeight = 50f * bestScale.coerceIn(0.9f, 1.2f)
        val englishLabelHeight = 50f * bestScale.coerceIn(0.9f, 1.2f)
        val footerAreaHeight = 150f * bestScale.coerceIn(0.85f, 1.2f)
        val totalTextHeight = urduLayout.height + englishLayout.height
        val minRequiredHeight = headerAreaHeight + urduLabelHeight + englishLabelHeight + footerAreaHeight + totalTextHeight + 220f

        val totalHeight = maxOf(targetHeight, minRequiredHeight.toInt())

        // Calculate flexible vertical gaps to utilize empty space proportionally
        val remainingSpace = (totalHeight - (headerAreaHeight + urduLabelHeight + englishLabelHeight + footerAreaHeight + totalTextHeight)).coerceAtLeast(120f)
        val sectionGap = (remainingSpace / 5f).coerceIn(32f, 110f)
        val topMargin = ((remainingSpace - (sectionGap * 3.5f)) / 2f).coerceIn(50f, 140f)

        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Islamic Deep Emerald Gradient-style Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#062319") // Deep Emerald
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

        // 2. Decode Logo and Draw Light Watermark in Center of 9:16 Canvas
        val logoRaw = getLogoBitmap(context)
        if (logoRaw != null) {
            val transparentLogo = createWatermarkBitmap(logoRaw)
            val watermarkSize = (width * 0.76f).toInt()
            val wmLeft = (width - watermarkSize) / 2f
            val wmTop = (totalHeight - watermarkSize) / 2f
            val destRect = RectF(wmLeft, wmTop, wmLeft + watermarkSize, wmTop + watermarkSize)

            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
                alpha = 20 // ~8% opacity for subtle, elegant branding behind text
            }
            canvas.drawBitmap(transparentLogo, null, destRect, watermarkPaint)
        }

        // 3. Outer Decorative Gold Border (9:16 rounded frame)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D4AF37") // Royal Gold
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(28f, 28f, (width - 28).toFloat(), (totalHeight - 28).toFloat(), 36f, 36f, borderPaint)

        // 4. Inner Subtle Emerald Border
        val innerBorderPaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(42f, 42f, (width - 42).toFloat(), (totalHeight - 42).toFloat(), 26f, 26f, innerBorderPaint)

        // 5. Draw Circular Alnoor Logo Medallion in Top-Right Corner
        if (logoRaw != null) {
            val cornerLogoSize = 120f
            val cornerRight = width - 60f
            val cornerTop = 56f
            val cornerLeft = cornerRight - cornerLogoSize
            val cornerBottom = cornerTop + cornerLogoSize
            val centerX = (cornerLeft + cornerRight) / 2f
            val centerY = (cornerTop + cornerBottom) / 2f
            val radius = cornerLogoSize / 2f

            val discPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, radius, discPaint)

            val logoInset = 6f
            val logoRect = RectF(cornerLeft + logoInset, cornerTop + logoInset, cornerRight - logoInset, cornerBottom - logoInset)
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
            }
            canvas.drawBitmap(logoRaw, null, logoRect, logoPaint)

            val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                style = Paint.Style.STROKE
                strokeWidth = 3.5f
            }
            canvas.drawCircle(centerX, centerY, radius, rimPaint)
        }

        var currentY = topMargin + 30f

        // Header: App Title
        canvas.drawText("AL NOOR ISLAMI • DAILY HADITH", width / 2f, currentY + 34f, headerPaint)
        currentY += 72f * bestScale.coerceIn(0.9f, 1.2f)

        // Subheader: Book Name & Hadith Reference
        val refText = "${hadith.book} • Hadith #${hadith.hadithNumber} (${hadith.grade})"
        canvas.drawText(refText, width / 2f, currentY + 24f, subHeaderPaint)
        currentY += 60f * bestScale.coerceIn(0.9f, 1.2f)

        // Gold ornamental divider line
        val linePaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            strokeWidth = 3.5f
        }
        canvas.drawLine(width / 3.5f, currentY, width * 2.5f / 3.5f, currentY, linePaint)
        currentY += sectionGap

        // Urdu Translation Label
        canvas.drawText("اردو ترجمہ و مفہوم (Urdu Translation):", horizontalPadding.toFloat(), currentY, urduLabelPaint)
        currentY += 46f * bestScale.coerceIn(0.9f, 1.2f)

        // Urdu Translation Text Block
        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), currentY)
        urduLayout.draw(canvas)
        canvas.restore()
        currentY += urduLayout.height + sectionGap

        // Elegant Divider Line between Urdu and English
        val subtleLinePaint = Paint().apply {
            color = Color.parseColor("#1B4D3E")
            strokeWidth = 2.5f
        }
        canvas.drawLine(horizontalPadding.toFloat(), currentY, (width - horizontalPadding).toFloat(), currentY, subtleLinePaint)
        currentY += (sectionGap * 0.85f)

        // English Translation Label
        canvas.drawText("English Translation & Message:", horizontalPadding.toFloat(), currentY, englishLabelPaint)
        currentY += 44f * bestScale.coerceIn(0.9f, 1.2f)

        // English Translation Text Block
        canvas.save()
        canvas.translate(horizontalPadding.toFloat(), currentY)
        englishLayout.draw(canvas)
        canvas.restore()
        currentY += englishLayout.height + sectionGap

        // Mini gold divider line before footer
        canvas.drawLine(width / 3.2f, currentY, width * 2.2f / 3.2f, currentY, linePaint)
        currentY += 46f * bestScale.coerceIn(0.9f, 1.2f)

        // Footer: Organization Name
        canvas.drawText("Alnoor International Trust", width / 2f, currentY, footerOrgPaint)
        currentY += 42f * bestScale.coerceIn(0.9f, 1.2f)

        // Footer: WhatsApp & Email Contact
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
        lineSpacingMult: Float = 1.3f
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
