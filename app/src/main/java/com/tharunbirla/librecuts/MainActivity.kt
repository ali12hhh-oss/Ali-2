package com.tharunbirla.librecuts

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tharunbirla.librecuts.ui.screens.DashboardCallbacks
import com.tharunbirla.librecuts.ui.screens.DashboardScreen
import com.tharunbirla.librecuts.ui.screens.DashboardUiState
import com.tharunbirla.librecuts.ui.screens.DraftProject
import com.tharunbirla.librecuts.ui.theme.ProdlineTheme
import com.tharunbirla.librecuts.utils.setBounceClickListener
import android.view.View
import androidx.core.os.LocaleListCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private var dashboardState by mutableStateOf(DashboardUiState())

    private val selectVideoLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    Log.e("VideoSelection", "Could not take persistable permission", e)
                }
                Log.d("VideoSelection", "Video selected: $uri")
                navigateToEditingScreen(uri)
            } else {
                Log.e("VideoSelectionError", "No video selected")
            }
        }

    private val openProjectLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: Exception) {
                    Log.e("ProjectSelection", "Could not take persistable permission for project URI", e)
                }
                Log.d("ProjectSelection", "Project selected: $uri")
                val intent = Intent(this, ProjectImportActivity::class.java).apply {
                    putExtra("PROJECT_URI", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivity(intent)
            } else {
                Log.e("ProjectSelectionError", "No project selected")
            }
        }

    private val selectFolderLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
                    prefs.edit().putString("export_directory_uri", uri.toString()).apply()
                    dashboardState = dashboardState.copy(videoFolderLabel = resolveFolderLabel(uri, R.string.str_default_movies_librecuts))
                } catch (e: Exception) {
                    Log.e("FolderSelectionError", "Error securing permission for URI", e)
                    showToast(getString(R.string.toast_failed_to_set_export_folder))
                }
            }
        }

    private val selectAudioFolderLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
                    prefs.edit().putString("export_audio_directory_uri", uri.toString()).apply()
                    dashboardState = dashboardState.copy(audioFolderLabel = resolveFolderLabel(uri, R.string.str_default_music_librecuts))
                } catch (e: Exception) {
                    Log.e("FolderSelectionError", "Error securing permission for URI", e)
                    showToast(getString(R.string.toast_failed_to_set_export_folder))
                }
            }
        }

    private val selectSnapshotFolderLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            if (uri != null) {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)

                    val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
                    prefs.edit().putString("export_snapshot_directory_uri", uri.toString()).apply()
                    dashboardState = dashboardState.copy(snapshotFolderLabel = resolveFolderLabel(uri, R.string.str_default_pictures_librecuts))
                } catch (e: Exception) {
                    Log.e("FolderSelectionError", "Error securing permission for URI", e)
                    showToast(getString(R.string.toast_failed_to_set_export_folder))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        dashboardState = buildInitialDashboardState()

        setContent {
            ProdlineTheme {
                DashboardScreen(
                    uiState = dashboardState,
                    callbacks = dashboardCallbacks()
                )
            }
        }

        // Onboarding / Welcome Dialog
        val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean("first_launch_v1", true)
        if (isFirstLaunch) {
            showOnboardingDialog(prefs)
        }

        // Handle shared/intent videos (once — recreations must not stack duplicate editors)
        if (savedInstanceState == null) {
            handleIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val refreshed = loadDrafts()
        if (refreshed != dashboardState.drafts) {
            dashboardState = dashboardState.copy(drafts = refreshed)
        }
    }

    private fun dashboardCallbacks() = DashboardCallbacks(
        onNewProject = { selectVideo() },
        onOpenProject = { openProjectLauncher.launch(arrayOf("*/*")) },
        onDraftOpen = { draft -> openDraft(draft) },
        onDraftDelete = { draft -> deleteDraft(draft) },
        onSelectVideoFolder = { selectFolderLauncher.launch(null) },
        onSelectAudioFolder = { selectAudioFolderLauncher.launch(null) },
        onSelectSnapshotFolder = { selectSnapshotFolderLauncher.launch(null) },
        onLanguageClick = { showLanguageDialog() },
        onToggleHaptic = {
            val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
            val current = prefs.getBoolean("haptic_feedback", true)
            prefs.edit().putBoolean("haptic_feedback", !current).apply()
            dashboardState = dashboardState.copy(hapticEnabled = !current)
        },
        onToggleFullscreen = {
            val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
            val current = prefs.getBoolean("fullscreen_editor", true)
            prefs.edit().putBoolean("fullscreen_editor", !current).apply()
            dashboardState = dashboardState.copy(fullscreenEditor = !current)
        },
        onEncoderClick = { showEncoderDialog() },
        onCheckUpdates = { checkForUpdates() },
        onOpenLicenses = {
            com.mikepenz.aboutlibraries.LibsBuilder()
                .withActivityTitle(getString(R.string.str_open_source_licenses))
                .withSearchEnabled(true)
                .start(this)
        },
        onOpenUrl = { url -> openUrl(url) },
        onTabSelected = { index -> dashboardState = dashboardState.copy(selectedTab = index) }
    )

    private fun buildInitialDashboardState(): DashboardUiState {
        val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)

        val videoLabel = prefs.getString("export_directory_uri", null)?.let {
            resolveFolderLabel(Uri.parse(it), R.string.str_default_movies_librecuts)
        } ?: getString(R.string.str_default_movies_librecuts)

        val audioLabel = prefs.getString("export_audio_directory_uri", null)?.let {
            resolveFolderLabel(Uri.parse(it), R.string.str_default_music_librecuts)
        } ?: getString(R.string.str_default_music_librecuts)

        val snapshotLabel = prefs.getString("export_snapshot_directory_uri", null)?.let {
            resolveFolderLabel(Uri.parse(it), R.string.str_default_pictures_librecuts)
        } ?: getString(R.string.str_default_pictures_librecuts)

        val versionName = try {
            "v${packageManager.getPackageInfo(packageName, 0).versionName}"
        } catch (_: Exception) {
            "v1.0-beta5"
        }

        return DashboardUiState(
            selectedTab = 0,
            videoFolderLabel = videoLabel,
            audioFolderLabel = audioLabel,
            snapshotFolderLabel = snapshotLabel,
            languageLabel = currentLanguageLabel(),
            hapticEnabled = prefs.getBoolean("haptic_feedback", true),
            fullscreenEditor = prefs.getBoolean("fullscreen_editor", true),
            encoderHardware = (prefs.getString("default_encoder", "hardware") ?: "hardware") == "hardware",
            versionName = versionName,
            drafts = loadDrafts()
        )
    }

    private fun currentLanguageLabel(): String {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty) return getString(R.string.str_system_default)
        val locale = currentLocales.get(0)
        val tag = locale?.toLanguageTag() ?: ""
        val name = when (tag.lowercase()) {
            "pt-br" -> "Português (Brasil)"
            else -> locale?.getDisplayName(locale)?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
        }
        return name ?: getString(R.string.str_system_default)
    }

    // ── Drafts ───────────────────────────────────────────────────────────────

    private fun draftsDir(): File = File(filesDir, "drafts").apply { mkdirs() }

    internal fun loadDrafts(): List<DraftProject> {
        return draftsDir().listFiles { f -> f.isFile && f.extension == "json" }
            ?.map { DraftProject(name = it.nameWithoutExtension, path = it.absolutePath, lastModified = it.lastModified()) }
            ?.sortedByDescending { it.lastModified }
            ?: emptyList()
    }

    private fun openDraft(draft: DraftProject) {
        val intent = Intent(this, ProjectImportActivity::class.java).apply {
            putExtra("DRAFT_PATH", draft.path)
        }
        startActivity(intent)
    }

    private fun deleteDraft(draft: DraftProject) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_draft_title)
            .setMessage(getString(R.string.delete_draft_message, draft.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                File(draft.path).delete()
                dashboardState = dashboardState.copy(drafts = loadDrafts())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // ── Folder labels ────────────────────────────────────────────────────────

    private fun resolveFolderLabel(uri: Uri?, defaultResId: Int): String {
        if (uri == null) return getString(defaultResId)
        return try {
            val path = uri.lastPathSegment?.split(":")?.lastOrNull()
            if (!path.isNullOrEmpty()) path else getString(R.string.str_custom_directory)
        } catch (_: Exception) {
            getString(R.string.str_custom_directory)
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    private fun showEncoderDialog() {
        val prefs = getSharedPreferences("librecuts_prefs", MODE_PRIVATE)
        val currentEncoder = prefs.getString("default_encoder", "hardware") ?: "hardware"
        val options = arrayOf(
            getString(R.string.str_encoder_hardware),
            getString(R.string.str_encoder_software)
        )
        val selectedIndex = if (currentEncoder == "software") 1 else 0

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.str_default_encoder)
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val chosenEncoder = if (which == 1) "software" else "hardware"
                prefs.edit().putString("default_encoder", chosenEncoder).apply()
                dashboardState = dashboardState.copy(encoderHardware = chosenEncoder == "hardware")
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLanguageDialog() {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.language_bottom_sheet_dialog, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.btnCloseSheet)?.setBounceClickListener {
            dialog.dismiss()
        }

        val container = view.findViewById<android.widget.LinearLayout>(R.id.layoutLanguageContainer)
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (currentLocales.isEmpty) "" else (currentLocales.get(0)?.toLanguageTag() ?: "")

        for (item in getAvailableLanguages()) {
            val itemView = layoutInflater.inflate(R.layout.item_language_selection, container, false)
            val tvName = itemView.findViewById<android.widget.TextView>(R.id.tvLanguageName)
            val ivCheck = itemView.findViewById<android.widget.ImageView>(R.id.ivCheckLanguage)

            tvName.text = item.displayName

            val isSelected = if (item.tag.isEmpty()) {
                currentTag.isEmpty()
            } else {
                currentTag.equals(item.tag, ignoreCase = true) ||
                        (item.tag.length == 2 && currentTag.startsWith(item.tag, ignoreCase = true))
            }

            ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE

            itemView.setBounceClickListener {
                val appLocale = if (item.tag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(item.tag)
                }
                AppCompatDelegate.setApplicationLocales(appLocale)
                dashboardState = dashboardState.copy(languageLabel = currentLanguageLabel())
                dialog.dismiss()
            }

            container?.addView(itemView)
        }

        dialog.show()
    }

    private data class LanguageItem(
        val tag: String,
        val displayName: String
    )

    private fun getAvailableLanguages(): List<LanguageItem> {
        // Vidora/Ali-2 currently exposes exactly two user-selectable languages.
        // AppCompatDelegate handles persistence and applies the correct RTL/LTR layout.
        return listOf(
            LanguageItem("en", "English"),
            LanguageItem("ar", "العربية")
        )
    }

    // ── Media import flow ────────────────────────────────────────────────────

    private val selectMultipleMediaLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri>? ->
            if (!uris.isNullOrEmpty()) {
                uris.forEach { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: Exception) {
                        Log.d("VideoSelection", "Could not take persistable permission: ${e.message}")
                    }
                }
                navigateToEditingScreen(uris)
            }
        }

    private fun selectVideo() {
        Log.d("VideoSelection", "Launching media picker.")
        val picker = com.tharunbirla.librecuts.customviews.MediaPickerBottomSheet().apply {
            initialMediaType = com.tharunbirla.librecuts.customviews.MediaPickerBottomSheet.MediaType.ALL
            showCategoryTabs = true
            showAudioTab = false
            isMultiSelect = true
            onMediaListSelectedListener = { uris ->
                uris.forEach { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: Exception) {
                        Log.d("VideoSelection", "Could not take persistable permission: ${e.message}")
                    }
                }
                navigateToEditingScreen(uris)
            }
            onMediaSelectedListener = { uri ->
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    Log.d("VideoSelection", "Could not take persistable permission: ${e.message}")
                }
                navigateToEditingScreen(listOf(uri))
            }
            onBrowseSystemFoldersRequested = {
                showCustomFolderExplorer()
            }
        }
        picker.show(supportFragmentManager, "MediaPickerBottomSheet")
    }

    private fun showCustomFolderExplorer() {
        val explorer = com.tharunbirla.librecuts.customviews.CustomFileExplorerBottomSheet().apply {
            targetFilter = com.tharunbirla.librecuts.customviews.CustomFileExplorerBottomSheet.FilterType.ALL
            isMultiSelect = true
            onFilesSelectedListener = { uris ->
                uris.forEach { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: Exception) {
                        Log.d("FolderSelection", "Could not take persistable permission: ${e.message}")
                    }
                }
                navigateToEditingScreen(uris)
            }
            onFileSelectedListener = { uri, _ ->
                navigateToEditingScreen(listOf(uri))
            }
        }
        explorer.show(supportFragmentManager, "CustomFileExplorerBottomSheet")
    }

    private fun navigateToEditingScreen(mediaUris: List<Uri>) {
        if (mediaUris.isEmpty()) return
        Log.d("Navigation", "Navigating to editing screen with ${mediaUris.size} URIs: $mediaUris")
        val intent = Intent(this, VideoEditingActivity::class.java).apply {
            putExtra("VIDEO_URI", mediaUris.first())
            putParcelableArrayListExtra("EXTRA_MEDIA_URIS", ArrayList(mediaUris))
            data = mediaUris.first()
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun navigateToEditingScreen(videoUri: Uri) {
        navigateToEditingScreen(listOf(videoUri))
    }

    // ── Intent handling ──────────────────────────────────────────────────────

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_SEND == action && type != null) {
            if (type.startsWith("video/") || type.startsWith("image/")) {
                @Suppress("DEPRECATION")
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                    Log.d("SharedVideo", "Received SEND intent with media URI: $uri")
                    navigateToEditingScreen(uri)
                }
            }
        } else if ((Intent.ACTION_VIEW == action || Intent.ACTION_EDIT == action) && type != null) {
            if (type.startsWith("video/") || type.startsWith("image/")) {
                intent.data?.let { uri ->
                    Log.d("SharedVideo", "Received VIEW/EDIT intent with media URI: $uri")
                    navigateToEditingScreen(uri)
                }
            }
        }
    }

    private fun showOnboardingDialog(prefs: android.content.SharedPreferences) {
        val dialog = android.app.Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_welcome_onboarding, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val lp = window.attributes
            lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = lp
        }

        val tvVersion = view.findViewById<android.widget.TextView>(R.id.tvOnboardingVersion)
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersion.text = getString(R.string.version_format, pInfo.versionName)
        } catch (_: Exception) {
            tvVersion.text = getString(R.string.version_format, "1.0-beta5")
        }

        view.findViewById<View>(R.id.layoutStarGithub)?.setBounceClickListener {
            openUrl("https://github.com/Vicky8106/Prodline-AI")
        }
        view.findViewById<View>(R.id.layoutSponsorGithub)?.setBounceClickListener {
            openUrl("https://github.com/Vicky8106/Prodline-AI#support")
        }
        view.findViewById<View>(R.id.layoutDiscord)?.setBounceClickListener {
            openUrl("https://discord.gg/gwr3nE7YW")
        }
        view.findViewById<View>(R.id.layoutTroubleshooting)?.setBounceClickListener {
            openUrl("https://github.com/Vicky8106/Prodline-AI#troubleshooting")
        }
        view.findViewById<View>(R.id.btnOnboardingGetStarted)?.setBounceClickListener {
            prefs.edit().putBoolean("first_launch_v1", false).apply()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (_: Exception) {
            showToast(getString(R.string.toast_unable_to_open_link))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkForUpdates() {
        showToast(getString(R.string.toast_checking_for_updates))
        openUrl("https://github.com/Vicky8106/Prodline-AI/releases/latest")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
