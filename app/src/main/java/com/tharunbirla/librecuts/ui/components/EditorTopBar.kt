package com.tharunbirla.librecuts.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tharunbirla.librecuts.R

/**
 * Compose replacement for the legacy XML top action bar: back navigation,
 * undo/redo with live enablement, and the export action.
 */
@Composable
fun EditorTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    exportEnabled: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onExport: () -> Unit,
    busy: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TopBarIconAction(
            iconRes = R.drawable.ic_back_24,
            contentDescription = "Back",
            onClick = onBack
        )
        Spacer(modifier = Modifier.weight(1f))
        TopBarIconAction(
            iconRes = R.drawable.ic_undo_24,
            contentDescription = "Undo",
            enabled = canUndo && !busy,
            onClick = onUndo
        )
        TopBarIconAction(
            iconRes = R.drawable.ic_redo_24,
            contentDescription = "Redo",
            enabled = canRedo && !busy,
            onClick = onRedo
        )
        Spacer(modifier = Modifier.width(8.dp))
        ExportPill(enabled = exportEnabled, onClick = onExport)
    }
}

@Composable
private fun ExportPill(enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        },
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            text = "Export",
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}
