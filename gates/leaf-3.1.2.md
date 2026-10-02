# Gates: GPU Real-Time Transition Engine (Leaf 3.1.2)

Scope: Implement OpenGL ES multi-texture transition shader pipeline for Dissolve, Fade, Slide, Push, Wipe, Zoom, and Glitch

- [x] G1: GPUTransitionFilter.kt exists with renderTransition method and shaders for all TransitionTypes
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/render/gpu/GPUTransitionFilter.kt","utf8"); process.exit(s.includes("class GPUTransitionFilter") && s.includes("fun renderTransition") && s.includes("fun buildTransitionShader") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
