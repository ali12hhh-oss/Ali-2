package com.tharunbirla.librecuts

import com.tharunbirla.librecuts.utils.TimestampParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimestampParserTest {

    @Test
    fun testParseMmSsDash() {
        assertEquals(3L, TimestampParser.parseTimestampSeconds("0-03.png"))
        assertEquals(80L, TimestampParser.parseTimestampSeconds("1-20.jpg"))
    }

    @Test
    fun testParseMmSsUnderscore() {
        assertEquals(125L, TimestampParser.parseTimestampSeconds("2_05.webp"))
    }

    @Test
    fun testParseHhMmSs() {
        assertEquals(3725L, TimestampParser.parseTimestampSeconds("1-02-05.png"))
    }

    @Test
    fun testParseBareDigits() {
        assertEquals(3L, TimestampParser.parseTimestampSeconds("0003.png"))
        assertEquals(80L, TimestampParser.parseTimestampSeconds("0120.png"))
        assertEquals(80L, TimestampParser.parseTimestampSeconds("120.png"))
        assertEquals(3L, TimestampParser.parseTimestampSeconds("3.png"))
        assertEquals(45L, TimestampParser.parseTimestampSeconds("45.png"))
    }

    @Test
    fun testStripDirectories() {
        assertEquals(9L, TimestampParser.parseTimestampSeconds("imgs/subfolder/0-09.png"))
        assertEquals(9L, TimestampParser.parseTimestampSeconds("C:\\Users\\Photos\\0-09.png"))
    }

    @Test
    fun testUnparseableReturnsNull() {
        assertNull(TimestampParser.parseTimestampSeconds("hero.png"))
        assertNull(TimestampParser.parseTimestampSeconds("scene_a.png"))
        assertNull(TimestampParser.parseTimestampSeconds(""))
        assertNull(TimestampParser.parseTimestampSeconds(null))
    }

    @Test
    fun testBuildTimestampTimeline() {
        val filenames = listOf("0-00.png", "0-03.png", "0-10.png")
        val timeline = TimestampParser.buildTimestampTimeline(filenames) { it }

        assertEquals(3, timeline.size)
        // clip 0: 0s to 3s -> 3000ms duration
        assertEquals(0L, timeline[0].startTimelineMs)
        assertEquals(3000L, timeline[0].durationMs)

        // clip 1: 3s to 10s -> 7000ms duration
        assertEquals(3000L, timeline[1].startTimelineMs)
        assertEquals(7000L, timeline[1].durationMs)

        // clip 2: last clip -> default 5000ms duration
        assertEquals(10000L, timeline[2].startTimelineMs)
        assertEquals(5000L, timeline[2].durationMs)
    }
}
