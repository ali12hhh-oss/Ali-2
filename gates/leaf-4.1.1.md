# Gates: Audio Mix & Fade Envelope Engine (Leaf 4.1.1)

Scope: Implement audio volume calculation, fade in/out curves, and ducking attenuation math

- [x] G1: AudioMixEngine.kt exists with computeEffectiveVolume, calculateFadeGain, and applyDucking methods
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/audio/AudioMixEngine.kt","utf8"); process.exit(s.includes("fun computeEffectiveVolume") && s.includes("fun calculateFadeGain") && s.includes("fun applyDucking") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: AudioMixEngine unit tests compile and pass
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.audio.AudioMixEngineTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: BUILD SUCCESSFUL in 59s | 29 actionable tasks: 1 executed, 28 up-to-date
