package com.example.studyspace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studyspace.data.database.DailyStats
import com.example.studyspace.data.database.StudySession
import com.example.studyspace.data.database.SubjectStats
import com.example.studyspace.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// ====================================================
// HomeViewModel - ViewModel untuk layar Dashboard
// ====================================================
class HomeViewModel(private val repository: StudyRepository) : ViewModel() {

    // ---- UI State ----
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Daily goal dalam menit (default 120 menit = 2 jam)
    private val _dailyGoalMinutes = MutableStateFlow(120)
    val dailyGoalMinutes: StateFlow<Int> = _dailyGoalMinutes.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Kombinasikan beberapa Flow dari repository
            combine(
                repository.getTotalDurationToday(),
                repository.getSessionCountToday(),
                repository.getRecentSessions(),
            ) { totalDuration, sessionCount, recentSessions ->
                HomeUiState(
                    isLoading = false,
                    totalStudyTimeToday = totalDuration,
                    sessionCountToday = sessionCount,
                    recentSessions = recentSessions,
                    dailyGoalSeconds = _dailyGoalMinutes.value * 60
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /** Memperbarui target harian */
    fun updateDailyGoal(minutes: Int) {
        _dailyGoalMinutes.value = minutes
        _uiState.value = _uiState.value.copy(
            dailyGoalSeconds = minutes * 60
        )
    }

    /** Factory untuk membuat HomeViewModel dengan dependency */
    class Factory(private val repository: StudyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/** UI State untuk layar Home */
data class HomeUiState(
    val isLoading: Boolean = true,
    val totalStudyTimeToday: Int = 0,       // Dalam detik
    val sessionCountToday: Int = 0,
    val recentSessions: List<StudySession> = emptyList(),
    val dailyGoalSeconds: Int = 7200,        // 2 jam default
    val error: String? = null
) {
    /** Progress 0.0 - 1.0 untuk daily goal indicator */
    val goalProgress: Float
        get() = if (dailyGoalSeconds > 0) {
            (totalStudyTimeToday.toFloat() / dailyGoalSeconds).coerceIn(0f, 1f)
        } else 0f

    /** Apakah daily goal sudah tercapai */
    val isGoalAchieved: Boolean
        get() = totalStudyTimeToday >= dailyGoalSeconds
}
