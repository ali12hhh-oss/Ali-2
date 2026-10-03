package com.tharunbirla.librecuts.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tharunbirla.librecuts.MainActivity
import com.tharunbirla.librecuts.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class ExportService : Service() {

    private val TAG = "ExportService"
    private val CHANNEL_ID = "ExportChannel"
    private val NOTIFICATION_ID = 1001

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var ffmpegEngine: FFmpegRenderEngine

    /** In-flight export jobs; the service only stops when this reaches zero. */
    private val activeJobs = java.util.concurrent.atomic.AtomicInteger(0)
    private val notificationIds = java.util.concurrent.atomic.AtomicInteger(NOTIFICATION_ID)

    companion object {
        const val ACTION_EXPORT_PROGRESS = "com.tharunbirla.librecuts.ACTION_EXPORT_PROGRESS"
        const val ACTION_EXPORT_SUCCESS = "com.tharunbirla.librecuts.ACTION_EXPORT_SUCCESS"
        const val ACTION_EXPORT_FAILURE = "com.tharunbirla.librecuts.ACTION_EXPORT_FAILURE"

        const val EXTRA_PROGRESS = "extra_progress"
        const val EXTRA_SAVED_URI = "extra_saved_uri"
        const val EXTRA_ERROR = "extra_error"

        const val EXTRA_COMMAND = "extra_command"
        const val EXTRA_TEMP_OUTPUT_PATH = "extra_temp_output_path"
        const val EXTRA_CONCAT_FILE_PATH = "extra_concat_file_path"
        const val EXTRA_TOTAL_DURATION_SECS = "extra_total_duration_secs"
        const val EXTRA_IS_AUDIO_ONLY = "extra_is_audio_only"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ffmpegEngine = FFmpegRenderEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val command = intent.getStringExtra(EXTRA_COMMAND) ?: return START_NOT_STICKY
        val tempOutputPath = intent.getStringExtra(EXTRA_TEMP_OUTPUT_PATH) ?: return START_NOT_STICKY
        val concatFilePath = intent.getStringExtra(EXTRA_CONCAT_FILE_PATH)
        val durationSecs = intent.getDoubleExtra(EXTRA_TOTAL_DURATION_SECS, 0.0)
        val isAudioOnly = intent.getBooleanExtra(EXTRA_IS_AUDIO_ONLY, false)

        val jobId = notificationIds.incrementAndGet()
        startForeground(jobId, buildNotification(0, "Exporting..."))
        activeJobs.incrementAndGet()

        serviceScope.launch {
            val tempFile = File(tempOutputPath)
            val concatFile = concatFilePath?.let(::File)
            try {
                val result = ffmpegEngine.exportFinal(
                    ffmpegCommand = command,
                    totalDurationSecs = durationSecs,
                    onProgress = { progress ->
                        updateNotification(jobId, progress, "Exporting...")
                        broadcastProgress(progress)
                    }
                )

                val terminalOutcome = when (result) {
                    is FFmpegRenderEngine.RenderResult.Success -> {
                        ExportLifecycle.publish(saveVideoToGallery(tempFile, isAudioOnly)?.toString())
                    }
                    is FFmpegRenderEngine.RenderResult.Failure -> ExportLifecycle.error(result.error)
                    is FFmpegRenderEngine.RenderResult.Cancelled -> ExportLifecycle.cancel()
                }

                reportTerminalOutcome(terminalOutcome, isAudioOnly, jobId)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Export exception", e)
                reportTerminalOutcome(ExportLifecycle.error(e.message), isAudioOnly, jobId)
            } finally {
                deleteTemporaryFile(tempFile)
                concatFile?.let(::deleteTemporaryFile)
                if (activeJobs.decrementAndGet() <= 0) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Video Export",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(progress: Int, title: String): Notification {
        return buildNotification(NOTIFICATION_ID, progress, title)
    }

    private fun buildNotification(notificationId: Int, progress: Int, title: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(if (progress > 0) "$progress%" else "Starting...")
            .setSmallIcon(R.drawable.ic_save_24)
            .setProgress(100, progress, progress == 0)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(notificationId: Int, progress: Int, title: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, buildNotification(notificationId, progress, title))
    }

    private fun showCompletionNotification(title: String, message: String, videoUri: Uri?, notificationId: Int = NOTIFICATION_ID + 1) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            if (videoUri != null) {
                setDataAndType(videoUri, if (message.contains("Audio", true)) "audio/*" else "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                setClass(this@ExportService, MainActivity::class.java)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_check_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    private fun broadcastProgress(progress: Int) {
        val intent = Intent(ACTION_EXPORT_PROGRESS).apply {
            putExtra(EXTRA_PROGRESS, progress)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastSuccess(savedUri: String) {
        val intent = Intent(ACTION_EXPORT_SUCCESS).apply {
            putExtra(EXTRA_SAVED_URI, savedUri)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun broadcastFailure(error: String) {
        val intent = Intent(ACTION_EXPORT_FAILURE).apply {
            putExtra(EXTRA_ERROR, error)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun reportTerminalOutcome(outcome: ExportLifecycle.TerminalOutcome, isAudioOnly: Boolean, notificationId: Int = NOTIFICATION_ID + 1) {
        when (outcome) {
            is ExportLifecycle.TerminalOutcome.Published -> {
                val savedUri = Uri.parse(outcome.uri)
                showCompletionNotification(
                    "Export Complete",
                    if (isAudioOnly) "Audio saved to gallery" else "Video saved to gallery",
                    savedUri,
                    notificationId + 1
                )
                broadcastSuccess(outcome.uri)
                Log.d(TAG, "Export published: $savedUri")
            }
            ExportLifecycle.TerminalOutcome.Cancelled -> {
                showCompletionNotification("Export Cancelled", "The export was cancelled", null, notificationId + 1)
                broadcastFailure("Export cancelled")
            }
            is ExportLifecycle.TerminalOutcome.Failed -> {
                showCompletionNotification("Export Failed", outcome.message, null, notificationId + 1)
                broadcastFailure(outcome.message)
                Log.e(TAG, "Export failed: ${outcome.message}")
            }
        }
    }

    private fun deleteTemporaryFile(file: File) {
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Unable to remove temporary export file: ${file.absolutePath}")
        }
    }

    private fun saveVideoToGallery(videoFile: File, isAudioOnly: Boolean): Uri? {
        val mimeType = if (isAudioOnly) "audio/mpeg" else "video/mp4"
        val ext = if (isAudioOnly) ".mp3" else ".mp4"
        val prefix = if (isAudioOnly) "LibreCuts_Audio_" else "LibreCuts_"
        
        val sharedPreferences = getSharedPreferences("librecuts_prefs", Context.MODE_PRIVATE)
        val prefKey = if (isAudioOnly) "export_audio_directory_uri" else "export_directory_uri"
        val customUriString = sharedPreferences.getString(prefKey, null)
        
        if (customUriString != null) {
            var customOutputUri: Uri? = null
            try {
                val treeUri = Uri.parse(customUriString)
                val parentUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(
                    treeUri,
                    android.provider.DocumentsContract.getTreeDocumentId(treeUri)
                )
                customOutputUri = android.provider.DocumentsContract.createDocument(
                    contentResolver,
                    parentUri,
                    mimeType,
                    "${prefix}${System.currentTimeMillis()}$ext"
                )
                if (customOutputUri != null) {
                    contentResolver.openOutputStream(customOutputUri)?.use { output ->
                        videoFile.inputStream().use { input -> input.copyTo(output) }
                    } ?: throw IllegalStateException("Could not open the selected export location")
                    return customOutputUri
                }
            } catch (e: Exception) {
                customOutputUri?.let { contentResolver.delete(it, null, null) }
                Log.e(TAG, "Error saving to custom directory: ${e.message}, falling back to default", e)
            }
        }

        return try {
            val displayName = "${prefix}${System.currentTimeMillis()}${ext}"
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        if (isAudioOnly) Environment.DIRECTORY_MUSIC + "/LibreCuts"
                        else Environment.DIRECTORY_MOVIES + "/LibreCuts"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (isAudioOnly) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = contentResolver.insert(collectionUri, contentValues)
                ?: throw IllegalStateException("MediaStore could not create the gallery item")

            try {
                contentResolver.openOutputStream(uri)?.use { output ->
                    videoFile.inputStream().use { input -> input.copyTo(output) }
                } ?: throw IllegalStateException("Could not open the gallery export location")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val publishValues = android.content.ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    contentResolver.update(uri, publishValues, null, null)
                }
                uri
            } catch (e: Exception) {
                contentResolver.delete(uri, null, null)
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to default gallery: ${e.message}", e)
            throw IllegalStateException(
                "Failed to publish exported media: ${e.message ?: "unknown storage error"}",
                e
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
