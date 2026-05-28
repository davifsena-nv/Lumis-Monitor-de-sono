package com.lumis.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumis.character.LumisController
import com.lumis.character.LumisState
import com.lumis.data.SleepRepository
import com.lumis.health.HealthConnectManager
import com.lumis.health.SleepDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val sleepDataState: SleepDataState = SleepDataState.Loading,
    val lumisState: LumisState = LumisState.REGULAR,
    val weeklyScore: Int = 0,
    val nextAlarmTime: String? = null,
    val lastNightSummary: String = "",
    val isAlarmActive: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val sleepRepository: SleepRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            if (!healthConnectManager.isAvailable()) {
                _uiState.update { it.copy(sleepDataState = SleepDataState.HealthConnectUnavailable) }
                return@launch
            }
            if (!healthConnectManager.hasPermissions()) {
                _uiState.update { it.copy(sleepDataState = SleepDataState.NoPermission) }
                return@launch
            }
            try {
                val sessions = healthConnectManager.readSleepSessions(7)
                if (sessions.isEmpty()) {
                    _uiState.update { it.copy(sleepDataState = SleepDataState.NoData) }
                    return@launch
                }

                val weeklyScore = com.lumis.health.SleepScoreCalculator.calculateWeeklyScore(sessions)
                val lumisState = LumisController.getStateFromScore(weeklyScore)
                val lastNight = sessions.maxByOrNull { it.endTime }
                val summary = lastNight?.let { formatLastNightSummary(it.startTime, it.endTime) } ?: ""

                val config = sleepRepository.getAlarmConfig()
                val nextAlarm = sleepRepository.getScheduledAlarmTime()?.let { formatTime(it) }

                _uiState.update {
                    it.copy(
                        sleepDataState = SleepDataState.Success(sessions, weeklyScore, lumisState),
                        lumisState = lumisState,
                        weeklyScore = weeklyScore,
                        nextAlarmTime = nextAlarm,
                        lastNightSummary = summary,
                        isAlarmActive = config?.isActive ?: false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(sleepDataState = SleepDataState.NoData) }
            }
        }
    }

    private fun formatLastNightSummary(start: Instant, end: Instant): String {
        val hours = java.time.Duration.between(start, end).toHours()
        val minutes = java.time.Duration.between(start, end).toMinutes() % 60
        return "${hours}h${if (minutes > 0) "${minutes}min" else ""} de sono"
    }

    private fun formatTime(instant: Instant): String {
        return instant.atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}
