package com.tharunbirla.librecuts

import android.app.Application
import android.content.Intent
import android.util.Log
import com.tharunbirla.librecuts.utils.ErrorCode

class LibreCutsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupGlobalCrashHandler()
    }

    private fun setupGlobalCrashHandler() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LibreCutsCrash", "CRITICAL CRASH IN APPLICATION", throwable)

            // Start a dedicated Error Activity to show the dialog. It runs in a separate
            // process so the crash-report screen can outlive this dying process.
            val intent = Intent(this, ErrorDisplayActivity::class.java).apply {
                putExtra("ERROR_CODE", ErrorCode.UNEXPECTED_CRASH.code)
                putExtra("ERROR_LOG", Log.getStackTraceString(throwable))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)

            // Give the :crash process a moment to spawn before terminating here.
            try {
                Thread.sleep(400)
            } catch (_: InterruptedException) {
            }

            oldHandler?.uncaughtException(thread, throwable)
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }
}
