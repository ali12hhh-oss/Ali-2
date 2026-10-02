# Changelog

All notable changes to **Prodline AI** (`wiki8106work`) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0-prodline] - 2026-08-22

### Upstream Origin & Attribution
* **Base Project**: Forked and evolved from [LibreCuts](https://github.com/tharunbirla/LibreCuts) by Tharun Birla.
* **Maintainer / Development**: Maintained and extended by **Vicky8106** (`wiki8106work`).

### Added
- **Multi-Format Media Import (Images & Photos)**:
  - Added full support for importing static image files (`.jpg`, `.jpeg`, `.png`, `.webp`, `.bmp`, `.heic`) as primary timeline clips.
  - Implemented automatic image-to-timeline duration mapping with configurable clip display durations (default 3.0s).
  - Added seamless aspect-ratio fitting and canvas scaling for mixed video and image projects.
- **Tabbed Media Picker (`MediaPickerBottomSheet`)**:
  - Added dedicated navigation tabs for **All**, **Videos**, and **Photos**.
  - Added visual selection indicators (`bg_media_selected_border.xml`), item badges, and real-time selection counters.
  - Implemented asynchronous thumbnail rendering with memory-optimized bitmap cache.
- **Enhanced In-App File Explorer (`CustomFileExplorerBottomSheet`)**:
  - Added breadcrumb navigation path with instant parent directory traversal.
  - Added smart filtering for Video, Photo, and Audio file types.
  - Added directory sorting (folders-first, alphabetical file order) and folder depth navigation.
- **Robust Timestamp & Duration Parsing (`TimestampParser`)**:
  - Introduced `TimestampParser` utility supporting various timestamp formats (`HH:MM:SS.mmm`, `MM:SS`, raw milliseconds, microsecond FFmpeg formats, float seconds).
  - Added automatic fallback metadata extraction for variable frame rate (VFR) streams and audio/video track reconciliation.
  - Added unit test suite (`TimestampParserTest.kt`) covering edge cases, zero-durations, and invalid time string formats.
- **Project Rebranding & Modernization**:
  - Rebranded application to **Prodline AI**.
  - Updated repository links, issue templates, and localized resource strings.

### Changed
- Refactored `MainActivity.kt` and `VideoEditingActivity.kt` media loading pipelines to accept both video and static image URIs transparently.
- Improved memory management during batch media selection to prevent OutOfMemory errors on high-resolution image sets.
- Updated default export directories to `Movies/ProdlineAI`, `Pictures/ProdlineAI`, and `Music/ProdlineAI`.
- Streamlined GitHub workflow issue templates and crash reporting endpoints to point to `Vicky8106/Prodline-AI`.

### Fixed
- Fixed crash/unhandled exception when importing photo assets into the video timeline.
- Fixed duration calculation discrepancies between audio tracks and video frames in multi-clip timelines.
- Fixed thumbnail flickering when switching media picker tabs.
