package com.example.studyspace.data.repository

import com.example.studyspace.data.database.SensorLog
import com.example.studyspace.data.database.StudyDao

// ====================================================
// SensorRepository - Repository untuk data sensor
// Menyimpan log sensor yang terkait dengan sesi belajar
// ====================================================
class SensorRepository(private val studyDao: StudyDao) {

    /** Menyimpan satu log sensor ke database */
    suspend fun insertSensorLog(log: SensorLog): Long {
        return studyDao.insertSensorLog(log)
    }

    /** Mengambil semua log sensor untuk sesi tertentu */
    suspend fun getSensorLogsForSession(sessionId: Int): List<SensorLog> {
        return studyDao.getSensorLogsForSession(sessionId)
    }

    /** Menghapus log sensor untuk sesi tertentu */
    suspend fun deleteSensorLogsForSession(sessionId: Int) {
        studyDao.deleteSensorLogsForSession(sessionId)
    }
}
