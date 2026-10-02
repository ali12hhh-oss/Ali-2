package com.tharunbirla.librecuts.timeline.ui

/**
 * Modes for the contextual two-tier editing toolbar.
 */
enum class ToolbarMode {
    ROOT,               // Primary editor mode (Canvas, Music, Text, Sticker, PIP, Filter, Adjust, Crop)
    CLIP_SELECTED,      // Video clip selected (Split, Speed, Volume, Crop, Filter, Adjust, Mask, Chroma, Reverse, Freeze, Duplicate, Delete)
    AUDIO_SELECTED,     // Audio track selected (Volume, Fade, Split, Beat, Ducking, Delete)
    OVERLAY_SELECTED,   // Text / PIP overlay selected (Edit, Style, Animation, Opacity, Split, Duplicate, Delete)
    CANVAS_MODE,        // Canvas ratio & background mode
    COLOR_FILTER_MODE   // LUT filters and 12-channel adjustments
}

/**
 * A discrete actionable item displayed on the bottom toolbar.
 */
data class ToolbarAction(
    val id: String,
    val title: String,
    val isEnabled: Boolean = true
)

/**
 * Controller orchestrating the InShot/CapCut style two-tier contextual editing toolbars.
 */
class EditingToolbarController(
    initialMode: ToolbarMode = ToolbarMode.ROOT
) {
    var currentMode: ToolbarMode = initialMode
        private set

    var onActionClicked: ((ToolbarAction) -> Unit)? = null
    var onModeChanged: ((ToolbarMode) -> Unit)? = null

    fun setMode(newMode: ToolbarMode) {
        if (currentMode != newMode) {
            currentMode = newMode
            onModeChanged?.invoke(newMode)
        }
    }

    /**
     * Returns the complete contextual action list for the currently active mode.
     */
    fun getActionsForMode(mode: ToolbarMode = currentMode): List<ToolbarAction> {
        return when (mode) {
            ToolbarMode.ROOT -> listOf(
                ToolbarAction("canvas", "Canvas"),
                ToolbarAction("music", "Music"),
                ToolbarAction("text", "Text"),
                ToolbarAction("sticker", "Sticker"),
                ToolbarAction("pip", "PIP"),
                ToolbarAction("filter", "Filter"),
                ToolbarAction("adjust", "Adjust"),
                ToolbarAction("crop", "Crop"),
                ToolbarAction("background", "Background")
            )
            ToolbarMode.CLIP_SELECTED -> listOf(
                ToolbarAction("split", "Split"),
                ToolbarAction("speed", "Speed"),
                ToolbarAction("volume", "Volume"),
                ToolbarAction("crop", "Crop"),
                ToolbarAction("filter", "Filter"),
                ToolbarAction("adjust", "Adjust"),
                ToolbarAction("mask", "Mask"),
                ToolbarAction("chroma", "Chroma Key"),
                ToolbarAction("reverse", "Reverse"),
                ToolbarAction("freeze", "Freeze"),
                ToolbarAction("duplicate", "Duplicate"),
                ToolbarAction("replace", "Replace"),
                ToolbarAction("delete", "Delete")
            )
            ToolbarMode.AUDIO_SELECTED -> listOf(
                ToolbarAction("audio_volume", "Volume"),
                ToolbarAction("audio_fade", "Fade"),
                ToolbarAction("audio_split", "Split"),
                ToolbarAction("audio_beat", "Beat Detect"),
                ToolbarAction("audio_ducking", "Ducking"),
                ToolbarAction("audio_delete", "Delete")
            )
            ToolbarMode.OVERLAY_SELECTED -> listOf(
                ToolbarAction("overlay_edit", "Edit"),
                ToolbarAction("overlay_style", "Style"),
                ToolbarAction("overlay_anim", "Animation"),
                ToolbarAction("overlay_opacity", "Opacity"),
                ToolbarAction("overlay_split", "Split"),
                ToolbarAction("overlay_duplicate", "Duplicate"),
                ToolbarAction("overlay_delete", "Delete")
            )
            ToolbarMode.CANVAS_MODE -> listOf(
                ToolbarAction("ratio_9_16", "9:16"),
                ToolbarAction("ratio_16_9", "16:9"),
                ToolbarAction("ratio_1_1", "1:1"),
                ToolbarAction("ratio_4_5", "4:5"),
                ToolbarAction("ratio_custom", "Custom"),
                ToolbarAction("bg_blur", "Blur"),
                ToolbarAction("bg_color", "Color"),
                ToolbarAction("bg_gradient", "Gradient")
            )
            ToolbarMode.COLOR_FILTER_MODE -> listOf(
                ToolbarAction("lut_original", "Original"),
                ToolbarAction("lut_cinema", "Cinema"),
                ToolbarAction("lut_vintage", "Vintage"),
                ToolbarAction("lut_warm", "Warm"),
                ToolbarAction("lut_cold", "Cold"),
                ToolbarAction("lut_bw", "B&W"),
                ToolbarAction("lut_cyberpunk", "Cyberpunk"),
                ToolbarAction("adjust_exposure", "Exposure"),
                ToolbarAction("adjust_contrast", "Contrast"),
                ToolbarAction("adjust_saturation", "Saturation")
            )
        }
    }
}
