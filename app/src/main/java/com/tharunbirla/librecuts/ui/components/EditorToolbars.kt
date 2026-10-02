package com.tharunbirla.librecuts.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tharunbirla.librecuts.R
import com.tharunbirla.librecuts.ui.theme.LibreCutsTheme

enum class ToolbarState {
    MAIN, VIDEO, TEXT, IMAGE, AUDIO, CROP, SPEED, SUBTITLES, BACKGROUND, KEYFRAME, VOICE_OVER
}

@Composable
fun EditorToolbars(
    state: ToolbarState,
    onStateChange: (ToolbarState) -> Unit,
    onAction: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 4.dp
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                // Modes slide in from the bottom edge; leaving modes slide back down.
                (slideInVertically(animationSpec = tween(180)) { it / 3 } + fadeIn(tween(180)))
                    .togetherWith(
                        slideOutVertically(animationSpec = tween(140)) { it / 3 } + fadeOut(tween(140))
                    )
            },
            label = "toolbarMode"
        ) { current ->
            when (current) {
                ToolbarState.MAIN -> MainToolbar(onStateChange, onAction)
                ToolbarState.VIDEO -> VideoToolbar(onStateChange, onAction)
                ToolbarState.TEXT -> TextToolbar(onStateChange, onAction)
                ToolbarState.IMAGE -> ImageToolbar(onStateChange, onAction)
                ToolbarState.AUDIO -> AudioToolbar(onStateChange, onAction)
                ToolbarState.CROP -> CropToolbar(onStateChange, onAction)
                ToolbarState.SPEED -> SpeedToolbar(onStateChange, onAction)
                ToolbarState.SUBTITLES -> SubtitlesToolbar(onStateChange, onAction)
                ToolbarState.BACKGROUND -> BackgroundToolbar(onStateChange, onAction)
                ToolbarState.KEYFRAME -> KeyframeToolbar(onStateChange, onAction)
                ToolbarState.VOICE_OVER -> VoiceOverToolbar(onStateChange, onAction)
            }
        }
    }
}

@Composable
fun MainToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_text_24, stringResource(R.string.tool_text)) { onAction("TEXT_ACTION") }
        ToolbarButton(R.drawable.ic_layers_24, stringResource(R.string.tool_overlay)) { onAction("MEDIA_OVERLAY_ACTION") }
        ToolbarButton(R.drawable.ic_brush_24, stringResource(R.string.tool_draw)) { onAction("DRAW_ACTION") }
        ToolbarButton(R.drawable.ic_audio_24, stringResource(R.string.tool_audio)) { onAction("AUDIO_ACTION") }
        ToolbarButton(R.drawable.ic_mic_24, stringResource(R.string.tool_voice)) { onAction("VOICEOVER_ACTION") }
        ToolbarButton(R.drawable.ic_closed_caption_24, stringResource(R.string.tool_subtitles)) { onAction("SUBTITLES_ACTION") }
        ToolbarButton(R.drawable.ic_crop_24, stringResource(R.string.tool_crop)) { onAction("CROP_ACTION") }
        ToolbarButton(R.drawable.ic_canvas_24, stringResource(R.string.tool_canvas)) { onAction("CANVAS_ACTION") }
    }
}

@Composable
fun VideoToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_back)) { onAction("VIDEO_CANCEL") }
        ToolbarButton(R.drawable.ic_split_24, stringResource(R.string.tool_split)) { onAction("VIDEO_SPLIT") }
        ToolbarButton(R.drawable.ic_timer_24, stringResource(R.string.tool_duration)) { onAction("VIDEO_DURATION") }
        ToolbarButton(R.drawable.ic_cut_24, stringResource(R.string.tool_trim)) { onAction("VIDEO_TRIM") }
        ToolbarButton(R.drawable.ic_speed_24, stringResource(R.string.tool_speed)) { onAction("VIDEO_SPEED") }
        ToolbarButton(R.drawable.ic_delete_24, stringResource(R.string.tool_delete)) { onAction("VIDEO_DELETE") }
        ToolbarButton(R.drawable.ic_audio_24, stringResource(R.string.tool_extract_audio)) { onAction("VIDEO_EXTRACT_AUDIO") }
        ToolbarButton(R.drawable.ic_freeze_24, stringResource(R.string.tool_freeze)) { onAction("VIDEO_FREEZE") }
    }
}

@Composable
fun TextToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("TEXT_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("TEXT_DONE") }
        ToolbarButton(R.drawable.ic_edit_24, stringResource(R.string.tool_edit)) { onAction("TEXT_EDIT") }
        ToolbarButton(R.drawable.ic_timer_24, stringResource(R.string.tool_animate)) { onAction("TEXT_ANIMATE") }
        ToolbarButton(R.drawable.ic_split_24, stringResource(R.string.tool_split)) { onAction("TEXT_SPLIT") }
        ToolbarButton(R.drawable.ic_copy_24, stringResource(R.string.tool_duplicate)) { onAction("TEXT_DUPLICATE") }
        ToolbarButton(R.drawable.ic_delete_24, stringResource(R.string.tool_delete)) { onAction("TEXT_DELETE") }
    }
}

@Composable
fun ImageToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("IMAGE_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("IMAGE_DONE") }
        ToolbarButton(R.drawable.ic_split_24, stringResource(R.string.tool_split)) { onAction("IMAGE_SPLIT") }
        ToolbarButton(R.drawable.ic_copy_24, stringResource(R.string.tool_duplicate)) { onAction("IMAGE_DUPLICATE") }
        ToolbarButton(R.drawable.ic_delete_24, stringResource(R.string.tool_delete)) { onAction("IMAGE_DELETE") }
    }
}

@Composable
fun AudioToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("AUDIO_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("AUDIO_DONE") }
        ToolbarButton(R.drawable.ic_volume_up_24, stringResource(R.string.tool_volume)) { onAction("AUDIO_VOLUME") }
        ToolbarButton(R.drawable.ic_copy_24, stringResource(R.string.tool_duplicate)) { onAction("AUDIO_DUPLICATE") }
        ToolbarButton(R.drawable.ic_delete_24, stringResource(R.string.tool_delete)) { onAction("AUDIO_DELETE") }
    }
}

@Composable
fun CropToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("CROP_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("CROP_DONE") }
        ToolbarButton(R.drawable.ic_crop_24, stringResource(R.string.tool_reset)) { onAction("CROP_RESET") }
    }
}

@Composable
fun SpeedToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("SPEED_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("SPEED_DONE") }
    }
}

@Composable
fun SubtitlesToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("SUBTITLES_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("SUBTITLES_DONE") }
    }
}

@Composable
fun BackgroundToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("BACKGROUND_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("BACKGROUND_DONE") }
    }
}

@Composable
fun KeyframeToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("KEYFRAME_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("KEYFRAME_DONE") }
    }
}

@Composable
fun VoiceOverToolbar(onStateChange: (ToolbarState) -> Unit, onAction: (String) -> Unit) {
    ToolbarRow {
        ToolbarButton(R.drawable.ic_back_24, stringResource(R.string.lbl_cancel)) { onAction("VOICEOVER_CANCEL") }
        ToolbarButton(R.drawable.ic_check_24, stringResource(R.string.lbl_done), emphasized = true) { onAction("VOICEOVER_DONE") }
    }
}

@Composable
private fun ToolbarRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
fun ToolbarButton(iconRes: Int, text: String, emphasized: Boolean = false, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val tint = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val labelTint = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            })
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = tint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = labelTint
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditorToolbarsPreview() {
    LibreCutsTheme {
        EditorToolbars(state = ToolbarState.MAIN, onStateChange = {}, onAction = {})
    }
}
