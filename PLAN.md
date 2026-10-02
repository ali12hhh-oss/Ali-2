# Plan: Prodline AI Core Video Editor (Phase 5: Hardware Export Engine & Quality Hub)

Depth: tree 4   Mode: orchestrated
Budget note: Hardware-accelerated export pipeline, 4K/60fps quality presets, and real-time bitrate/filesize estimator

## Contract

Decided BEFORE fan-out:

- Interfaces:
  - `ExportBitrateEstimator`: Computes video bitrate (kbps) and estimated file size (MB) based on resolution, framerate, bitrate quality tier, and duration.
  - `ExportQualityPreset`: Presets for 480p, 720p, 1080p Full HD, 2K, 4K Ultra HD at 24/25/30/50/60 FPS.
  - `HardwareExportEngine`: Manages hardware export pipeline with fallback to FFmpeg command graphs.
- Data ownership:
  - Leaf 5.1.1 owns: `app/src/main/java/com/tharunbirla/librecuts/export/ExportBitrateEstimator.kt`, `app/src/test/java/com/tharunbirla/librecuts/export/ExportBitrateEstimatorTest.kt`
  - Leaf 5.1.2 owns: `app/src/main/java/com/tharunbirla/librecuts/export/HardwareExportEngine.kt`
- Naming and conventions:
  - Package: `com.tharunbirla.librecuts.export`

## Tree and leaf manifest

- 5 Hardware Export Engine & Quality Hub ...................... gates/node-root-5.md
  - 5.1 Export Quality & Hardware Rendering ................... gates/node-5.1.md
    - 5.1.1 Export Bitrate & Size Estimator ................... gates/leaf-5.1.1.md
      - Owns: app/src/main/java/com/tharunbirla/librecuts/export/ExportBitrateEstimator.kt, app/src/test/java/com/tharunbirla/librecuts/export/ExportBitrateEstimatorTest.kt
      - Needs: none
      - Tier: standard
    - 5.1.2 Hardware Export Engine & Preset Config ............ gates/leaf-5.1.2.md
      - Owns: app/src/main/java/com/tharunbirla/librecuts/export/HardwareExportEngine.kt
      - Needs: 5.1.1
      - Tier: standard

## Dispatch schedule

- Wave 1 (immediate): 5.1.1 (Bitrate Estimator) + 5.1.2 (Hardware Export Engine) [launch concurrently]
- Wave 2 (on all leaves verified): run node-5.1 integration gate, then node-root-5 gates

## Status log

- 2026-08-22 15:14 Phase 5 initiated
