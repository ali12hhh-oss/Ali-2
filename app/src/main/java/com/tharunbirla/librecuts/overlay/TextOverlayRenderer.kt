package com.tharunbirla.librecuts.overlay

import com.tharunbirla.librecuts.timeline.TextOverlayConfig

/**
 * Text animation preset categories.
 */
enum class TextAnimationType {
    NONE,
    FADE_IN_OUT,
    TYPEWRITER,
    POP_BOUNCE,
    SLIDE_UP,
    ZOOM_IN,
    GLITCH_KINETIC
}

/**
 * Typography styling calculation and animation evaluator.
 */
object TextOverlayRenderer {

    data class TextLayoutBounds(
        val widthPx: Float,
        val heightPx: Float,
        val lineCount: Int
    )

    /**
     * Estimates multi-line bounding box for text layout.
     */
    fun calculateTextBounds(config: TextOverlayConfig, viewWidthPx: Float): TextLayoutBounds {
        val lines = config.text.split("\n")
        val maxCharCount = lines.maxOfOrNull { it.length } ?: 1
        val estimatedCharWidth = config.fontSizeSp * 0.6f + config.letterSpacing
        val estimatedWidth = (maxCharCount * estimatedCharWidth).coerceAtMost(viewWidthPx)
        val lineHeight = config.fontSizeSp * 1.3f + config.lineSpacing
        val estimatedHeight = lines.size * lineHeight

        return TextLayoutBounds(
            widthPx = estimatedWidth,
            heightPx = estimatedHeight,
            lineCount = lines.size
        )
    }

    /**
     * Formats raw subtitle list into timed display blocks.
     */
    fun formatSubtitleBlocks(subtitles: List<Pair<Long, String>>, durationPerLineMs: Long = 3000L): List<Triple<Long, Long, String>> {
        return subtitles.map { (startMs, text) ->
            Triple(startMs, startMs + durationPerLineMs, text.trim())
        }
    }
}
