package com.lumis.health

import androidx.health.connect.client.records.SleepSessionRecord
import com.lumis.character.LumisState

sealed class SleepDataState {
    object Loading : SleepDataState()
    object NoPermission : SleepDataState()
    object NoData : SleepDataState()
    object HealthConnectUnavailable : SleepDataState()
    data class Success(
        val sessions: List<SleepSessionRecord>,
        val weeklyScore: Int,
        val lumisState: LumisState,
    ) : SleepDataState()
}
