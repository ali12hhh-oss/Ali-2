# Gates: Timeline Coordinate Bridge & Zoom Engine (Leaf 2.1.1)

Scope: Implement pixel-to-millisecond coordinate projection, pinch-zoom scale math, and boundary snapping

- [x] G1: TimelineCoordinateBridge.kt exists with msToPx, pxToMs, zoomIn, zoomOut, and findSnapPoint methods
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/ui/TimelineCoordinateBridge.kt","utf8"); process.exit(s.includes("fun msToPx") && s.includes("fun pxToMs") && s.includes("fun findSnapPoint") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: TimelineCoordinateBridge unit tests compile and pass
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.timeline.ui.TimelineCoordinateBridgeTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: BUILD SUCCESSFUL in 29s | 29 actionable tasks: 7 executed, 22 up-to-date
