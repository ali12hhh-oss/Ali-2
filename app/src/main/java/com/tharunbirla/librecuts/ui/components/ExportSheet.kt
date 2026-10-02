package com.tharunbirla.librecuts.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tharunbirla.librecuts.R

private val RESOLUTIONS = listOf(360, 480, 720, 1080, 1440, 2160)
private val FPS_OPTIONS = listOf(24, 25, 30, 50, 60)

/**
 * Material 3 export options sheet: resolution, frame rate, audio-only toggle,
 * live size estimate and directory selection.
 *
 * Size estimation and directory lookup stay on the activity side (they need
 * project state); this composable stays pure UI plus selection callbacks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSheet(
    initialResolution: Int,
    initialFps: Int,
    initialAudioOnly: Boolean,
    estimatedSizeText: String,
    directoryTitle: String,
    directoryPath: String,
    onDismiss: () -> Unit,
    onSettingsChanged: (resolution: Int, fps: Int, audioOnly: Boolean) -> Unit,
    onPickDirectory: () -> Unit,
    onExport: () -> Unit,
    onSaveProject: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var resolution by remember { mutableIntStateOf(initialResolution.takeIf { it in RESOLUTIONS } ?: 1080) }
    var fps by remember { mutableIntStateOf(initialFps.takeIf { it in FPS_OPTIONS } ?: 30) }
    var audioOnly by remember { mutableStateOf(initialAudioOnly) }

    fun notify() = onSettingsChanged(resolution, fps, audioOnly)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Export",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            ChipGroup("Resolution", RESOLUTIONS.map { "${it}p" }, RESOLUTIONS.indexOf(resolution)) { index ->
                resolution = RESOLUTIONS[index]
                notify()
            }
            Spacer(Modifier.height(8.dp))
            ChipGroup("Frame rate", FPS_OPTIONS.map { "$it" }, FPS_OPTIONS.indexOf(fps)) { index ->
                fps = FPS_OPTIONS[index]
                notify()
            }
            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Audio only",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Export just the sound track",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = audioOnly, onCheckedChange = {
                    audioOnly = it
                    notify()
                })
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        directoryTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        directoryPath,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                TopBarIconAction(
                    iconRes = R.drawable.ic_folder_24,
                    contentDescription = "Choose folder",
                    onClick = onPickDirectory
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Estimated size",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    estimatedSizeText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    onDismiss()
                    onExport()
                },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Export video", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    onDismiss()
                    onSaveProject()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save project", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChipGroup(title: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, label ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
