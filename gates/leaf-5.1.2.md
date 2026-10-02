# Gates: Hardware Export Engine & Preset Config (Leaf 5.1.2)

Scope: Implement hardware export engine configuration with resolution and framerate quality presets

- [x] G1: HardwareExportEngine.kt exists with ExportConfig, ExportResolution, and buildExportCommand methods
  CHECK: node -e 'const fs=require("fs"); const s=fs.readFileSync("app/src/main/java/com/tharunbirla/librecuts/export/HardwareExportEngine.kt","utf8"); process.exit(s.includes("class HardwareExportEngine") && s.includes("enum class ExportResolution") && s.includes("fun buildExportParameters") ? 0 : 1);'
  EXPECT: /.*/
  EVIDENCE: (no output)
