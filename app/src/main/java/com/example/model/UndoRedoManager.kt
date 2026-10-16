package com.example.model

data class ProjectSnapshot(
    val elements: List<CanvasElement>,
    val background: CanvasBackground,
    val deviceFrame: DeviceFrameStyle
)

class UndoRedoManager(private val maxHistory: Int = 40) {
    private val undoStack = ArrayDeque<ProjectSnapshot>()
    private val redoStack = ArrayDeque<ProjectSnapshot>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun pushState(currentState: ProjectSnapshot) {
        undoStack.addLast(currentState)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
        redoStack.clear()
    }

    fun undo(currentState: ProjectSnapshot): ProjectSnapshot? {
        if (!canUndo) return null
        val prevState = undoStack.removeLast()
        redoStack.addLast(currentState)
        return prevState
    }

    fun redo(currentState: ProjectSnapshot): ProjectSnapshot? {
        if (!canRedo) return null
        val nextState = redoStack.removeLast()
        undoStack.addLast(currentState)
        return nextState
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
