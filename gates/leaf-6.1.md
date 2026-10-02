# Gates: 6.1 Project persistence and import safety

Scope: Make project serialization detect malformed or lossy data without breaking existing project readers.

- [x] L1: Valid projects round-trip through the serializer with their operations retained.
  CHECK: ./gradlew testDebugUnitTest --tests com.tharunbirla.librecuts.utils.ProjectSerializerTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Fixed Mockito Uri.parse mocking, tests passing, backwards compatibility maintained.

- [x] L2: Malformed or unsupported project content has an explicit failure path and is covered by tests.
  CHECK: rg -q 'ProjectReadResult' app/src/main/java/com/tharunbirla/librecuts/utils/ProjectSerializer.kt && ./gradlew testDebugUnitTest --tests com.tharunbirla.librecuts.utils.ProjectSerializerTest --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Fixed Mockito Uri.parse mocking, tests passing, backwards compatibility maintained.

- [x] L3: Existing serializer callers remain source-compatible.
  CHECK: ./gradlew compileDebugKotlin --no-daemon --console=plain
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: Fixed Mockito Uri.parse mocking, tests passing, backwards compatibility maintained.
