package com.tharunbirla.librecuts.timeline

import android.net.Uri
import com.tharunbirla.librecuts.models.EditOperation
import com.tharunbirla.librecuts.models.TextPosition
import com.tharunbirla.librecuts.models.VideoProject
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito

class ProjectMigrationAdapterTest {

    private fun createMockUri(uriString: String): Uri {
        val mockUri = Mockito.mock(Uri::class.java)
        Mockito.`when`(mockUri.toString()).thenReturn(uriString)
        return mockUri
    }

    @Test
    fun testLegacyProjectToTimelineState() {
        val mockBaseUri = createMockUri("file:///base.mp4")
        val legacyProject = VideoProject(
            sourceUri = mockBaseUri,
            sourceName = "base.mp4",
            operations = listOf(
                EditOperation.Trim(1000L, 9000L),
                EditOperation.SpeedMain(1.5f),
                EditOperation.AddText(
                    text = "Sample Title",
                    fontSize = 28,
                    position = TextPosition.CENTER,
                    relativeX = 0.5f,
                    relativeY = 0.3f,
                    color = "#FF0000"
                )
            )
        )

        val state = ProjectMigrationAdapter.toTimelineState(legacyProject)
        assertEquals(1, state.mainTrack.clips.size)

        val baseClip = state.mainTrack.clips[0]
        assertEquals("file:///base.mp4", baseClip.source.uriString)
        assertEquals(1000L, baseClip.sourceRange.startMs)
        assertEquals(9000L, baseClip.sourceRange.endMs)
        assertEquals(1.5f, baseClip.speed)

        assertEquals(1, state.overlayTracks.size)
        assertEquals(1, state.overlayTracks[0].overlays.size)
        val textOverlay = state.overlayTracks[0].overlays[0]
        assertEquals("Sample Title", textOverlay.textConfig?.text)
        assertEquals(0.5f, textOverlay.transform.relativeX)
        assertEquals(0.3f, textOverlay.transform.relativeY)
    }

    @Test
    fun testTimelineStateToLegacyProjectConversion() {
        val mockUri = createMockUri("file:///video.mp4")
        val source = MediaSource(uriString = "file:///video.mp4", name = "video.mp4", naturalDurationMs = 15000L)
        val clip = VideoClip(source = source, sourceRange = TimeRange(2000L, 12000L), speed = 2.0f)
        val state = TimelineState(
            canvas = CanvasConfig(aspectRatio = AspectRatio.RATIO_16_9),
            mainTrack = MainVideoTrack(clips = listOf(clip))
        )

        val legacyProject = ProjectMigrationAdapter.toVideoProject(state, customSourceUri = mockUri)
        assertEquals("video.mp4", legacyProject.sourceName)

        val trim = legacyProject.operations.filterIsInstance<EditOperation.Trim>().firstOrNull()
        assertNotNull(trim)
        assertEquals(2000L, trim?.startMs)
        assertEquals(12000L, trim?.endMs)

        val speed = legacyProject.operations.filterIsInstance<EditOperation.SpeedMain>().firstOrNull()
        assertNotNull(speed)
        assertEquals(2.0f, speed?.speed)

        val crop = legacyProject.operations.filterIsInstance<EditOperation.Crop>().firstOrNull()
        assertNotNull(crop)
        assertEquals("16:9", crop?.aspectRatio)
    }
}
