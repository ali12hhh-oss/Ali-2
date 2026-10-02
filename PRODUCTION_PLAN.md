# Prodline AI: functional editor hardening plan

Depth: 4  
Mode: orchestrated  
Product target: an original, offline, open-source Android video editor that provides dependable baseline short-form editing. This plan intentionally excludes AI features and does not copy third-party branding or assets.

## Contract

The existing app contains the user-facing feature families. This phase makes their data and rendering paths dependable before expanding the interface.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 6.1 Project persistence | `utils/ProjectSerializer.kt`, its new unit test | Existing `serialize` / `deserialize` callers remain source-compatible. Malformed project data must fail explicitly rather than silently dropping edits. |
| 6.2 Timeline integrity | `timeline/TimelineMathEngine.kt`, `timeline/TimelineCommandManager.kt`, their tests | Timeline mutations preserve valid non-negative ranges and undo/redo produces the expected project state. No activity or layout files are touched. |
| 6.3 Export lifecycle | `services/ExportService.kt`, its new unit-testable helper | Export output is not reported as complete until it is published; errors/cancellation clean up partial output and report a clear terminal state. No UI/activity files are touched. |

Shared conventions:

- Kotlin code remains under `com.tharunbirla.librecuts`.
- New pure logic must have JVM unit tests under the matching test package.
- Preserve existing public behaviour unless a test demonstrates an invalid or lossy path.
- Do not modify files owned by another leaf or the user’s in-progress activity/layout work.

## Tree

- 6 Functional editor hardening ................................ `gates/node-root-6.md`
  - 6.1 Project persistence and import safety .................. `gates/leaf-6.1.md`
  - 6.2 Timeline edit integrity ................................ `gates/leaf-6.2.md`
  - 6.3 Export lifecycle reliability ........................... `gates/leaf-6.3.md`

## Status log

- 2026-08-22: Phase initiated; feature scope limited to functional offline editing and reliability.

## Phase 7: UI/UX Polish and Timeline Interactions

Depth: 4
Mode: orchestrated
Product target: Refine the timeline interactions, especially the trim stems, and achieve a robust, crash-free, production-grade video editor UX mirroring professional apps like InShot/VN.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 7.1 Timeline Stems & Trimming UX | `customviews/TrackTrimView.kt`, `customviews/CustomVideoSeeker.kt` | Trim handles ("stems") must have larger, forgiving touch targets and smoother drag responses without overly aggressive snapping or jumpiness. |
| 7.2 UI Consistency & Professional Polish | `res/layout/*.xml`, `res/values/colors.xml`, `VideoEditingActivity.kt` | The interface must feel unified, dark-themed, and responsive. Remove any layout jank or misaligned margins. Make it feel like a cohesive, professional editing environment. |
| 7.3 Crash Resilience & Edge Cases | `VideoEditingActivity.kt`, `services/FFmpegRenderEngine.kt` | App must not crash on rapid user inputs, invalid media files, backgrounding during export, or OOM. Defensive checks added to all UI boundary methods. |

## Tree
- 7 UI/UX Polish and Timeline Interactions ..................... `gates/node-root-7.md`
  - 7.1 Timeline Stems & Trimming UX ........................... `gates/leaf-7.1.md`
  - 7.2 UI Consistency & Professional Polish ................... `gates/leaf-7.2.md`
  - 7.3 Crash Resilience & Edge Cases .......................... `gates/leaf-7.3.md`

## Phase 8: Jetpack Compose Migration & Modern UI

Depth: 4
Mode: orchestrated
Product target: Aggressively rewrite the UI into Jetpack Compose using the claude-android-ninja and mobile-android-design skill stacks. We will use incremental adoption (ComposeView) to replace the static XML toolbars with dynamic, animated, Material 3 Compose components without breaking the core timeline.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 8.1 Compose Foundation & Theming | `app/build.gradle`, `app/src/main/java/.../theme/` | Add Jetpack Compose dependencies to Gradle. Create MD3 `Theme.kt`, `Color.kt`, and `Type.kt`. The build must succeed and a basic Compose @Preview must render. |
| 8.2 Main Editor Toolbars Migration | `activity_video_editing.xml`, `VideoEditingActivity.kt`, `ui/components/EditorToolbars.kt` | Replace the legacy XML toolbars (`videoEditingToolbar`, `textEditingToolbar`, etc.) with a single `ComposeView`. The Compose UI must emit state changes back to the Activity to maintain core logic. |

## Tree
- 8 Jetpack Compose Migration & Modern UI ........................ `gates/node-root-8.md`
  - 8.1 Compose Foundation & Theming ........................... `gates/leaf-8.1.md`
  - 8.2 Main Editor Toolbars Migration ......................... `gates/leaf-8.2.md`

## Phase 9: Edge-to-Edge & Modernization

Depth: 4
Mode: orchestrated
Product target: Modernize the app to edge-to-edge screens and apply Material 3 design polish to the new Compose toolbars.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 9.1 Edge-to-Edge Implementation | `MainActivity.kt`, `VideoEditingActivity.kt`, `themes.xml` | Implement edge-to-edge display using `enableEdgeToEdge()`. Remove legacy translucent system bar configurations from `themes.xml`. |
| 9.2 Compose Toolbar Polish | `ui/components/EditorToolbars.kt` | Enhance `ToolbarButton` with proper padding and Material 3 touch targets. Adjust toolbars to respect `navigationBarsPadding()` so they don't overlap system gesture bars. |

## Tree
- 9 Edge-to-Edge & Modernization ............................... `gates/node-root-9.md`
  - 9.1 Edge-to-Edge Implementation ............................ `gates/leaf-9.1.md`
  - 9.2 Compose Toolbar Polish ................................. `gates/leaf-9.2.md`

## Phase 10: State Machine Architecture & Bug Eradication

Depth: 4
Mode: orchestrated
Product target: Eliminate all state machine dead-ends, UI stranding, and interaction lockups (specifically the Sticker/PIP lockup and timeline undo explosion).

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 10.1 UI State Decoupling | `VideoEditingViewModel.kt`, `VideoEditingActivity.kt` | Migrate manual `View.VISIBLE`/`GONE` toggles to a centralized `UIState` observed by the Activity. Ensure there is always a valid exit path back to the `MAIN` state. |
| 10.2 Media Loading Concurrency | `customviews/DraggableImageOverlayView.kt`, `customviews/ImageOverlayView.kt` | Push `MediaMetadataRetriever` extraction and file I/O to background coroutines. The main thread must never block during PIP/Sticker media selection or rendering. |
| 10.3 Timeline Drag Throttling | `customviews/DraggableImageOverlayView.kt`, `VideoEditingActivity.kt` | Decouple `onTouchEvent` movement from `viewModel.updateOperation()`. Keyframe and mask adjustments must batch states or commit strictly on `ACTION_UP` to prevent undo stack explosions and timeline rebuild thrashing. |

## Tree
- 10 State Machine Architecture & Bug Eradication ................ `gates/node-root-10.md`
  - 10.1 UI State Decoupling ................................... `gates/leaf-10.1.md`
  - 10.2 Media Loading Concurrency ............................. `gates/leaf-10.2.md`
  - 10.3 Timeline Drag Throttling .............................. `gates/leaf-10.3.md`

## Phase 11: Core Feature Parity (MVP InShot-level)

Depth: 4
Mode: orchestrated
Product target: Implement missing critical capabilities necessary for the app to function as a fully featured professional short-form editor.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 11.1 Explicit Toolbar Commits | `ui/components/EditorToolbars.kt`, `VideoEditingActivity.kt` | Add explicit "Done/Checkmark" and "Cancel/X" buttons to Compose toolbars, ending the overloading of the "Back" action. |
| 11.2 Text Animations & VFX Hooks | `models/EditOperation.kt`, `services/FFmpegRenderEngine.kt` | Extend the FFmpeg `filter_complex` pipeline and UI to support text entry/exit animations and robust visual effects (VFX overlays). |
| 11.3 Timeline Gestures & Project Dashboard | `VideoEditingActivity.kt`, `MainActivity.kt`, `customviews/TimelineHorizontalScrollView.kt` | Add standard two-finger pinch-to-zoom to the timeline for micro-edits, and implement a true home screen dashboard for managing serialized project drafts. |

## Tree
- 11 Core Feature Parity (MVP InShot-level) ...................... `gates/node-root-11.md`
  - 11.1 Explicit Toolbar Commits .............................. `gates/leaf-11.1.md`
  - 11.2 Text Animations & VFX Hooks ........................... `gates/leaf-11.2.md`
  - 11.3 Timeline Gestures & Project Dashboard ................. `gates/leaf-11.3.md`

## Phase 12: Complete Jetpack Compose revamp

Depth: 4
Mode: orchestrated
Product target: modernize every user-facing surface to Material 3 / Jetpack Compose following Google's incremental-adoption guidance. The hardened custom timeline/overlay views remain wrapped via AndroidView interop this phase — they are load-bearing and freshly bug-fixed; a native Compose timeline is the explicit next milestone rather than a silent omission.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 12.1 Design system v2 | `ui/theme/`, new `ui/components/ProdlineComponents.kt` | Full dark editor palette on MD3 ColorScheme roles, typography scale, shapes, shared components (top bar, tool icon button, labeled slider). No behavior changes. |
| 12.2 Home dashboard | `MainActivity.kt` | Activity hosts a Compose screen via setContent; all SAF launchers preserved and invoked through callbacks; drafts list from app-private `files/drafts`; export-folder settings rendered in Compose. ViewBinding removed. |
| 12.3 Editor top chrome | `activity_video_editing.xml`, `VideoEditingActivity.kt`, `ui/components/EditorTopBar.kt` | Back/undo/redo/preview/export strip rendered by Compose; state flows one-way into it, actions emitted out; legacy view IDs kept as hidden stubs where code still references them. |
| 12.4 Export sheet | `ui/components/ExportSheet.kt`, `VideoEditingActivity.kt` | Resolution/FPS/audio-only selection in a Compose ModalBottomSheet bound to `setExportSettings`; replaces any legacy export dialog path without changing export engine contracts. |

## Tree
- 12 Complete Jetpack Compose revamp ......................... gates/node-root-12.md
  - 12.1 Design system v2 ..................................... gates/leaf-12.1.md
  - 12.2 Home dashboard ....................................... gates/leaf-12.2.md
  - 12.3 Editor top chrome .................................... gates/leaf-12.3.md
  - 12.4 Export sheet ......................................... gates/leaf-12.4.md

## Phase 13: Bug eradication sweep

Depth: 4
Mode: orchestrated
Product target: an offline, rock-solid baseline editor. No AI features, no network. Audit every feature family end-to-end and fix all confirmed defects: crashes, ANRs, dead-end UI states, data loss, preview/export mismatches. Feature scope stays frozen; this phase only removes defects found in the audit.

| Leaf | Owns | Interface contract |
| --- | --- | --- |
| 13.1 Export/render pipeline | `services/`, `export/`, `render/` | Export never reports success without a playable file; cancellation cleans partial output; no main-thread I/O. |
| 13.2 Overlay & gesture surfaces | `customviews/`, `overlay/` | No ghost overlays, no drag-induced undo storms, correct hit-testing and cleanup; retrievers closed. |
| 13.3 Editor state machine & Compose chrome | `VideoEditingActivity.kt`, `viewmodels/`, `ui/` | Every mode has an exit path back to MAIN; Compose toolbars/sheet/dashboard emit valid actions; no stranded controls. |
| 13.4 Timeline integrity | `timeline/`, trim/multiclip views | Mutations keep non-negative ranges; undo/redo round-trips; zoom/scroll consistent. |
| 13.5 Persistence & dashboard | `MainActivity.kt`, `ProjectImportActivity.kt`, `utils/`, `models/` | Drafts save/load/delete reliably; malformed data fails explicitly; import cannot wedge the app. |

Shared conventions: Kotlin under `com.tharunbirla.librecuts`; fixes carry regression tests where JVM-testable; no feature additions; no new permissions; app remains fully offline.

## Tree
- 13 Bug eradication sweep .................................... gates/node-root-13.md
  - 13.1 Export/render pipeline ................................ gates/leaf-13.1.md
  - 13.2 Overlay & gesture surfaces ........................... gates/leaf-13.2.md
  - 13.3 Editor state machine & Compose chrome .................. gates/leaf-13.3.md
  - 13.4 Timeline integrity .................................... gates/leaf-13.4.md
  - 13.5 Persistence & dashboard ............................... gates/leaf-13.5.md
