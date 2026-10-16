package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object CanvasBitmapRenderer {

    suspend fun renderProjectToBitmap(
        context: Context,
        project: DesignProject,
        scale: Float = 1.0f,
        withDeviceFrame: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val baseWidth = project.width
        val baseHeight = project.height

        val framePaddingHorizontal = if (withDeviceFrame) 32f * scale else 0f
        val framePaddingTop = if (withDeviceFrame) 48f * scale else 0f
        val framePaddingBottom = if (withDeviceFrame) 48f * scale else 0f

        val totalWidth = (baseWidth * scale + framePaddingHorizontal * 2).toInt()
        val totalHeight = (baseHeight * scale + framePaddingTop + framePaddingBottom).toInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val canvasLeft = framePaddingHorizontal
        val canvasTop = framePaddingTop
        val canvasRight = canvasLeft + (baseWidth * scale)
        val canvasBottom = canvasTop + (baseHeight * scale)
        val canvasRect = RectF(canvasLeft, canvasTop, canvasRight, canvasBottom)

        // Draw Device Frame Exterior if enabled
        if (withDeviceFrame) {
            drawDeviceFrameExterior(canvas, totalWidth.toFloat(), totalHeight.toFloat(), canvasRect, scale)
        }

        // Clip to canvas area
        canvas.save()
        val clipPath = Path()
        val cornerRadius = if (withDeviceFrame) 36f * scale else 0f
        clipPath.addRoundRect(canvasRect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(clipPath)

        // Translate to Canvas origin
        canvas.translate(canvasLeft, canvasTop)
        canvas.scale(scale, scale)

        // Draw Canvas Background
        drawBackground(context, canvas, project.background, baseWidth.toFloat(), baseHeight.toFloat())

        // Sort elements by zIndex
        val sortedElements = project.elements
            .filter { it.isVisible }
            .sortedBy { it.zIndex }

        for (element in sortedElements) {
            drawElement(context, canvas, element)
        }

        canvas.restore()

        // Draw Device Frame Interior Details (Bezel, Notch / Punch Hole, Home Bar) if enabled
        if (withDeviceFrame) {
            drawDeviceFrameDetails(canvas, canvasRect, project.deviceFrame, scale)
        }

        bitmap
    }

    private fun drawBackground(
        context: Context,
        canvas: Canvas,
        background: CanvasBackground,
        width: Float,
        height: Float
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        when (background.type) {
            BackgroundType.SOLID -> {
                paint.color = background.colorStart.toInt()
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            BackgroundType.GRADIENT -> {
                val shader = LinearGradient(
                    0f, 0f, 0f, height,
                    background.colorStart.toInt(),
                    background.colorEnd.toInt(),
                    Shader.TileMode.CLAMP
                )
                paint.shader = shader
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            BackgroundType.TRANSPARENT -> {
                // Clear transparent
            }
            BackgroundType.IMAGE -> {
                background.imageUri?.let { uriStr ->
                    try {
                        val uri = Uri.parse(uriStr)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bgBitmap = BitmapFactory.decodeStream(stream)
                            if (bgBitmap != null) {
                                val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
                                val dstRect = RectF(0f, 0f, width, height)
                                canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
                            }
                        }
                    } catch (e: Exception) {
                        paint.color = 0xFFF1F5F9.toInt()
                        canvas.drawRect(0f, 0f, width, height, paint)
                    }
                } ?: run {
                    paint.color = 0xFFFFFFFF.toInt()
                    canvas.drawRect(0f, 0f, width, height, paint)
                }
            }
        }
    }

    private fun drawElement(context: Context, canvas: Canvas, element: CanvasElement) {
        canvas.save()
        canvas.translate(element.x, element.y)

        val alphaInt = (element.opacity * 255).toInt().coerceIn(0, 255)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            if (element.backgroundColorSecondary != null) {
                shader = LinearGradient(
                    0f, 0f, element.width, element.height,
                    element.backgroundColor.toInt(),
                    element.backgroundColorSecondary.toInt(),
                    Shader.TileMode.CLAMP
                )
            } else {
                color = element.backgroundColor.toInt()
            }
            alpha = (alpha * (element.opacity)).toInt().coerceIn(0, 255)
        }

        val borderPaint = if (element.borderWidth > 0) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = element.borderWidth
                color = element.borderColor.toInt()
                alpha = alphaInt
            }
        } else null

        val rect = RectF(0f, 0f, element.width, element.height)
        val radius = element.borderRadius

        when (element.type) {
            ElementType.SHAPE_CIRCLE -> {
                val cx = element.width / 2f
                val cy = element.height / 2f
                val r = minOf(cx, cy)
                canvas.drawCircle(cx, cy, r, bgPaint)
                borderPaint?.let { canvas.drawCircle(cx, cy, r, it) }
            }

            ElementType.BUTTON_PRIMARY, ElementType.BUTTON_OUTLINED, ElementType.BUTTON_TEXT,
            ElementType.BUTTON_ICON, ElementType.BUTTON_FAB, ElementType.SHAPE_CONTAINER,
            ElementType.SHAPE_RECTANGLE, ElementType.SHAPE_ROUNDED_RECT, ElementType.CONTENT_CARD,
            ElementType.CONTENT_CHIP -> {
                if (element.elevation > 0) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = element.shadowColor.toInt()
                        maskFilter = BlurMaskFilter(element.elevation, BlurMaskFilter.Blur.NORMAL)
                    }
                    val shadowRect = RectF(0f, element.elevation / 2, element.width, element.height + element.elevation)
                    canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint)
                }

                if (element.backgroundColor != 0L) {
                    canvas.drawRoundRect(rect, radius, radius, bgPaint)
                }
                borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }

                // Text inside button/card/chip
                if (element.text.isNotEmpty()) {
                    drawCenteredText(
                        canvas, element.text, element.width, element.height,
                        element.textColor.toInt(), element.fontSize, element.fontWeight
                    )
                }
            }

            ElementType.TEXT_HEADING, ElementType.TEXT_SUBTITLE, ElementType.TEXT_BODY, ElementType.TEXT_CAPTION -> {
                if (element.backgroundColor != 0L) {
                    canvas.drawRoundRect(rect, radius, radius, bgPaint)
                }
                drawMultilineText(
                    canvas, element.text, element.width, element.height,
                    element.textColor.toInt(), element.fontSize, element.fontWeight, element.textAlign
                )
            }

            ElementType.INPUT_TEXT, ElementType.INPUT_PASSWORD -> {
                if (element.variant == "Underline") {
                    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = element.borderColor.toInt()
                        strokeWidth = if (element.borderWidth > 0) element.borderWidth else 2f
                    }
                    canvas.drawLine(0f, element.height - 2f, element.width, element.height - 2f, linePaint)
                } else {
                    canvas.drawRoundRect(rect, radius, radius, bgPaint)
                    borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }
                }

                val displayText = if (element.text.isNotEmpty()) element.text else element.placeholder
                val textCol = if (element.text.isNotEmpty()) element.textColor.toInt() else 0xFF94A3B8.toInt()
                drawLeftAlignedText(canvas, displayText, element.width, element.height, textCol, 14f, "Normal", 16f)
            }

            ElementType.INPUT_SEARCH -> {
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }

                // Draw search icon circle & line
                val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF94A3B8.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                val iconX = 20f
                val iconY = element.height / 2f
                canvas.drawCircle(iconX, iconY - 2, 6f, iconPaint)
                canvas.drawLine(iconX + 4, iconY + 2, iconX + 8, iconY + 6, iconPaint)

                val displayText = if (element.text.isNotEmpty()) element.text else element.placeholder
                drawLeftAlignedText(canvas, displayText, element.width, element.height, 0xFF94A3B8.toInt(), 14f, "Normal", 36f)
            }

            ElementType.INPUT_CHECKBOX -> {
                val boxSize = 22f
                val boxRect = RectF(4f, (element.height - boxSize) / 2f, 4f + boxSize, (element.height + boxSize) / 2f)
                val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (element.isChecked) 0xFF4F46E5.toInt() else 0xFFCBD5E1.toInt()
                    style = if (element.isChecked) Paint.Style.FILL else Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(boxRect, 4f, 4f, boxPaint)

                if (element.isChecked) {
                    val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 2.5f
                        strokeCap = Paint.Cap.ROUND
                    }
                    val path = Path().apply {
                        moveTo(boxRect.left + 5f, boxRect.top + 11f)
                        lineTo(boxRect.left + 9f, boxRect.top + 15f)
                        lineTo(boxRect.left + 17f, boxRect.top + 7f)
                    }
                    canvas.drawPath(path, checkPaint)
                }

                drawLeftAlignedText(canvas, element.text, element.width, element.height, element.textColor.toInt(), 14f, "Normal", 34f)
            }

            ElementType.INPUT_RADIO -> {
                val r = 11f
                val cy = element.height / 2f
                val cx = 15f
                val radioPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (element.isChecked) 0xFF4F46E5.toInt() else 0xFFCBD5E1.toInt()
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawCircle(cx, cy, r, radioPaint)

                if (element.isChecked) {
                    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFF4F46E5.toInt()
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(cx, cy, 6f, innerPaint)
                }

                drawLeftAlignedText(canvas, element.text, element.width, element.height, element.textColor.toInt(), 14f, "Normal", 34f)
            }

            ElementType.INPUT_SWITCH -> {
                val trackWidth = 50f
                val trackHeight = 28f
                val trackRect = RectF(0f, (element.height - trackHeight) / 2f, trackWidth, (element.height + trackHeight) / 2f)
                val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (element.isChecked) 0xFF4F46E5.toInt() else 0xFFCBD5E1.toInt()
                }
                canvas.drawRoundRect(trackRect, trackHeight / 2f, trackHeight / 2f, trackPaint)

                val thumbR = 11f
                val thumbX = if (element.isChecked) trackWidth - thumbR - 3f else thumbR + 3f
                val thumbY = element.height / 2f
                val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                }
                canvas.drawCircle(thumbX, thumbY, thumbR, thumbPaint)
            }

            ElementType.INPUT_SLIDER -> {
                val trackHeight = 6f
                val trackY = (element.height - trackHeight) / 2f
                val bgTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE2E8F0.toInt() }
                canvas.drawRoundRect(RectF(0f, trackY, element.width, trackY + trackHeight), 3f, 3f, bgTrack)

                val activeWidth = element.width * element.progress.coerceIn(0f, 1f)
                val activeTrack = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF4F46E5.toInt() }
                canvas.drawRoundRect(RectF(0f, trackY, activeWidth, trackY + trackHeight), 3f, 3f, activeTrack)

                val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF4F46E5.toInt() }
                canvas.drawCircle(activeWidth, element.height / 2f, 10f, thumbPaint)
                val innerThumb = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
                canvas.drawCircle(activeWidth, element.height / 2f, 4f, innerThumb)
            }

            ElementType.CONTENT_IMAGE -> {
                if (element.imageUri != null) {
                    try {
                        val uri = Uri.parse(element.imageUri)
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            if (bmp != null) {
                                val clipP = Path()
                                clipP.addRoundRect(rect, radius, radius, Path.Direction.CW)
                                canvas.clipPath(clipP)

                                val srcR = Rect(0, 0, bmp.width, bmp.height)
                                canvas.drawBitmap(bmp, srcR, rect, bgPaint)
                            }
                        }
                    } catch (_: Exception) {
                        canvas.drawRoundRect(rect, radius, radius, bgPaint)
                    }
                } else {
                    canvas.drawRoundRect(rect, radius, radius, bgPaint)
                    borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }
                    drawCenteredText(canvas, "صورة", element.width, element.height, 0xFF64748B.toInt(), 14f, "Normal")
                }
            }

            ElementType.CONTENT_AVATAR -> {
                val cx = element.width / 2f
                val cy = element.height / 2f
                val r = minOf(cx, cy)
                canvas.drawCircle(cx, cy, r, bgPaint)
                borderPaint?.let { canvas.drawCircle(cx, cy, r, it) }

                if (element.text.isNotEmpty()) {
                    drawCenteredText(canvas, element.text, element.width, element.height, element.textColor.toInt(), element.fontSize, element.fontWeight)
                }
            }

            ElementType.CONTENT_DIVIDER -> {
                val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = element.backgroundColor.toInt()
                    strokeWidth = element.height
                }
                canvas.drawLine(0f, element.height / 2f, element.width, element.height / 2f, divPaint)
            }

            ElementType.CONTENT_BADGE -> {
                val cx = element.width / 2f
                val cy = element.height / 2f
                canvas.drawCircle(cx, cy, minOf(cx, cy), bgPaint)
                drawCenteredText(canvas, element.badgeCount, element.width, element.height, element.textColor.toInt(), 11f, "Bold")
            }

            ElementType.CONTENT_PROGRESS -> {
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                val fillWidth = element.width * element.progress.coerceIn(0f, 1f)
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = element.textColor.toInt() }
                canvas.drawRoundRect(RectF(0f, 0f, fillWidth, element.height), radius, radius, fillPaint)
            }

            ElementType.CONTENT_LOADING -> {
                val strokeW = 4f
                val r = minOf(element.width, element.height) / 2f - strokeW
                val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = strokeW
                    strokeCap = Paint.Cap.ROUND
                    color = element.textColor.toInt()
                }
                canvas.drawArc(
                    RectF(element.width / 2f - r, element.height / 2f - r, element.width / 2f + r, element.height / 2f + r),
                    -90f, 270f, false, arcPaint
                )
            }

            ElementType.NAV_TOP_BAR -> {
                canvas.drawRect(rect, bgPaint)
                val borderP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFE2E8F0.toInt()
                    strokeWidth = 1f
                }
                canvas.drawLine(0f, element.height - 1f, element.width, element.height - 1f, borderP)

                // Title
                drawCenteredText(canvas, element.text, element.width, element.height, element.textColor.toInt(), 18f, "Bold")

                // Back arrow
                val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = element.textColor.toInt()
                    strokeWidth = 2.5f
                    strokeCap = Paint.Cap.ROUND
                }
                val ax = 24f
                val ay = element.height / 2f
                canvas.drawLine(ax + 6, ay - 6, ax, ay, arrowPaint)
                canvas.drawLine(ax + 6, ay + 6, ax, ay, arrowPaint)
                canvas.drawLine(ax, ay, ax + 14, ay, arrowPaint)
            }

            ElementType.NAV_BOTTOM -> {
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }

                val items = if (element.items.isNotEmpty()) element.items else listOf("الرئيسية", "البحث", "المفضلة", "حسابي")
                val itemWidth = element.width / items.size

                for (i in items.indices) {
                    val itemX = i * itemWidth
                    val isSelected = (i == 0)
                    val itemColor = if (isSelected) 0xFF4F46E5.toInt() else 0xFF94A3B8.toInt()

                    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = itemColor
                    }
                    val iconY = element.height * 0.38f
                    canvas.drawCircle(itemX + itemWidth / 2f, iconY, 8f, dotPaint)

                    val textP = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = itemColor
                        textSize = 10f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(items[i], itemX + itemWidth / 2f, element.height * 0.78f, textP)
                }
            }

            ElementType.NAV_TAB_BAR -> {
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                val tabs = if (element.items.isNotEmpty()) element.items else listOf("التبويب 1", "التبويب 2")
                val tabW = element.width / tabs.size
                for (i in tabs.indices) {
                    val tx = i * tabW
                    val isSelected = (i == 0)
                    if (isSelected) {
                        val selP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = 0xFF4F46E5.toInt()
                            style = Paint.Style.STROKE
                            strokeWidth = 3f
                        }
                        canvas.drawLine(tx + 16f, element.height - 2f, tx + tabW - 16f, element.height - 2f, selP)
                    }
                    val tColor = if (isSelected) 0xFF4F46E5.toInt() else 0xFF64748B.toInt()
                    val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = tColor
                        textSize = 13f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(tabs[i], tx + tabW / 2f, element.height / 2f + 5f, tp)
                }
            }

            else -> {
                canvas.drawRoundRect(rect, radius, radius, bgPaint)
                borderPaint?.let { canvas.drawRoundRect(rect, radius, radius, it) }
            }
        }

        canvas.restore()
    }

    private fun drawCenteredText(
        canvas: Canvas, text: String, width: Float, height: Float,
        textColor: Int, fontSize: Float, fontWeight: String
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = fontSize
            textAlign = Paint.Align.CENTER
            typeface = if (fontWeight == "Bold") Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val yPos = (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(text, width / 2f, yPos, paint)
    }

    private fun drawLeftAlignedText(
        canvas: Canvas, text: String, width: Float, height: Float,
        textColor: Int, fontSize: Float, fontWeight: String, leftPadding: Float
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = fontSize
            textAlign = Paint.Align.LEFT
            typeface = if (fontWeight == "Bold") Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val yPos = (height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
        canvas.drawText(text, leftPadding, yPos, paint)
    }

    private fun drawMultilineText(
        canvas: Canvas, text: String, width: Float, height: Float,
        textColor: Int, fontSize: Float, fontWeight: String, textAlign: String
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = fontSize
            typeface = if (fontWeight == "Bold") Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val alignment = when (textAlign) {
            "Left" -> Layout.Alignment.ALIGN_NORMAL
            "Right" -> Layout.Alignment.ALIGN_OPPOSITE
            else -> Layout.Alignment.ALIGN_CENTER
        }
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(alignment)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .build()

        val yOffset = ((height - staticLayout.height) / 2f).coerceAtLeast(0f)
        canvas.save()
        canvas.translate(0f, yOffset)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawDeviceFrameExterior(
        canvas: Canvas, totalWidth: Float, totalHeight: Float,
        canvasRect: RectF, scale: Float
    ) {
        // Outer dark chassis
        val chassisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
        }
        val frameRect = RectF(0f, 0f, totalWidth, totalHeight)
        canvas.drawRoundRect(frameRect, 48f * scale, 48f * scale, chassisPaint)

        // Subtle silver rim highlight
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f * scale
            color = 0xFF475569.toInt()
        }
        canvas.drawRoundRect(frameRect, 48f * scale, 48f * scale, rimPaint)
    }

    private fun drawDeviceFrameDetails(
        canvas: Canvas, canvasRect: RectF, style: DeviceFrameStyle, scale: Float
    ) {
        when (style) {
            DeviceFrameStyle.ANDROID_MODERN -> {
                // Punch hole camera in top center
                val cameraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0F172A.toInt()
                }
                val lensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF1E293B.toInt()
                }
                val cx = canvasRect.centerX()
                val cy = canvasRect.top + 16f * scale
                val r = 7f * scale
                canvas.drawCircle(cx, cy, r, cameraPaint)
                canvas.drawCircle(cx, cy, r * 0.5f, lensPaint)

                // Top speaker ear-piece slit
                val speakerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF334155.toInt()
                    strokeWidth = 2f * scale
                    strokeCap = Paint.Cap.ROUND
                }
                canvas.drawLine(cx - 24f * scale, canvasRect.top - 8f * scale, cx + 24f * scale, canvasRect.top - 8f * scale, speakerPaint)

                // Bottom gesture home bar
                val homeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF94A3B8.toInt()
                    strokeWidth = 3f * scale
                    strokeCap = Paint.Cap.ROUND
                }
                val hY = canvasRect.bottom - 10f * scale
                canvas.drawLine(cx - 40f * scale, hY, cx + 40f * scale, hY, homeBarPaint)
            }

            DeviceFrameStyle.MODERN_NOTCH -> {
                // Dynamic pill island
                val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                }
                val cx = canvasRect.centerX()
                val topY = canvasRect.top + 10f * scale
                val pillRect = RectF(cx - 44f * scale, topY, cx + 44f * scale, topY + 22f * scale)
                canvas.drawRoundRect(pillRect, 11f * scale, 11f * scale, pillPaint)

                // Home indicator bar
                val homeBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0F172A.toInt()
                    strokeWidth = 4f * scale
                    strokeCap = Paint.Cap.ROUND
                }
                val hY = canvasRect.bottom - 10f * scale
                canvas.drawLine(cx - 48f * scale, hY, cx + 48f * scale, hY, homeBarPaint)
            }

            DeviceFrameStyle.SIMPLE_BORDER -> {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.style = Paint.Style.STROKE
                    strokeWidth = 3f * scale
                    color = 0xFFCBD5E1.toInt()
                }
                canvas.drawRoundRect(canvasRect, 20f * scale, 20f * scale, borderPaint)
            }

            DeviceFrameStyle.NONE -> {
                // No extra frame details
            }
        }
    }

    // Save Bitmap to MediaStore Gallery & Cache
    suspend fun saveBitmapToFileAndGallery(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        fileName: String
    ): Pair<Uri?, File?> = withContext(Dispatchers.IO) {
        val extension = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
        val mimeType = if (format == Bitmap.CompressFormat.PNG) "image/png" else "image/jpeg"
        val fullFileName = "$fileName.$extension"

        // 1. Save to App Cache for Sharing via FileProvider
        val cacheDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val cacheFile = File(cacheDir, fullFileName)
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(format, 95, out)
        }

        // 2. Save to MediaStore (Pictures/ScreenCraft) so it's directly visible in user's Gallery
        var galleryUri: Uri? = null
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ScreenCraft")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out: OutputStream ->
                    bitmap.compress(format, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                galleryUri = uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Pair(galleryUri, cacheFile)
    }

    fun shareExportedImage(context: Context, file: File, mimeType: String = "image/png") {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة التصميم").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
