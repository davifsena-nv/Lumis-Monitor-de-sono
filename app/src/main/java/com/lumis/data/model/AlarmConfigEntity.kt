package com.lumis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_config")
data class AlarmConfigEntity(
    @PrimaryKey val id: Int = 1,
    val numCycles: Int = 4,
    val cycleDuration: Int = 90,
    val sleepLatency: Int = 15,
    val maxWakeTimeHour: Int? = null,
    val maxWakeTimeMinute: Int? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isActive: Boolean = true,
    val scheduledAlarmTime: Long? = null,  // epoch millis do próximo alarme agendado
)
