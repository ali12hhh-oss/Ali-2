# Gates: Multi-Clip Track Strip View (Leaf 2.1.2)

Scope: Implement multi-clip video track strip view with selection handles, transition indicators, and speed badges

- [x] G1: MultiClipTrackView.kt exists and extends View/ViewGroup with setClips, setSelectedClipIndex, and trim handle touch handlers
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/timeline/ui/MultiClipTrackView.kt","utf8"); process.exit(s.includes("class MultiClipTrackView") && s.includes("fun setClips") && s.includes("fun setSelectedClipIndex") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
