package com.lumis.data

import com.lumis.alarm.AlarmConfig
import com.lumis.data.dao.AlarmConfigDao
import com.lumis.data.dao.SleepNightDao
import com.lumis.data.model.AlarmConfigEntity
import com.lumis.data.model.SleepNight
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRepository @Inject constructor(
    private val sleepNightDao: SleepNightDao,
    private val alarmConfigDao: AlarmConfigDao,
) {
    suspend fun getAlarmConfig(): AlarmConfig? {
        return alarmConfigDao.get()?.toDomain()
    }

    suspend fun saveAlarmConfig(config: AlarmConfig) {
        alarmConfigDao.save(config.toEntity())
    }

    suspend fun getScheduledAlarmTime(): Instant? {
        return alarmConfigDao.get()?.scheduledAlarmTime?.let { Instant.ofEpochMilli(it) }
    }

    suspend fun saveScheduledAlarmTime(instant: Instant?) {
        alarmConfigDao.updateScheduledTime(instant?.toEpochMilli())
    }

    suspend fun saveSleepNight(night: SleepNight) {
        sleepNightDao.insert(night)
    }

    suspend fun getRecentNights(limit: Int = 7): List<SleepNight> {
        return sleepNightDao.getRecent(limit)
    }

    // Mappers

    private fun AlarmConfigEntity.toDomain() = AlarmConfig(
        numCycles = numCycles,
        cycleDuration = cycleDuration,
        sleepLatency = sleepLatency,
        maxWakeTime = if (maxWakeTimeHour != null && maxWakeTimeMinute != null)
            LocalTime.of(maxWakeTimeHour, maxWakeTimeMinute)
        else null,
        soundEnabled = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        isActive = isActive,
    )

    private fun AlarmConfig.toEntity() = AlarmConfigEntity(
        numCycles = numCycles,
        cycleDuration = cycleDuration,
        sleepLatency = sleepLatency,
        maxWakeTimeHour = maxWakeTime?.hour,
        maxWakeTimeMinute = maxWakeTime?.minute,
        soundEnabled = soundEnabled,
        vibrationEnabled = vibrationEnabled,
        isActive = isActive,
    )
}
