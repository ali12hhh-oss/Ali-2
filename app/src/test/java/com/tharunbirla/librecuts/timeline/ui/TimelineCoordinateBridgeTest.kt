package com.tharunbirla.librecuts.timeline.ui

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimelineCoordinateBridgeTest {

    private lateinit var bridge: TimelineCoordinateBridge

    @Before
    fun setUp() {
        bridge = TimelineCoordinateBridge(basePixelsPerSecond = 100f, initialScaleFactor = 1.0f)
    }

    @Test
    fun testMsToPxConversion() {
        // 1 second (1000ms) = 100px at 1.0x
        assertEquals(100f, bridge.msToPx(1000L), 0.001f)
        assertEquals(500f, bridge.msToPx(5000L), 0.001f)
        assertEquals(0f, bridge.msToPx(0L), 0.001f)
    }

    @Test
    fun testPxToMsConversion() {
        assertEquals(1000L, bridge.pxToMs(100f))
        assertEquals(5000L, bridge.pxToMs(500f))
        assertEquals(0L, bridge.pxToMs(0f))
    }

    @Test
    fun testZoomScaling() {
        // Zoom in to 2.0x (1000ms = 200px)
        bridge.setScale(2.0f)
        assertEquals(200f, bridge.msToPx(1000L), 0.001f)
        assertEquals(1000L, bridge.pxToMs(200f))

        // Zoom out to 0.5x (1000ms = 50px)
        bridge.setScale(0.5f)
        assertEquals(50f, bridge.msToPx(1000L), 0.001f)
        assertEquals(1000L, bridge.pxToMs(50f))
    }

    @Test
    fun testMagneticSnapping() {
        val targets = listOf(0L, 5000L, 10000L, 15000L)

        // Within 150ms threshold
        assertEquals(5000L, bridge.findSnapPoint(5050L, targets, 150L))
        assertEquals(10000L, bridge.findSnapPoint(9920L, targets, 150L))

        // Outside threshold -> no snap
        assertNull(bridge.findSnapPoint(5300L, targets, 150L))
    }

    @Test
    fun testVisibleTimeRange() {
        val range = bridge.getVisibleTimeRange(scrollX = 200f, viewportWidthPx = 800f)
        assertEquals(2000L, range.startMs)
        assertEquals(10000L, range.endMs)
        assertEquals(8000L, range.durationMs)
    }

    @Test
    fun testZoomAnchoredAtPlayhead() {
        // At 1.0x (100 px/s = 0.1 px/ms), 5000ms is at 500px
        val scroll1 = bridge.calculateZoomedScrollX(anchorTimeMs = 5000L, newScale = 1.0f)
        assertEquals(500, scroll1)

        // When zoomed in to 2.0x (200 px/s = 0.2 px/ms), 5000ms is at 1000px
        val scroll2 = bridge.calculateZoomedScrollX(anchorTimeMs = 5000L, newScale = 2.0f)
        assertEquals(1000, scroll2)
    }

    @Test
    fun testFocalPointZoom() {
        // Current scroll = 200px (2000ms), touch focal point = 100px (1000ms into viewport -> 3000ms content)
        val focalScroll = bridge.calculateFocalZoomedScrollX(
            focalXInViewportPx = 100f,
            currentScrollX = 200f,
            newScale = 2.0f
        )
        // At 2.0x, 3000ms is 600px. 600px - 100px focal offset = 500px
        assertEquals(500, focalScroll)
    }
}
