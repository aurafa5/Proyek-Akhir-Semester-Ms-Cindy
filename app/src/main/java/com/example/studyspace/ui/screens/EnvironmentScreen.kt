package com.example.studyspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspace.ui.components.*
import com.example.studyspace.ui.theme.*
import com.example.studyspace.viewmodel.EnvironmentViewModel

// ====================================================
// EnvironmentScreen - Layar Monitoring Sensor
// ====================================================

@Composable
fun EnvironmentScreen(viewModel: EnvironmentViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lingkungan Belajar",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )

            // Toggle monitoring button
            Switch(
                checked = uiState.isMonitoring,
                onCheckedChange = { viewModel.toggleMonitoring() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryBlue,
                    checkedTrackColor = PrimaryBlue.copy(alpha = 0.4f),
                    uncheckedThumbColor = SecondaryText,
                    uncheckedTrackColor = CardBorder
                )
            )
        }

        Text(
            text = if (uiState.isMonitoring) "Monitoring aktif" else "Monitoring tidak aktif",
            style = MaterialTheme.typography.labelSmall,
            color = if (uiState.isMonitoring) SuccessGreen else SecondaryText
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pesan informatif
        if (!uiState.isMonitoring) {
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Aktifkan monitoring untuk mulai membaca data sensor perangkat Anda secara real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Light Sensor Card
        SectionHeader(title = "Intensitas Cahaya")
        Spacer(modifier = Modifier.height(8.dp))
        LightSensorCard(
            lightData = uiState.lightData,
            isAvailable = uiState.sensorAvailability.hasLightSensor
        )

        // Grafik perubahan cahaya (tampil hanya saat ada data)
        if (uiState.lightHistory.size >= 2 && uiState.isMonitoring) {
            Spacer(modifier = Modifier.height(12.dp))
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Riwayat Cahaya",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                LightIntensityChart(
                    values = uiState.lightHistory,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Min/Max display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: ${uiState.lightHistory.minOrNull()?.toInt() ?: 0} lux",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = "Max: ${uiState.lightHistory.maxOrNull()?.toInt() ?: 0} lux",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accelerometer Card
        SectionHeader(title = "Gerakan Perangkat")
        Spacer(modifier = Modifier.height(8.dp))
        AccelerometerCard(
            data = uiState.accelerometerData,
            isAvailable = uiState.sensorAvailability.hasAccelerometer
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Informasi tambahan tentang sensor
        StudyCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SecondarySurface
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = WarningYellow,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                )
                Column {
                    Text(
                        text = "Tips Lingkungan Belajar",
                        style = MaterialTheme.typography.labelMedium,
                        color = PrimaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "Gunakan pencahayaan minimal 200-500 lux",
                        "Letakkan HP di permukaan yang stabil",
                        "Hindari silau langsung dari lampu atau matahari",
                        "Pastikan ruangan cukup terang namun tidak menyilaukan"
                    ).forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
