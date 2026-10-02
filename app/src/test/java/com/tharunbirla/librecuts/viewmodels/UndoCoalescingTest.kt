package com.tharunbirla.librecuts.viewmodels

import android.net.Uri
import com.tharunbirla.librecuts.commands.UpdateOperationCommand
import com.tharunbirla.librecuts.models.EditOperation
import com.tharunbirla.librecuts.models.TextPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

/**
 * Verifies that rapid-fire coalesced updates of the same kind collapse into a
 * single undo entry — the regression guard for the "undo explosion" bug where
 * slider drags pushed ~60 history states per second and rebuilt the timeline
 * on every tick.
 */
class UndoCoalescingTest {

    private fun createMockUri(uriString: String): Uri {
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(mockUri.toString()).thenReturn(uriString)
        return mockUri
    }

    private fun newViewModelWithText(): Pair<VideoEditingViewModel, EditOperation.AddText> {
        val vm = VideoEditingViewModel()
        vm.initializeProject(createMockUri("file:///base.mp4"), "base.mp4")
        val textOp = EditOperation.AddText(
            text = "hello",
            fontSize = 28,
            position = TextPosition.CENTER,
            startTimeMs = 0L,
            endTimeMs = 3000L
        )
        vm.addOperation(textOp)
        return vm to textOp
    }

    @Test
    fun `burst of coalesced updates collapses into one undo entry`() {
        val (vm, op) = newViewModelWithText()
        // addOperation pushed one entry
        assertEquals(1, vm.undoStack.value.size)

        // Simulate a slider drag: 20 ticks within the coalesce window, same key
        repeat(20) { i ->
            val updated = op.copy(opacity = 0.1f + i * 0.01f)
            vm.executeCommand(UpdateOperationCommand(op.id, "Modify Effect") { updated }, "volume:${op.id}")
        }

        assertEquals(
            "20 rapid updates with one coalesce key must collapse to ONE extra undo entry",
            2,
            vm.undoStack.value.size
        )
        // Final state must reflect the last update
        val finalText = vm.project.value?.operations?.filterIsInstance<EditOperation.AddText>()?.find { it.id == op.id }
        assertEquals(0.29f, finalText?.opacity ?: -1f, 0.001f)
    }

    @Test
    fun `different coalesce keys do not merge`() {
        val (vm, op) = newViewModelWithText()
        val updatedA = op.copy(opacity = 0.5f)
        val updatedB = op.copy(borderThickness = 3)
        vm.executeCommand(UpdateOperationCommand(op.id, "Modify Effect") { updatedA }, "key-a")
        vm.executeCommand(UpdateOperationCommand(op.id, "Modify Effect") { updatedB }, "key-b")
        assertEquals("distinct keys must each push history", 3, vm.undoStack.value.size)
    }

    @Test
    fun `null coalesce key always pushes history`() {
        val (vm, op) = newViewModelWithText()
        repeat(5) {
            vm.executeCommand(UpdateOperationCommand(op.id, "Modify Effect") { op.copy(opacity = 0.9f) })
        }
        assertEquals(6, vm.undoStack.value.size)
    }

    @Test
    fun `undo after burst restores pre-burst state`() {
        val (vm, op) = newViewModelWithText()
        val originalOpacity = op.opacity
        repeat(10) { i ->
            val updated = op.copy(opacity = i * 0.05f)
            vm.executeCommand(UpdateOperationCommand(op.id, "Modify Effect") { updated }, "opacity:${op.id}")
        }
        assertTrue(vm.uiState.value.canUndo)
        vm.undo()
        val restored = vm.project.value?.operations?.filterIsInstance<EditOperation.AddText>()?.find { it.id == op.id }
        assertEquals(
            "one undo step must restore the state before the whole drag burst",
            originalOpacity,
            restored?.opacity ?: -1f,
            0.0001f
        )
    }
}
