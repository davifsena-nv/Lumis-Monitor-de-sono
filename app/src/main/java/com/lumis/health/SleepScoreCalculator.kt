package com.lumis.health

import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration

object SleepScoreCalculator {

    fun calculateWeeklyScore(sessions: List<SleepSessionRecord>): Int {
        if (sessions.isEmpty()) return 0
        return sessions.map { calculateNightScore(it) }.average().toInt()
    }

    fun calculateNightScore(session: SleepSessionRecord): Int {
        val stages = session.stages
        if (stages.isEmpty()) return 50

        val totalMinutes = stages.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }
        if (totalMinutes == 0L) return 0

        val deepMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val remMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_REM }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val lightMinutes = stages
            .filter { it.stage == SleepSessionRecord.STAGE_TYPE_LIGHT }
            .sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }

        val deepPct = (deepMinutes.toFloat() / totalMinutes) * 100
        val remPct = (remMinutes.toFloat() / totalMinutes) * 100
        val lightPct = (lightMinutes.toFloat() / totalMinutes) * 100

        val durationBonus = when {
            totalMinutes >= 480 -> 10
            totalMinutes >= 420 -> 7
            totalMinutes >= 360 -> 4
            else -> 0
        }

        return ((deepPct * 0.4) + (remPct * 0.3) + (lightPct * 0.2) + durationBonus)
            .toInt()
            .coerceIn(0, 100)
    }
}

// Extensão para parsear fases para exibição no DataScreen
data class SleepPhaseData(
    val startMinute: Float,
    val endMinute: Float,
    val phase: String,
)

fun parseSleepPhases(session: SleepSessionRecord): List<SleepPhaseData> {
    val sessionStart = session.startTime
    return session.stages.map { stage ->
        val startMin = Duration.between(sessionStart, stage.startTime).toMinutes().toFloat()
        val endMin = Duration.between(sessionStart, stage.endTime).toMinutes().toFloat()
        val phaseName = when (stage.stage) {
            SleepSessionRecord.STAGE_TYPE_DEEP -> "deep"
            SleepSessionRecord.STAGE_TYPE_REM -> "rem"
            SleepSessionRecord.STAGE_TYPE_LIGHT -> "light"
            SleepSessionRecord.STAGE_TYPE_AWAKE -> "awake"
            else -> "unknown"
        }
        SleepPhaseData(startMin, endMin, phaseName)
    }
}
