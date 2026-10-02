# LibreCuts Autonomous Bug Report & Feature Analysis

## 1. State Machine Dead-Ends & Critical Bugs

### 1.1 The Sticker/PIP Addition Lockup & ANR
**Symptom:** Adding an image or video overlay (Sticker/PIP) frequently locks the UI or causes an ANR.
**Root Causes:**
1. **Main Thread Blocking:** When a user selects a video for PIP, `DraggableImageOverlayView.loadOverlayMedia(uri)` synchronously instantiates `MediaMetadataRetriever`, calls `setDataSource(path)`, and extracts a frame on the UI thread. This completely halts the main thread and triggers ANRs for larger video files.
2. **Zero-Duration Ghost Overlays:** When an overlay is added to an empty project (`getTotalSequenceDuration() == 0`), the logic in `VideoEditingActivity.kt` caps the `endTimeMs` to the total sequence duration. This results in `startTimeMs = 0` and `endTimeMs = 0`. Because `durationMs` is `0`, the timeline container (`imageTrackContainer`) refuses to render a visual block for it. The overlay exists in the `VideoProject` but can never be selected, moved, or deleted, functionally leaving a ghost layer on the player.
3. **UI Element Stranding:** In `enterImageEditingMode`, `editingControlsWrapper` is set to `GONE`. Committing the sticker relies entirely on the Compose `ImageToolbar`'s "Back" button (`IMAGE_CANCEL`). If a user manages to dismiss the state through another timeline interaction without triggering `IMAGE_CANCEL` or `commitActiveEditsIfAny()`, the main controls remain hidden, permanently stranding the user. Furthermore, once an overlay is added, the legacy `stemAddSticker` button is permanently hidden (`View.GONE`), making the UI feel broken and unresponsive.

### 1.2 The "Undo Explosion" / Timeline Freeze
**Symptom:** Dragging a keyframed sticker, PIP, or mask causes massive lag, often freezing the application.
**Root Causes:**
- Inside `DraggableImageOverlayView.kt`'s `onTouchEvent`, dragging (via `ACTION_MOVE`) continuously invokes `onPositionChanged` and `onMaskChanged`.
- In `VideoEditingActivity.kt`, these callbacks immediately call `viewModel.updateOperation()`.
- `updateOperation()` executes a command that creates a new `HistoryState`, pushes it to the `undoStack`, and updates `_project.value`.
- This triggers `project.collect`, which invokes `renderTracks(project)`, destroying and rebuilding the entire sequence timeline UI synchronously.
- **Result:** Moving a sticker creates ~60 undo states per second and rebuilds the timeline 60 times a second, completely trashing memory and locking the UI. (Updates must be throttled or only committed on `ACTION_UP`).

### 1.3 Missing Explicit Commit Actions in Toolbars
**Symptom:** Ambiguous or broken exit states from sub-menus.
**Root Causes:**
- The Compose `ImageToolbar` and `TextToolbar` lack explicit "Done" or "Checkmark" buttons. They overload "Back" (`IMAGE_CANCEL`) to act as a commit trigger. This is structurally unsafe; if the user's intent was to discard the addition, it instead forcefully adds it.

---

## 2. Missing Core Features (Feature Parity for "InShot-Level" MVP)

To achieve true feature parity with applications like InShot or CapCut, the following core features must be implemented or drastically polished:

### 2.1 Text Animations & Effects
While basic text overlays (fonts, colors, strokes) are supported, the application lacks entry/exit animations (e.g., fade-in, typewriter, bounce, slide). This is a staple of standard mobile video editors.

### 2.2 Advanced Visual Effects (VFX) & Filter Library
The codebase currently has basic parameter adjustments (Brightness, Contrast, Saturation) mapped to FFmpeg `eq` filters. An InShot-level app requires a library of preset visual effects (VHS, Blur, Glitch, Cinematic LUTs). The FFmpeg rendering engine must be extended to support complex `filter_complex` chains for these VFX overlays.

### 2.3 Gestural Timeline Navigation
The timeline currently uses basic scroll listeners. To match modern editors, the timeline requires robust two-finger pinch-to-zoom capabilities, allowing users to dynamically scale `pixelsPerMs` to make micro-trims or view the macro-sequence.

### 2.4 True Project Management Dashboard
While `ProjectSerializer.kt` exists, the app boots directly into `VideoEditingActivity.kt`. An MVP requires a "Home/Dashboard" screen to view, duplicate, rename, and delete saved draft projects.

### 2.5 Centralized UI State Architecture (MVI)
The activity currently operates as a 9,600+ line monolithic state machine, manually toggling `View.GONE` and `View.VISIBLE` for dozens of legacy layouts. Phase 10 must migrate all state to a strict, sealed `UIState` class managed by the ViewModel, where the UI solely observes and reacts, preventing dead-ends.
