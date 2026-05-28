package com.lumis.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_nights")
data class SleepNight(
    @PrimaryKey val date: String,         // ISO date: "2025-05-28"
    val startTime: Long,                  // epoch millis
    val endTime: Long,                    // epoch millis
    val score: Int,
    val deepMinutes: Int,
    val remMinutes: Int,
    val lightMinutes: Int,
    val totalMinutes: Int,
)
