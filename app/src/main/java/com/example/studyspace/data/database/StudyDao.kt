package com.example.studyspace.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ====================================================
// StudyDao - Data Access Object
// Berisi semua operasi database untuk StudySpace
// ====================================================
@Dao
interface StudyDao {

    // ---- StudySession Operations ----

    /** Menyimpan sesi belajar baru ke database */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    /** Memperbarui sesi belajar yang sudah ada */
    @Update
    suspend fun updateSession(session: StudySession)

    /** Menghapus satu sesi belajar */
    @Delete
    suspend fun deleteSession(session: StudySession)

    /** Menghapus sesi berdasarkan ID */
    @Query("DELETE FROM study_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    /** Mengambil satu sesi berdasarkan ID */
    @Query("SELECT * FROM study_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): StudySession?

    /** Mengamati semua sesi belajar, diurutkan dari terbaru */
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    /** Mengambil sesi belajar hari ini */
    @Query("""
        SELECT * FROM study_sessions 
        WHERE startTime >= :startOfDay AND startTime < :endOfDay
        ORDER BY startTime DESC
    """)
    fun getSessionsToday(startOfDay: Long, endOfDay: Long): Flow<List<StudySession>>

    /** Mengambil sesi belajar dalam rentang waktu tertentu */
    @Query("""
        SELECT * FROM study_sessions 
        WHERE startTime >= :startTime AND startTime < :endTime
        ORDER BY startTime DESC
    """)
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySession>>

    /** Mengambil total durasi belajar hari ini (dalam detik) */
    @Query("""
        SELECT COALESCE(SUM(duration), 0) FROM study_sessions 
        WHERE startTime >= :startOfDay AND startTime < :endOfDay
    """)
    fun getTotalDurationToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    /** Mengambil jumlah sesi hari ini */
    @Query("""
        SELECT COUNT(*) FROM study_sessions 
        WHERE startTime >= :startOfDay AND startTime < :endOfDay
    """)
    fun getSessionCountToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    /** Mengambil sesi berdasarkan mata pelajaran */
    @Query("""
        SELECT * FROM study_sessions 
        WHERE subject = :subject
        ORDER BY startTime DESC
    """)
    fun getSessionsBySubject(subject: String): Flow<List<StudySession>>

    /** Mengambil 5 sesi terbaru untuk dashboard */
    @Query("SELECT * FROM study_sessions ORDER BY startTime DESC LIMIT 5")
    fun getRecentSessions(): Flow<List<StudySession>>

    /** Mengambil statistik durasi per hari dalam 7 hari terakhir */
    @Query("""
        SELECT 
            strftime('%Y-%m-%d', startTime / 1000, 'unixepoch', 'localtime') as date,
            SUM(duration) as totalDuration,
            COUNT(*) as sessionCount
        FROM study_sessions
        WHERE startTime >= :weekStartTime
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getWeeklyStats(weekStartTime: Long): Flow<List<DailyStats>>

    /** Mengambil mata pelajaran yang paling sering dipelajari */
    @Query("""
        SELECT subject, SUM(duration) as totalDuration, COUNT(*) as sessionCount
        FROM study_sessions
        GROUP BY subject
        ORDER BY totalDuration DESC
        LIMIT 1
    """)
    fun getMostStudiedSubject(): Flow<SubjectStats?>

    /** Mengambil semua mata pelajaran yang unik */
    @Query("SELECT DISTINCT subject FROM study_sessions ORDER BY subject ASC")
    fun getAllSubjects(): Flow<List<String>>

    /** Mengambil rata-rata durasi sesi */
    @Query("SELECT COALESCE(AVG(duration), 0) FROM study_sessions")
    fun getAverageSessionDuration(): Flow<Float>

    /** Mengambil total semua waktu belajar */
    @Query("SELECT COALESCE(SUM(duration), 0) FROM study_sessions")
    fun getTotalStudyTime(): Flow<Int>

    /** Mengambil total semua sesi */
    @Query("SELECT COUNT(*) FROM study_sessions")
    fun getTotalSessionCount(): Flow<Int>

    // ---- StudySubject Operations ----

    /** Menyimpan mata pelajaran baru */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubject(subject: StudySubject): Long

    /** Mengamati semua mata pelajaran */
    @Query("SELECT * FROM study_subjects ORDER BY subjectName ASC")
    fun getAllStudySubjects(): Flow<List<StudySubject>>

    // ---- SensorLog Operations ----

    /** Menyimpan log sensor */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSensorLog(log: SensorLog): Long

    /** Mengambil log sensor untuk sesi tertentu */
    @Query("""
        SELECT * FROM sensor_logs 
        WHERE sessionId = :sessionId 
        ORDER BY recordedAt ASC
    """)
    suspend fun getSensorLogsForSession(sessionId: Int): List<SensorLog>

    /** Menghapus semua log sensor untuk sesi tertentu */
    @Query("DELETE FROM sensor_logs WHERE sessionId = :sessionId")
    suspend fun deleteSensorLogsForSession(sessionId: Int)
}

// ---- Data Classes untuk Query Kompleks ----

/** Statistik harian untuk grafik */
data class DailyStats(
    val date: String,
    val totalDuration: Int,
    val sessionCount: Int
)

/** Statistik mata pelajaran */
data class SubjectStats(
    val subject: String,
    val totalDuration: Int,
    val sessionCount: Int
)
