package com.tharunbirla.librecuts.timeline

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TimelineCommandManagerTest {

    private lateinit var sampleClip1: VideoClip
    private lateinit var sampleClip2: VideoClip
    private lateinit var initialState: TimelineState
    private lateinit var commandManager: TimelineCommandManager

    @Before
    fun setUp() {
        val source1 = MediaSource(uriString = "file:///video1.mp4", name = "video1.mp4", naturalDurationMs = 10000L)
        val source2 = MediaSource(uriString = "file:///video2.mp4", name = "video2.mp4", naturalDurationMs = 8000L)
        sampleClip1 = VideoClip(id = "clip_1", source = source1, sourceRange = TimeRange(0L, 10000L))
        sampleClip2 = VideoClip(id = "clip_2", source = source2, sourceRange = TimeRange(0L, 8000L))
        initialState = TimelineState(mainTrack = MainVideoTrack(clips = listOf(sampleClip1, sampleClip2)))
        commandManager = TimelineCommandManager(initialState)
    }

    @Test
    fun testExecuteAndUndoSplit() {
        assertFalse(commandManager.canUndo)
        assertFalse(commandManager.canRedo)

        // Split clip at 4000ms
        commandManager.execute(SplitClipCommand(0, 4000L))
        assertEquals(3, commandManager.currentState.mainTrack.clips.size)
        assertTrue(commandManager.canUndo)
        assertFalse(commandManager.canRedo)
        assertEquals("Split Clip", commandManager.undoActionName)

        // Undo
        val stateAfterUndo = commandManager.undo()
        assertNotNull(stateAfterUndo)
        assertEquals(2, commandManager.currentState.mainTrack.clips.size)
        assertFalse(commandManager.canUndo)
        assertTrue(commandManager.canRedo)
        assertEquals("Split Clip", commandManager.redoActionName)

        // Redo
        val stateAfterRedo = commandManager.redo()
        assertNotNull(stateAfterRedo)
        assertEquals(3, commandManager.currentState.mainTrack.clips.size)
        assertTrue(commandManager.canUndo)
        assertFalse(commandManager.canRedo)
    }

    @Test
    fun testExecuteAndUndoRippleDelete() {
        commandManager.execute(RippleDeleteClipCommand(0))
        assertEquals(1, commandManager.currentState.mainTrack.clips.size)
        assertEquals(sampleClip2.id, commandManager.currentState.mainTrack.clips[0].id)

        commandManager.undo()
        assertEquals(2, commandManager.currentState.mainTrack.clips.size)
        assertEquals(sampleClip1.id, commandManager.currentState.mainTrack.clips[0].id)
    }

    @Test
    fun testExecuteAndUndoSpeedChange() {
        commandManager.execute(ChangeSpeedCommand(0, 2.0f))
        assertEquals(5000L, commandManager.currentState.mainTrack.clips[0].timelineDurationMs)

        commandManager.undo()
        assertEquals(10000L, commandManager.currentState.mainTrack.clips[0].timelineDurationMs)
    }

    @Test
    fun testMultiStepUndoRedoHistory() {
        // Step 1: Duplicate clip 0 (2 -> 3 clips)
        commandManager.execute(DuplicateClipCommand(0))
        assertEquals(3, commandManager.currentState.mainTrack.clips.size)

        // Step 2: Delete clip 1 (3 -> 2 clips)
        commandManager.execute(RippleDeleteClipCommand(1))
        assertEquals(2, commandManager.currentState.mainTrack.clips.size)

        // Step 3: Change speed of clip 0
        commandManager.execute(ChangeSpeedCommand(0, 0.5f))
        assertEquals(20000L, commandManager.currentState.mainTrack.clips[0].timelineDurationMs)

        assertEquals(3, commandManager.historyCount)

        // Undo step 3
        commandManager.undo()
        assertEquals(10000L, commandManager.currentState.mainTrack.clips[0].timelineDurationMs)

        // Undo step 2
        commandManager.undo()
        assertEquals(3, commandManager.currentState.mainTrack.clips.size)

        // Undo step 1
        commandManager.undo()
        assertEquals(2, commandManager.currentState.mainTrack.clips.size)
        assertFalse(commandManager.canUndo)
    }

    @Test
    fun undoRedoRestoresExactStatesAfterTrimAndMove() {
        val afterTrim = commandManager.execute(TrimClipCommand(0, 2_000L, 7_000L))
        val afterMove = commandManager.execute(ReorderClipsCommand(1, 0))

        assertEquals(listOf("clip_2", "clip_1"), afterMove.mainTrack.clips.map { it.id })
        assertEquals(TimeRange(2_000L, 7_000L), afterMove.mainTrack.clips[1].sourceRange)

        assertEquals(afterTrim, commandManager.undo())
        assertEquals(initialState, commandManager.undo())
        assertEquals(afterTrim, commandManager.redo())
        assertEquals(afterMove, commandManager.redo())
    }

    @Test
    fun noOpCommandDoesNotConsumeUndoHistoryOrClearRedoHistory() {
        commandManager.execute(ChangeSpeedCommand(0, 2f))
        val changedState = commandManager.currentState
        commandManager.undo()

        commandManager.execute(SplitClipCommand(0, 0L))

        assertEquals(initialState, commandManager.currentState)
        assertEquals(0, commandManager.historyCount)
        assertTrue(commandManager.canRedo)
        assertEquals(changedState, commandManager.redo())
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveHistoryLimitIsRejected() {
        TimelineCommandManager(initialState, maxHistorySize = 0)
    }
}
