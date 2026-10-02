package com.tharunbirla.librecuts.timeline

/**
 * Reversible command for non-linear timeline editing.
 * Captures previous state snapshots on execution to ensure 100% deterministic undo.
 */
interface TimelineCommand {
    val name: String
    fun execute(currentState: TimelineState): TimelineState
    fun undo(currentState: TimelineState): TimelineState
}

/**
 * Command to split a clip at a specific timeline position.
 */
class SplitClipCommand(
    private val clipIndex: Int,
    private val splitPositionMs: Long
) : TimelineCommand {
    override val name: String = "Split Clip"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.splitClip(currentState, clipIndex, splitPositionMs)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to ripple-delete a clip.
 */
class RippleDeleteClipCommand(
    private val clipIndex: Int
) : TimelineCommand {
    override val name: String = "Delete Clip"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.rippleDeleteClip(currentState, clipIndex)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to trim a clip's boundaries.
 */
class TrimClipCommand(
    private val clipIndex: Int,
    private val newStartSourceMs: Long,
    private val newEndSourceMs: Long
) : TimelineCommand {
    override val name: String = "Trim Clip"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.trimClip(currentState, clipIndex, newStartSourceMs, newEndSourceMs)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to duplicate a clip.
 */
class DuplicateClipCommand(
    private val clipIndex: Int
) : TimelineCommand {
    override val name: String = "Duplicate Clip"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.duplicateClip(currentState, clipIndex)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to insert a freeze frame.
 */
class FreezeFrameCommand(
    private val clipIndex: Int,
    private val freezePositionMs: Long,
    private val freezeDurationMs: Long = 3000L
) : TimelineCommand {
    override val name: String = "Freeze Frame"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.freezeFrame(currentState, clipIndex, freezePositionMs, freezeDurationMs)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to change clip playback speed.
 */
class ChangeSpeedCommand(
    private val clipIndex: Int,
    private val newSpeed: Float
) : TimelineCommand {
    override val name: String = "Change Speed"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.changeSpeed(currentState, clipIndex, newSpeed)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to reorder clips on the main track.
 */
class ReorderClipsCommand(
    private val fromIndex: Int,
    private val toIndex: Int
) : TimelineCommand {
    override val name: String = "Reorder Clips"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.reorderClips(currentState, fromIndex, toIndex)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to set or update transitions.
 */
class SetTransitionCommand(
    private val clipIndex: Int,
    private val transition: TransitionEffect
) : TimelineCommand {
    override val name: String = "Set Transition"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return TimelineMathEngine.setTransition(currentState, clipIndex, transition)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to add an audio clip.
 */
class AddAudioClipCommand(
    private val audioClip: AudioClip,
    private val trackIndex: Int = 0
) : TimelineCommand {
    override val name: String = "Add Audio"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        val tracks = currentState.audioTracks.toMutableList()
        if (tracks.isEmpty()) {
            tracks.add(AudioTrack(clips = listOf(audioClip)))
        } else {
            val targetTrack = tracks.getOrElse(trackIndex) { tracks.first() }
            val updatedClips = targetTrack.clips + audioClip
            tracks[trackIndex.coerceIn(0, tracks.lastIndex)] = targetTrack.copy(clips = updatedClips)
        }
        return currentState.copy(audioTracks = tracks)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to add an overlay clip (PIP, Text, Sticker).
 */
class AddOverlayClipCommand(
    private val overlayClip: OverlayClip,
    private val trackIndex: Int = 0
) : TimelineCommand {
    override val name: String = "Add Overlay"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        val tracks = currentState.overlayTracks.toMutableList()
        if (tracks.isEmpty()) {
            tracks.add(OverlayTrack(overlays = listOf(overlayClip)))
        } else {
            val targetTrack = tracks.getOrElse(trackIndex) { tracks.first() }
            val updatedOverlays = targetTrack.overlays + overlayClip
            tracks[trackIndex.coerceIn(0, tracks.lastIndex)] = targetTrack.copy(overlays = updatedOverlays)
        }
        return currentState.copy(overlayTracks = tracks)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}

/**
 * Command to update canvas aspect ratio and background.
 */
class UpdateCanvasCommand(
    private val newCanvasConfig: CanvasConfig
) : TimelineCommand {
    override val name: String = "Update Canvas"
    private var previousState: TimelineState? = null

    override fun execute(currentState: TimelineState): TimelineState {
        previousState = currentState
        return currentState.copy(canvas = newCanvasConfig)
    }

    override fun undo(currentState: TimelineState): TimelineState {
        return previousState ?: currentState
    }
}
