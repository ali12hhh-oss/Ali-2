import re

with open("app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorToolbars.kt", "r") as f:
    content = f.read()

# Add navigationBarsPadding
content = re.sub(
    r'modifier = Modifier\s*\n\s*\.fillMaxWidth\(\)\s*\n\s*\.horizontalScroll\(rememberScrollState\(\)\)\s*\n\s*\.padding\(horizontal = 8\.dp\)',
    'modifier = Modifier\n            .fillMaxWidth()\n            .navigationBarsPadding()\n            .horizontalScroll(rememberScrollState())\n            .padding(horizontal = 8.dp)',
    content
)

# Update ToolbarButton to Material 3 standard
new_button = """@Composable
fun ToolbarButton(iconRes: Int, text: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}"""

content = re.sub(r'@Composable\nfun ToolbarButton.*?\}', new_button, content, flags=re.DOTALL)

with open("app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorToolbars.kt", "w") as f:
    f.write(content)
