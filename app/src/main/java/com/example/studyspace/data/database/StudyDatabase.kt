package com.example.studyspace.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ====================================================
// StudyDatabase - Room Database Configuration
// Singleton pattern untuk mencegah multiple instances
// ====================================================
@Database(
    entities = [
        StudySession::class,
        StudySubject::class,
        SensorLog::class
    ],
    version = 1,
    exportSchema = false // Set true jika ingin export schema untuk migration
)
abstract class StudyDatabase : RoomDatabase() {

    /** Akses ke DAO untuk operasi database */
    abstract fun studyDao(): StudyDao

    companion object {
        // @Volatile memastikan perubahan pada INSTANCE terlihat ke semua thread
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        /**
         * Mendapatkan instance database (Singleton)
         * Menggunakan double-checked locking untuk thread safety
         */
        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_space_database" // Nama file database
                )
                    .fallbackToDestructiveMigration() // Hapus dan buat ulang jika ada perubahan schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
