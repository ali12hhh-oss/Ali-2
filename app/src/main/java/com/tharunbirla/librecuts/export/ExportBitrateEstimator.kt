package com.tharunbirla.librecuts.export

/**
 * Quality tiers for video encoding bitrate.
 */
enum class BitrateQuality(val multiplier: Float, val label: String) {
    LOW(0.6f, "Low (Small Size)"),
    STANDARD(1.0f, "Standard (Recommended)"),
    HIGH(1.5f, "High Quality"),
    MAXIMUM(2.2f, "Maximum Quality")
}

/**
 * Supported standard export resolutions.
 */
enum class ExportResolution(val width: Int, val height: Int, val baseBitrateKbps: Int, val label: String) {
    RES_360P(640, 360, 900, "360p SD"),
    RES_480P(854, 480, 1500, "480p SD"),
    RES_720P(1280, 720, 3500, "720p HD"),
    RES_1080P(1920, 1080, 8000, "1080p Full HD"),
    RES_2K(2560, 1440, 16000, "2K QHD"),
    RES_4K(3840, 2160, 35000, "4K Ultra HD");

    companion object {
        fun fromHeight(h: Int): ExportResolution = when {
            h <= 360 -> RES_360P
            h <= 480 -> RES_480P
            h <= 720 -> RES_720P
            h <= 1080 -> RES_1080P
            h <= 1440 -> RES_2K
            else -> RES_4K
        }
    }
}

/**
 * Engine for calculating optimal video encoding bitrates, aspect-ratio-aware dimensions,
 * and dynamic estimated export file size.
 */
object ExportBitrateEstimator {

    /**
     * Calculates aspect-ratio-aware target pixel dimensions for video encoding.
     * Guarantees even dimensions (divisible by 2) for H.264/HEVC codec compatibility.
     */
    fun calculateTargetDimensions(baseHeight: Int, aspectRatioStr: String): Pair<Int, Int> {
        val cleanBase = baseHeight.coerceIn(360, 2160)
        val (targetW, targetH) = when (aspectRatioStr) {
            "9:16" -> {
                // Portrait (e.g. 1080x1920 for 1080p, 720x1280 for 720p)
                val h = (cleanBase * 16) / 9
                Pair(cleanBase, h)
            }
            "1:1" -> {
                // Square (e.g. 1080x1080)
                Pair(cleanBase, cleanBase)
            }
            "4:5" -> {
                // Instagram Portrait (e.g. 1080x1350)
                val h = (cleanBase * 5) / 4
                Pair(cleanBase, h)
            }
            "3:4" -> {
                // 3:4 Portrait (e.g. 1080x1440)
                val h = (cleanBase * 4) / 3
                Pair(cleanBase, h)
            }
            "2:3" -> {
                // 2:3 Portrait (e.g. 1080x1620)
                val h = (cleanBase * 3) / 2
                Pair(cleanBase, h)
            }
            "21:9" -> {
                // Cinema Ultrawide (e.g. 2560x1080)
                val w = (cleanBase * 21) / 9
                Pair(w, cleanBase)
            }
            "16:9", "Original", "Custom" -> {
                // Standard Landscape (e.g. 1920x1080 for 1080p, 1280x720 for 720p)
                val w = (cleanBase * 16) / 9
                Pair(w, cleanBase)
            }
            else -> {
                val w = (cleanBase * 16) / 9
                Pair(w, cleanBase)
            }
        }
        val alignedW = (targetW / 2) * 2
        val alignedH = (targetH / 2) * 2
        return Pair(alignedW, alignedH)
    }

    /**
     * Calculates the target video bitrate in kbps given resolution, frame rate, and quality tier.
     */
    fun calculateBitrateKbps(
        resolution: ExportResolution,
        frameRate: Int = 30,
        quality: BitrateQuality = BitrateQuality.STANDARD
    ): Int {
        val fpsMultiplier = when {
            frameRate >= 60 -> 1.5f
            frameRate >= 50 -> 1.35f
            frameRate <= 24 -> 0.9f
            else -> 1.0f // 30 fps baseline
        }
        val targetBitrate = resolution.baseBitrateKbps * quality.multiplier * fpsMultiplier
        return targetBitrate.toInt().coerceAtLeast(500)
    }

    /**
     * Estimates export file size in bytes given total timeline duration.
     */
    fun estimateFileSizeBytes(
        durationMs: Long,
        videoBitrateKbps: Int,
        audioBitrateKbps: Int = 192
    ): Long {
        if (durationMs <= 0L) return 0L
        val totalBitrateKbps = videoBitrateKbps + audioBitrateKbps
        val totalBits = (totalBitrateKbps * 1000L) * (durationMs / 1000.0)
        return (totalBits / 8.0).toLong()
    }

    /**
     * Estimates export file size in Megabytes (MB).
     */
    fun estimateFileSizeMb(
        durationMs: Long,
        videoBitrateKbps: Int,
        audioBitrateKbps: Int = 192
    ): Float {
        val bytes = estimateFileSizeBytes(durationMs, videoBitrateKbps, audioBitrateKbps)
        return (bytes.toFloat() / (1024f * 1024f))
    }
}
