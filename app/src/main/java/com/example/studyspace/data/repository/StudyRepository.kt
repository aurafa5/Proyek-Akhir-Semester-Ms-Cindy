package com.example.studyspace.data.repository

import com.example.studyspace.data.database.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

// ====================================================
// StudyRepository - Repository Layer
// Menjadi jembatan antara ViewModel dan Database
// Semua akses data melalui repository ini
// ====================================================
class StudyRepository(private val studyDao: StudyDao) {

    // ---- Session Operations ----

    /** Menyimpan sesi belajar baru, mengembalikan ID sesi */
    suspend fun insertSession(session: StudySession): Long {
        return studyDao.insertSession(session)
    }

    /** Memperbarui sesi belajar */
    suspend fun updateSession(session: StudySession) {
        studyDao.updateSession(session)
    }

    /** Menghapus sesi belajar berdasarkan ID */
    suspend fun deleteSessionById(sessionId: Int) {
        studyDao.deleteSessionById(sessionId)
    }

    /** Mengambil sesi berdasarkan ID */
    suspend fun getSessionById(sessionId: Int): StudySession? {
        return studyDao.getSessionById(sessionId)
    }

    /** Mengamati semua sesi belajar (Flow) */
    fun getAllSessions(): Flow<List<StudySession>> {
        return studyDao.getAllSessions()
    }

    /** Mengamati sesi belajar hari ini */
    fun getSessionsToday(): Flow<List<StudySession>> {
        val (startOfDay, endOfDay) = getTodayRange()
        return studyDao.getSessionsToday(startOfDay, endOfDay)
    }

    /** Mengamati total durasi belajar hari ini (dalam detik) */
    fun getTotalDurationToday(): Flow<Int> {
        val (startOfDay, endOfDay) = getTodayRange()
        return studyDao.getTotalDurationToday(startOfDay, endOfDay)
    }

    /** Mengamati jumlah sesi hari ini */
    fun getSessionCountToday(): Flow<Int> {
        val (startOfDay, endOfDay) = getTodayRange()
        return studyDao.getSessionCountToday(startOfDay, endOfDay)
    }

    /** Mengamati 5 sesi terbaru */
    fun getRecentSessions(): Flow<List<StudySession>> {
        return studyDao.getRecentSessions()
    }

    /** Mengamati sesi berdasarkan mata pelajaran */
    fun getSessionsBySubject(subject: String): Flow<List<StudySession>> {
        return studyDao.getSessionsBySubject(subject)
    }

    /** Mengamati semua mata pelajaran yang unik */
    fun getAllSubjects(): Flow<List<String>> {
        return studyDao.getAllSubjects()
    }

    // ---- Statistics Operations ----

    /** Mengamati statistik mingguan */
    fun getWeeklyStats(): Flow<List<DailyStats>> {
        val weekStart = getWeekStartTime()
        return studyDao.getWeeklyStats(weekStart)
    }

    /** Mengamati mata pelajaran yang paling sering dipelajari */
    fun getMostStudiedSubject(): Flow<SubjectStats?> {
        return studyDao.getMostStudiedSubject()
    }

    /** Mengamati rata-rata durasi sesi (dalam detik) */
    fun getAverageSessionDuration(): Flow<Float> {
        return studyDao.getAverageSessionDuration()
    }

    /** Mengamati total waktu belajar keseluruhan */
    fun getTotalStudyTime(): Flow<Int> {
        return studyDao.getTotalStudyTime()
    }

    /** Mengamati total jumlah sesi */
    fun getTotalSessionCount(): Flow<Int> {
        return studyDao.getTotalSessionCount()
    }

    // ---- SensorLog Operations ----

    /** Menyimpan log sensor */
    suspend fun insertSensorLog(log: SensorLog): Long {
        return studyDao.insertSensorLog(log)
    }

    /** Mengambil log sensor untuk sesi tertentu */
    suspend fun getSensorLogsForSession(sessionId: Int): List<SensorLog> {
        return studyDao.getSensorLogsForSession(sessionId)
    }

    // ---- Helper Functions ----

    /**
     * Menghitung awal dan akhir hari ini dalam Unix timestamp
     * Digunakan untuk query sesi hari ini
     */
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        // Set ke awal hari (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // Set ke akhir hari (23:59:59)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    /**
     * Menghitung awal minggu ini (Senin) dalam Unix timestamp
     */
    private fun getWeekStartTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
