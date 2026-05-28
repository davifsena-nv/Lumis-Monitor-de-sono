package com.lumis.ui.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumis.health.HealthConnectManager
import com.lumis.health.SleepScoreCalculator
import com.lumis.health.parseSleepPhases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class DayScore(val label: String, val score: Int)

data class SleepPhaseSegment(
    val startMinute: Float,
    val endMinute: Float,
    val phase: String,
)

data class DataUiState(
    val isLoading: Boolean = true,
    val weekScores: List<DayScore> = emptyList(),
    val lastNightPhases: List<SleepPhaseSegment> = emptyList(),
    val totalHoursLastNight: Float = 0f,
    val deepPercent: Int = 0,
    val remPercent: Int = 0,
    val lightPercent: Int = 0,
    val errorMessage: String? = null,
)

@HiltViewModel
class DataViewModel @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    private val dayFormatter = DateTimeFormatter.ofPattern("EEE").withLocale(java.util.Locale("pt", "BR"))

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val sessions = healthConnectManager.readSleepSessions(7)

                val weekScores = sessions
                    .sortedBy { it.startTime }
                    .map { session ->
                        val label = session.startTime
                            .atZone(ZoneId.systemDefault())
                            .format(dayFormatter)
                            .replaceFirstChar { it.uppercase() }
                        DayScore(label, SleepScoreCalculator.calculateNightScore(session))
                    }

                val lastNight = sessions.maxByOrNull { it.endTime }
                val phases = lastNight?.let { s ->
                    parseSleepPhases(s).map {
                        SleepPhaseSegment(it.startMinute, it.endMinute, it.phase)
                    }
                } ?: emptyList()

                val totalMinutes = lastNight?.let {
                    java.time.Duration.between(it.startTime, it.endTime).toMinutes()
                } ?: 0L

                val deepMin = phases.filter { it.phase == "deep" }
                    .sumOf { (it.endMinute - it.startMinute).toDouble() }
                val remMin = phases.filter { it.phase == "rem" }
                    .sumOf { (it.endMinute - it.startMinute).toDouble() }
                val lightMin = phases.filter { it.phase == "light" }
                    .sumOf { (it.endMinute - it.startMinute).toDouble() }

                val deepPct = if (totalMinutes > 0) ((deepMin / totalMinutes) * 100).toInt() else 0
                val remPct = if (totalMinutes > 0) ((remMin / totalMinutes) * 100).toInt() else 0
                val lightPct = if (totalMinutes > 0) ((lightMin / totalMinutes) * 100).toInt() else 0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        weekScores = weekScores,
                        lastNightPhases = phases,
                        totalHoursLastNight = totalMinutes / 60f,
                        deepPercent = deepPct,
                        remPercent = remPct,
                        lightPercent = lightPct,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Erro ao carregar dados: ${e.message}")
                }
            }
        }
    }
}
