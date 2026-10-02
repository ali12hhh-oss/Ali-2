package com.tharunbirla.librecuts.export

import org.junit.Assert.*
import org.junit.Test

class ExportBitrateEstimatorTest {

    @Test
    fun testCalculateBitrate1080pStandard() {
        val bitrate = ExportBitrateEstimator.calculateBitrateKbps(
            ExportResolution.RES_1080P,
            frameRate = 30,
            quality = BitrateQuality.STANDARD
        )
        assertEquals(8000, bitrate)
    }

    @Test
    fun testCalculateBitrate4K60FpsMaximum() {
        val bitrate = ExportBitrateEstimator.calculateBitrateKbps(
            ExportResolution.RES_4K,
            frameRate = 60,
            quality = BitrateQuality.MAXIMUM
        )
        // 35000 * 2.2 * 1.5 = 115500 kbps
        assertEquals(115500, bitrate)
    }

    @Test
    fun testEstimateFileSize() {
        // 60 seconds of 1080p (8000kbps video + 192kbps audio = 8192kbps = 1024 KB/s)
        // 60s * 1024 KB/s = ~60 MB
        val sizeMb = ExportBitrateEstimator.estimateFileSizeMb(
            durationMs = 60000L,
            videoBitrateKbps = 8000,
            audioBitrateKbps = 192
        )
        assertTrue(sizeMb in 55f..65f)
    }

    @Test
    fun testCalculateTargetDimensionsForAllAspectRatios() {
        // 1080p Base Height Tests
        val (w916, h916) = ExportBitrateEstimator.calculateTargetDimensions(1080, "9:16")
        assertEquals(1080, w916)
        assertEquals(1920, h916) // TikTok / Reels 1080x1920

        val (w11, h11) = ExportBitrateEstimator.calculateTargetDimensions(1080, "1:1")
        assertEquals(1080, w11)
        assertEquals(1080, h11) // Instagram Square 1080x1080

        val (w45, h45) = ExportBitrateEstimator.calculateTargetDimensions(1080, "4:5")
        assertEquals(1080, w45)
        assertEquals(1350, h45) // Instagram Portrait 1080x1350

        val (w34, h34) = ExportBitrateEstimator.calculateTargetDimensions(1080, "3:4")
        assertEquals(1080, w34)
        assertEquals(1440, h34) // 3:4 Portrait 1080x1440

        val (w23, h23) = ExportBitrateEstimator.calculateTargetDimensions(1080, "2:3")
        assertEquals(1080, w23)
        assertEquals(1620, h23) // 2:3 Portrait 1080x1620

        val (w169, h169) = ExportBitrateEstimator.calculateTargetDimensions(1080, "16:9")
        assertEquals(1920, w169)
        assertEquals(1080, h169) // YouTube 1920x1080

        val (w219, h219) = ExportBitrateEstimator.calculateTargetDimensions(1080, "21:9")
        assertEquals(2520, w219)
        assertEquals(1080, h219) // Cinema Ultrawide 2520x1080 (macroblock aligned)

        // Verify all output dimensions are divisible by 2
        assertTrue(w916 % 2 == 0 && h916 % 2 == 0)
        assertTrue(w11 % 2 == 0 && h11 % 2 == 0)
        assertTrue(w45 % 2 == 0 && h45 % 2 == 0)
        assertTrue(w34 % 2 == 0 && h34 % 2 == 0)
        assertTrue(w23 % 2 == 0 && h23 % 2 == 0)
        assertTrue(w169 % 2 == 0 && h169 % 2 == 0)
        assertTrue(w219 % 2 == 0 && h219 % 2 == 0)
    }
}
