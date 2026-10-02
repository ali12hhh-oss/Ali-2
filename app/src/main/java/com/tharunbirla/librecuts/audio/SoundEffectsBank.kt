package com.tharunbirla.librecuts.audio

/**
 * Categorized royalty-free Sound Effects (SFX) bank.
 */
object SoundEffectsBank {

    data class SoundEffectItem(
        val id: String,
        val title: String,
        val category: String,
        val durationMs: Long,
        val assetPath: String
    )

    fun getCategoryList(): List<String> {
        return listOf("Transitions", "Bells & Chimes", "Impacts", "Laughter & Crowd", "Nature", "Ambience")
    }

    fun getEffectsForCategory(category: String): List<SoundEffectItem> {
        return when (category.lowercase()) {
            "transitions" -> listOf(
                SoundEffectItem("sfx_whoosh_1", "Fast Whoosh", "Transitions", 800L, "sfx/transitions/whoosh_fast.mp3"),
                SoundEffectItem("sfx_whoosh_2", "Cinematic Swish", "Transitions", 1200L, "sfx/transitions/swish_cinematic.mp3"),
                SoundEffectItem("sfx_glitch_1", "Digital Glitch", "Transitions", 600L, "sfx/transitions/glitch_digital.mp3")
            )
            "impacts" -> listOf(
                SoundEffectItem("sfx_cinematic_boom", "Deep Cinematic Boom", "Impacts", 2500L, "sfx/impacts/boom_deep.mp3"),
                SoundEffectItem("sfx_punch", "Punch Impact", "Impacts", 500L, "sfx/impacts/punch.mp3")
            )
            "bells & chimes" -> listOf(
                SoundEffectItem("sfx_ding", "Notification Ding", "Bells & Chimes", 1000L, "sfx/bells/ding_bell.mp3"),
                SoundEffectItem("sfx_success_chime", "Success Chime", "Bells & Chimes", 1800L, "sfx/bells/chime_success.mp3")
            )
            else -> listOf(
                SoundEffectItem("sfx_camera_shutter", "Camera Click", "General", 400L, "sfx/general/camera_click.mp3"),
                SoundEffectItem("sfx_pop", "Bubble Pop", "General", 300L, "sfx/general/pop.mp3")
            )
        }
    }
}
