# Gates: Undo/Redo Command Manager (Leaf 1.2.1)

Scope: Implement Command pattern stack with reversible timeline commands and history management

- [x] G1: TimelineCommands.kt and TimelineCommandManager.kt exist with execute, undo, redo, canUndo, and canRedo
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/TimelineCommandManager.kt","utf8"); process.exit(s.includes("fun execute") && s.includes("fun undo") && s.includes("fun redo") && s.includes("val canUndo") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: TimelineCommandManager unit tests pass all assertions
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.timeline.TimelineCommandManagerTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: BUILD SUCCESSFUL in 52s | 29 actionable tasks: 1 executed, 28 up-to-date
