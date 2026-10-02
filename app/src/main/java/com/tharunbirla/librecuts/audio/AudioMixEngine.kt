package com.tharunbirla.librecuts.audio

import com.tharunbirla.librecuts.timeline.AudioClip
import com.tharunbirla.librecuts.timeline.AudioTrack

/**
 * Pure mathematical audio mixing engine for gain control, fade curves, and ducking attenuation.
 */
object AudioMixEngine {

    /**
     * Computes the final effective volume for an audio clip at a specific timeline timestamp.
     */
    fun computeEffectiveVolume(
        clip: AudioClip,
        track: AudioTrack,
        currentTimelineMs: Long,
        isPrimaryVoiceActive: Boolean = false
    ): Float {
        if (track.isMuted) return 0f

        val baseVolume = clip.volume * track.volume
        val fadeGain = calculateFadeGain(
            relativeTimeMs = currentTimelineMs - clip.timelineStartMs,
            clipDurationMs = clip.sourceRange.durationMs,
            fadeInMs = clip.fadeInMs,
            fadeOutMs = clip.fadeOutMs
        )

        val duckingMultiplier = if (clip.isDucking && isPrimaryVoiceActive) 0.3f else 1.0f

        return (baseVolume * fadeGain * duckingMultiplier).coerceIn(0f, 2f)
    }

    /**
     * Calculates linear/smooth fade gain multiplier (0.0 to 1.0) given in/out boundary constraints.
     */
    fun calculateFadeGain(
        relativeTimeMs: Long,
        clipDurationMs: Long,
        fadeInMs: Long,
        fadeOutMs: Long
    ): Float {
        if (relativeTimeMs < 0L || relativeTimeMs > clipDurationMs) return 0f
        if (clipDurationMs <= 0L) return 1f

        var inGain = 1f
        if (fadeInMs > 0L && relativeTimeMs < fadeInMs) {
            inGain = (relativeTimeMs.toFloat() / fadeInMs.toFloat()).coerceIn(0f, 1f)
        }

        var outGain = 1f
        val timeUntilEnd = clipDurationMs - relativeTimeMs
        if (fadeOutMs > 0L && timeUntilEnd < fadeOutMs) {
            outGain = (timeUntilEnd.toFloat() / fadeOutMs.toFloat()).coerceIn(0f, 1f)
        }

        return minOf(inGain, outGain)
    }

    /**
     * Attenuates volume when speech / voiceover is present.
     */
    fun applyDucking(volume: Float, duckingAttenuation: Float = 0.3f): Float {
        return (volume * duckingAttenuation).coerceIn(0f, 2f)
    }
}
