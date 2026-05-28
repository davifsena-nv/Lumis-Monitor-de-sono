package com.lumis.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lumis.alarm.AlarmCalculator
import com.lumis.alarm.AlarmConfig
import com.lumis.alarm.AlarmScheduler
import com.lumis.alarm.InactivityDetector
import com.lumis.alarm.SleepDetectionWorker
import com.lumis.data.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AlarmUiState(
    val numCycles: Int = 4,
    val cycleDuration: Int = 90,
    val sleepLatency: Int = 15,
    val maxWakeTime: LocalTime? = null,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isActive: Boolean = true,
    val hasUsageStatsPermission: Boolean = false,
    val previewWakeTime: String = "",
)

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val sleepRepository: SleepRepository,
    private val alarmScheduler: AlarmScheduler,
    private val inactivityDetector: InactivityDetector,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
        checkPermissions()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            val config = sleepRepository.getAlarmConfig() ?: return@launch
            _uiState.update {
                it.copy(
                    numCycles = config.numCycles,
                    cycleDuration = config.cycleDuration,
                    sleepLatency = config.sleepLatency,
                    maxWakeTime = config.maxWakeTime,
                    soundEnabled = config.soundEnabled,
                    vibrationEnabled = config.vibrationEnabled,
                    isActive = config.isActive,
                )
            }
            updatePreview()
        }
    }

    private fun checkPermissions() {
        _uiState.update { it.copy(hasUsageStatsPermission = inactivityDetector.hasPermission()) }
    }

    fun updateNumCycles(value: Int) {
        _uiState.update { it.copy(numCycles = value) }
        updatePreview()
        saveConfig()
    }

    fun updateCycleDuration(value: Int) {
        _uiState.update { it.copy(cycleDuration = value) }
        updatePreview()
        saveConfig()
    }

    fun updateSleepLatency(value: Int) {
        _uiState.update { it.copy(sleepLatency = value) }
        updatePreview()
        saveConfig()
    }

    fun updateMaxWakeTime(time: LocalTime?) {
        _uiState.update { it.copy(maxWakeTime = time) }
        saveConfig()
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
        saveConfig()
    }

    fun toggleVibration() {
        _uiState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
        saveConfig()
    }

    fun toggleAlarm() {
        val newState = !_uiState.value.isActive
        _uiState.update { it.copy(isActive = newState) }
        if (!newState) alarmScheduler.cancelAlarm()
        saveConfig()
    }

    private fun updatePreview() {
        val state = _uiState.value
        val config = AlarmConfig(
            numCycles = state.numCycles,
            cycleDuration = state.cycleDuration,
            sleepLatency = state.sleepLatency,
            maxWakeTime = state.maxWakeTime,
        )
        val exampleSleepTime = LocalTime.of(23, 0)
        val wakeTime = AlarmCalculator.previewWakeTime(exampleSleepTime, config)
        _uiState.update {
            it.copy(previewWakeTime = "Se dormir às 23h → acorda às ${wakeTime.hour}h${wakeTime.minute.toString().padStart(2, '0')}")
        }
    }

    private fun saveConfig() {
        viewModelScope.launch {
            val state = _uiState.value
            sleepRepository.saveAlarmConfig(
                AlarmConfig(
                    numCycles = state.numCycles,
                    cycleDuration = state.cycleDuration,
                    sleepLatency = state.sleepLatency,
                    maxWakeTime = state.maxWakeTime,
                    soundEnabled = state.soundEnabled,
                    vibrationEnabled = state.vibrationEnabled,
                    isActive = state.isActive,
                )
            )
        }
    }
}
