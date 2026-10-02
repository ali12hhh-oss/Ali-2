# Gates: Keyframe Evaluator & Easing Curves (Leaf 3.2.1)

Scope: Implement keyframe evaluation engine for translation, scale, rotation, and opacity with linear and Bezier easing

- [x] G1: KeyframeEvaluator.kt exists with evaluateTransform, interpolateKeyframes, and easing curves
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/render/keyframes/KeyframeEvaluator.kt","utf8"); process.exit(s.includes("object KeyframeEvaluator") && s.includes("fun evaluateTransform") && s.includes("fun interpolate") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: KeyframeEvaluator unit tests compile and pass
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.render.keyframes.KeyframeEvaluatorTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: BUILD SUCCESSFUL in 34s | 29 actionable tasks: 7 executed, 22 up-to-date
