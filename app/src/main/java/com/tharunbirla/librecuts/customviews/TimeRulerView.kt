package com.tharunbirla.librecuts.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import java.util.Locale

class TimeRulerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var videoDurationMs: Long = 0L

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#32323E")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5A5A6A")
        strokeWidth = 1.2f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0B0C0")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0EC")
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    init {
        val density = resources.displayMetrics.density
        textPaint.textSize = 9.5f * density
        tickPaint.strokeWidth = 1.2f * density
        majorTickPaint.strokeWidth = 1.8f * density
        axisPaint.strokeWidth = 1f * density
    }

    fun setVideoDuration(durationMs: Long) {
        this.videoDurationMs = durationMs
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (videoDurationMs <= 0 || width <= 0) return

        val msPerPixel = videoDurationMs.toFloat() / width
        if (msPerPixel <= 0f) return

        val density = resources.displayMetrics.density
        val heightVal = height.toFloat()
        val textY = 12f * density
        val tickBottom = heightVal - 2f * density

        // Draw bottom subtle axis line
        canvas.drawLine(0f, heightVal - 1f, width.toFloat(), heightVal - 1f, axisPaint)

        // Dynamic step calculations based on pixels/zoom level
        val targetWidthPx = 70f * density
        val targetMs = targetWidthPx * msPerPixel

        val stepMs = when {
            targetMs <= 50L -> 50L
            targetMs <= 100L -> 100L
            targetMs <= 250L -> 250L
            targetMs <= 500L -> 500L
            targetMs <= 1000L -> 1000L
            targetMs <= 2000L -> 2000L
            targetMs <= 5000L -> 5000L
            targetMs <= 10000L -> 10000L
            targetMs <= 30000L -> 30000L
            targetMs <= 60000L -> 60000L
            targetMs <= 120000L -> 120000L
            targetMs <= 300000L -> 300000L
            else -> 600000L
        }

        val minorStepMs = if (stepMs == 50L || stepMs == 250L) stepMs / 5 else maxOf(10L, stepMs / 5)

        // Draw minor and major ticks across stem
        var currentMs = 0L
        while (currentMs <= videoDurationMs) {
            val x = currentMs / msPerPixel

            if (currentMs % stepMs == 0L) {
                // Major tick: clean line from middle to bottom
                canvas.drawLine(x, heightVal * 0.45f, x, tickBottom, majorTickPaint)

                val minutes = (currentMs / 60000).toInt()
                val seconds = ((currentMs % 60000) / 1000).toInt()
                val timeStr = if (stepMs < 1000L) {
                    val tenths = ((currentMs % 1000) / 100).toInt()
                    String.format(Locale.US, "%02d:%02d.%d", minutes, seconds, tenths)
                } else {
                    String.format(Locale.US, "%02d:%02d", minutes, seconds)
                }

                // Draw high-contrast timestamp label
                canvas.drawText(timeStr, x, textY, textPaint)
            } else {
                // Minor tick: short thin line near bottom
                canvas.drawLine(x, heightVal * 0.72f, x, tickBottom, tickPaint)
            }

            currentMs += minorStepMs
        }
    }
}
