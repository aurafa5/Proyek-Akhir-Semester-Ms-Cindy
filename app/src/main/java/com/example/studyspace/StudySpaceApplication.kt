package com.example.studyspace

import android.app.Application
import com.example.studyspace.data.database.StudyDatabase

// ====================================================
// StudySpaceApplication - Application class
// Inisialisasi komponen yang diperlukan di level app
// ====================================================
class StudySpaceApplication : Application() {

    // Database instance - lazy initialization
    // Hanya dibuat ketika pertama kali diakses
    val database: StudyDatabase by lazy {
        StudyDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Inisialisasi komponen global jika diperlukan
    }
}
