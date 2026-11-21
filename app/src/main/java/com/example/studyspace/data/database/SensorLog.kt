package com.example.studyspace.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ====================================================
// SensorLog Entity - Menyimpan log sensor per sesi
// Digunakan untuk menyimpan sample cahaya secara periodik
// (bukan setiap sensor event, untuk efisiensi)
// ====================================================
@Entity(
    tableName = "sensor_logs",
    foreignKeys = [
        ForeignKey(
            entity = StudySession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Hapus log jika sesi dihapus
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class SensorLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ID sesi belajar terkait
    val sessionId: Int,

    // Intensitas cahaya dalam lux
    val lightIntensity: Float,

    // Waktu pencatatan (Unix timestamp dalam milidetik)
    val recordedAt: Long = System.currentTimeMillis()
)
