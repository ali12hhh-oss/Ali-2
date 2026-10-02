package com.tharunbirla.librecuts.timeline.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tharunbirla.librecuts.timeline.TransitionEffect
import com.tharunbirla.librecuts.timeline.TransitionType
import com.tharunbirla.librecuts.timeline.VideoClip

/**
 * High-performance custom canvas View rendering multi-clip video sequences
 * with InShot/CapCut style tactile selection handles and transition markers.
 */
class MultiClipTrackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var bridge = TimelineCoordinateBridge()
    private var clips: List<VideoClip> = emptyList()
    private var transitions: Map<Int, TransitionEffect> = emptyMap()

    var selectedClipIndex: Int? = null
        private set

    enum class TrimHandle { NONE, START, END }
    private var activeDragHandle = TrimHandle.NONE
    private var touchDownX = 0f

    // Callback listeners
    var onClipSelected: ((Int) -> Unit)? = null
    var onTransitionClicked: ((Int) -> Unit)? = null
    var onClipTrimAdjusting: ((clipIndex: Int, newStartMs: Long, newEndMs: Long) -> Unit)? = null
    var onClipTrimFinished: ((clipIndex: Int, newStartMs: Long, newEndMs: Long) -> Unit)? = null

    private val density = resources.displayMetrics.density

    // Paints
    private val clipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2C2C34")
        style = Paint.Style.FILL
    }
    private val clipBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#444452")
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val selectionBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFCC00") // InShot Gold/Yellow
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFCC00")
        style = Paint.Style.FILL
    }
    private val handleGripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 12f * density
        isFakeBoldText = true
    }
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.FILL
    }
    private val transitionBtnBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.FILL
    }
    private val transitionGlyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun setClips(newClips: List<VideoClip>, newTransitions: Map<Int, TransitionEffect> = emptyMap()) {
        this.clips = newClips
        this.transitions = newTransitions
        if (selectedClipIndex != null && selectedClipIndex!! >= newClips.size) {
            selectedClipIndex = null
        }
        invalidate()
    }

    fun setSelectedClipIndex(index: Int?) {
        if (selectedClipIndex != index) {
            selectedClipIndex = index
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (clips.isEmpty()) return

        val viewHeight = height.toFloat()
        val cornerRadius = 6f * density
        val handleWidth = 14f * density
        var currentTimelineMs = 0L

        for ((index, clip) in clips.withIndex()) {
            val clipDurationMs = clip.timelineDurationMs
            val leftPx = bridge.msToPx(currentTimelineMs)
            val rightPx = bridge.msToPx(currentTimelineMs + clipDurationMs)
            val clipRect = RectF(leftPx, 4f * density, rightPx, viewHeight - 4f * density)

            // Draw clip background
            canvas.drawRoundRect(clipRect, cornerRadius, cornerRadius, clipBgPaint)
            canvas.drawRoundRect(clipRect, cornerRadius, cornerRadius, clipBorderPaint)

            // Draw clip name / duration badge
            val label = if (clip.speed != 1.0f) "${clip.source.name} (${clip.speed}x)" else clip.source.name
            canvas.drawText(label, leftPx + 8f * density, viewHeight / 2f + 4f * density, textPaint)

            // If selected, draw luminous gold selection border and tactile handles
            if (index == selectedClipIndex) {
                canvas.drawRoundRect(clipRect, cornerRadius, cornerRadius, selectionBorderPaint)

                // Left handle
                val leftHandleRect = RectF(leftPx, 4f * density, leftPx + handleWidth, viewHeight - 4f * density)
                canvas.drawRoundRect(leftHandleRect, cornerRadius, cornerRadius, handlePaint)
                canvas.drawLine(leftPx + handleWidth / 2f, viewHeight * 0.35f, leftPx + handleWidth / 2f, viewHeight * 0.65f, handleGripPaint)

                // Right handle
                val rightHandleRect = RectF(rightPx - handleWidth, 4f * density, rightPx, viewHeight - 4f * density)
                canvas.drawRoundRect(rightHandleRect, cornerRadius, cornerRadius, handlePaint)
                canvas.drawLine(rightPx - handleWidth / 2f, viewHeight * 0.35f, rightPx - handleWidth / 2f, viewHeight * 0.65f, handleGripPaint)
            }

            // Draw transition marker between adjacent clips
            if (index < clips.size - 1) {
                val isAdjacentSelected = (selectedClipIndex == index || selectedClipIndex == index + 1)
                if (!isAdjacentSelected) {
                    val transitionCenterX = rightPx
                    val transitionCenterY = viewHeight / 2f
                    val halfW = 7f * density
                    val transRect = RectF(transitionCenterX - halfW, transitionCenterY - halfW, transitionCenterX + halfW, transitionCenterY + halfW)
                    canvas.drawRoundRect(transRect, 3f * density, 3f * density, transitionBtnBgPaint)

                    val trans = transitions[index]
                    if (trans != null && trans.type != TransitionType.NONE) {
                        canvas.drawLine(transitionCenterX - 3f * density, transitionCenterY - 3f * density, transitionCenterX + 3f * density, transitionCenterY + 3f * density, transitionGlyphPaint)
                    } else {
                        canvas.drawLine(transitionCenterX, transitionCenterY - 3f * density, transitionCenterX, transitionCenterY + 3f * density, transitionGlyphPaint)
                    }
                }
            }

            currentTimelineMs += clipDurationMs
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        val hitPadding = 12f * density

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchDownX = x
                activeDragHandle = TrimHandle.NONE

                // Check if user tapped a trim handle on the selected clip
                selectedClipIndex?.let { selIdx ->
                    if (selIdx in clips.indices) {
                        val clip = clips[selIdx]
                        val startMs = getClipTimelineStartMs(selIdx)
                        val leftPx = bridge.msToPx(startMs)
                        val rightPx = bridge.msToPx(startMs + clip.timelineDurationMs)
                        val handleWidth = 14f * density
                        val halfWidth = (rightPx - leftPx) / 2f

                        val leftZoneEnd = (leftPx + handleWidth + hitPadding).coerceAtMost(leftPx + halfWidth)
                        val rightZoneStart = (rightPx - handleWidth - hitPadding).coerceAtLeast(leftPx + halfWidth)

                        if (x in (leftPx - hitPadding)..leftZoneEnd) {
                            activeDragHandle = TrimHandle.START
                            parent?.requestDisallowInterceptTouchEvent(true)
                            return true
                        } else if (x in rightZoneStart..(rightPx + hitPadding)) {
                            activeDragHandle = TrimHandle.END
                            parent?.requestDisallowInterceptTouchEvent(true)
                            return true
                        }
                    }
                }

                // Check if user tapped on a transition point (only when no clip is selected)
                if (selectedClipIndex == null) {
                    var runningMs = 0L
                    for (i in 0 until clips.size - 1) {
                        runningMs += clips[i].timelineDurationMs
                        val transX = bridge.msToPx(runningMs)
                        if (kotlin.math.abs(x - transX) <= 12f * density) {
                            onTransitionClicked?.invoke(i)
                            return true
                        }
                    }
                }

                // Check which clip was tapped
                var currentMs = 0L
                for ((idx, clip) in clips.withIndex()) {
                    val startPx = bridge.msToPx(currentMs)
                    val endPx = bridge.msToPx(currentMs + clip.timelineDurationMs)
                    if (x in startPx..endPx) {
                        selectedClipIndex = idx
                        onClipSelected?.invoke(idx)
                        invalidate()
                        return true
                    }
                    currentMs += clip.timelineDurationMs
                }
            }
            MotionEvent.ACTION_MOVE -> {
                selectedClipIndex?.let { selIdx ->
                    if (activeDragHandle != TrimHandle.NONE && selIdx in clips.indices) {
                        val clip = clips[selIdx]
                        val startTimelineMs = getClipTimelineStartMs(selIdx)
                        val touchMs = bridge.pxToMs(x)

                        when (activeDragHandle) {
                            TrimHandle.START -> {
                                val deltaTimelineMs = (touchMs - startTimelineMs).coerceAtLeast(0L)
                                val newStartSourceMs = (clip.sourceRange.startMs + (deltaTimelineMs * clip.speed).toLong())
                                    .coerceIn(0L, clip.sourceRange.endMs - (100L * clip.speed).toLong())
                                onClipTrimAdjusting?.invoke(selIdx, newStartSourceMs, clip.sourceRange.endMs)
                            }
                            TrimHandle.END -> {
                                val newDurationMs = (touchMs - startTimelineMs).coerceAtLeast(100L)
                                val newEndSourceMs = (clip.sourceRange.startMs + (newDurationMs * clip.speed).toLong())
                                    .coerceIn(clip.sourceRange.startMs + (100L * clip.speed).toLong(), clip.source.naturalDurationMs)
                                onClipTrimAdjusting?.invoke(selIdx, clip.sourceRange.startMs, newEndSourceMs)
                            }
                            else -> {}
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selectedClipIndex?.let { selIdx ->
                    if (activeDragHandle != TrimHandle.NONE && selIdx in clips.indices) {
                        val clip = clips[selIdx]
                        onClipTrimFinished?.invoke(selIdx, clip.sourceRange.startMs, clip.sourceRange.endMs)
                    }
                }
                activeDragHandle = TrimHandle.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun getClipTimelineStartMs(clipIndex: Int): Long {
        return clips.take(clipIndex).sumOf { it.timelineDurationMs }
    }
}
