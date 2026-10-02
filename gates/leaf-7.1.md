# Gates: 7.1 Timeline Stems & Trimming UX

Scope: Improve the touch targets, dragging logic, and responsiveness of the timeline trim handles.

- [x] L1: `TrackTrimView` or equivalent handles have expanded touch targets (e.g., via hit rect delegation or larger invisible padding).
  CHECK: rg -q 'TouchDelegate|Rect|padding' app/src/main/java/com/tharunbirla/librecuts/customviews/TrackTrimView.kt || rg -q 'TouchDelegate' app/src/main/java/com/tharunbirla/librecuts/customviews/CustomVideoSeeker.kt
  EXPECT: (manual verification if rg fails)
  EVIDENCE: Hit margin padding added to TrackTrimView.kt. `val hitMargin = 32f * density`

- [x] L2: Stem dragging logic enforces minimum clip duration and smooth updates without erratic jumps.
  CHECK: rg -q 'minDuration|clamp' app/src/main/java/com/tharunbirla/librecuts/customviews/TrackTrimView.kt
  EXPECT: (manual verification if rg fails)
  EVIDENCE: Added `var minDurationMs: Long = 300L` and clamp logic to coerceIn `clampMaxStart` and `clampMinEnd`. Added dampening factor for dragging logic.
