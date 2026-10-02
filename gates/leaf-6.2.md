# Gates: 6.2 Timeline edit integrity

Scope: Ensure the pure timeline command and math modules protect against invalid ranges and retain undo/redo correctness.

- [x] L1: Edge cases around trim, split, move, and undo/redo are unit-tested.
  CHECK: ./gradlew testDebugUnitTest --tests 'com.tharunbirla.librecuts.timeline.*' --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: All tests passed.

- [x] L2: Timeline mutation code rejects or normalizes invalid negative/empty ranges at its module seam.
  CHECK: ./gradlew testDebugUnitTest --tests 'com.tharunbirla.librecuts.timeline.*' --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: All tests passed.
