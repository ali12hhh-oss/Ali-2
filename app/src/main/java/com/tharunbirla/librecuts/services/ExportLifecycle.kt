package com.tharunbirla.librecuts.services

/**
 * Maps the render and publication stages to the one terminal state visible to
 * the editor. A render is not an export until its output has been published.
 */
object ExportLifecycle {

    sealed interface TerminalOutcome {
        data class Published(val uri: String) : TerminalOutcome
        data object Cancelled : TerminalOutcome
        data class Failed(val message: String) : TerminalOutcome
    }

    fun publish(publishedUri: String?): TerminalOutcome =
        publishedUri
            ?.takeIf { it.isNotBlank() }
            ?.let { TerminalOutcome.Published(it) }
            ?: TerminalOutcome.Failed("Failed to publish exported media")

    fun cancel(): TerminalOutcome = TerminalOutcome.Cancelled

    fun error(message: String?): TerminalOutcome = TerminalOutcome.Failed(
        message?.takeIf { it.isNotBlank() } ?: "Unknown export error"
    )
}
