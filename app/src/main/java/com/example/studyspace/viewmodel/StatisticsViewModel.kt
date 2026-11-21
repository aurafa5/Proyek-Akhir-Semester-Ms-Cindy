package com.example.studyspace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studyspace.data.database.DailyStats
import com.example.studyspace.data.database.SubjectStats
import com.example.studyspace.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ====================================================
// StatisticsViewModel - ViewModel untuk layar Statistik
// ====================================================
class StatisticsViewModel(private val repository: StudyRepository) : ViewModel() {

    // ---- UI State ----
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            // Kombinasikan semua statistik
            combine(
                repository.getWeeklyStats(),
                repository.getMostStudiedSubject(),
                repository.getAverageSessionDuration(),
                repository.getTotalStudyTime(),
                repository.getTotalSessionCount()
            ) { weeklyStats, mostStudied, avgDuration, totalTime, totalSessions ->
                // Hitung total waktu minggu ini
                val weeklyTotal = weeklyStats.sumOf { it.totalDuration }

                StatisticsUiState(
                    isLoading = false,
                    weeklyStats = weeklyStats,
                    weeklyTotalSeconds = weeklyTotal,
                    mostStudiedSubject = mostStudied,
                    averageSessionSeconds = avgDuration.toInt(),
                    totalStudyTimeSeconds = totalTime,
                    totalSessionCount = totalSessions
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    class Factory(private val repository: StudyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatisticsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/** UI State untuk layar Statistics */
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val weeklyStats: List<DailyStats> = emptyList(),
    val weeklyTotalSeconds: Int = 0,
    val mostStudiedSubject: SubjectStats? = null,
    val averageSessionSeconds: Int = 0,
    val totalStudyTimeSeconds: Int = 0,
    val totalSessionCount: Int = 0
)
