package com.example.studyspace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studyspace.sensor.AccelerometerData
import com.example.studyspace.sensor.LightCategory
import com.example.studyspace.sensor.LightData
import com.example.studyspace.sensor.SensorAvailability
import com.example.studyspace.sensor.SensorManagerHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ====================================================
// EnvironmentViewModel - ViewModel untuk layar Sensor
// ====================================================
class EnvironmentViewModel(
    private val sensorHelper: SensorManagerHelper
) : ViewModel() {

    // ---- UI State ----
    private val _uiState = MutableStateFlow(EnvironmentUiState())
    val uiState: StateFlow<EnvironmentUiState> = _uiState.asStateFlow()

    // Riwayat pembacaan cahaya untuk grafik (max 60 data poin)
    private val lightHistory = ArrayDeque<Float>(60)

    init {
        observeSensorData()
    }

    private fun observeSensorData() {
        // Amati data accelerometer
        viewModelScope.launch {
            sensorHelper.accelerometerData.collect { data ->
                _uiState.value = _uiState.value.copy(
                    accelerometerData = data
                )
            }
        }

        // Amati data light sensor
        viewModelScope.launch {
            sensorHelper.lightData.collect { data ->
                // Tambahkan ke history untuk grafik
                if (lightHistory.size >= 60) {
                    lightHistory.removeFirst()
                }
                lightHistory.addLast(data.lux)

                _uiState.value = _uiState.value.copy(
                    lightData = data,
                    lightHistory = lightHistory.toList()
                )
            }
        }

        // Amati ketersediaan sensor
        viewModelScope.launch {
            sensorHelper.sensorAvailability.collect { availability ->
                _uiState.value = _uiState.value.copy(
                    sensorAvailability = availability
                )
            }
        }
    }

    /** Mulai monitoring sensor */
    fun startMonitoring() {
        sensorHelper.startMonitoring()
        _uiState.value = _uiState.value.copy(isMonitoring = true)
    }

    /** Hentikan monitoring sensor */
    fun stopMonitoring() {
        sensorHelper.stopMonitoring()
        _uiState.value = _uiState.value.copy(isMonitoring = false)
    }

    /** Toggle monitoring */
    fun toggleMonitoring() {
        if (_uiState.value.isMonitoring) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorHelper.stopMonitoring()
    }

    class Factory(private val sensorHelper: SensorManagerHelper) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EnvironmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EnvironmentViewModel(sensorHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/** UI State untuk layar Environment */
data class EnvironmentUiState(
    val isMonitoring: Boolean = false,
    val accelerometerData: AccelerometerData = AccelerometerData(),
    val lightData: LightData = LightData(),
    val lightHistory: List<Float> = emptyList(),
    val sensorAvailability: SensorAvailability = SensorAvailability()
)
