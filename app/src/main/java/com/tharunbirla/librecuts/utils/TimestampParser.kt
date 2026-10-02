package com.tharunbirla.librecuts.utils

import java.util.regex.Pattern

/**
 * TimestampParser parses timecodes/timestamps from media filenames (e.g. 0-03.png, 1-20.jpg, 01-02-05.png)
 * and constructs chronologically sorted timeline clips with automatic delta durations matching Auto Editor behavior.
 */
object TimestampParser {

    private val HH_MM_SS_PATTERN = Pattern.compile("^(\\d+)[-_](\\d{1,2})[-_](\\d{1,2})$")
    private val MM_SS_PATTERN = Pattern.compile("^(\\d+)[-_](\\d{1,2})$")
    private val BARE_DIGITS_PATTERN = Pattern.compile("^\\d+$")

    /**
     * Parse a filename into a timestamp in seconds.
     * Returns null if the filename does not encode a valid timestamp.
     *
     * Supported formats:
     * - hh-mm-ss or hh_mm_ss (e.g. "1-02-05.png" -> 3725s)
     * - mm-ss or mm_ss (e.g. "0-03.png" -> 3s, "1-20.jpg" -> 80s)
     * - 3-4 digit bare mmss (e.g. "0120.png" -> 80s, "120.png" -> 80s)
     * - 1-2 digit bare seconds (e.g. "3.png" -> 3s, "45.png" -> 45s)
     */
    fun parseTimestampSeconds(filename: String?): Long? {
        if (filename.isNullOrBlank()) return null

        // Strip directory path and extension
        val baseName = filename
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .substringBeforeLast('.')
            .trim()

        if (baseName.isEmpty()) return null

        // 1. hh-mm-ss / hh_mm_ss
        val hhMmSsMatcher = HH_MM_SS_PATTERN.matcher(baseName)
        if (hhMmSsMatcher.matches()) {
            val h = hhMmSsMatcher.group(1)?.toLongOrNull() ?: return null
            val m = hhMmSsMatcher.group(2)?.toLongOrNull() ?: return null
            val s = hhMmSsMatcher.group(3)?.toLongOrNull() ?: return null
            return h * 3600L + m * 60L + s
        }

        // 2. mm-ss / mm_ss
        val mmSsMatcher = MM_SS_PATTERN.matcher(baseName)
        if (mmSsMatcher.matches()) {
            val m = mmSsMatcher.group(1)?.toLongOrNull() ?: return null
            val s = mmSsMatcher.group(2)?.toLongOrNull() ?: return null
            return m * 60L + s
        }

        // 3. Bare digits
        val bareMatcher = BARE_DIGITS_PATTERN.matcher(baseName)
        if (bareMatcher.matches()) {
            if (baseName.length >= 3) {
                val secs = baseName.takeLast(2).toLongOrNull() ?: return null
                val mins = baseName.dropLast(2).toLongOrNull() ?: return null
                return mins * 60L + secs
            }
            return baseName.toLongOrNull()
        }

        return null
    }

    /**
     * Parse a filename into timestamp in milliseconds.
     */
    fun parseTimestampMs(filename: String?): Long? {
        val seconds = parseTimestampSeconds(filename) ?: return null
        return seconds * 1000L
    }

    data class TimestampedItem<T>(
        val item: T,
        val filename: String,
        val timestampMs: Long?,
        var durationMs: Long = 5000L,
        var startTimelineMs: Long = 0L
    )

    /**
     * Checks if at least one item in the list encodes a valid timestamp in its filename.
     */
    fun <T> hasAnyTimestamp(items: List<T>, filenameExtractor: (T) -> String): Boolean {
        return items.any { parseTimestampSeconds(filenameExtractor(it)) != null }
    }

    /**
     * Builds a list of timeline clips with automatically computed durations based on timestamp deltas.
     *
     * If timestamped filenames are detected:
     * - Items are sorted chronologically by their timestamp.
     * - The duration of clip[i] is (timestamp[i+1] - timestamp[i]).
     * - If delta <= 0 (e.g. duplicate or out-of-order), a minimum duration (e.g. 1000ms) or defaultDurationMs is used.
     * - The last clip receives defaultDurationMs (e.g. 5000ms).
     */
    fun <T> buildTimestampTimeline(
        items: List<T>,
        defaultDurationMs: Long = 5000L,
        minDurationMs: Long = 500L,
        filenameExtractor: (T) -> String
    ): List<TimestampedItem<T>> {
        if (items.isEmpty()) return emptyList()

        val parsed = items.map { item ->
            val name = filenameExtractor(item)
            val timeMs = parseTimestampMs(name)
            TimestampedItem(item = item, filename = name, timestampMs = timeMs, durationMs = defaultDurationMs)
        }

        val hasTimestamps = parsed.any { it.timestampMs != null }
        if (!hasTimestamps) {
            var currTime = 0L
            return parsed.map {
                val start = currTime
                currTime += it.durationMs
                it.copy(startTimelineMs = start)
            }
        }

        // Sort items that have timestamps chronologically; un-timestamped items follow their arrival order
        val sorted = parsed.sortedWith(compareBy<TimestampedItem<T>> { it.timestampMs ?: Long.MAX_VALUE })

        var runningTime = 0L
        val result = mutableListOf<TimestampedItem<T>>()

        for (i in sorted.indices) {
            val current = sorted[i]
            val next = sorted.getOrNull(i + 1)

            val duration = if (current.timestampMs != null && next?.timestampMs != null) {
                val delta = next.timestampMs - current.timestampMs
                if (delta > 0) delta else defaultDurationMs
            } else {
                defaultDurationMs
            }.coerceAtLeast(minDurationMs)

            val start = current.timestampMs ?: runningTime
            runningTime = start + duration

            result.add(
                current.copy(
                    durationMs = duration,
                    startTimelineMs = start
                )
            )
        }

        return result
    }
}
