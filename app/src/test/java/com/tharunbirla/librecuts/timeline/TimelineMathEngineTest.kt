package com.tharunbirla.librecuts.timeline

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimelineMathEngineTest {

    private lateinit var sampleSource1: MediaSource
    private lateinit var sampleSource2: MediaSource
    private lateinit var sampleClip1: VideoClip
    private lateinit var sampleClip2: VideoClip
    private lateinit var initialState: TimelineState

    @Before
    fun setUp() {
        sampleSource1 = MediaSource(
            uriString = "file:///dummy/video1.mp4",
            name = "video1.mp4",
            naturalDurationMs = 10000L
        )
        sampleSource2 = MediaSource(
            uriString = "file:///dummy/video2.mp4",
            name = "video2.mp4",
            naturalDurationMs = 8000L
        )
        sampleClip1 = VideoClip(
            id = "clip_1",
            source = sampleSource1,
            sourceRange = TimeRange(0L, 10000L)
        )
        sampleClip2 = VideoClip(
            id = "clip_2",
            source = sampleSource2,
            sourceRange = TimeRange(0L, 8000L)
        )
        initialState = TimelineState(
            mainTrack = MainVideoTrack(
                clips = listOf(sampleClip1, sampleClip2)
            )
        )
    }

    @Test
    fun testTotalDurationCalculation() {
        assertEquals(18000L, initialState.totalDurationMs)
    }

    @Test
    fun testSplitClipAtPlayhead() {
        // Split first clip at 4000ms
        val stateAfterSplit = TimelineMathEngine.splitClip(initialState, 0, 4000L)
        assertEquals(3, stateAfterSplit.mainTrack.clips.size)

        val leftClip = stateAfterSplit.mainTrack.clips[0]
        val rightClip = stateAfterSplit.mainTrack.clips[1]
        val untouchedClip = stateAfterSplit.mainTrack.clips[2]

        assertEquals(4000L, leftClip.timelineDurationMs)
        assertEquals(0L, leftClip.sourceRange.startMs)
        assertEquals(4000L, leftClip.sourceRange.endMs)

        assertEquals(6000L, rightClip.timelineDurationMs)
        assertEquals(4000L, rightClip.sourceRange.startMs)
        assertEquals(10000L, rightClip.sourceRange.endMs)

        assertEquals(sampleClip2.id, untouchedClip.id)
        assertEquals(18000L, stateAfterSplit.totalDurationMs)
    }

    @Test
    fun testSplitClipWithSpeed() {
        // Double speed clip (5000ms timeline duration for 10000ms natural)
        val speedClip = sampleClip1.copy(speed = 2.0f)
        val state = TimelineState(mainTrack = MainVideoTrack(clips = listOf(speedClip)))

        // Split at 2500ms on timeline
        val stateAfterSplit = TimelineMathEngine.splitClip(state, 0, 2500L)
        assertEquals(2, stateAfterSplit.mainTrack.clips.size)

        val left = stateAfterSplit.mainTrack.clips[0]
        val right = stateAfterSplit.mainTrack.clips[1]

        assertEquals(0L, left.sourceRange.startMs)
        assertEquals(5000L, left.sourceRange.endMs)
        assertEquals(2500L, left.timelineDurationMs)

        assertEquals(5000L, right.sourceRange.startMs)
        assertEquals(10000L, right.sourceRange.endMs)
        assertEquals(2500L, right.timelineDurationMs)
    }

    @Test
    fun testRippleDeleteClip() {
        val stateAfterDelete = TimelineMathEngine.rippleDeleteClip(initialState, 0)
        assertEquals(1, stateAfterDelete.mainTrack.clips.size)
        assertEquals(sampleClip2.id, stateAfterDelete.mainTrack.clips[0].id)
        assertEquals(8000L, stateAfterDelete.totalDurationMs)
    }

    @Test
    fun testTrimClipInAndOut() {
        // Trim clip 1 to 2000ms..7000ms
        val stateAfterTrim = TimelineMathEngine.trimClip(initialState, 0, 2000L, 7000L)
        assertEquals(5000L, stateAfterTrim.mainTrack.clips[0].timelineDurationMs)
        assertEquals(13000L, stateAfterTrim.totalDurationMs)
    }

    @Test
    fun trimNormalizesNegativeAndCrossedHandlesToANonEmptyRange() {
        val trimmed = TimelineMathEngine.trimClip(initialState, 0, -50L, -1L)

        assertEquals(0L, trimmed.mainTrack.clips[0].sourceRange.startMs)
        assertEquals(10L, trimmed.mainTrack.clips[0].sourceRange.endMs)
        assertTrue(trimmed.mainTrack.clips[0].sourceRange.durationMs > 0L)

        val crossed = TimelineMathEngine.trimClip(initialState, 0, 9_999L, 1_000L)
        assertEquals(9_990L, crossed.mainTrack.clips[0].sourceRange.startMs)
        assertEquals(10_000L, crossed.mainTrack.clips[0].sourceRange.endMs)
    }

    @Test
    fun trimShortSourceDoesNotThrowOrCreateAnEmptyRange() {
        val shortSource = sampleSource1.copy(naturalDurationMs = 5L)
        val shortClip = sampleClip1.copy(source = shortSource, sourceRange = TimeRange(0L, 5L))
        val state = TimelineState(mainTrack = MainVideoTrack(clips = listOf(shortClip)))

        val trimmed = TimelineMathEngine.trimClip(state, 0, 4L, 0L)

        assertEquals(TimeRange(0L, 5L), trimmed.mainTrack.clips.single().sourceRange)
    }

    @Test(expected = IllegalArgumentException::class)
    fun insertRejectsAnEmptyVideoClipRange() {
        TimelineMathEngine.insertClip(
            initialState,
            1,
            sampleClip1.copy(sourceRange = TimeRange(1_000L, 1_000L))
        )
    }

    @Test
    fun splitAtOrOutsideClipBoundariesIsANoOp() {
        assertSame(initialState, TimelineMathEngine.splitClip(initialState, 0, -1L))
        assertSame(initialState, TimelineMathEngine.splitClip(initialState, 0, 0L))
        assertSame(initialState, TimelineMathEngine.splitClip(initialState, 0, 10_000L))
        assertSame(initialState, TimelineMathEngine.splitClip(initialState, 0, 10_001L))
    }

    @Test
    fun testDuplicateClip() {
        val stateAfterDup = TimelineMathEngine.duplicateClip(initialState, 0)
        assertEquals(3, stateAfterDup.mainTrack.clips.size)
        assertEquals(sampleClip1.source.name, stateAfterDup.mainTrack.clips[1].source.name)
        assertNotEquals(sampleClip1.id, stateAfterDup.mainTrack.clips[1].id)
        assertEquals(28000L, stateAfterDup.totalDurationMs)
    }

    @Test
    fun testFreezeFrameInsertion() {
        // Freeze at 5000ms for 3000ms
        val stateAfterFreeze = TimelineMathEngine.freezeFrame(initialState, 0, 5000L, 3000L)
        assertEquals(4, stateAfterFreeze.mainTrack.clips.size)

        val freezeClip = stateAfterFreeze.mainTrack.clips[1]
        assertTrue(freezeClip.isFreezeFrame)
        assertTrue(freezeClip.source.isImage)
        assertEquals(3000L, freezeClip.timelineDurationMs)
        assertEquals(21000L, stateAfterFreeze.totalDurationMs)
    }

    @Test
    fun testReorderClips() {
        val stateAfterReorder = TimelineMathEngine.reorderClips(initialState, 0, 1)
        assertEquals(sampleClip2.id, stateAfterReorder.mainTrack.clips[0].id)
        assertEquals(sampleClip1.id, stateAfterReorder.mainTrack.clips[1].id)
    }

    @Test
    fun reorderMovesClipAndClearsTransitionsThatWouldNoLongerMatch() {
        val transition = TransitionEffect(type = TransitionType.DISSOLVE)
        val state = initialState.copy(mainTrack = initialState.mainTrack.copy(transitions = mapOf(0 to transition)))

        val moved = TimelineMathEngine.reorderClips(state, 1, 0)

        assertEquals(listOf("clip_2", "clip_1"), moved.mainTrack.clips.map { it.id })
        assertTrue(moved.mainTrack.transitions.isEmpty())
    }

    @Test
    fun testChangeSpeed() {
        val stateAfterSpeed = TimelineMathEngine.changeSpeed(initialState, 0, 0.5f) // 0.5x speed -> 20000ms
        assertEquals(20000L, stateAfterSpeed.mainTrack.clips[0].timelineDurationMs)
        assertEquals(28000L, stateAfterSpeed.totalDurationMs)
    }
}
