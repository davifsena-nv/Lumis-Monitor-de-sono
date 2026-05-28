package com.lumis.alarm

import java.time.LocalTime

data class AlarmConfig(
    val numCycles: Int = 4,
    val cycleDuration: Int = 90,
    val sleepLatency: Int = 15,
    val maxWakeTime: LocalTime? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isActive: Boolean = true,
)
