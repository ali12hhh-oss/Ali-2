# Gates: 6.3 Export lifecycle reliability

Scope: Keep export output and terminal states reliable under success, cancellation, and failure.

- [x] L1: A pure, unit-tested export lifecycle module defines publish, cancel, and error terminal outcomes.
  CHECK: ./gradlew testDebugUnitTest --tests com.tharunbirla.librecuts.services.ExportLifecycleTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Lifecycle tests passed and ExportService is compiling with correct usage.

- [x] L2: ExportService uses the lifecycle module to avoid reporting an incomplete output as finished.
  CHECK: rg -q 'ExportLifecycle' app/src/main/java/com/tharunbirla/librecuts/services/ExportService.kt && ./gradlew compileDebugKotlin --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Lifecycle tests passed and ExportService is compiling with correct usage.
