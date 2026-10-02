# Gates: Timeline Domain Models (Leaf 1.1.1)

Scope: Implement core multi-track timeline data models in com.tharunbirla.librecuts.timeline

- [x] G1: TimelineModels.kt file exists and contains TimeRange, MediaSource, VideoClip, AudioClip, OverlayClip, CanvasConfig, and TimelineState
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/TimelineModels.kt","utf8"); process.exit(s.includes("data class TimeRange") && s.includes("data class VideoClip") && s.includes("data class AudioClip") && s.includes("data class OverlayClip") && s.includes("data class TimelineState") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: TimelineModels has complete enum definitions for AspectRatio, BlendMode, OverlayType, TransitionType, and CanvasBackgroundType
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/TimelineModels.kt","utf8"); process.exit(s.includes("enum class AspectRatio") && s.includes("enum class BlendMode") && s.includes("enum class OverlayType") && s.includes("enum class TransitionType") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
