package com.tharunbirla.librecuts.timeline.ui

import com.tharunbirla.librecuts.timeline.ColorGradeConfig

/**
 * Controller for the 12-channel color adjustments and LUT preset selection suite.
 */
class ColorGradeSheetController(
    initialConfig: ColorGradeConfig = ColorGradeConfig()
) {
    var currentConfig: ColorGradeConfig = initialConfig
        private set

    var onConfigChanged: ((ColorGradeConfig) -> Unit)? = null

    /**
     * Preset aesthetic LUT filters with calibrated parameters.
     */
    enum class LutPreset(val displayName: String, val lutName: String) {
        ORIGINAL("Original", ""),
        CINEMA("Cinema", "cinema_teal_orange"),
        VINTAGE("Vintage", "vintage_warm"),
        WARM("Warm Sunlight", "warm_golden"),
        COLD("Cold Nordic", "cool_arctic"),
        BW("B&W Classic", "noir_monochrome"),
        CYBERPUNK("Cyberpunk", "neon_cyberpunk"),
        RETRO("Retro 90s", "vhs_retro"),
        MOODY("Moody Dark", "moody_shadows"),
        PASTEL("Pastel Soft", "soft_pastel")
    }

    fun setLutFilter(preset: LutPreset, intensity: Float = 1.0f) {
        currentConfig = currentConfig.copy(
            filterLutName = if (preset == LutPreset.ORIGINAL) null else preset.lutName,
            filterIntensity = intensity.coerceIn(0f, 1f)
        )
        onConfigChanged?.invoke(currentConfig)
    }

    fun updateAdjustment(channel: String, value: Float) {
        currentConfig = when (channel.lowercase()) {
            "exposure", "brightness" -> currentConfig.copy(brightness = value.coerceIn(-1f, 1f))
            "contrast" -> currentConfig.copy(contrast = value.coerceIn(0f, 2f))
            "saturation" -> currentConfig.copy(saturation = value.coerceIn(0f, 2f))
            "warmth" -> currentConfig.copy(warmth = value.coerceIn(-1f, 1f))
            "tint" -> currentConfig.copy(tint = value.coerceIn(-1f, 1f))
            "highlights" -> currentConfig.copy(highlights = value.coerceIn(-1f, 1f))
            "shadows" -> currentConfig.copy(shadows = value.coerceIn(-1f, 1f))
            "vibrance" -> currentConfig.copy(vibrance = value.coerceIn(-1f, 1f))
            "sharpness" -> currentConfig.copy(sharpness = value.coerceIn(0f, 1f))
            "vignette" -> currentConfig.copy(vignette = value.coerceIn(0f, 1f))
            "fade" -> currentConfig.copy(fade = value.coerceIn(0f, 1f))
            "grain" -> currentConfig.copy(grain = value.coerceIn(0f, 1f))
            else -> currentConfig
        }
        onConfigChanged?.invoke(currentConfig)
    }

    fun resetAdjustments() {
        currentConfig = ColorGradeConfig()
        onConfigChanged?.invoke(currentConfig)
    }
}
