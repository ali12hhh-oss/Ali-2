package com.tharunbirla.librecuts.audio

/**
 * Resamples raw audio amplitudes into a normalized fixed-length array suitable for smooth UI waveform display.
 */
object AudioWaveformGenerator {

    /**
     * Resamples an arbitrary array of audio peak amplitudes into targetBarCount samples (0.0 to 1.0).
     */
    fun resampleAmplitudes(rawAmplitudes: FloatArray, targetBarCount: Int = 100): FloatArray {
        if (rawAmplitudes.isEmpty() || targetBarCount <= 0) {
            return FloatArray(targetBarCount) { 0.1f }
        }

        val result = FloatArray(targetBarCount)
        val step = rawAmplitudes.size.toFloat() / targetBarCount.toFloat()

        for (i in 0 until targetBarCount) {
            val startIdx = (i * step).toInt().coerceIn(0, rawAmplitudes.lastIndex)
            val endIdx = ((i + 1) * step).toInt().coerceIn(startIdx + 1, rawAmplitudes.size)

            var peak = 0f
            for (j in startIdx until endIdx) {
                if (rawAmplitudes[j] > peak) {
                    peak = rawAmplitudes[j]
                }
            }
            result[i] = peak.coerceIn(0.05f, 1.0f)
        }

        return result
    }
}
