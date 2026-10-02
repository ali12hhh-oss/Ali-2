# Prodline AI production-readiness roadmap

Prodline AI is being developed as an original, offline short-form video editor. This roadmap targets feature parity in user outcomes with mainstream mobile editors; it does not reproduce another product's branding, artwork, or proprietary templates.

## Current capability matrix

| Area | Existing implementation | Verification state | Next production work |
| --- | --- | --- | --- |
| Import and project setup | Video/image/audio picker, file explorer, image-sequence support | Debug unit suite passes | Persist URI access, malformed-media recovery, instrumented import tests |
| Timeline editing | Trim, split, reorder, multi-clip tracks, snapping, zoom, undo/redo | Timeline math and coordinate tests | Gesture tests, preview/render equivalence, accessibility actions |
| Canvas and transforms | Crop, aspect ratios, canvas backgrounds, PiP/image/text overlays, masks, keyframes | Partial unit coverage | Typed transform model, consistent preview/export rendering |
| Audio | Music, voice-over, waveform, fades, ducking, mute and MP3 export | Audio mix tests | Mixing/export integration tests and interruption recovery |
| Visual finishing | Filters, colour adjustments, transitions, speed, freeze, reverse, draw, subtitles | Partial unit coverage | Deterministic render tests and content validation |
| Export reliability | Resolution/FPS selection, bitrate estimate, MediaCodec preference and FFmpeg fallback | Export estimator tests | Validated typed export plans, codec/device capability detection, cancellation and storage handling |
| Product quality | Material UI, sharing, foreground export service, saved projects | Debug unit suite passes | Accessibility audit, lifecycle recovery, telemetry-free error reports, release signing and device matrix |

## Delivery milestones

1. **Foundation:** harden the current timeline/export changes with typed configuration, validation, and automated tests.
2. **Editing reliability:** make preview, timeline state, project serialization, and final rendering agree under undo/redo, process death, and malformed inputs.
3. **Creator workflow:** finish overlays, audio, subtitles, templates, and project recovery with clear empty/error/loading states.
4. **Release readiness:** run device/API-level validation, accessibility checks, export stress tests, privacy review, and signed-release checks.

## Export reliability

An export request must be validated before a render starts. Resolution, frame rate, codec, bitrate, and output dimensions belong in one typed module. The UI should consume a display estimate from that module instead of duplicating codec rules. The renderer should receive only a validated plan, which keeps invalid combinations out of the FFmpeg/MediaCodec layers.

## Deferred: native Compose timeline rewrite

The multi-track timeline, trim stems and overlay views stay as hardened custom Views hosted through AndroidView interop. Rationale: they are load-bearing gesture surfaces that were just stabilized in Phase 10; rewriting them blind (no attached device for interaction testing) risks regressing core editing for zero user-visible gain. A native Compose timeline remains the follow-up milestone once device testing is available.
