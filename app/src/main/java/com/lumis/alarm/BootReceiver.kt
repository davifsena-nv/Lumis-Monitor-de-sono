package com.lumis.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Reinicia o worker de detecção de sono após reinício do dispositivo
        val workRequest = OneTimeWorkRequestBuilder<SleepDetectionWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
