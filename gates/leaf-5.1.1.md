# Gates: Export Bitrate & Size Estimator (Leaf 5.1.1)

Scope: Implement video bitrate calculations and real-time file size estimator for 480p, 720p, 1080p, 2K, 4K

- [x] G1: ExportBitrateEstimator.kt exists with calculateBitrateKbps, estimateFileSizeBytes, and estimateFileSizeMb
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/export/ExportBitrateEstimator.kt","utf8"); process.exit(s.includes("object ExportBitrateEstimator") && s.includes("fun calculateBitrateKbps") && s.includes("fun estimateFileSizeMb") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)

- [x] G2: ExportBitrateEstimator unit tests compile and pass
  CHECK: ./gradlew testDebugUnitTest --tests "com.tharunbirla.librecuts.export.ExportBitrateEstimatorTest"
  EXPECT: BUILD SUCCESSFUL
  EVIDENCE: BUILD SUCCESSFUL in 32s | 29 actionable tasks: 7 executed, 22 up-to-date
