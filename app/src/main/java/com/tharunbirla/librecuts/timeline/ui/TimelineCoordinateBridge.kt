package com.tharunbirla.librecuts.timeline.ui

import com.tharunbirla.librecuts.timeline.TimeRange

/**
 * Coordinate mapping bridge between timeline milliseconds and screen pixel coordinates.
 * Handles pinch-to-zoom scaling, viewport bounds, and magnetic snapping.
 */
class TimelineCoordinateBridge(
    var basePixelsPerSecond: Float = 100f, // 100px = 1 second at 1.0x scale
    initialScaleFactor: Float = 1.0f,
    val minScaleFactor: Float = 0.1f,  // Full project overview
    val maxScaleFactor: Float = 10.0f  // Sub-frame precision
) {
    var scaleFactor: Float = initialScaleFactor.coerceIn(minScaleFactor, maxScaleFactor)
        private set

    val pixelsPerMs: Float get() = (basePixelsPerSecond * scaleFactor) / 1000f
    val msPerPixel: Float get() = 1f / pixelsPerMs.coerceAtLeast(0.0001f)

    /**
     * Converts a millisecond timestamp to pixel X coordinate.
     */
    fun msToPx(timeMs: Long): Float {
        return (timeMs.coerceAtLeast(0L) * pixelsPerMs)
    }

    /**
     * Converts pixel X coordinate to millisecond timestamp.
     */
    fun pxToMs(xPx: Float): Long {
        return (xPx.coerceAtLeast(0f) * msPerPixel).toLong()
    }

    /**
     * Updates zoom scale factor smoothly within allowed limits.
     */
    fun setScale(newScale: Float) {
        scaleFactor = newScale.coerceIn(minScaleFactor, maxScaleFactor)
    }

    fun zoomBy(deltaScale: Float) {
        setScale(scaleFactor * deltaScale)
    }

    /**
     * Returns the visible millisecond time range within the current viewport.
     */
    fun getVisibleTimeRange(scrollX: Float, viewportWidthPx: Float): TimeRange {
        val startMs = pxToMs(scrollX)
        val endMs = pxToMs(scrollX + viewportWidthPx)
        return TimeRange(startMs, endMs)
    }

    /**
     * Calculates the new scroll offset after zooming while maintaining the given anchor timestamp
     * (e.g. current playhead position) at the same visual viewport center.
     */
    fun calculateZoomedScrollX(anchorTimeMs: Long, newScale: Float): Int {
        val newPixelsPerMs = (basePixelsPerSecond * newScale.coerceIn(minScaleFactor, maxScaleFactor)) / 1000f
        return (anchorTimeMs.coerceAtLeast(0L) * newPixelsPerMs).toInt()
    }

    /**
     * Calculates the new scroll offset after zooming around an arbitrary touch focal point.
     */
    fun calculateFocalZoomedScrollX(focalXInViewportPx: Float, currentScrollX: Float, newScale: Float): Int {
        val focalTimeMs = pxToMs(currentScrollX + focalXInViewportPx)
        val newPixelsPerMs = (basePixelsPerSecond * newScale.coerceIn(minScaleFactor, maxScaleFactor)) / 1000f
        val newFocalContentPx = focalTimeMs * newPixelsPerMs
        return (newFocalContentPx - focalXInViewportPx).toInt().coerceAtLeast(0)
    }

    /**
     * Finds the nearest magnetic snap point within snapThresholdMs if one exists.
     */
    fun findSnapPoint(timeMs: Long, snapTargets: List<Long>, snapThresholdMs: Long = 150L): Long? {
        if (snapTargets.isEmpty()) return null

        var closestTarget: Long? = null
        var minDistance = Long.MAX_VALUE

        for (target in snapTargets) {
            val dist = kotlin.math.abs(timeMs - target)
            if (dist <= snapThresholdMs && dist < minDistance) {
                minDistance = dist
                closestTarget = target
            }
        }
        return closestTarget
    }
}
