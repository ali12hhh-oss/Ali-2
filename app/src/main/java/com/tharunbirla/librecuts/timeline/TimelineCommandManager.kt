package com.tharunbirla.librecuts.timeline

/**
 * Manages the undo/redo history stack for the timeline state.
 */
class TimelineCommandManager(
    initialState: TimelineState = TimelineState(),
    private val maxHistorySize: Int = 50
) {
    init {
        require(maxHistorySize > 0) { "maxHistorySize must be positive" }
    }

    private val undoStack = ArrayDeque<TimelineCommand>()
    private val redoStack = ArrayDeque<TimelineCommand>()

    var currentState: TimelineState = initialState
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    val undoActionName: String? get() = undoStack.lastOrNull()?.name
    val redoActionName: String? get() = redoStack.lastOrNull()?.name

    val historyCount: Int get() = undoStack.size

    /**
     * Executes a command, updates the current state, and pushes to the undo stack.
     */
    fun execute(command: TimelineCommand): TimelineState {
        val newState = command.execute(currentState)
        if (newState == currentState) return currentState

        currentState = newState
        undoStack.addLast(command)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        return currentState
    }

    /**
     * Undoes the last executed command.
     */
    fun undo(): TimelineState? {
        if (!canUndo) return null
        val command = undoStack.removeLast()
        val newState = command.undo(currentState)
        currentState = newState
        redoStack.addLast(command)
        return currentState
    }

    /**
     * Redoes the last undone command.
     */
    fun redo(): TimelineState? {
        if (!canRedo) return null
        val command = redoStack.removeLast()
        val newState = command.execute(currentState)
        currentState = newState
        undoStack.addLast(command)
        return currentState
    }

    /**
     * Resets history and initializes with a new state.
     */
    fun reset(state: TimelineState) {
        currentState = state
        undoStack.clear()
        redoStack.clear()
    }
}
