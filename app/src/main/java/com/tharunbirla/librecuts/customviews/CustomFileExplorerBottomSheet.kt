package com.tharunbirla.librecuts.customviews

import android.content.ContentUris
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tharunbirla.librecuts.R
import com.tharunbirla.librecuts.utils.setBounceClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class CustomFileExplorerBottomSheet : BottomSheetDialogFragment() {

    enum class FilterType { ALL, VIDEO, IMAGE, AUDIO }

    data class FileNode(
        val file: File,
        val isDirectory: Boolean,
        val displayName: String,
        val subtext: String,
        val durationMs: Long = 0L,
        val uri: Uri
    )

    var targetFilter: FilterType = FilterType.ALL
    var isMultiSelect: Boolean = true
    var onFileSelectedListener: ((Uri, File) -> Unit)? = null
    var onFilesSelectedListener: ((List<Uri>) -> Unit)? = null

    private lateinit var rvExplorerNodes: RecyclerView
    private lateinit var pbExplorerLoading: ProgressBar
    private lateinit var tvExplorerEmpty: TextView
    private lateinit var tvCurrentPath: TextView
    private lateinit var btnUpDirectory: ImageView
    private lateinit var btnExplorerSelectAll: MaterialButton
    private lateinit var chipGroupExplorerFilter: ChipGroup
    private lateinit var chipFilterAll: Chip
    private lateinit var chipFilterVideos: Chip
    private lateinit var chipFilterImages: Chip
    private lateinit var chipFilterAudio: Chip
    private lateinit var layoutExplorerBottomBar: LinearLayout
    private lateinit var tvExplorerSelectedCount: TextView
    private lateinit var btnExplorerImportSelected: MaterialButton

    private var currentDirectory: File = Environment.getExternalStorageDirectory()
    private val rawDirectoryFiles = mutableListOf<File>()
    private val displayedNodeList = mutableListOf<FileNode>()
    private val selectedNodes = mutableListOf<FileNode>()
    private var adapter: NodeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_file_explorer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnCloseExplorer)?.setOnClickListener {
            dismiss()
        }

        rvExplorerNodes = view.findViewById(R.id.rvExplorerNodes)
        pbExplorerLoading = view.findViewById(R.id.pbExplorerLoading)
        tvExplorerEmpty = view.findViewById(R.id.tvExplorerEmpty)
        tvCurrentPath = view.findViewById(R.id.tvCurrentPath)
        btnUpDirectory = view.findViewById(R.id.btnUpDirectory)
        btnExplorerSelectAll = view.findViewById(R.id.btnExplorerSelectAll)
        chipGroupExplorerFilter = view.findViewById(R.id.chipGroupExplorerFilter)
        chipFilterAll = view.findViewById(R.id.chipFilterAll)
        chipFilterVideos = view.findViewById(R.id.chipFilterVideos)
        chipFilterImages = view.findViewById(R.id.chipFilterImages)
        chipFilterAudio = view.findViewById(R.id.chipFilterAudio)
        layoutExplorerBottomBar = view.findViewById(R.id.layoutExplorerBottomBar)
        tvExplorerSelectedCount = view.findViewById(R.id.tvExplorerSelectedCount)
        btnExplorerImportSelected = view.findViewById(R.id.btnExplorerImportSelected)

        rvExplorerNodes.layoutManager = LinearLayoutManager(requireContext())
        adapter = NodeAdapter(displayedNodeList) { selectedNode ->
            if (selectedNode.isDirectory) {
                navigateToDirectory(selectedNode.file)
            } else {
                handleFileNodeClick(selectedNode)
            }
        }
        rvExplorerNodes.adapter = adapter

        btnUpDirectory.setOnClickListener {
            val parent = currentDirectory.parentFile
            if (parent != null && parent.canRead()) {
                navigateToDirectory(parent)
            }
        }

        btnExplorerSelectAll.visibility = if (isMultiSelect) View.VISIBLE else View.GONE
        btnExplorerSelectAll.setOnClickListener {
            toggleSelectAllInFolder()
        }

        btnExplorerImportSelected.setBounceClickListener {
            if (selectedNodes.isNotEmpty()) {
                val uris = selectedNodes.map { findMediaStoreUriForFile(it.file) ?: it.uri }
                if (onFilesSelectedListener != null) {
                    onFilesSelectedListener?.invoke(uris)
                } else {
                    onFileSelectedListener?.invoke(uris.first(), selectedNodes.first().file)
                }
                dismiss()
            }
        }

        when (targetFilter) {
            FilterType.ALL -> chipFilterAll.isChecked = true
            FilterType.VIDEO -> chipFilterVideos.isChecked = true
            FilterType.IMAGE -> chipFilterImages.isChecked = true
            FilterType.AUDIO -> chipFilterAudio.isChecked = true
        }

        chipGroupExplorerFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            targetFilter = when {
                checkedIds.contains(R.id.chipFilterAll) -> FilterType.ALL
                checkedIds.contains(R.id.chipFilterVideos) -> FilterType.VIDEO
                checkedIds.contains(R.id.chipFilterImages) -> FilterType.IMAGE
                checkedIds.contains(R.id.chipFilterAudio) -> FilterType.AUDIO
                else -> FilterType.ALL
            }
            refreshDisplayedNodes()
        }

        setupLocationChips(view)
        navigateToDirectory(currentDirectory)
        updateBottomBar()
    }

    private fun handleFileNodeClick(node: FileNode) {
        if (isMultiSelect) {
            val existingIndex = selectedNodes.indexOfFirst { it.file.absolutePath == node.file.absolutePath }
            if (existingIndex >= 0) {
                selectedNodes.removeAt(existingIndex)
            } else {
                selectedNodes.add(node)
            }
            adapter?.notifyDataSetChanged()
            updateBottomBar()
            updateSelectAllButtonState()
        } else {
            val mediaStoreUri = findMediaStoreUriForFile(node.file) ?: node.uri
            if (onFilesSelectedListener != null) {
                onFilesSelectedListener?.invoke(listOf(mediaStoreUri))
            } else {
                onFileSelectedListener?.invoke(mediaStoreUri, node.file)
            }
            dismiss()
        }
    }

    private fun toggleSelectAllInFolder() {
        val filesInFolder = displayedNodeList.filter { !it.isDirectory }
        val allSelected = filesInFolder.isNotEmpty() && filesInFolder.all { fileNode ->
            selectedNodes.any { it.file.absolutePath == fileNode.file.absolutePath }
        }

        if (allSelected) {
            val pathsToRemove = filesInFolder.map { it.file.absolutePath }.toSet()
            selectedNodes.removeAll { it.file.absolutePath in pathsToRemove }
        } else {
            for (node in filesInFolder) {
                if (selectedNodes.none { it.file.absolutePath == node.file.absolutePath }) {
                    selectedNodes.add(node)
                }
            }
        }

        adapter?.notifyDataSetChanged()
        updateBottomBar()
        updateSelectAllButtonState()
    }

    private fun updateSelectAllButtonState() {
        val filesInFolder = displayedNodeList.filter { !it.isDirectory }
        val allSelected = filesInFolder.isNotEmpty() && filesInFolder.all { fileNode ->
            selectedNodes.any { it.file.absolutePath == fileNode.file.absolutePath }
        }
        btnExplorerSelectAll.text = if (allSelected) "Deselect All" else "Select All"
    }

    private fun updateBottomBar() {
        val count = selectedNodes.size
        if (count > 0 && isMultiSelect) {
            layoutExplorerBottomBar.visibility = View.VISIBLE
            tvExplorerSelectedCount.text = context.getString(R.string.files_selected, count, if (count > 1) "s" else "")
            btnExplorerImportSelected.text = context.getString(R.string.import_count, count)
        } else {
            layoutExplorerBottomBar.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        dialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        dialog?.behavior?.skipCollapsed = true
    }

    private fun setupLocationChips(view: View) {
        view.findViewById<View>(R.id.chipDownloads)?.setOnClickListener {
            navigateToDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        }
        view.findViewById<View>(R.id.chipDcim)?.setOnClickListener {
            navigateToDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
        }
        view.findViewById<View>(R.id.chipMovies)?.setOnClickListener {
            navigateToDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
        }
        view.findViewById<View>(R.id.chipPictures)?.setOnClickListener {
            navigateToDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
        }
        view.findViewById<View>(R.id.chipRootStorage)?.setOnClickListener {
            navigateToDirectory(Environment.getExternalStorageDirectory())
        }
    }

    private fun navigateToDirectory(directory: File) {
        if (!directory.exists() || !directory.isDirectory) return
        currentDirectory = directory
        tvCurrentPath.text = directory.absolutePath

        pbExplorerLoading.visibility = View.VISIBLE
        tvExplorerEmpty.visibility = View.GONE
        displayedNodeList.clear()
        rawDirectoryFiles.clear()
        adapter?.notifyDataSetChanged()

        lifecycleScope.launch(Dispatchers.IO) {
            val files = try {
                directory.listFiles()?.toList() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                rawDirectoryFiles.clear()
                rawDirectoryFiles.addAll(files)
                refreshDisplayedNodes()
            }
        }
    }

    private fun refreshDisplayedNodes() {
        pbExplorerLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            val nodes = mutableListOf<FileNode>()

            val (dirs, files) = rawDirectoryFiles.partition { it.isDirectory }
            val sortedDirs = dirs.filterNot { it.name.startsWith(".") }.sortedBy { it.name.lowercase(Locale.getDefault()) }
            val sortedFiles = files.filterNot { it.name.startsWith(".") }.sortedBy { it.name.lowercase(Locale.getDefault()) }

            for (dir in sortedDirs) {
                val childCount = try { dir.list()?.size ?: 0 } catch (e: Exception) { 0 }
                nodes.add(
                    FileNode(
                        file = dir,
                        isDirectory = true,
                        displayName = dir.name,
                        subtext = "$childCount items",
                        uri = Uri.fromFile(dir)
                    )
                )
            }

            for (file in sortedFiles) {
                val ext = file.extension.lowercase(Locale.getDefault())
                val isVideo = ext in listOf("mp4", "mkv", "mov", "avi", "3gp", "webm")
                val isImage = ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
                val isAudio = ext in listOf("mp3", "wav", "m4a", "aac", "ogg", "flac")

                val matches = when (targetFilter) {
                    FilterType.ALL -> isVideo || isImage || isAudio
                    FilterType.VIDEO -> isVideo
                    FilterType.IMAGE -> isImage
                    FilterType.AUDIO -> isAudio
                }

                if (matches) {
                    var durationMs = 0L
                    if (isVideo || isAudio) {
                        durationMs = extractMediaDuration(file)
                    }
                    val sizeStr = formatFileSize(file.length())
                    val dateStr = formatFileDate(file.lastModified())

                    nodes.add(
                        FileNode(
                            file = file,
                            isDirectory = false,
                            displayName = file.name,
                            subtext = "$sizeStr • $dateStr",
                            durationMs = durationMs,
                            uri = Uri.fromFile(file)
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                pbExplorerLoading.visibility = View.GONE
                displayedNodeList.clear()
                displayedNodeList.addAll(nodes)
                adapter?.notifyDataSetChanged()
                updateSelectAllButtonState()

                if (displayedNodeList.isEmpty()) {
                    tvExplorerEmpty.visibility = View.VISIBLE
                } else {
                    tvExplorerEmpty.visibility = View.GONE
                }
            }
        }
    }

    private fun extractMediaDuration(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            try { retriever.release() } catch (_: Exception) {}
            0L
        }
    }

    private fun findMediaStoreUriForFile(file: File): Uri? {
        val context = context ?: return null
        val ext = file.extension.lowercase(Locale.getDefault())
        val isVideo = ext in listOf("mp4", "mkv", "mov", "avi", "3gp", "webm")
        val isImage = ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
        val isAudio = ext in listOf("mp3", "wav", "m4a", "aac", "ogg", "flac")

        val (contentUri, proj) = when {
            isVideo -> Pair(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Video.Media._ID))
            isImage -> Pair(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID))
            isAudio -> Pair(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Audio.Media._ID))
            else -> return null
        }

        try {
            val selection = "${MediaStore.MediaColumns.DATA} = ?"
            val selectionArgs = arrayOf(file.absolutePath)
            context.contentResolver.query(contentUri, proj, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    return ContentUris.withAppendedId(contentUri, id)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return null
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatted = String.format(Locale.getDefault(), "%.1f", bytes / Math.pow(1024.0, digitGroups.toDouble()))
        return "$formatted ${units[digitGroups]}"
    }

    private fun formatFileDate(timestampMs: Long): String {
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(java.util.Date(timestampMs))
    }

    private inner class NodeAdapter(
        private val nodes: List<FileNode>,
        private val onItemClick: (FileNode) -> Unit
    ) : RecyclerView.Adapter<NodeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NodeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_explorer_node, parent, false)
            return NodeViewHolder(view)
        }

        override fun onBindViewHolder(holder: NodeViewHolder, position: Int) {
            val node = nodes[position]
            val isSelected = selectedNodes.any { it.file.absolutePath == node.file.absolutePath }
            holder.bind(node, isSelected, onItemClick)
        }

        override fun getItemCount(): Int = nodes.size
    }

    private inner class NodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivNodeIcon: com.google.android.material.imageview.ShapeableImageView = itemView.findViewById(R.id.ivNodeIcon)
        val tvNodeDuration: TextView = itemView.findViewById(R.id.tvNodeDuration)
        val tvNodeName: TextView = itemView.findViewById(R.id.tvNodeName)
        val tvNodeSubtext: TextView = itemView.findViewById(R.id.tvNodeSubtext)
        val cbNodeSelect: MaterialCheckBox = itemView.findViewById(R.id.cbNodeSelect)
        val ivChevron: ImageView = itemView.findViewById(R.id.ivChevron)

        fun bind(node: FileNode, isSelected: Boolean, onItemClick: (FileNode) -> Unit) {
            itemView.tag = node.file.absolutePath
            tvNodeName.text = node.displayName
            tvNodeSubtext.text = node.subtext

            if (node.isDirectory) {
                ivChevron.visibility = View.VISIBLE
                cbNodeSelect.visibility = View.GONE
                tvNodeDuration.visibility = View.GONE
                ivNodeIcon.setImageResource(R.drawable.ic_folder_24)
                ivNodeIcon.clearColorFilter()
                ivNodeIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                val density = itemView.context.resources.displayMetrics.density
                val pad = (10 * density).toInt()
                ivNodeIcon.setPadding(pad, pad, pad, pad)
            } else {
                ivChevron.visibility = View.GONE
                cbNodeSelect.visibility = if (isMultiSelect) View.VISIBLE else View.GONE
                cbNodeSelect.isChecked = isSelected

                val ext = node.file.extension.lowercase(Locale.getDefault())
                val isVideo = ext in listOf("mp4", "mkv", "mov", "avi", "3gp", "webm")
                val isImage = ext in listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
                val isAudio = ext in listOf("mp3", "wav", "m4a", "aac", "ogg", "flac")

                if (isVideo || isAudio) {
                    if (node.durationMs > 0) {
                        tvNodeDuration.visibility = View.VISIBLE
                        tvNodeDuration.text = formatDuration(node.durationMs)
                    } else {
                        tvNodeDuration.visibility = View.GONE
                    }
                } else {
                    tvNodeDuration.visibility = View.GONE
                }

                if (isAudio) {
                    ivNodeIcon.setImageResource(R.drawable.ic_audio_24)
                    ivNodeIcon.clearColorFilter()
                    ivNodeIcon.setColorFilter(android.graphics.Color.WHITE)
                    val density = itemView.context.resources.displayMetrics.density
                    val pad = (10 * density).toInt()
                    ivNodeIcon.setPadding(pad, pad, pad, pad)
                } else {
                    ivNodeIcon.setPadding(0, 0, 0, 0)
                    ivNodeIcon.clearColorFilter()
                    ivNodeIcon.setImageResource(if (isVideo) R.drawable.ic_play_24 else R.drawable.ic_image_24)

                    lifecycleScope.launch(Dispatchers.IO) {
                        val thumb = loadThumbnail(node.file, isVideo)
                        withContext(Dispatchers.Main) {
                            if (itemView.tag == node.file.absolutePath && thumb != null) {
                                ivNodeIcon.setImageBitmap(thumb)
                            }
                        }
                    }
                }
            }

            itemView.setOnClickListener {
                onItemClick(node)
            }
        }

        private fun loadThumbnail(file: File, isVideo: Boolean): Bitmap? {
            val context = itemView.context ?: return null
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val uri = findMediaStoreUriForFile(file) ?: Uri.fromFile(file)
                    context.contentResolver.loadThumbnail(uri, Size(200, 200), null)
                } else {
                    if (isVideo) {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(file.absolutePath)
                        val frame = retriever.frameAtTime
                        retriever.release()
                        frame
                    } else {
                        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 }
                        android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                    }
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
