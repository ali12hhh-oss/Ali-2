package com.tharunbirla.librecuts.timeline

/**
 * Pure functional mathematical engine for timeline manipulation and ripple editing.
 * Guarantees zero side effects and complete state immutability.
 */
object TimelineMathEngine {

    /**
     * Splits a video clip at the given global timeline position into two distinct clips.
     */
    fun splitClip(state: TimelineState, clipIndex: Int, splitTimelinePositionMs: Long): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        val targetClip = clips[clipIndex]
        requireEditableRange(targetClip)
        val clipStartTimelineMs = mainTrack.getClipTimelineStartMs(clipIndex)
        val relativeTimelineOffsetMs = splitTimelinePositionMs - clipStartTimelineMs

        // If split is at boundaries, cannot split
        if (relativeTimelineOffsetMs <= 0L || relativeTimelineOffsetMs >= targetClip.timelineDurationMs) {
            return state
        }

        // Account for playback speed
        val relativeSourceOffsetMs = (relativeTimelineOffsetMs * targetClip.speed).toLong()
        val splitSourceTimeMs = targetClip.sourceRange.startMs + relativeSourceOffsetMs

        if (splitSourceTimeMs <= targetClip.sourceRange.startMs || splitSourceTimeMs >= targetClip.sourceRange.endMs) {
            return state
        }

        val leftClip = targetClip.copy(
            id = "${targetClip.id}_left_${System.nanoTime()}",
            sourceRange = TimeRange(targetClip.sourceRange.startMs, splitSourceTimeMs)
        )

        val rightClip = targetClip.copy(
            id = "${targetClip.id}_right_${System.nanoTime()}",
            sourceRange = TimeRange(splitSourceTimeMs, targetClip.sourceRange.endMs)
        )

        val newClips = clips.toMutableList().apply {
            set(clipIndex, leftClip)
            add(clipIndex + 1, rightClip)
        }

        // Re-index transitions: shifts transitions at >= clipIndex + 1 by +1
        val newTransitions = mutableMapOf<Int, TransitionEffect>()
        for ((idx, transition) in mainTrack.transitions) {
            when {
                idx < clipIndex -> newTransitions[idx] = transition
                idx == clipIndex -> newTransitions[idx + 1] = transition // Shift outgoing transition
                idx > clipIndex -> newTransitions[idx + 1] = transition
            }
        }

        val newMainTrack = mainTrack.copy(clips = newClips, transitions = newTransitions)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Deletes a clip from the primary track and ripples all subsequent clips to the left.
     */
    fun rippleDeleteClip(state: TimelineState, clipIndex: Int): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        requireEditableRange(clips[clipIndex])

        val newClips = clips.toMutableList().apply {
            removeAt(clipIndex)
        }

        val newTransitions = mutableMapOf<Int, TransitionEffect>()
        for ((idx, transition) in mainTrack.transitions) {
            when {
                idx < clipIndex -> newTransitions[idx] = transition
                idx == clipIndex -> { /* Transition removed with clip */ }
                idx > clipIndex -> newTransitions[idx - 1] = transition
            }
        }

        val newMainTrack = mainTrack.copy(clips = newClips, transitions = newTransitions)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Trims a clip's in/out points and ripples subsequent clips.
     */
    fun trimClip(state: TimelineState, clipIndex: Int, newStartSourceMs: Long, newEndSourceMs: Long): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        val targetClip = clips[clipIndex]
        requireEditableRange(targetClip)

        // Keep the result inside the source and non-empty, even if a drag handle
        // crosses the other handle or the caller supplies a negative value.
        val sourceDurationMs = targetClip.source.naturalDurationMs
        val minimumDurationMs = minOf(MINIMUM_CLIP_DURATION_MS, sourceDurationMs)
        val clampedStart = newStartSourceMs.coerceIn(0L, sourceDurationMs - minimumDurationMs)
        val requestedEnd = newEndSourceMs.coerceIn(0L, sourceDurationMs)
        val clampedEnd = maxOf(requestedEnd, clampedStart + minimumDurationMs)

        val trimmedClip = targetClip.copy(
            sourceRange = TimeRange(clampedStart, clampedEnd)
        )

        val newClips = clips.toMutableList().apply {
            set(clipIndex, trimmedClip)
        }

        val newMainTrack = mainTrack.copy(clips = newClips)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Duplicates a clip and inserts it immediately after the target clip.
     */
    fun duplicateClip(state: TimelineState, clipIndex: Int): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        val targetClip = clips[clipIndex]
        requireEditableRange(targetClip)
        val duplicatedClip = targetClip.copy(
            id = "${targetClip.id}_dup_${System.nanoTime()}"
        )

        val newClips = clips.toMutableList().apply {
            add(clipIndex + 1, duplicatedClip)
        }

        val newTransitions = mutableMapOf<Int, TransitionEffect>()
        for ((idx, transition) in mainTrack.transitions) {
            when {
                idx <= clipIndex -> newTransitions[idx] = transition
                idx > clipIndex -> newTransitions[idx + 1] = transition
            }
        }

        val newMainTrack = mainTrack.copy(clips = newClips, transitions = newTransitions)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Inserts a static freeze frame for a specified duration at the given position.
     */
    fun freezeFrame(
        state: TimelineState,
        clipIndex: Int,
        freezeTimelinePositionMs: Long,
        freezeDurationMs: Long = 3000L
    ): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        val targetClip = clips[clipIndex]
        requireEditableRange(targetClip)
        val clipStartTimelineMs = mainTrack.getClipTimelineStartMs(clipIndex)
        val relativeTimelineOffsetMs = freezeTimelinePositionMs - clipStartTimelineMs

        val normalizedFreezeDurationMs = freezeDurationMs.coerceAtLeast(1L)
        val freezeSource = MediaSource(
            uriString = targetClip.source.uriString,
            name = "Freeze Frame: ${targetClip.source.name}",
            naturalDurationMs = normalizedFreezeDurationMs,
            width = targetClip.source.width,
            height = targetClip.source.height,
            isImage = true
        )

        val freezeClip = VideoClip(
            id = "freeze_${System.nanoTime()}",
            source = freezeSource,
            sourceRange = TimeRange(0L, normalizedFreezeDurationMs),
            isFreezeFrame = true
        )

        // If freeze is at start boundary
        if (relativeTimelineOffsetMs <= 0L) {
            val newClips = clips.toMutableList().apply { add(clipIndex, freezeClip) }
            return state.copy(mainTrack = mainTrack.copy(clips = newClips))
        }

        // If freeze is at end boundary
        if (relativeTimelineOffsetMs >= targetClip.timelineDurationMs) {
            val newClips = clips.toMutableList().apply { add(clipIndex + 1, freezeClip) }
            return state.copy(mainTrack = mainTrack.copy(clips = newClips))
        }

        // Split first, then insert freeze frame between left and right
        val stateAfterSplit = splitClip(state, clipIndex, freezeTimelinePositionMs)
        val splitClips = stateAfterSplit.mainTrack.clips.toMutableList()
        splitClips.add(clipIndex + 1, freezeClip)

        return stateAfterSplit.copy(mainTrack = stateAfterSplit.mainTrack.copy(clips = splitClips))
    }

    /**
     * Changes clip playback speed and recalculates timeline duration.
     */
    fun changeSpeed(state: TimelineState, clipIndex: Int, newSpeed: Float): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(clipIndex in clips.indices) { "Clip index $clipIndex out of range (0..${clips.lastIndex})" }

        val targetClip = clips[clipIndex]
        requireEditableRange(targetClip)
        val normalizedSpeed = if (newSpeed.isFinite()) newSpeed.coerceIn(0.1f, 100f) else 1.0f
        val speedClip = targetClip.copy(speed = normalizedSpeed)

        val newClips = clips.toMutableList().apply {
            set(clipIndex, speedClip)
        }

        val newMainTrack = mainTrack.copy(clips = newClips)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Reorders clips from fromIndex to toIndex.
     */
    fun reorderClips(state: TimelineState, fromIndex: Int, toIndex: Int): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        require(fromIndex in clips.indices && toIndex in clips.indices) { "Invalid indices: $fromIndex, $toIndex" }
        if (fromIndex == toIndex) return state

        val newClips = clips.toMutableList()
        val item = newClips.removeAt(fromIndex)
        newClips.add(toIndex, item)

        // Clear transitions on reorder to prevent mismatched clip transitions
        val newMainTrack = mainTrack.copy(clips = newClips, transitions = emptyMap())
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Inserts a new video/photo clip into the primary track.
     */
    fun insertClip(state: TimelineState, index: Int, clip: VideoClip): TimelineState {
        val mainTrack = state.mainTrack
        val clips = mainTrack.clips
        requireEditableRange(clip)
        val insertIndex = index.coerceIn(0, clips.size)

        val newClips = clips.toMutableList().apply {
            add(insertIndex, clip)
        }

        val newMainTrack = mainTrack.copy(clips = newClips)
        return state.copy(mainTrack = newMainTrack)
    }

    /**
     * Sets or updates a transition effect after the clip at clipIndex.
     */
    fun setTransition(state: TimelineState, clipIndex: Int, transition: TransitionEffect): TimelineState {
        val mainTrack = state.mainTrack
        require(clipIndex in 0 until mainTrack.clipCount - 1) {
            "Transitions can only be set between adjacent clips (0 until ${mainTrack.clipCount - 1})"
        }

        val newTransitions = mainTrack.transitions.toMutableMap().apply {
            if (transition.type == TransitionType.NONE) {
                remove(clipIndex)
            } else {
                put(clipIndex, transition)
            }
        }

        return state.copy(mainTrack = mainTrack.copy(transitions = newTransitions))
    }

    /**
     * Timeline edits must never manufacture or carry forward a clip that cannot
     * occupy timeline space. The model permits an empty [TimeRange] for generic
     * uses, so enforce the stronger video-clip invariant at this editing seam.
     */
    private fun requireEditableRange(clip: VideoClip) {
        require(clip.source.naturalDurationMs > 0L) {
            "Video source duration must be positive: ${clip.source.naturalDurationMs}"
        }
        require(clip.sourceRange.durationMs > 0L) { "Video clip source range must not be empty" }
        require(clip.sourceRange.endMs <= clip.source.naturalDurationMs) {
            "Video clip source range must lie within its source duration"
        }
    }

    private const val MINIMUM_CLIP_DURATION_MS = 10L
}
