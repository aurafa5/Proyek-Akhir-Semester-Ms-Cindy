package com.example.studyspace.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// ====================================================
// StudySubject Entity - Menyimpan daftar mata pelajaran
// ====================================================
@Entity(tableName = "study_subjects")
data class StudySubject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Nama mata pelajaran
    val subjectName: String,

    // Warna representasi mata pelajaran (hex string, misal: "#5B8CFF")
    val color: String = "#5B8CFF",

    // Waktu dibuat (Unix timestamp)
    val createdAt: Long = System.currentTimeMillis()
)
