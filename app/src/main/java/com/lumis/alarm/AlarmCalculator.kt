package com.lumis.alarm

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmCalculator {

    fun calculate(sleepDetectedAt: Instant, config: AlarmConfig): Instant {
        val totalMinutes = config.sleepLatency + (config.numCycles * config.cycleDuration)
        val wakeTime = sleepDetectedAt.plusSeconds(totalMinutes * 60L)

        config.maxWakeTime?.let { max ->
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.now(zone)
            var maxDateTime = now.toLocalDate().atTime(max)
            // Se o horário limite já passou hoje, considera amanhã
            if (maxDateTime.toInstant(zone.rules.getOffset(now)).isBefore(sleepDetectedAt)) {
                maxDateTime = maxDateTime.plusDays(1)
            }
            val maxInstant = maxDateTime.toInstant(zone.rules.getOffset(maxDateTime))
            if (wakeTime.isAfter(maxInstant)) return maxInstant
        }

        return wakeTime
    }

    fun previewWakeTime(sleepAt: LocalTime, config: AlarmConfig): LocalTime {
        val totalMinutes = config.sleepLatency + (config.numCycles * config.cycleDuration)
        return sleepAt.plusMinutes(totalMinutes.toLong())
    }
}
