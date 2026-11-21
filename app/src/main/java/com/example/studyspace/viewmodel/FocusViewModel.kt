package com.example.studyspace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studyspace.data.database.StudySession
import com.example.studyspace.data.repository.StudyRepository
import com.example.studyspace.sensor.SensorManagerHelper
import com.example.studyspace.sensor.SensorSessionSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ====================================================
// FocusViewModel - ViewModel untuk layar Timer
// Menggunakan timestamp-based timer untuk akurasi
// ====================================================
class FocusViewModel(
    private val repository: StudyRepository,
    private val sensorHelper: SensorManagerHelper
) : ViewModel() {

    // ---- UI State ----
    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    // Job untuk coroutine timer
    private var timerJob: Job? = null

    // Waktu mulai aktual (timestamp)
    private var actualStartTime: Long = 0L

    // Waktu sebelumnya saat timer dijeda
    private var pausedElapsedTime: Long = 0L

    init {
        // Set durasi default (25 menit)
        setDuration(25)
    }

    // ---- Timer Control Methods ----

    /**
     * Mulai timer
     * Menggunakan timestamp-based approach untuk akurasi
     * - Tidak akan terpengaruh recomposition
     * - Akurat meski ada delay kecil di coroutine
     */
    fun startTimer() {
        if (_uiState.value.timerState != TimerState.IDLE &&
            _uiState.value.timerState != TimerState.PAUSED) return

        // Validasi input
        if (_uiState.value.subject.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Masukkan nama mata pelajaran terlebih dahulu")
            return
        }

        val currentState = _uiState.value

        if (currentState.timerState == TimerState.IDLE) {
            // Sesi baru: catat waktu mulai
            actualStartTime = System.currentTimeMillis()
            pausedElapsedTime = 0L
            sensorHelper.startMonitoring()
        }

        _uiState.value = _uiState.value.copy(
            timerState = TimerState.RUNNING,
            error = null
        )

        // Mulai coroutine timer
        // Update setiap 100ms untuk animasi yang halus
        val resumeTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(100) // Update setiap 100ms

                val now = System.currentTimeMillis()
                // Hitung elapsed time yang akurat berdasarkan timestamp
                val elapsedSinceResume = now - resumeTime
                val totalElapsed = pausedElapsedTime + elapsedSinceResume
                val totalDurationMs = _uiState.value.totalDurationSeconds * 1000L
                val remainingMs = (totalDurationMs - totalElapsed).coerceAtLeast(0)

                _uiState.value = _uiState.value.copy(
                    remainingSeconds = (remainingMs / 1000).toInt(),
                    elapsedSeconds = (totalElapsed / 1000).toInt()
                )

                // Timer selesai
                if (remainingMs <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    /** Jeda timer */
    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null

        // Simpan elapsed time saat dijeda
        val currentState = _uiState.value
        pausedElapsedTime = (currentState.totalDurationSeconds - currentState.remainingSeconds).toLong() * 1000L

        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
    }

    /** Lanjutkan timer setelah dijeda */
    fun resumeTimer() {
        startTimer()
    }

    /** Reset timer ke kondisi awal */
    fun resetTimer() {
        timerJob?.cancel()
        timerJob = null
        sensorHelper.stopMonitoring()
        sensorHelper.resetSessionData()
        pausedElapsedTime = 0L
        actualStartTime = 0L

        val duration = _uiState.value.totalDurationSeconds
        _uiState.value = _uiState.value.copy(
            timerState = TimerState.IDLE,
            remainingSeconds = duration,
            elapsedSeconds = 0,
            showSessionSummary = false,
            completedSession = null,
            error = null
        )
    }

    /** Dipanggil ketika timer selesai */
    private fun onTimerFinished() {
        timerJob = null
        sensorHelper.stopMonitoring()

        val sensorSummary = sensorHelper.getSessionSummary()
        val endTime = System.currentTimeMillis()
        val actualDuration = ((endTime - actualStartTime) / 1000).toInt()

        val currentState = _uiState.value

        // Simpan sesi ke database
        viewModelScope.launch {
            val session = StudySession(
                subject = currentState.subject,
                startTime = actualStartTime,
                endTime = endTime,
                duration = actualDuration,
                notes = currentState.notes,
                averageLight = sensorSummary.averageLight,
                minLight = sensorSummary.minLight,
                maxLight = sensorSummary.maxLight,
                movementCount = sensorSummary.movementCount,
                mode = currentState.selectedMode.value
            )

            val sessionId = repository.insertSession(session)

            _uiState.value = _uiState.value.copy(
                timerState = TimerState.FINISHED,
                remainingSeconds = 0,
                showSessionSummary = true,
                completedSession = session.copy(id = sessionId.toInt())
            )
        }
    }

    /** Menutup dialog ringkasan sesi dan reset timer */
    fun dismissSessionSummary() {
        resetTimer()
    }

    // ---- Settings Methods ----

    /** Mengatur mata pelajaran */
    fun setSubject(subject: String) {
        _uiState.value = _uiState.value.copy(subject = subject, error = null)
    }

    /** Mengatur catatan belajar */
    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    /** Mengatur mode timer */
    fun setMode(mode: StudyMode) {
        if (_uiState.value.timerState != TimerState.IDLE) return
        _uiState.value = _uiState.value.copy(selectedMode = mode)
        // Set durasi default sesuai mode
        val defaultDuration = when (mode) {
            StudyMode.FOCUS -> 25
            StudyMode.SHORT_BREAK -> 5
            StudyMode.LONG_BREAK -> 15
        }
        setDuration(defaultDuration)
    }

    /** Mengatur durasi timer (dalam menit) */
    fun setDuration(minutes: Int) {
        if (_uiState.value.timerState != TimerState.IDLE) return
        val seconds = minutes * 60
        _uiState.value = _uiState.value.copy(
            selectedDurationMinutes = minutes,
            totalDurationSeconds = seconds,
            remainingSeconds = seconds
        )
    }

    /** Mengatur durasi kustom */
    fun setCustomDuration(minutes: Int) {
        if (minutes in 1..180) {
            setDuration(minutes)
        }
    }

    /** Clear error */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    /** Factory untuk membuat FocusViewModel */
    class Factory(
        private val repository: StudyRepository,
        private val sensorHelper: SensorManagerHelper
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FocusViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FocusViewModel(repository, sensorHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

// ---- Enums dan Data Classes ----

/** Mode timer */
enum class StudyMode(val displayName: String, val value: String) {
    FOCUS("Focus", "focus"),
    SHORT_BREAK("Short Break", "short_break"),
    LONG_BREAK("Long Break", "long_break")
}

/** Status timer */
enum class TimerState {
    IDLE,       // Timer belum dimulai
    RUNNING,    // Timer sedang berjalan
    PAUSED,     // Timer dijeda
    FINISHED    // Timer selesai
}

/** UI State untuk layar Focus */
data class FocusUiState(
    val timerState: TimerState = TimerState.IDLE,
    val selectedMode: StudyMode = StudyMode.FOCUS,
    val selectedDurationMinutes: Int = 25,
    val totalDurationSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val elapsedSeconds: Int = 0,
    val subject: String = "",
    val notes: String = "",
    val showSessionSummary: Boolean = false,
    val completedSession: StudySession? = null,
    val error: String? = null
) {
    /** Progress 0.0 - 1.0 untuk circular progress */
    val progress: Float
        get() = if (totalDurationSeconds > 0) {
            1f - (remainingSeconds.toFloat() / totalDurationSeconds.toFloat())
        } else 0f

    /** Format waktu tersisa (MM:SS) */
    val formattedRemainingTime: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
