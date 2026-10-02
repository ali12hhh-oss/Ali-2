# Gates: Waveform Generator & Sound Effects Bank (Leaf 4.1.2)

Scope: Implement audio waveform generator and categorized royalty-free SFX sound bank

- [x] G1: AudioWaveformGenerator.kt and SoundEffectsBank.kt exist with resampleAmplitudes and getCategoryList
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/audio/AudioWaveformGenerator.kt","utf8"); const s2=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/audio/SoundEffectsBank.kt","utf8"); process.exit(s.includes("fun resampleAmplitudes") && s2.includes("fun getCategoryList") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
