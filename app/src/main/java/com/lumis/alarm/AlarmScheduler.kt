package com.lumis.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleAlarm(wakeTime: Instant) {
        val pendingIntent = buildMainPendingIntent() ?: return
        val alarmClockInfo = AlarmManager.AlarmClockInfo(wakeTime.toEpochMilli(), pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelAlarm() {
        val pendingIntent = buildMainPendingIntent(PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    // Reagenda a notificação a cada 30s até o usuário dispensar
    fun scheduleRepeatingNotification() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_IS_REPEAT, true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REPEAT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        val triggerAt = System.currentTimeMillis() + REPEAT_INTERVAL_MS
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelRepeatingNotification() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REPEAT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    fun getNextAlarmTime(): Instant? {
        return alarmManager.nextAlarmClock?.triggerTime?.let { Instant.ofEpochMilli(it) }
    }

    private fun buildMainPendingIntent(flags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, flags)
    }

    companion object {
        const val ALARM_REQUEST_CODE = 1001
        const val REPEAT_REQUEST_CODE = 1002
        const val REPEAT_INTERVAL_MS = 30_000L
    }
}
