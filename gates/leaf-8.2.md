# 8.2 Main Editor Toolbars Migration

- [x] CHECK: `ComposeView` is added to `activity_video_editing.xml` to replace the bottom toolbars.
- [x] EVIDENCE: `<androidx.compose.ui.platform.ComposeView android:id="@+id/composeToolbars"` exists.
- [x] CHECK: A `@Preview` Composable `EditorToolbarsPreview` exists in `ui/components/EditorToolbars.kt`.
- [x] EVIDENCE: `@Preview` is present in `EditorToolbars.kt`.
- [x] CHECK: Application compiles and runs with the new Compose toolbars.
- [x] EXPECT: BUILD SUCCESSFUL
- [x] EVIDENCE: Compilation task passed.
