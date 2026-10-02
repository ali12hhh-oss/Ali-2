package com.tharunbirla.librecuts.timeline.ui

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EditingToolbarControllerTest {

    private lateinit var controller: EditingToolbarController

    @Before
    fun setUp() {
        controller = EditingToolbarController()
    }

    @Test
    fun testInitialRootModeActions() {
        assertEquals(ToolbarMode.ROOT, controller.currentMode)
        val actions = controller.getActionsForMode()
        assertTrue(actions.any { it.id == "canvas" })
        assertTrue(actions.any { it.id == "music" })
        assertTrue(actions.any { it.id == "text" })
        assertTrue(actions.any { it.id == "pip" })
    }

    @Test
    fun testSwitchToClipSelectedMode() {
        var modeChangeEmitted: ToolbarMode? = null
        controller.onModeChanged = { modeChangeEmitted = it }

        controller.setMode(ToolbarMode.CLIP_SELECTED)
        assertEquals(ToolbarMode.CLIP_SELECTED, controller.currentMode)
        assertEquals(ToolbarMode.CLIP_SELECTED, modeChangeEmitted)

        val clipActions = controller.getActionsForMode()
        assertTrue(clipActions.any { it.id == "split" })
        assertTrue(clipActions.any { it.id == "speed" })
        assertTrue(clipActions.any { it.id == "freeze" })
        assertTrue(clipActions.any { it.id == "delete" })
    }

    @Test
    fun testSwitchToAudioSelectedMode() {
        controller.setMode(ToolbarMode.AUDIO_SELECTED)
        val audioActions = controller.getActionsForMode()
        assertTrue(audioActions.any { it.id == "audio_fade" })
        assertTrue(audioActions.any { it.id == "audio_beat" })
        assertTrue(audioActions.any { it.id == "audio_ducking" })
    }
}
