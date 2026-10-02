package com.tharunbirla.librecuts.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tharunbirla.librecuts.utils.performAppHapticFeedback

class TrackTrimView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var videoDurationMs: Long = 0L
    var maxDurationMs: Long = 0L
    var maxSelectionDurationMs: Long? = null
    var startTimeMs: Long = 0L
    var endTimeMs: Long = 0L
    var activeStartMs: Long = 0L
    var activeEndMs: Long = 0L

    enum class DragTarget { NONE, LEFT, RIGHT, CENTER }

    var onTrimChanged: ((Long, Long, DragTarget) -> Unit)? = null
    var onTrimAdjusting: ((Long, Long) -> Unit)? = null
    var onTrimAdjustingWithDelta: ((Long, Long, Long, Long) -> Unit)? = null
    var onDragStateChanged: ((Boolean) -> Unit)? = null
    var customMsPerPixel: Float? = null

    var trackColor: Int = Color.parseColor("#4285F4") // Default blue
    var trackLabel: String? = null
    var isSelectedTrack: Boolean = false
    var isMainVideoTrack: Boolean = false
    var isTrimEnabled: Boolean = true
    var trackIcon: android.graphics.drawable.Drawable? = null
    var trackThumbnail: android.graphics.Bitmap? = null
    var isAudioTrack: Boolean = false
    var onTrackClicked: (() -> Unit)? = null

    var beats: List<Long> = emptyList()
    var internalStartMs: Long = 0L
    var keyframes: List<Long> = emptyList()
    var audioAmplitudes: FloatArray? = null

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 32f
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#252429") // solid color to mask video frames and create a shrinking effect
        style = Paint.Style.FILL
    }
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#40FFFFFF") // Semi-transparent white
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }
    private val beatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val rectF = RectF()
    private val thumbnailRectF = RectF()
    private val handleWidth = 24f

    private var dragTarget = DragTarget.NONE
    private var lastTouchX = 0f
    private var downTouchX = 0f
    private var lastRawX = 0f
    private var downRawX = 0f
    private var downRawY = 0f

    private var handleScaleAnimator: android.animation.ValueAnimator? = null
    private var currentHandleScale = 1.0f

    private var isDraggingHandle = false
        set(value) {
            if (field != value) {
                field = value
                val targetScale = if (value) 1.6f else 1.0f
                handleScaleAnimator?.cancel()
                handleScaleAnimator = android.animation.ValueAnimator.ofFloat(currentHandleScale, targetScale).apply {
                    duration = 150
                    addUpdateListener {
                        currentHandleScale = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }

                if (value) {
                    performAppHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                } else {
                    performAppHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                }
            }
        }

    fun setRange(videoDurationMs: Long, startTimeMs: Long, endTimeMs: Long) {
        this.videoDurationMs = videoDurationMs
        this.startTimeMs = startTimeMs.coerceIn(0L, videoDurationMs)
        this.endTimeMs = endTimeMs.coerceIn(this.startTimeMs, videoDurationMs)
        invalidate()
    }

    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E61E1E26")
        style = Paint.Style.FILL
    }
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (videoDurationMs <= 0 || width <= 0) return

        val density = resources.displayMetrics.density
        badgeTextPaint.textSize = 10f * density

        val msPerPixel = customMsPerPixel ?: (if (videoDurationMs > 0) videoDurationMs.toFloat() / width else 1.0f)
        val startX = if (isMainVideoTrack) 0f else (startTimeMs / msPerPixel)
        val endX = if (isMainVideoTrack) width.toFloat() else (endTimeMs / msPerPixel)

        rectF.set(startX, 0f, endX, height.toFloat())

        // Draw track fill
        if (isMainVideoTrack) {
            if (isSelectedTrack) {
                if (isTrimEnabled) {
                    // Draw a luminous InShot gold border enclosing the active range when selected
                    borderPaint.color = Color.parseColor("#FFCC00") // InShot Gold
                    borderPaint.strokeWidth = 3f * density
                    val inset = borderPaint.strokeWidth / 2f
                    val selRect = RectF(rectF.left + inset, rectF.top + inset, rectF.right - inset, rectF.bottom - inset)
                    canvas.drawRoundRect(selRect, 6f * density, 6f * density, borderPaint)
                } else {
                    // Main track selection highlight without trimmer handles/ghosts
                    borderPaint.color = Color.parseColor("#FFCC00") // InShot Gold selection
                    borderPaint.strokeWidth = 3f * density
                    val inset = borderPaint.strokeWidth / 2f
                    val selRect = RectF(rectF.left + inset, rectF.top + inset, rectF.right - inset, rectF.bottom - inset)
                    canvas.drawRoundRect(selRect, 6f * density, 6f * density, borderPaint)

                    val overlay = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#1AFFCC00")
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(selRect, 6f * density, 6f * density, overlay)
                }
            }
        } else {
            trackPaint.color = trackColor
            trackPaint.alpha = 200
            canvas.drawRoundRect(rectF, 8f * density, 8f * density, trackPaint)
        }

        // Draw Audio Wave Background
        if (isAudioTrack) {
            canvas.save()
            canvas.clipRect(rectF)
            val centerY = height / 2f
            val maxAmplitude = height * 0.35f

            if (audioAmplitudes != null && audioAmplitudes!!.isNotEmpty()) {
                val amps = audioAmplitudes!!
                var x = startX + handleWidth + 4f
                val waveSpacing = 6f // Closer spacing for real waveforms

                while (x < endX - handleWidth) {
                    val relativeTimeMs = ((x - startX) * msPerPixel).toLong()
                    val index = ((relativeTimeMs.toFloat() / videoDurationMs) * amps.size).toInt()

                    val ampValue = if (index in amps.indices) amps[index] else 0.0f
                    val amplitude = maxAmplitude * ampValue

                    if (amplitude > 1f) {
                        canvas.drawLine(x, centerY - amplitude, x, centerY + amplitude, wavePaint)
                    } else {
                        canvas.drawLine(x, centerY - 1f, x, centerY + 1f, wavePaint)
                    }
                    x += waveSpacing
                }
            } else {
                var x = startX + handleWidth + 4f
                val waveSpacing = 12f
                var timeOffset = 0f
                while (x < endX - handleWidth) {
                    val amplitude = maxAmplitude * (0.3f + 0.7f * Math.abs(Math.sin((x + timeOffset) * 0.05).toFloat()))
                    canvas.drawLine(x, centerY - amplitude, x, centerY + amplitude, wavePaint)
                    x += waveSpacing
                    timeOffset += 1f
                }
            }
            canvas.restore()
        }

        // Draw Beat Markers
        if (beats.isNotEmpty()) {
            canvas.save()
            canvas.clipRect(rectF)
            val beatRadius = 4f
            val yPos = height - 12f
            for (beatTimeMs in beats) {
                val relativeToInternalStart = beatTimeMs - internalStartMs
                if (relativeToInternalStart >= 0) {
                    val beatX = startX + (relativeToInternalStart / msPerPixel)
                    if (beatX <= endX) {
                        canvas.drawCircle(beatX, yPos, beatRadius, beatPaint)
                    }
                }
            }
            canvas.restore()
        }

        // Draw Keyframe Markers
        if (keyframes.isNotEmpty()) {
            canvas.save()
            canvas.clipRect(rectF)
            val kfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700") // Gold color for keyframes
                style = Paint.Style.FILL
            }
            val size = 12f
            val halfSize = size / 2f
            val yPos = height - 12f
            val diamondPath = android.graphics.Path()

            for (kfTimeMs in keyframes) {
                val kfX = startX + (kfTimeMs / msPerPixel)
                if (kfX >= startX && kfX <= endX) {
                    diamondPath.reset()
                    diamondPath.moveTo(kfX, yPos - halfSize)
                    diamondPath.lineTo(kfX + halfSize, yPos)
                    diamondPath.lineTo(kfX, yPos + halfSize)
                    diamondPath.lineTo(kfX - halfSize, yPos)
                    diamondPath.close()
                    canvas.drawPath(diamondPath, kfPaint)
                }
            }
            canvas.restore()
        }

        // Draw track border for secondary tracks
        if (!isMainVideoTrack) {
            if (isSelectedTrack) {
                borderPaint.color = Color.parseColor("#FFCC00") // InShot Gold
                borderPaint.strokeWidth = 3f * density
                canvas.drawRoundRect(rectF, 8f * density, 8f * density, borderPaint)

                val selectionOverlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1AFFCC00")
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(rectF, 8f * density, 8f * density, selectionOverlayPaint)
            } else {
                borderPaint.color = Color.parseColor("#33FFFFFF")
                borderPaint.strokeWidth = 1.5f * density
                canvas.drawRoundRect(rectF, 8f * density, 8f * density, borderPaint)
            }
        }

        // Draw Icon, Thumbnail, and Label if available
        var textStartX = startX + handleWidth + 16f
        val iconSize = (20f * density).toInt()
        val iconTop = (height - iconSize) / 2

        if (trackIcon != null) {
            trackIcon?.setBounds(textStartX.toInt(), iconTop, (textStartX + iconSize).toInt(), iconTop + iconSize)
            trackIcon?.setTint(Color.WHITE)
            trackIcon?.draw(canvas)
            textStartX += iconSize + 8f * density
        }

        if (trackThumbnail != null) {
            val thumbWidth = (iconSize * 1.5f)
            thumbnailRectF.set(textStartX, iconTop.toFloat(), textStartX + thumbWidth, iconTop.toFloat() + iconSize)
            canvas.drawBitmap(trackThumbnail!!, null, thumbnailRectF, trackPaint)
            textStartX += thumbWidth + 8f * density
        }

        if (trackLabel != null) {
            val padding = 16f * density
            val maxTextWidth = (endX - startX) - (textStartX - startX) - handleWidth - padding
            if (maxTextWidth > 20f) {
                val textPaintObj = android.text.TextPaint(textPaint)
                val textToDraw = android.text.TextUtils.ellipsize(
                    trackLabel,
                    textPaintObj,
                    maxTextWidth,
                    android.text.TextUtils.TruncateAt.END
                ).toString()

                val textY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(textToDraw, textStartX, textY, textPaint)
            }
        }

        if (isTrimEnabled) {
            val currentHandleWidth = (14f * density) * currentHandleScale
            handlePaint.color = Color.parseColor("#FFCC00") // InShot Gold

            // Draw left handle with rounded outer edge
            val leftHandleRect = RectF(startX, 0f, startX + currentHandleWidth, height.toFloat())
            canvas.drawRoundRect(leftHandleRect, 6f * density, 6f * density, handlePaint)

            // Draw right handle with rounded outer edge
            val rightHandleRect = RectF(endX - currentHandleWidth, 0f, endX, height.toFloat())
            canvas.drawRoundRect(rightHandleRect, 6f * density, 6f * density, handlePaint)

            // Draw tactile grip lines on handles
            val gripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                style = Paint.Style.STROKE
                strokeWidth = 2f * density
                strokeCap = Paint.Cap.ROUND
            }

            val leftMidX = startX + currentHandleWidth / 2f
            canvas.drawLine(leftMidX, height * 0.35f, leftMidX, height * 0.65f, gripPaint)

            val rightMidX = endX - currentHandleWidth / 2f
            canvas.drawLine(rightMidX, height * 0.35f, rightMidX, height * 0.65f, gripPaint)

            // Draw floating live duration pill when dragging handles or selected
            if (isDraggingHandle || isSelectedTrack) {
                val durationSec = (endTimeMs - startTimeMs) / 1000f
                val durStr = String.format(java.util.Locale.US, "%.1fs", durationSec)
                val badgeW = badgeTextPaint.measureText(durStr) + 14f * density
                val badgeH = 16f * density
                val badgeCenterX = (startX + endX) / 2f
                val badgeRect = RectF(badgeCenterX - badgeW / 2f, 2f * density, badgeCenterX + badgeW / 2f, 2f * density + badgeH)
                canvas.drawRoundRect(badgeRect, 3f * density, 3f * density, badgeBgPaint)
                val badgeTextY = 2f * density + badgeH / 2f - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
                canvas.drawText(durStr, badgeCenterX, badgeTextY, badgeTextPaint)
            }
        }
    }

    var minDurationMs: Long = 300L
    private var initialTrimStartMs: Long = 0L
    private var initialTrimEndMs: Long = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (videoDurationMs <= 0) return false

        val density = resources.displayMetrics.density
        val handleW = 16f * density
        // Expanded touch target padding
        val hitMargin = 32f * density

        val msPerPixel = customMsPerPixel ?: (if (videoDurationMs > 0) videoDurationMs.toFloat() / width else 1.0f)
        val startX = if (isMainVideoTrack) 0f else (startTimeMs / msPerPixel)
        val endX = if (isMainVideoTrack) width.toFloat() else (endTimeMs / msPerPixel)

        val leftZoneEnd = (startX + handleW + hitMargin).coerceAtMost(startX + (endX - startX) * 0.45f)
        val rightZoneStart = (endX - handleW - hitMargin).coerceAtLeast(startX + (endX - startX) * 0.55f)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                downTouchX = event.x
                lastRawX = event.rawX
                downRawX = event.rawX
                downRawY = event.rawY
                initialTrimStartMs = activeStartMs
                initialTrimEndMs = activeEndMs

                dragTarget = if (isTrimEnabled) {
                    when {
                        event.x in (startX - hitMargin)..leftZoneEnd -> DragTarget.LEFT
                        event.x in rightZoneStart..(endX + hitMargin) -> DragTarget.RIGHT
                        event.x > leftZoneEnd && event.x < rightZoneStart -> DragTarget.CENTER
                        else -> DragTarget.NONE
                    }
                } else {
                    // If not enabled for trimming, any touch is a center tap (to select it)
                    DragTarget.CENTER
                }

                isDraggingHandle = (dragTarget == DragTarget.LEFT || dragTarget == DragTarget.RIGHT)
                if (isDraggingHandle) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    onDragStateChanged?.invoke(true)
                }
                return dragTarget != DragTarget.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragTarget == DragTarget.CENTER) {
                    val totalDx = Math.abs(event.rawX - downRawX)
                    val totalDy = Math.abs(event.rawY - downRawY)
                    // If user is swiping horizontally across the clip body, let parent scroll
                    if (totalDx > 12f * density && totalDx > totalDy * 1.2f) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                        dragTarget = DragTarget.NONE
                        return false
                    }
                    return true
                }

                // Smooth dampening factor for less responsive jumps
                val rawDelta = event.rawX - downRawX
                val dampeningFactor = 0.8f
                val deltaRawX = rawDelta * dampeningFactor
                val totalDeltaMs = (deltaRawX * msPerPixel).toLong()

                when (dragTarget) {
                    DragTarget.LEFT -> {
                        val minStart = 0L
                        val clampMaxStart = (activeEndMs - minDurationMs).coerceAtLeast(minStart)
                        val targetStartMs = (initialTrimStartMs + totalDeltaMs).coerceIn(minStart, clampMaxStart)
                        val dtMs = targetStartMs - activeStartMs
                        if (dtMs != 0L) {
                            activeStartMs = targetStartMs
                            startTimeMs = targetStartMs
                            invalidate()
                            onTrimAdjusting?.invoke(activeStartMs, activeEndMs)
                            onTrimAdjustingWithDelta?.invoke(activeStartMs, activeEndMs, dtMs, 0L)
                        }
                    }
                    DragTarget.RIGHT -> {
                        val clampMinEnd = activeStartMs + minDurationMs
                        val limit = if (maxDurationMs > 0L) maxDurationMs else 600000L
                        val targetEndMs = (initialTrimEndMs + totalDeltaMs).coerceIn(clampMinEnd, limit.coerceAtLeast(clampMinEnd))
                        val dtMs = targetEndMs - activeEndMs
                        if (dtMs != 0L) {
                            activeEndMs = targetEndMs
                            endTimeMs = targetEndMs
                            invalidate()
                            onTrimAdjusting?.invoke(activeStartMs, activeEndMs)
                            onTrimAdjustingWithDelta?.invoke(activeStartMs, activeEndMs, 0L, dtMs)
                        }
                    }
                    DragTarget.CENTER, DragTarget.NONE -> {}
                }

                lastRawX = event.rawX
                lastTouchX = event.x
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragTarget != DragTarget.NONE) {
                    val wasDrag = Math.abs(event.rawX - downRawX) > 10f
                    if (!wasDrag && dragTarget == DragTarget.CENTER && event.action == MotionEvent.ACTION_UP) {
                        onTrackClicked?.invoke()
                    } else if (dragTarget == DragTarget.LEFT || dragTarget == DragTarget.RIGHT) {
                        onTrimChanged?.invoke(activeStartMs, activeEndMs, dragTarget)
                    }
                    dragTarget = DragTarget.NONE
                    isDraggingHandle = false
                    parent?.requestDisallowInterceptTouchEvent(false)
                    onDragStateChanged?.invoke(false)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
