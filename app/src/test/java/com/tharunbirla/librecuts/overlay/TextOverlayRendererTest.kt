package com.tharunbirla.librecuts.overlay

import com.tharunbirla.librecuts.timeline.TextOverlayConfig
import org.junit.Assert.*
import org.junit.Test

class TextOverlayRendererTest {

    @Test
    fun testCalculateTextBounds() {
        val config = TextOverlayConfig(
            text = "Line 1\nLine 2",
            fontSizeSp = 20f
        )
        val bounds = TextOverlayRenderer.calculateTextBounds(config, 1080f)
        assertEquals(2, bounds.lineCount)
        assertTrue(bounds.widthPx > 0f)
        assertTrue(bounds.heightPx > 0f)
    }

    @Test
    fun testSubtitleBlockFormatting() {
        val raw = listOf(
            Pair(0L, "Hello World"),
            Pair(3000L, "Welcome to Prodline AI")
        )
        val formatted = TextOverlayRenderer.formatSubtitleBlocks(raw, 2500L)
        assertEquals(2, formatted.size)
        assertEquals(0L, formatted[0].first)
        assertEquals(2500L, formatted[0].second)
        assertEquals("Hello World", formatted[0].third)
    }
}
