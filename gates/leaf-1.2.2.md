# Gates: Legacy VideoProject Adapter & Serializer (Leaf 1.2.2)

Scope: Implement bidirectional adapter between legacy VideoProject and new multi-track TimelineState for backward compatibility and project saving

- [x] G1: ProjectMigrationAdapter.kt exists with toTimelineState and toVideoProject converters
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/ProjectMigrationAdapter.kt","utf8"); process.exit(s.includes("fun toTimelineState") && s.includes("fun toVideoProject") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: ProjectMigrationAdapter unit tests pass all roundtrip conversion tests
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.timeline.ProjectMigrationAdapterTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: 29 actionable tasks: 5 executed, 24 up-to-date | OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
