#!/bin/bash

# Fix EditorToolbars.kt syntax error
sed -i '268,274d' app/src/main/java/com/tharunbirla/librecuts/ui/components/EditorToolbars.kt

# Replace enableEdgeToEdge with WindowCompat.setDecorFitsSystemWindows
sed -i 's/androidx.activity.enableEdgeToEdge()/androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)/g' app/src/main/java/com/tharunbirla/librecuts/MainActivity.kt
sed -i 's/androidx.activity.enableEdgeToEdge()/androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)/g' app/src/main/java/com/tharunbirla/librecuts/VideoEditingActivity.kt

