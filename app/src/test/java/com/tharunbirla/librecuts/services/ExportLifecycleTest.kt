package com.tharunbirla.librecuts.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportLifecycleTest {

    @Test
    fun `publish completes only after a non-blank published uri exists`() {
        val published = ExportLifecycle.publish("content://media/external/video/42")
        val incomplete = ExportLifecycle.publish(null)

        assertEquals(
            ExportLifecycle.TerminalOutcome.Published("content://media/external/video/42"),
            published
        )
        assertEquals(
            ExportLifecycle.TerminalOutcome.Failed("Failed to publish exported media"),
            incomplete
        )
    }

    @Test
    fun `cancel has a distinct terminal outcome`() {
        assertTrue(ExportLifecycle.cancel() is ExportLifecycle.TerminalOutcome.Cancelled)
    }

    @Test
    fun `error supplies a stable message for null or blank failures`() {
        assertEquals(
            ExportLifecycle.TerminalOutcome.Failed("Unknown export error"),
            ExportLifecycle.error(" ")
        )
        assertEquals(
            ExportLifecycle.TerminalOutcome.Failed("Renderer exited with code 1"),
            ExportLifecycle.error("Renderer exited with code 1")
        )
    }
}
