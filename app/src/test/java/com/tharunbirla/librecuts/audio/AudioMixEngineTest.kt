package com.tharunbirla.librecuts.audio

import com.tharunbirla.librecuts.timeline.AudioClip
import com.tharunbirla.librecuts.timeline.AudioTrack
import com.tharunbirla.librecuts.timeline.TimeRange
import org.junit.Assert.*
import org.junit.Test

class AudioMixEngineTest {

    @Test
    fun testFadeInAndFadeOutGain() {
        val durationMs = 10000L
        val fadeInMs = 2000L
        val fadeOutMs = 2000L

        // At 0ms -> 0.0 gain
        assertEquals(0f, AudioMixEngine.calculateFadeGain(0L, durationMs, fadeInMs, fadeOutMs), 0.001f)

        // At 1000ms (half of fade-in) -> 0.5 gain
        assertEquals(0.5f, AudioMixEngine.calculateFadeGain(1000L, durationMs, fadeInMs, fadeOutMs), 0.001f)

        // At 5000ms (middle sustained) -> 1.0 gain
        assertEquals(1.0f, AudioMixEngine.calculateFadeGain(5000L, durationMs, fadeInMs, fadeOutMs), 0.001f)

        // At 9000ms (half of fade-out) -> 0.5 gain
        assertEquals(0.5f, AudioMixEngine.calculateFadeGain(9000L, durationMs, fadeInMs, fadeOutMs), 0.001f)

        // At 10000ms (end) -> 0.0 gain
        assertEquals(0f, AudioMixEngine.calculateFadeGain(10000L, durationMs, fadeInMs, fadeOutMs), 0.001f)
    }

    @Test
    fun testAudioDuckingAttenuation() {
        val clip = AudioClip(
            audioUriString = "file:///bgm.mp3",
            sourceRange = TimeRange(0L, 10000L),
            timelineStartMs = 0L,
            volume = 1.0f,
            isDucking = true
        )
        val track = AudioTrack(clips = listOf(clip))

        // Normal playback -> 1.0 volume
        val normalVol = AudioMixEngine.computeEffectiveVolume(clip, track, 5000L, isPrimaryVoiceActive = false)
        assertEquals(1.0f, normalVol, 0.001f)

        // Voice active -> ducked to 0.3x
        val duckedVol = AudioMixEngine.computeEffectiveVolume(clip, track, 5000L, isPrimaryVoiceActive = true)
        assertEquals(0.3f, duckedVol, 0.001f)
    }
}
