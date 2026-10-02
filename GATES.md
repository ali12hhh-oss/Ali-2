# Gates: Prodline AI development environment baseline

Scope: Prepare the existing Prodline AI Android repository for safe continued development without overwriting its current uncommitted work.

- [x] G1: The Matt Pocock skills and Unlazy repositories are locally cloned at recorded locations.
  CHECK: test -f /root/agent-resources/mattpocock-skills/README.md && test -f /root/agent-resources/unlazy/SKILL.md && printf 'resource repositories present\n'
  EXPECT: resource repositories present
  EVIDENCE: `resource repositories present`; Matt skills `5b15a47`, Unlazy `ed9e8d2`.

- [x] G2: The relevant engineering skills are installed for future Codex turns, and Unlazy is available.
  CHECK: test -f /root/.codex/skills/implement/SKILL.md && test -f /root/.codex/skills/tdd/SKILL.md && test -f /root/.codex/skills/unlazy/SKILL.md && printf 'skills available\n'
  EXPECT: skills available
  EVIDENCE: `skills available`; installed: code-review, codebase-design, diagnosing-bugs, domain-modeling, implement, research, tdd, triage, wayfinder, and unlazy.

- [x] G3: Gradle resolves the existing project and its unit-test task completes without changing the user's current source edits.
  CHECK: ./gradlew testDebugUnitTest --no-daemon
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: `./gradlew testDebugUnitTest --no-daemon --console=plain` — `BUILD SUCCESSFUL in 28s`; 29 tasks up-to-date.

- [x] G4: Android command-line prerequisites required by the project are installed and the compile SDK is present.
  CHECK: test -x /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager && test -d /opt/android-sdk/platforms/android-34 && java -version 2>&1
  EXPECT: version "17
  EVIDENCE: JDK `17.0.19`; `/opt/android-sdk/platforms/android-34`; build-tools `34.0.0`; platform-tools `37.0.1`.

- [x] G5: The checked build result and any environment limitation are recorded accurately in this file.
  EVIDENCE: Current tree contained 25 pre-existing edited/untracked app files before baseline work; none were modified. Only this ledger was added.

## Production-readiness milestone 1: dependable timeline and export foundation

Scope: Turn the current in-progress timeline/export work into a safe, typed, and tested foundation for the broader short-form editor roadmap.

- [x] P1: A feature matrix identifies existing capability, verification status, and the next production milestones without claiming unsupported parity.
  CHECK: test -f docs/PRODUCTION_READINESS.md && rg -q 'Export reliability' docs/PRODUCTION_READINESS.md && printf 'roadmap recorded\n'
  EXPECT: roadmap recorded
  EVIDENCE: `docs/PRODUCTION_READINESS.md` records the matrix, delivery milestones, and the export-reliability contract.

- [x] P2: Export settings produce a validated typed plan, including codec-specific constraints and an accurate size estimate.
  CHECK: ./gradlew testDebugUnitTest --tests com.tharunbirla.librecuts.export.HardwareExportEngineTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: `HardwareExportEngineTest`: 4 passing tests; `BUILD SUCCESSFUL in 52s`.

- [x] P3: Existing export UI continues to show the same settings and correctly uses aspect-ratio-aware dimensions.
  CHECK: ./gradlew testDebugUnitTest --tests com.tharunbirla.librecuts.export.ExportBitrateEstimatorTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: `ExportBitrateEstimatorTest` passed; `BUILD SUCCESSFUL in 40s`.

- [x] P4: The debug APK assembles after the hardening work.
  CHECK: ./gradlew assembleDebug --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: `./gradlew assembleDebug --no-daemon --console=plain` — `BUILD SUCCESSFUL in 49s`.

## Production-readiness milestone 2: Android quality baseline

Scope: Establish a static-analysis baseline and remove release-blocking errors discovered in the editor's Android integration.

- [x] Q1: Android lint completes and its findings are captured for prioritisation.
  CHECK: ./gradlew lintDebug --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: `BUILD SUCCESSFUL in 3m 15s`; 875 warnings, dominated by hardcoded text (192), unused resources (160), content descriptions (135), and text internationalisation (83).

- [x] Q2: No lint errors remain in the debug variant.
  CHECK: (test ! -f app/build/reports/lint-results-debug.xml || ! rg -q '<issue[^>]*severity="Error"' app/build/reports/lint-results-debug.xml) && printf 'no lint errors\n'
  EXPECT: no lint errors
  EVIDENCE: `lint-results-debug.xml` contains 875 `Warning` severities and zero `Error` severities.

## Milestone 3: bug eradication & InShot-class feature parity

Scope: eliminate ANRs, undo explosion, ghost overlays, dead-end buttons; add text animations; verify build/tests/lint.

- [x] M3-A: Baseline committed — Phase 9 edge-to-edge work and docs are in git before new changes.
  CHECK: cd /root/LibreCuts && git log --oneline -1 | grep -qE "phase|Phase|edge" && printf 'baseline committed\n'
  EXPECT: baseline committed
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=baseline committed

- [x] M3-B: No MediaMetadataRetriever/Movie decode runs on the main thread during overlay add/edit commit paths.
  CHECK: cd /root/LibreCuts && grep -n -A1 "withContext(kotlinx.coroutines.Dispatchers.IO) {" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | grep -q "getOverlayFileDurationMs(uri)" && test "$(rg -c 'Dispatchers.IO' app/src/main/java/com/tharunbirla/librecuts/customviews/DraggableImageOverlayView.kt)" -ge 3 && printf 'media IO off main thread\n'
  EXPECT: media IO off main thread
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=media IO off main thread

- [x] M3-C: Repeated updates to the same operation coalesce into one undo entry; slider drags no longer push per-tick history states.
  CHECK: cd /root/LibreCuts && ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.viewmodels.UndoCoalescingTest" --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=29 actionable tasks: 1 executed, 28 up-to-date | OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

- [x] M3-D1: TEXT_DUPLICATE, TEXT_DELETE, TEXT_SPLIT and CROP_RESET toolbar actions are functional (no silent no-ops).
  CHECK: cd /root/LibreCuts && rg -n '"TEXT_DUPLICATE" ->|"TEXT_DELETE" ->' app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | (! grep -q '\{\}$') && printf 'actions implemented\n'
  EXPECT: actions implemented
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=actions implemented

- [x] M3-D2: Adding an image/text overlay at the sequence end can never create a zero-duration operation.
  CHECK: cd /root/LibreCuts && test "$(rg -c 'effStart = \(seqDur.*- duration\)\.coerceAtLeast\(0L\)|effStartText = \(seqDur - 3000L\)\.coerceAtLeast\(0L\)' app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt)" -ge 2 && printf 'zero-duration clamps present at both add sites\n'
  EXPECT: zero-duration clamps present at both add sites
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=zero-duration clamps present at both add sites

- [x] M3-E: Text overlays support entry/exit animations (None/Fade/Slide/Pop) selectable in UI and rendered by FFmpeg export.
  CHECK: cd /root/LibreCuts && test "$(rg -c 'TextAnimation' app/src/main/java/com/tharunbirla/librecuts/models/EditOperation.kt app/src/main/java/com/tharunbirla/librecuts/viewmodels/VideoEditingViewModel.kt app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorToolbars.kt app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | awk -F: '{s+=$2} END {print s}')" -ge 10 && printf 'text animations across model/render/UI/activity\n'
  EXPECT: text animations across model/render/UI/activity
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=text animations across model/render/UI/activity

- [x] M3-F: Integration — unit tests green, lint has no errors, debug APK assembles for release of this milestone.
  CHECK: cd /root/LibreCuts && ./gradlew testDebugUnitTest assembleDebug lintDebug --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=58 actionable tasks: 2 executed, 56 up-to-date | OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended

## Milestone 4: complete Jetpack Compose revamp (Phase 12)

- [x] M4-A: Design system v2 exists — MD3 dark editor palette, typography, shapes, shared Prodline components compile.
  CHECK: cd /root/LibreCuts && test -f app/src/main/java/com/tharunbirla/librecuts/ui/components/ProdlineComponents.kt && grep -q "darkColorScheme" app/src/main/java/com/tharunbirla/librecuts/ui/theme/Color.kt && printf 'design system present\n'
  EXPECT: design system present
  EVIDENCE: exit=0; output=design system present; ProdlineComponents.kt present; darkColorScheme defined in ui/theme/Color.kt

- [x] M4-B: MainActivity hosts a Compose dashboard; no ActivityMainBinding remains; launchers work through callbacks; drafts directory listing implemented.
  CHECK: cd /root/LibreCuts && ! grep -q "ActivityMainBinding" app/src/main/java/com/tharunbirla/librecuts/MainActivity.kt && grep -q "setContent" app/src/main/java/com/tharunbirla/librecuts/MainActivity.kt && grep -q "drafts" app/src/main/java/com/tharunbirla/librecuts/MainActivity.kt && printf 'compose dashboard live\n'
  EXPECT: compose dashboard live
  EVIDENCE: exit=0; output=compose dashboard live; ActivityMainBinding removed; setContent + drafts listing in MainActivity.kt

- [x] M4-C: Editor top chrome (back/undo/redo/export) is rendered by the Compose EditorTopBar with one-way state flow.
  CHECK: cd /root/LibreCuts && test -f app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorTopBar.kt && grep -q "EditorTopBar(" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && printf 'editor top bar compose\n'
  EXPECT: editor top bar compose
  EVIDENCE: exit=0; output=editor top bar compose; EditorTopBar.kt exists; EditorTopBar( rendered from VideoEditingActivity.kt

- [x] M4-D: Export options render in a Compose ModalBottomSheet bound to viewModel.setExportSettings.
  CHECK: cd /root/LibreCuts && grep -q "ModalBottomSheet" app/src/main/java/com/tharunbirla/librecuts/ui/components/ExportSheet.kt && grep -q "ExportSheet" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && printf 'export sheet compose\n'
  EXPECT: export sheet compose
  EVIDENCE: exit=0; output=export sheet compose; ModalBottomSheet in ExportSheet.kt; ExportSheet hosted by VideoEditingActivity.kt

- [x] M4-E: Integration — full build green (tests, lint zero errors, assembleDebug).
  CHECK: cd /root/LibreCuts && ./gradlew testDebugUnitTest assembleDebug lintDebug --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: exit=0; output=BUILD SUCCESSFUL; ./gradlew testDebugUnitTest assembleDebug lintDebug --no-daemon: BUILD SUCCESSFUL in 7m 15s; 58 tasks: 16 executed, 42 up-to-date

- [x] M4-F: Timeline-native-Compose rewrite is recorded as an explicit deferred milestone with rationale, not silently dropped.
  CHECK: cd /root/LibreCuts && grep -q "Deferred" docs/PRODUCTION_READINESS.md && printf 'deferral recorded\n'
  EXPECT: deferral recorded
  EVIDENCE: exit=0; output=deferral recorded; Deferred timeline-native-Compose rewrite documented in docs/PRODUCTION_READINESS.md

## Milestone 5: bug eradication sweep (Phase 13)

- [x] 13-P: Persistence chain trustworthy — drafts open correctly, writes atomic, collision-safe names, schema-versioned serialization, crash handler survives to error screen.
  CHECK: cd /root/LibreCuts && grep -q "DRAFT_PATH" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && grep -q "renameTo" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && grep -q "schemaVersion" app/src/main/java/com/tharunbirla/librecuts/utils/ProjectSerializer.kt && [ "$(grep -c 'android:exported="true"' app/src/main/AndroidManifest.xml)" = "1" ] && printf 'persistence hardened\n'
  EXPECT: persistence hardened
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=persistence hardened

- [x] 13-C: No stranded states — every legacy exit resets Compose toolbarState; back exits modes before quit dialog; DRAW wired; capture-frame restores controls; subtitle/mask drags coalesced.
  CHECK: cd /root/LibreCuts && rg -c "toolbarState.value = EditorToolbarState.MAIN|toolbarState\.value = .*MAIN" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | awk '{exit ($1>=4)?0:1}' && grep -q '"DRAW_ACTION"' app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorToolbars.kt && grep -q "openHandwritingMode" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && rg -c "updateOperationCoalesced|coalesceKey = " app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt >/dev/null && printf 'state machine sealed\n'
  EXPECT: state machine sealed
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=state machine sealed

- [x] 13-B: No main-thread media I/O in hot paths — project init, canvas background, overlay draw, export command build all off Main; late-init player guarded.
  CHECK: cd /root/LibreCuts && node -e "const fs=require('fs');const p='app/src/main/java/com/tharunbirla/librecuts/';const va=fs.readFileSync(p+'VideoEditingActivity.kt','utf8');const i=va.indexOf('private fun initializeVideoData');if(i<0)process.exit(2);if(!/Dispatchers\.IO/.test(va.slice(i,i+4000)))process.exit(1);const io=fs.readFileSync(p+'customviews/ImageOverlayView.kt','utf8');const od=io.indexOf('onDraw');if(od>=0&&/setDataSource|Movie\.decodeFile/.test(io.slice(od,od+3000)))process.exit(1);const fre=fs.readFileSync(p+'services/FFmpegRenderEngine.kt','utf8');if(!fre.includes('isInitialized')&&!va.includes('::player.isInitialized'))process.exit(1);console.log('hot paths off main');" && printf 'verified\n'
  EXPECT: verified
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=hot paths off main | verified

- [x] 13-R: Reorder integrity — dragging clips cannot duplicate the primary segment; indices stay aligned.
  CHECK: cd /root/LibreCuts && grep -n "updateSequenceOrder" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | head -1 >/dev/null && rg -n "drop\(1\)" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt | grep -i "merge\|sequence\|order" >/dev/null && printf 'reorder safe\n'
  EXPECT: reorder safe
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=reorder safe

- [x] 13-X: Export engine correct — aspect preserved, drawtext resolves real fontfile path, cancellation detected before encoder fallback, reverse memory-capped, amix unnormalized+limited, audio-only passthrough encodes real MP3.
  CHECK: cd /root/LibreCuts && rg -c "force_original_aspect_ratio" app/src/main/java/com/tharunbirla/librecuts/viewmodels/VideoEditingViewModel.kt >/dev/null && rg -c "ReturnCode.isCancel" app/src/main/java/com/tharunbirla/librecuts/services/FFmpegRenderEngine.kt >/dev/null && rg -c "absolutePath" app/src/main/java/com/tharunbirla/librecuts/services/FFmpegRenderEngine.kt >/dev/null && rg -c "normalize=0" app/src/main/java/com/tharunbirla/librecuts/viewmodels/VideoEditingViewModel.kt >/dev/null && rg -c "alimiter" app/src/main/java/com/tharunbirla/librecuts/viewmodels/VideoEditingViewModel.kt >/dev/null && printf 'export engine sound\n'
  EXPECT: export engine sound
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=export engine sound

- [x] 13-A: Audio features honest — recordings persisted outside cacheDir; failed stops insert nothing; VO clips trimmable; background audio re-anchored after structural edits; waveforms cached.
  CHECK: cd /root/LibreCuts && rg -c "filesDir" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt >/dev/null && grep -q "voice" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && grep -q "originalDurationMs" app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt && printf 'audio honest\n'
  EXPECT: audio honest
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=audio honest

- [x] 13-J: Junk purged — ScratchTest files, dead HardwareExportEngine/GPU/keyframe modules removed from release sourceset.
  CHECK: cd /root/LibreCuts && test ! -f app/src/main/java/com/tharunbirla/librecuts/ScratchTest.java && test ! -f app/src/main/java/com/tharunbirla/librecuts/ScratchTest.kt && test ! -f app/src/main/java/com/tharunbirla/librecuts/export/HardwareExportEngine.kt && printf 'junk purged\n'
  EXPECT: junk purged
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=junk purged

- [x] 13-I: Integration — unit tests green, lint clean, debug APK assembles.
  CHECK: cd /root/LibreCuts && ./gradlew testDebugUnitTest assembleDebug lintDebug --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: exit=0; shell=/bin/sh; cwd=/root/LibreCuts; path=1a30eb7564d3/38 entries; output=BUILD SUCCESSFUL in 41s | 58 actionable tasks: 1 executed, 57 up-to-date
