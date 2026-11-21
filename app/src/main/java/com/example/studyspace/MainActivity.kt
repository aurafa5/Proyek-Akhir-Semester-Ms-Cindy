package com.example.studyspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studyspace.data.database.StudyDatabase
import com.example.studyspace.data.repository.StudyRepository
import com.example.studyspace.sensor.SensorManagerHelper
import com.example.studyspace.ui.components.StudyBottomNavigation
import com.example.studyspace.ui.navigation.Screen
import com.example.studyspace.ui.screens.*
import com.example.studyspace.ui.theme.StudySpaceTheme
import com.example.studyspace.viewmodel.*

// ====================================================
// MainActivity - Entry point aplikasi
// ====================================================
class MainActivity : ComponentActivity() {

    // SensorManagerHelper dibuat di sini agar satu instance
    // digunakan oleh FocusViewModel dan EnvironmentViewModel
    private lateinit var sensorHelper: SensorManagerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inisialisasi sensor helper dengan lifecycle awareness
        sensorHelper = SensorManagerHelper(this)
        lifecycle.addObserver(sensorHelper)

        setContent {
            StudySpaceTheme {
                StudySpaceApp(sensorHelper = sensorHelper)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pastikan sensor berhenti saat app di-pause
        // SensorManagerHelper menangani ini via lifecycle
    }

    override fun onResume() {
        super.onResume()
        // Resume sensor jika sedang monitoring
        sensorHelper.resumeIfMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHelper.cleanup()
    }
}

// ====================================================
// StudySpaceApp - Root Composable
// Menginisialisasi semua dependency dan navigasi
// ====================================================
@Composable
fun StudySpaceApp(sensorHelper: SensorManagerHelper) {
    val navController = rememberNavController()

    // Ambil Application context untuk database
    val application = androidx.compose.ui.platform.LocalContext.current.applicationContext

    // Inisialisasi database dan repository (singleton via remember)
    val database = remember {
        StudyDatabase.getDatabase(application)
    }
    val repository = remember {
        StudyRepository(database.studyDao())
    }

    // ViewModel factories
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(repository)
    )
    val focusViewModel: FocusViewModel = viewModel(
        factory = FocusViewModel.Factory(repository, sensorHelper)
    )
    val environmentViewModel: EnvironmentViewModel = viewModel(
        factory = EnvironmentViewModel.Factory(sensorHelper)
    )
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(repository)
    )
    val statisticsViewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.Factory(repository)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.studyspace.ui.theme.Background,
        bottomBar = {
            StudyBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onStartSession = {
                        navController.navigate(Screen.Focus.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Focus.route) {
                FocusScreen(viewModel = focusViewModel)
            }

            composable(Screen.Environment.route) {
                EnvironmentScreen(viewModel = environmentViewModel)
            }

            composable(Screen.History.route) {
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
        }
    }
}
