package com.tharunbirla.librecuts.timeline

import android.net.Uri
import java.io.Serializable

/**
 * Immutable time range representation with millisecond precision.
 */
data class TimeRange(
    val startMs: Long,
    val endMs: Long
) : Serializable {
    init {
        require(startMs >= 0L) { "startMs cannot be negative: $startMs" }
        require(endMs >= startMs) { "endMs ($endMs) must be >= startMs ($startMs)" }
    }

    val durationMs: Long get() = endMs - startMs

    fun contains(timeMs: Long): Boolean = timeMs in startMs..endMs
    fun containsStrict(timeMs: Long): Boolean = timeMs in startMs until endMs

    fun clamp(timeMs: Long): Long = timeMs.coerceIn(startMs, endMs)

    fun overlaps(other: TimeRange): Boolean {
        return maxOf(startMs, other.startMs) < minOf(endMs, other.endMs)
    }

    fun shift(offsetMs: Long): TimeRange {
        val newStart = (startMs + offsetMs).coerceAtLeast(0L)
        val newEnd = (endMs + offsetMs).coerceAtLeast(newStart)
        return TimeRange(newStart, newEnd)
    }
}

/**
 * Metadata for a raw imported media asset.
 */
data class MediaSource(
    val uriString: String,
    val name: String,
    val naturalDurationMs: Long,
    val width: Int = 1920,
    val height: Int = 1080,
    val isImage: Boolean = false,
    val scrubProxyUriString: String? = null
) : Serializable {
    val uri: Uri get() = Uri.parse(uriString)
    val scrubProxyUri: Uri? get() = scrubProxyUriString?.let { Uri.parse(it) }

    constructor(
        uri: Uri,
        name: String,
        naturalDurationMs: Long,
        width: Int = 1920,
        height: Int = 1080,
        isImage: Boolean = false,
        scrubProxyUri: Uri? = null
    ) : this(
        uriString = uri.toString(),
        name = name,
        naturalDurationMs = naturalDurationMs,
        width = width,
        height = height,
        isImage = isImage,
        scrubProxyUriString = scrubProxyUri?.toString()
    )
}

/**
 * Normalized crop boundary fractions (0.0 to 1.0).
 */
data class CropFraction(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
) : Serializable {
    init {
        require(left in 0f..1f && right in 0f..1f && left < right) { "Invalid horizontal crop fractions" }
        require(top in 0f..1f && bottom in 0f..1f && top < bottom) { "Invalid vertical crop fractions" }
    }

    val widthFraction: Float get() = right - left
    val heightFraction: Float get() = bottom - top
}

/**
 * Comprehensive 12-channel color grading and LUT filter parameters.
 */
data class ColorGradeConfig(
    val brightness: Float = 0f,      // -1.0 to 1.0 (default 0.0)
    val contrast: Float = 1.0f,      // 0.0 to 2.0 (default 1.0)
    val saturation: Float = 1.0f,    // 0.0 to 2.0 (default 1.0)
    val warmth: Float = 0f,          // -1.0 (cool) to 1.0 (warm)
    val tint: Float = 0f,            // -1.0 (green) to 1.0 (magenta)
    val highlights: Float = 0f,      // -1.0 to 1.0
    val shadows: Float = 0f,         // -1.0 to 1.0
    val vibrance: Float = 0f,        // -1.0 to 1.0
    val sharpness: Float = 0f,       // 0.0 to 1.0
    val vignette: Float = 0f,        // 0.0 to 1.0
    val fade: Float = 0f,            // 0.0 to 1.0
    val grain: Float = 0f,           // 0.0 to 1.0
    val filterLutName: String? = null,
    val filterIntensity: Float = 1.0f // 0.0 to 1.0
) : Serializable

/**
 * Keyframe point for spatial and visual parameter animation.
 */
data class KeyframePoint(
    val timeMs: Long,
    val valueX: Float,
    val valueY: Float = 0f,
    val interpolation: String = "linear"
) : Serializable

/**
 * Spatial transformation and transparency for video, PIP, and text layers.
 */
data class KeyframeTransform(
    val relativeX: Float = 0.5f,
    val relativeY: Float = 0.5f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 1.0f,
    val positionKeyframes: List<KeyframePoint> = emptyList(),
    val scaleKeyframes: List<KeyframePoint> = emptyList(),
    val rotationKeyframes: List<KeyframePoint> = emptyList(),
    val opacityKeyframes: List<KeyframePoint> = emptyList()
) : Serializable

/**
 * Mask shape geometry.
 */
enum class MaskShape { NONE, SPLIT, SHUTTER, CIRCLE, RECTANGLE, HEART, STAR }

data class MaskConfig(
    val shape: MaskShape = MaskShape.NONE,
    val relativeX: Float = 0.5f,
    val relativeY: Float = 0.5f,
    val relativeWidth: Float = 0.5f,
    val relativeHeight: Float = 0.5f,
    val rotationDegrees: Float = 0f,
    val isInverted: Boolean = false,
    val feather: Float = 0f
) : Serializable

/**
 * A discrete video or photo segment on the primary track.
 */
data class VideoClip(
    val id: String = System.nanoTime().toString(),
    val source: MediaSource,
    val sourceRange: TimeRange,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isMuted: Boolean = false,
    val isReversed: Boolean = false,
    val isMirrored: Boolean = false,
    val cropRect: CropFraction? = null,
    val colorGrade: ColorGradeConfig = ColorGradeConfig(),
    val mask: MaskConfig = MaskConfig(),
    val isFreezeFrame: Boolean = false
) : Serializable {
    init {
        require(speed in 0.1f..100f) { "Speed must be in 0.1..100.0" }
        require(volume in 0f..2f) { "Volume must be in 0.0..2.0" }
    }

    /**
     * Effective duration of this clip on the timeline after accounting for speed.
     */
    val timelineDurationMs: Long
        get() = ((sourceRange.durationMs) / speed).toLong().coerceAtLeast(1L)
}

/**
 * Transition effects between adjacent clips on the primary track.
 */
enum class TransitionType {
    NONE, DISSOLVE, FADE_BLACK, FADE_WHITE,
    SLIDE_LEFT, SLIDE_RIGHT, SLIDE_UP, SLIDE_DOWN,
    PUSH_LEFT, PUSH_RIGHT, WIPE_LEFT, WIPE_RIGHT,
    ZOOM_IN, ZOOM_OUT, BLUR, FLASH, GLITCH
}

data class TransitionEffect(
    val id: String = System.nanoTime().toString(),
    val type: TransitionType = TransitionType.NONE,
    val durationMs: Long = 1000L
) : Serializable

/**
 * An independent audio clip (music, sound effect, or voice recording).
 */
data class AudioClip(
    val id: String = System.nanoTime().toString(),
    val audioUriString: String,
    val name: String = "Audio",
    val sourceRange: TimeRange,
    val timelineStartMs: Long = 0L,
    val volume: Float = 1.0f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val isDucking: Boolean = false,
    val beats: List<Long> = emptyList()
) : Serializable {
    val audioUri: Uri get() = Uri.parse(audioUriString)
    val timelineEndMs: Long get() = timelineStartMs + sourceRange.durationMs
    val timelineRange: TimeRange get() = TimeRange(timelineStartMs, timelineEndMs)

    constructor(
        id: String = System.nanoTime().toString(),
        audioUri: Uri,
        name: String = "Audio",
        sourceRange: TimeRange,
        timelineStartMs: Long = 0L,
        volume: Float = 1.0f,
        fadeInMs: Long = 0L,
        fadeOutMs: Long = 0L,
        isDucking: Boolean = false,
        beats: List<Long> = emptyList()
    ) : this(
        id = id,
        audioUriString = audioUri.toString(),
        name = name,
        sourceRange = sourceRange,
        timelineStartMs = timelineStartMs,
        volume = volume,
        fadeInMs = fadeInMs,
        fadeOutMs = fadeOutMs,
        isDucking = isDucking,
        beats = beats
    )
}

/**
 * Overlay types for secondary layers.
 */
enum class OverlayType { PIP_VIDEO, IMAGE, TEXT, STICKER, HANDWRITING }

/**
 * Blend modes for video and image overlays.
 */
enum class BlendMode { NORMAL, SCREEN, MULTIPLY, DARKEN, LIGHTEN, OVERLAY, COLOR_DODGE }

/**
 * Text styling and font configuration.
 */
data class TextOverlayConfig(
    val text: String,
    val fontSizeSp: Float = 24f,
    val colorHex: String = "#FFFFFF",
    val fontPath: String? = null,
    val alignment: String = "center",
    val borderWidth: Float = 0f,
    val borderColorHex: String = "#000000",
    val shadowColorHex: String = "#80000000",
    val shadowRadius: Float = 0f,
    val backgroundColorHex: String? = null,
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 0f
) : Serializable

/**
 * A secondary layer on an overlay track (PIP, Sticker, Text, etc.).
 */
data class OverlayClip(
    val id: String = System.nanoTime().toString(),
    val type: OverlayType,
    val uriString: String? = null,
    val textConfig: TextOverlayConfig? = null,
    val timelineRange: TimeRange,
    val transform: KeyframeTransform = KeyframeTransform(),
    val blendMode: BlendMode = BlendMode.NORMAL,
    val chromaKeyColorHex: String? = null,
    val chromaKeySimilarity: Float = 0.1f,
    val mask: MaskConfig = MaskConfig()
) : Serializable {
    val uri: Uri? get() = uriString?.let { Uri.parse(it) }

    constructor(
        id: String = System.nanoTime().toString(),
        type: OverlayType,
        uri: Uri?,
        textConfig: TextOverlayConfig? = null,
        timelineRange: TimeRange,
        transform: KeyframeTransform = KeyframeTransform(),
        blendMode: BlendMode = BlendMode.NORMAL,
        chromaKeyColorHex: String? = null,
        chromaKeySimilarity: Float = 0.1f,
        mask: MaskConfig = MaskConfig()
    ) : this(
        id = id,
        type = type,
        uriString = uri?.toString(),
        textConfig = textConfig,
        timelineRange = timelineRange,
        transform = transform,
        blendMode = blendMode,
        chromaKeyColorHex = chromaKeyColorHex,
        chromaKeySimilarity = chromaKeySimilarity,
        mask = mask
    )
}

/**
 * The primary sequential video track containing video/photo clips and transitions.
 */
data class MainVideoTrack(
    val clips: List<VideoClip> = emptyList(),
    val transitions: Map<Int, TransitionEffect> = emptyMap() // key = index of first clip
) : Serializable {
    val totalDurationMs: Long
        get() = clips.sumOf { it.timelineDurationMs }

    val clipCount: Int get() = clips.size

    fun getClipAtTimelinePosition(timelineMs: Long): Pair<Int, VideoClip>? {
        var runningMs = 0L
        for ((index, clip) in clips.withIndex()) {
            val clipDuration = clip.timelineDurationMs
            if (timelineMs in runningMs until (runningMs + clipDuration)) {
                return Pair(index, clip)
            }
            runningMs += clipDuration
        }
        if (clips.isNotEmpty() && timelineMs >= runningMs) {
            return Pair(clips.lastIndex, clips.last())
        }
        return null
    }

    fun getClipTimelineStartMs(clipIndex: Int): Long {
        require(clipIndex in clips.indices) { "Index $clipIndex out of bounds for ${clips.size} clips" }
        return clips.take(clipIndex).sumOf { it.timelineDurationMs }
    }
}

/**
 * Secondary audio track (BGM, sound effects, voiceovers).
 */
data class AudioTrack(
    val id: String = System.nanoTime().toString(),
    val name: String = "Audio Track",
    val clips: List<AudioClip> = emptyList(),
    val isMuted: Boolean = false,
    val volume: Float = 1.0f
) : Serializable

/**
 * Secondary visual overlay track (PIP videos, images, stickers, text).
 */
data class OverlayTrack(
    val id: String = System.nanoTime().toString(),
    val overlays: List<OverlayClip> = emptyList()
) : Serializable

/**
 * Standard platform aspect ratios.
 */
enum class AspectRatio(val displayName: String, val ratioWidth: Float, val ratioHeight: Float) {
    RATIO_9_16("9:16 (TikTok/Reels)", 9f, 16f),
    RATIO_16_9("16:9 (YouTube)", 16f, 9f),
    RATIO_1_1("1:1 (Instagram)", 1f, 1f),
    RATIO_4_5("4:5 (IG Portrait)", 4f, 5f),
    RATIO_2_3("2:3", 2f, 3f),
    RATIO_3_4("3:4", 3f, 4f),
    RATIO_21_9("21:9 (Cinema)", 21f, 9f),
    ORIGINAL("Original", 0f, 0f),
    CUSTOM("Custom", 0f, 0f)
}

/**
 * Canvas background fill types.
 */
enum class CanvasBackgroundType { BLUR, COLOR, GRADIENT, CUSTOM_IMAGE }

/**
 * Canvas background and aspect ratio settings.
 */
data class CanvasConfig(
    val aspectRatio: AspectRatio = AspectRatio.RATIO_9_16,
    val backgroundType: CanvasBackgroundType = CanvasBackgroundType.BLUR,
    val blurLevel: Float = 0.5f,
    val colorHex: String = "#000000",
    val gradientStartColorHex: String = "#333333",
    val gradientEndColorHex: String = "#000000",
    val customImageUriString: String? = null
) : Serializable {
    val customImageUri: Uri? get() = customImageUriString?.let { Uri.parse(it) }
}

/**
 * The unified immutable state of the non-linear multi-track timeline.
 */
data class TimelineState(
    val canvas: CanvasConfig = CanvasConfig(),
    val mainTrack: MainVideoTrack = MainVideoTrack(),
    val audioTracks: List<AudioTrack> = emptyList(),
    val overlayTracks: List<OverlayTrack> = emptyList(),
    val playheadMs: Long = 0L
) : Serializable {
    val totalDurationMs: Long
        get() {
            val mainDur = mainTrack.totalDurationMs
            val audioDur = audioTracks.flatMap { it.clips }.maxOfOrNull { it.timelineEndMs } ?: 0L
            val overlayDur = overlayTracks.flatMap { it.overlays }.maxOfOrNull { it.timelineRange.endMs } ?: 0L
            return maxOf(mainDur, audioDur, overlayDur)
        }

    val isEmpty: Boolean get() = mainTrack.clips.isEmpty()
}
