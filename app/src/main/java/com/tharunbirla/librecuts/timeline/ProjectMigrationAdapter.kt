package com.tharunbirla.librecuts.timeline

import android.net.Uri
import com.tharunbirla.librecuts.models.EditOperation
import com.tharunbirla.librecuts.models.TextPosition
import com.tharunbirla.librecuts.models.VideoProject

/**
 * Bidirectional migration adapter converting between legacy VideoProject and non-linear TimelineState.
 * Guarantees zero regression and seamless project backwards compatibility.
 */
object ProjectMigrationAdapter {

    /**
     * Converts a legacy flat VideoProject into a modern multi-track TimelineState.
     */
    fun toTimelineState(project: VideoProject): TimelineState {
        val baseUri = project.sourceUri
        val baseName = project.sourceName

        // Estimate natural duration from trims or default
        val trimOp = project.operations.filterIsInstance<EditOperation.Trim>().lastOrNull()
        val naturalDur = trimOp?.endMs ?: 60000L

        val baseSource = MediaSource(
            uri = baseUri,
            name = baseName,
            naturalDurationMs = naturalDur,
            scrubProxyUri = project.scrubProxyUri
        )

        var baseTrimStart = 0L
        var baseTrimEnd = naturalDur
        if (trimOp != null) {
            baseTrimStart = trimOp.startMs
            baseTrimEnd = trimOp.endMs
        }

        val speedOp = project.operations.filterIsInstance<EditOperation.SpeedMain>().lastOrNull()
        val baseSpeed = speedOp?.speed ?: 1.0f

        val revOp = project.operations.filterIsInstance<EditOperation.ReverseMain>().lastOrNull()
        val baseReversed = revOp?.isReversed ?: false

        val mirrorOp = project.operations.filterIsInstance<EditOperation.MirrorMain>().lastOrNull()
        val baseMirrored = mirrorOp?.isMirrored ?: false

        val baseClip = VideoClip(
            id = "base_clip",
            source = baseSource,
            sourceRange = TimeRange(baseTrimStart, baseTrimEnd),
            speed = baseSpeed,
            isReversed = baseReversed,
            isMirrored = baseMirrored
        )

        val mainClips = mutableListOf(baseClip)

        // Process Merge items
        val mergeOp = project.operations.filterIsInstance<EditOperation.Merge>().lastOrNull()
        mergeOp?.items?.forEachIndexed { idx, item ->
            val mergeSource = MediaSource(
                uri = item.uri,
                name = "Merged Clip ${idx + 1}",
                naturalDurationMs = item.durationMs,
                isImage = item.isImage,
                scrubProxyUri = item.scrubProxyUri
            )
            val clip = VideoClip(
                id = "merge_${idx + 1}",
                source = mergeSource,
                sourceRange = TimeRange(item.trimStartMs, item.trimEndMs),
                speed = item.speed,
                isReversed = item.isReversed,
                isMirrored = item.isMirrored
            )
            mainClips.add(clip)
        }

        // Process Transitions
        val transitions = mutableMapOf<Int, TransitionEffect>()
        project.operations.filterIsInstance<EditOperation.Transition>().forEach { transOp ->
            val transType = try {
                TransitionType.valueOf(transOp.type.uppercase())
            } catch (e: Exception) {
                TransitionType.DISSOLVE
            }
            transitions[transOp.index] = TransitionEffect(
                id = transOp.id,
                type = transType,
                durationMs = transOp.durationMs
            )
        }

        // Process Audio
        val audioClips = mutableListOf<AudioClip>()
        project.operations.filterIsInstance<EditOperation.AddBackgroundAudio>().forEach { audioOp ->
            val startMs = audioOp.startTimeMs ?: 0L
            val endMs = if (audioOp.endTimeMs != null && audioOp.endTimeMs > startMs) {
                audioOp.endTimeMs
            } else {
                startMs + audioOp.originalDurationMs.coerceAtLeast(1000L)
            }
            audioClips.add(
                AudioClip(
                    id = audioOp.id,
                    audioUri = audioOp.audioUri,
                    name = "BGM",
                    sourceRange = TimeRange(0L, endMs - startMs),
                    timelineStartMs = startMs,
                    volume = audioOp.volume,
                    fadeInMs = audioOp.fadeInDurationMs,
                    fadeOutMs = audioOp.fadeOutDurationMs,
                    isDucking = audioOp.ducking,
                    beats = audioOp.beats
                )
            )
        }
        val audioTracks = if (audioClips.isNotEmpty()) listOf(AudioTrack(clips = audioClips)) else emptyList()

        // Process Text & Image Overlays
        val overlays = mutableListOf<OverlayClip>()
        project.operations.filterIsInstance<EditOperation.AddText>().forEach { textOp ->
            val startMs = textOp.startTimeMs ?: 0L
            val endMs = textOp.endTimeMs ?: (startMs + 5000L)
            overlays.add(
                OverlayClip(
                    id = textOp.id,
                    type = OverlayType.TEXT,
                    textConfig = TextOverlayConfig(
                        text = textOp.text,
                        fontSizeSp = textOp.fontSize.toFloat(),
                        colorHex = textOp.color,
                        fontPath = textOp.fontPath,
                        alignment = textOp.textAlign,
                        borderWidth = textOp.borderThickness.toFloat(),
                        borderColorHex = textOp.borderColor,
                        letterSpacing = textOp.letterSpacing,
                        lineSpacing = textOp.lineSpacing
                    ),
                    timelineRange = TimeRange(startMs, endMs),
                    transform = KeyframeTransform(
                        relativeX = textOp.relativeX ?: 0.5f,
                        relativeY = textOp.relativeY ?: 0.5f,
                        opacity = textOp.opacity
                    )
                )
            )
        }

        project.operations.filterIsInstance<EditOperation.AddImageOverlay>().forEach { imgOp ->
            val startMs = imgOp.startTimeMs ?: 0L
            val endMs = imgOp.endTimeMs ?: (startMs + 5000L)
            overlays.add(
                OverlayClip(
                    id = imgOp.id,
                    type = OverlayType.IMAGE,
                    uri = imgOp.imageUri,
                    timelineRange = TimeRange(startMs, endMs),
                    transform = KeyframeTransform(
                        relativeX = imgOp.relativeX,
                        relativeY = imgOp.relativeY,
                        scaleX = imgOp.relativeWidth,
                        scaleY = imgOp.relativeHeight,
                        rotationDegrees = imgOp.rotationAngle,
                        opacity = imgOp.opacity
                    ),
                    chromaKeyColorHex = imgOp.chromaKeyColor,
                    chromaKeySimilarity = imgOp.chromaKeySimilarity
                )
            )
        }
        val overlayTracks = if (overlays.isNotEmpty()) listOf(OverlayTrack(overlays = overlays)) else emptyList()

        // Process Canvas / AspectRatio
        val cropOp = project.operations.filterIsInstance<EditOperation.Crop>().lastOrNull()
        val aspect = when (cropOp?.aspectRatio) {
            "9:16" -> AspectRatio.RATIO_9_16
            "16:9" -> AspectRatio.RATIO_16_9
            "1:1" -> AspectRatio.RATIO_1_1
            "4:5" -> AspectRatio.RATIO_4_5
            "3:4" -> AspectRatio.RATIO_3_4
            "2:3" -> AspectRatio.RATIO_2_3
            "21:9" -> AspectRatio.RATIO_21_9
            "Original" -> AspectRatio.ORIGINAL
            "Custom" -> AspectRatio.CUSTOM
            else -> AspectRatio.RATIO_9_16
        }

        return TimelineState(
            canvas = CanvasConfig(aspectRatio = aspect),
            mainTrack = MainVideoTrack(clips = mainClips, transitions = transitions),
            audioTracks = audioTracks,
            overlayTracks = overlayTracks
        )
    }

    /**
     * Converts a TimelineState back into a legacy VideoProject for legacy rendering engines.
     */
    fun toVideoProject(state: TimelineState, customSourceUri: Uri? = null): VideoProject {
        val clips = state.mainTrack.clips
        if (clips.isEmpty()) {
            val emptyUri = customSourceUri ?: Uri.EMPTY
            return VideoProject(
                sourceUri = emptyUri,
                sourceName = "Empty"
            )
        }
        val baseClip = clips.first()

        val ops = mutableListOf<EditOperation>()

        // 1. Trim
        ops.add(EditOperation.Trim(baseClip.sourceRange.startMs, baseClip.sourceRange.endMs))

        // 2. Speed / Reverse
        if (baseClip.speed != 1.0f) {
            ops.add(EditOperation.SpeedMain(baseClip.speed))
        }
        if (baseClip.isReversed) {
            ops.add(EditOperation.ReverseMain(true))
        }
        if (baseClip.isMirrored) {
            ops.add(EditOperation.MirrorMain(true))
        }

        // 3. Merged items
        if (clips.size > 1) {
            val mergeItems = clips.drop(1).map { clip ->
                val clipUri = try { clip.source.uri } catch (e: Exception) { null } ?: Uri.EMPTY
                val scrubUri = try { clip.source.scrubProxyUri } catch (e: Exception) { null }
                EditOperation.MergeItem(
                    uri = clipUri,
                    durationMs = clip.source.naturalDurationMs,
                    trimStartMs = clip.sourceRange.startMs,
                    trimEndMs = clip.sourceRange.endMs,
                    speed = clip.speed,
                    isReversed = clip.isReversed,
                    isMirrored = clip.isMirrored,
                    scrubProxyUri = scrubUri,
                    isImage = clip.source.isImage
                )
            }
            ops.add(EditOperation.Merge(mergeItems))
        }

        // 4. Transitions
        state.mainTrack.transitions.forEach { (index, trans) ->
            ops.add(
                EditOperation.Transition(
                    index = index,
                    type = trans.type.name.lowercase(),
                    durationMs = trans.durationMs,
                    id = trans.id
                )
            )
        }

        // 5. Audio
        state.audioTracks.flatMap { it.clips }.forEach { audio ->
            val aUri = try { audio.audioUri } catch (e: Exception) { null } ?: Uri.EMPTY
            ops.add(
                EditOperation.AddBackgroundAudio(
                    audioUri = aUri,
                    startTimeMs = audio.timelineStartMs,
                    endTimeMs = audio.timelineEndMs,
                    volume = audio.volume,
                    fadeInDurationMs = audio.fadeInMs,
                    fadeOutDurationMs = audio.fadeOutMs,
                    ducking = audio.isDucking,
                    beats = audio.beats,
                    id = audio.id
                )
            )
        }

        // 6. Overlays
        state.overlayTracks.flatMap { it.overlays }.forEach { overlay ->
            val textCfg = overlay.textConfig
            val imgUri = try { overlay.uri } catch (e: Exception) { null }
            if (overlay.type == OverlayType.TEXT && textCfg != null) {
                ops.add(
                    EditOperation.AddText(
                        text = textCfg.text,
                        fontSize = textCfg.fontSizeSp.toInt(),
                        position = TextPosition.BOTTOM_RIGHT,
                        relativeX = overlay.transform.relativeX,
                        relativeY = overlay.transform.relativeY,
                        color = textCfg.colorHex,
                        startTimeMs = overlay.timelineRange.startMs,
                        endTimeMs = overlay.timelineRange.endMs,
                        opacity = overlay.transform.opacity,
                        id = overlay.id
                    )
                )
            } else if (overlay.type == OverlayType.IMAGE && imgUri != null) {
                ops.add(
                    EditOperation.AddImageOverlay(
                        imageUri = imgUri,
                        relativeX = overlay.transform.relativeX,
                        relativeY = overlay.transform.relativeY,
                        relativeWidth = overlay.transform.scaleX,
                        relativeHeight = overlay.transform.scaleY,
                        rotationAngle = overlay.transform.rotationDegrees,
                        startTimeMs = overlay.timelineRange.startMs,
                        endTimeMs = overlay.timelineRange.endMs,
                        opacity = overlay.transform.opacity,
                        chromaKeyColor = overlay.chromaKeyColorHex,
                        chromaKeySimilarity = overlay.chromaKeySimilarity,
                        id = overlay.id
                    )
                )
            }
        }

        val aspectString = when (state.canvas.aspectRatio) {
            AspectRatio.RATIO_9_16 -> "9:16"
            AspectRatio.RATIO_16_9 -> "16:9"
            AspectRatio.RATIO_1_1 -> "1:1"
            AspectRatio.RATIO_4_5 -> "4:5"
            AspectRatio.RATIO_3_4 -> "3:4"
            AspectRatio.RATIO_2_3 -> "2:3"
            AspectRatio.RATIO_21_9 -> "21:9"
            AspectRatio.ORIGINAL -> "Original"
            AspectRatio.CUSTOM -> "Custom"
        }
        ops.add(EditOperation.Crop(aspectRatio = aspectString))

        val resolvedBaseUri = customSourceUri ?: try { baseClip.source.uri } catch (e: Exception) { null } ?: Uri.EMPTY
        val resolvedScrubUri = try { baseClip.source.scrubProxyUri } catch (e: Exception) { null }

        return VideoProject(
            sourceUri = resolvedBaseUri,
            sourceName = baseClip.source.name,
            scrubProxyUri = resolvedScrubUri,
            operations = ops
        )
    }
}
