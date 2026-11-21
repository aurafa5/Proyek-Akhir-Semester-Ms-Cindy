package com.example.studyspace.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// ====================================================
// StudySession Entity - Menyimpan data sesi belajar
// ====================================================
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nama mata pelajaran yang dipelajari
    val subject: String,

    // Waktu mulai sesi (Unix timestamp dalam milidetik)
    val startTime: Long,

    // Waktu selesai sesi (Unix timestamp dalam milidetik)
    val endTime: Long,

    // Durasi aktual sesi dalam detik
    val duration: Int,

    // Catatan belajar dari pengguna
    val notes: String = "",

    // Data sensor cahaya selama sesi
    val averageLight: Float = 0f,
    val minLight: Float = 0f,
    val maxLight: Float = 0f,

    // Jumlah pergerakan perangkat selama sesi
    val movementCount: Int = 0,

    // Mode sesi: "focus", "short_break", "long_break"
    val mode: String = "focus"
)
