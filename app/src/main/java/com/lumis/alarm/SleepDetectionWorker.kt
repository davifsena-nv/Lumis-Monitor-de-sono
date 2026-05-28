package com.lumis.alarm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.lumis.data.SleepRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.util.concurrent.TimeUnit

@HiltWorker
class SleepDetectionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val inactivityDetector: InactivityDetector,
    private val alarmScheduler: AlarmScheduler,
    private val sleepRepository: SleepRepository,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val config = sleepRepository.getAlarmConfig() ?: return Result.success()
        if (!config.isActive) return Result.success()
        if (!inactivityDetector.hasPermission()) return Result.success()

        val isInactive = inactivityDetector.isInactive(config.sleepLatency)
        if (!isInactive) {
            // Não dormiu ainda — reagendar verificação em 5 min
            scheduleNext()
            return Result.success()
        }

        // Usuário está inativo: calcula e agenda o alarme
        val sleepDetectedAt = inactivityDetector.getLastInteractionTime()
            ?: Instant.now().minusSeconds(config.sleepLatency * 60L)

        val wakeTime = AlarmCalculator.calculate(sleepDetectedAt, config)
        alarmScheduler.scheduleAlarm(wakeTime)

        return Result.success()
    }

    private fun scheduleNext() {
        val request = OneTimeWorkRequestBuilder<SleepDetectionWorker>()
            .setInitialDelay(CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        const val WORK_NAME = "sleep_detection"
        const val CHECK_INTERVAL_MINUTES = 5L

        fun startDetection(context: Context) {
            val request = OneTimeWorkRequestBuilder<SleepDetectionWorker>()
                .setInitialDelay(CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        fun stopDetection(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
