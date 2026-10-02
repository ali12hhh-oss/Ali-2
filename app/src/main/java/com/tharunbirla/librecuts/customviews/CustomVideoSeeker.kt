package com.tharunbirla.librecuts.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Locale

/**
 * CustomVideoSeeker — InShot & CapCut standard central stationary playhead stem.
 *
 * Visual & functional features:
 *  • Razor-sharp 1.5dp crisp white needle hairline (#FFFFFF)
 *  • 1.0dp subtle dark drop shadow (#50000000) for universal contrast over dark & light footage
 *  • Precision inverted marker head on the TimeRulerView
 *  • Floating live timecode indicator pill during scrubbing/seeking
 */
class CustomVideoSeeker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ── Paints ────────────────────────────────────────────────────────────────

    /** The vibrant InShot pink playhead needle */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2A6D")
        style = Paint.Style.STROKE
    }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2A6D")
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF2A6D") // InShot Electric Pink
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    /** Subtle drop shadow so the playhead stands out over all light/dark footage */
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    /** Floating timecode badge pill background */
    private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6121218")
        style = Paint.Style.FILL
    }

    /** Floating timecode text */
    private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    // ── State ─────────────────────────────────────────────────────────────────
    private var seekPosition = 0f   // 0..1
    private var videoDuration = 0L  // ms
    private var currentTimeMs: Long = 0L
    private var isScrubbing: Boolean = false
    var onSeekListener: ((Float) -> Unit)? = null

    // ── Geometry ──────────────────────────────────────────────────────────────
    private val badgeRect = RectF()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val density = resources.displayMetrics.density
        val seekX = width / 2f

        // Compact, refined InShot bookmark time pill dimensions
        badgeTextPaint.textSize = 9f * density
        val timeStr = formatTimecode(currentTimeMs)
        val textWidth = badgeTextPaint.measureText(timeStr)
        val padH = 5f * density
        val pillH = 15f * density
        val pillTop = 2f * density
        val pillLeft = seekX - (textWidth / 2f) - padH
        val pillRight = seekX + (textWidth / 2f) + padH
        val pillBottom = pillTop + pillH
        val pointerH = 3.5f * density
        val needleTop = pillBottom + pointerH

        linePaint.strokeWidth = 2f * density
        shadowPaint.strokeWidth = 4f * density

        // 1. Draw subtle drop shadow for the stationary needle
        canvas.drawLine(seekX + 0.5f * density, needleTop, seekX + 0.5f * density, height.toFloat(), shadowPaint)

        // 2. Draw the vertical InShot Pink needle hairline (Stationary Playhead Stick)
        canvas.drawLine(seekX, needleTop, seekX, height.toFloat(), linePaint)

        // 3. Draw the compact bookmark timecode pill at the top of the stick
        badgeRect.set(pillLeft, pillTop, pillRight, pillBottom)

        // Pill background
        canvas.drawRoundRect(badgeRect, 3.5f * density, 3.5f * density, badgeBgPaint)

        // Crisp InShot Pink pill border
        borderPaint.strokeWidth = 1.2f * density
        canvas.drawRoundRect(badgeRect, 3.5f * density, 3.5f * density, borderPaint)

        // Downward pointing pink pointer connecting the bookmark pill to the needle
        val pointerPath = Path().apply {
            moveTo(seekX - 3f * density, pillBottom)
            lineTo(seekX + 3f * density, pillBottom)
            lineTo(seekX, needleTop)
            close()
        }
        pointerPaint.style = Paint.Style.FILL
        canvas.drawPath(pointerPath, pointerPaint)

        // Draw timecode text centered in the bookmark pill
        val textY = pillTop + (pillH / 2f) - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
        canvas.drawText(timeStr, seekX, textY, badgeTextPaint)
    }

    private fun formatTimecode(ms: Long): String {
        val totalSecs = ms / 1000
        val minutes = totalSecs / 60
        val seconds = totalSecs % 60
        val tenths = (ms % 1000) / 100
        return String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
    }

    private var isDragging = false
    val isUserSeeking: Boolean get() = isDragging

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false // Let touch events pass through cleanly to the timeline below
    }

    fun setVideoDuration(duration: Long) {
        videoDuration = duration
        invalidate()
    }

    fun setSeekPosition(position: Float) {
        seekPosition = position.coerceIn(0f, 1f)
        currentTimeMs = (seekPosition * videoDuration).toLong()
        invalidate()
    }

    fun setCurrentTimeMs(timeMs: Long, isScrubbingActive: Boolean = false) {
        currentTimeMs = timeMs.coerceAtLeast(0L)
        isScrubbing = isScrubbingActive
        if (videoDuration > 0L) {
            seekPosition = (currentTimeMs.toFloat() / videoDuration).coerceIn(0f, 1f)
        }
        invalidate()
    }
}