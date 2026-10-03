package com.tharunbirla.librecuts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tharunbirla.librecuts.R
import com.tharunbirla.librecuts.ui.theme.AccentCoral

/** A draft project stored in the app-private drafts directory. */
data class DraftProject(
    val name: String,
    val path: String,
    val lastModified: Long
)

data class HomeTemplate(val id: String, val title: String, val subtitle: String, val iconRes: Int, val accent: Color)

data class DashboardUiState(
    val selectedTab: Int = 0,
    val videoFolderLabel: String = "",
    val audioFolderLabel: String = "",
    val snapshotFolderLabel: String = "",
    val languageLabel: String = "",
    val hapticEnabled: Boolean = true,
    val fullscreenEditor: Boolean = true,
    val encoderHardware: Boolean = true,
    val versionName: String = "",
    val drafts: List<DraftProject> = emptyList(),
    val templates: List<HomeTemplate> = emptyList()
)

class DashboardCallbacks(
    val onNewProject: () -> Unit,
    val onTemplateSelected: (HomeTemplate) -> Unit,
    val onOpenProject: () -> Unit,
    val onDraftOpen: (DraftProject) -> Unit,
    val onDraftDelete: (DraftProject) -> Unit,
    val onSelectVideoFolder: () -> Unit,
    val onSelectAudioFolder: () -> Unit,
    val onSelectSnapshotFolder: () -> Unit,
    val onLanguageClick: () -> Unit,
    val onToggleHaptic: () -> Unit,
    val onToggleFullscreen: () -> Unit,
    val onEncoderClick: () -> Unit,
    val onCheckUpdates: () -> Unit,
    val onOpenLicenses: () -> Unit,
    val onOpenUrl: (String) -> Unit,
    val onTabSelected: (Int) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    callbacks: DashboardCallbacks
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = callbacks.onLanguageClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_language_24),
                            contentDescription = stringResource(R.string.cd_change_language),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                listOf(
                    stringResource(R.string.tab_home) to R.drawable.ic_home_24,
                    stringResource(R.string.tab_settings) to R.drawable.ic_settings_24,
                    stringResource(R.string.tab_about) to R.drawable.ic_info_24
                ).forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = uiState.selectedTab == index,
                        onClick = { callbacks.onTabSelected(index) },
                        icon = { Icon(painterResource(icon), contentDescription = label) },
                        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (uiState.selectedTab) {
                0 -> HomeContent(uiState, callbacks)
                1 -> SettingsContent(uiState, callbacks)
                else -> AboutContent(uiState, callbacks)
            }
        }
    }
}

// ── Home ─────────────────────────────────────────────────────────────────────

@Composable
private fun HomeContent(uiState: DashboardUiState, callbacks: DashboardCallbacks) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TemplatesSection(uiState.templates, callbacks.onTemplateSelected, callbacks.onNewProject)
        }
        item {
            ActionCard(
                iconRes = R.drawable.ic_folder_24,
                title = stringResource(R.string.btn_open_project),
                subtitle = stringResource(R.string.open_project_subtitle),
                onClick = callbacks.onOpenProject
            )
        }
        if (uiState.drafts.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.drafts_section),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.drafts, key = { it.path }) { draft ->
                DraftRow(draft, callbacks)
            }
        } else {
            item {
                EmptyDraftsCard()
            }
        }
    }
}

@Composable
private fun EmptyDraftsCard() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_folder_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.drafts_empty_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.drafts_empty_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TemplatesSection(templates: List<HomeTemplate>, onTemplateSelected: (HomeTemplate) -> Unit, onNewProject: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.home_templates_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(stringResource(R.string.home_templates_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.home_templates_see_all), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable(onClick = onNewProject))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(end = 4.dp)) {
            items(templates, key = { it.id }) { template -> TemplateCard(template) { onTemplateSelected(template) } }
        }
    }
}

@Composable
private fun TemplateCard(template: HomeTemplate, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = Modifier.size(width = 164.dp, height = 188.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(118.dp).background(template.accent, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(82.dp).background(Color.Black.copy(alpha = 0.18f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Icon(painterResource(template.iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(template.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(template.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
@Composable
private fun ActionCard(iconRes: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                painterResource(R.drawable.ic_chevron_right_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DraftRow(draft: DraftProject, callbacks: DashboardCallbacks) {
    Card(
        onClick = { callbacks.onDraftOpen(draft) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_clips_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    draft.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
                        .format(java.util.Date(draft.lastModified)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { callbacks.onDraftDelete(draft) }
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete_24),
                    contentDescription = stringResource(R.string.delete),
                    tint = AccentCoral
                )
            }
        }
    }
}

// ── Settings ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsContent(uiState: DashboardUiState, callbacks: DashboardCallbacks) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item { SectionLabel(stringResource(R.string.settings_export_section)) }
        item {
            SettingRow(
                iconRes = R.drawable.ic_folder_24,
                title = stringResource(R.string.str_video_export_folder),
                value = uiState.videoFolderLabel,
                onClick = callbacks.onSelectVideoFolder
            )
        }
        item {
            SettingRow(
                iconRes = R.drawable.ic_audio_24,
                title = stringResource(R.string.str_audio_export_folder),
                value = uiState.audioFolderLabel,
                onClick = callbacks.onSelectAudioFolder
            )
        }
        item {
            SettingRow(
                iconRes = R.drawable.ic_image_24,
                title = stringResource(R.string.str_snapshot_export_folder),
                value = uiState.snapshotFolderLabel,
                onClick = callbacks.onSelectSnapshotFolder
            )
        }
        item { SectionLabel(stringResource(R.string.settings_editor_section)) }
        item {
            SettingRow(
                iconRes = R.drawable.ic_language_24,
                title = stringResource(R.string.btn_change_language),
                value = uiState.languageLabel,
                onClick = callbacks.onLanguageClick
            )
        }
        item {
            ToggleRow(
                iconRes = R.drawable.ic_bolt_24,
                title = stringResource(R.string.btn_toggle_haptic_feedback),
                checked = uiState.hapticEnabled,
                onToggle = callbacks.onToggleHaptic
            )
        }
        item {
            ToggleRow(
                iconRes = R.drawable.ic_fullscreen_24,
                title = stringResource(R.string.btn_toggle_fullscreen_editor),
                checked = uiState.fullscreenEditor,
                onToggle = callbacks.onToggleFullscreen
            )
        }
        item {
            SettingRow(
                iconRes = R.drawable.ic_bolt_24,
                title = stringResource(R.string.str_default_encoder),
                value = if (uiState.encoderHardware) {
                    stringResource(R.string.str_encoder_hardware)
                } else {
                    stringResource(R.string.str_encoder_software)
                },
                onClick = callbacks.onEncoderClick
            )
        }
        item { SectionLabel(stringResource(R.string.settings_general_section)) }
        item {
            SettingRow(
                iconRes = R.drawable.ic_refresh_24,
                title = stringResource(R.string.btn_check_for_updates),
                value = "",
                onClick = callbacks.onCheckUpdates
            )
        }
        item {
            SettingRow(
                iconRes = R.drawable.ic_page_info_24,
                title = stringResource(R.string.str_open_source_licenses),
                value = "",
                onClick = callbacks.onOpenLicenses
            )
        }
    }
}

// ── About ────────────────────────────────────────────────────────────────────

@Composable
private fun AboutContent(uiState: DashboardUiState, callbacks: DashboardCallbacks) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = uiState.versionName.ifEmpty { "v1.0" },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        AboutLink(R.drawable.ic_github_24, stringResource(R.string.btn_star_github)) {
            callbacks.onOpenUrl("https://github.com/Vicky8106/Prodline-AI")
        }
        AboutLink(R.drawable.ic_translate_24, stringResource(R.string.btn_translate)) {
            callbacks.onOpenUrl("https://hosted.weblate.org/engage/librecuts/")
        }
        AboutLink(R.drawable.ic_bug_24, stringResource(R.string.btn_report_bug)) {
            callbacks.onOpenUrl("https://github.com/Vicky8106/Prodline-AI/issues")
        }
        AboutLink(R.drawable.ic_heart_24, stringResource(R.string.btn_sponsor)) {
            callbacks.onOpenUrl("https://github.com/Vicky8106/Prodline-AI#support")
        }
    }
}

@Composable
private fun AboutLink(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ── Shared pieces ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingRow(iconRes: Int, title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painterResource(R.drawable.ic_chevron_right_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ToggleRow(iconRes: Int, title: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}
