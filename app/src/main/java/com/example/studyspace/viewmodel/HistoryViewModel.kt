package com.example.studyspace.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studyspace.data.database.StudySession
import com.example.studyspace.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ====================================================
// HistoryViewModel - ViewModel untuk layar Riwayat
// ====================================================
class HistoryViewModel(private val repository: StudyRepository) : ViewModel() {

    // ---- UI State ----
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // Filter mata pelajaran yang dipilih
    private val _selectedSubject = MutableStateFlow<String?>(null)

    init {
        loadHistory()
        loadSubjects()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            // Kombinasikan filter subjek dengan data
            _selectedSubject.flatMapLatest { subject ->
                if (subject == null) {
                    repository.getAllSessions()
                } else {
                    repository.getSessionsBySubject(subject)
                }
            }.collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    sessions = sessions
                )
            }
        }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            repository.getAllSubjects().collect { subjects ->
                _uiState.value = _uiState.value.copy(subjects = subjects)
            }
        }
    }

    /** Filter berdasarkan mata pelajaran */
    fun filterBySubject(subject: String?) {
        _selectedSubject.value = subject
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }

    /** Hapus sesi belajar */
    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSessionById(session.id)
        }
    }

    /** Tampilkan dialog konfirmasi hapus */
    fun showDeleteConfirmation(session: StudySession) {
        _uiState.value = _uiState.value.copy(sessionToDelete = session)
    }

    /** Batalkan hapus */
    fun cancelDelete() {
        _uiState.value = _uiState.value.copy(sessionToDelete = null)
    }

    /** Konfirmasi hapus sesi */
    fun confirmDelete() {
        val session = _uiState.value.sessionToDelete ?: return
        deleteSession(session)
        _uiState.value = _uiState.value.copy(sessionToDelete = null)
    }

    /** Tampilkan detail sesi */
    fun showSessionDetail(session: StudySession) {
        _uiState.value = _uiState.value.copy(selectedSession = session)
    }

    /** Tutup detail sesi */
    fun hideSessionDetail() {
        _uiState.value = _uiState.value.copy(selectedSession = null)
    }

    /** Update catatan sesi */
    fun updateSessionNotes(session: StudySession, newNotes: String) {
        viewModelScope.launch {
            val updatedSession = session.copy(notes = newNotes)
            repository.updateSession(updatedSession)
            _uiState.value = _uiState.value.copy(selectedSession = updatedSession)
        }
    }

    class Factory(private val repository: StudyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/** UI State untuk layar History */
data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessions: List<StudySession> = emptyList(),
    val subjects: List<String> = emptyList(),
    val selectedSubject: String? = null,
    val selectedSession: StudySession? = null,
    val sessionToDelete: StudySession? = null
)
