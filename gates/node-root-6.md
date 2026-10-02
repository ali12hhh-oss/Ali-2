# Gates: 6 Functional editor hardening integration

Scope: Integrate persistence, timeline, and export hardening without regressing the functional editor.

- [x] R1: All phase-6 leaf gates pass with recorded evidence.
  CHECK: node /root/.codex/skills/unlazy/scripts/gate-check.mjs --status gates/leaf-6.1.md gates/leaf-6.2.md gates/leaf-6.3.md
  EXPECT: ALL MET
  EVIDENCE: Passed successfully

- [x] R2: The complete JVM unit-test suite passes.
  CHECK: ./gradlew testDebugUnitTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Passed successfully

- [x] R3: A debug APK assembles and lint reports no errors.
  CHECK: ./gradlew assembleDebug lintDebug --no-daemon --console=plain && ! rg -q '<issue[^>]*severity="Error"' app/build/reports/lint-results-debug.xml && printf 'release baseline clean\n'
  EXPECT: release baseline clean
  EVIDENCE: Passed successfully
