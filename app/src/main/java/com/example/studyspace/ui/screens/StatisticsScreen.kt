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
import com.example.studyspace.viewmodel.StatisticsViewModel

// ====================================================
// StatisticsScreen - Layar Statistik Belajar
// ====================================================

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
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

        Text(
            text = "Statistik",
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.totalSessionCount == 0) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = TertiaryText,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Belum ada data statistik",
                        style = MaterialTheme.typography.titleSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = "Selesaikan beberapa sesi belajar untuk melihat statistik",
                        style = MaterialTheme.typography.bodySmall,
                        color = TertiaryText
                    )
                }
            }
        } else {
            // Overview Stats
            SectionHeader(title = "Ringkasan")
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Belajar",
                    value = formatStudyTime(uiState.totalStudyTimeSeconds),
                    valueColor = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Filled.AccessTime, null,
                            tint = PrimaryBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
                StatCard(
                    title = "Total Sesi",
                    value = "${uiState.totalSessionCount}",
                    valueColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Filled.CheckCircle, null,
                            tint = AccentPurple.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Rata-rata Sesi",
                    value = formatStudyTime(uiState.averageSessionSeconds),
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Filled.Timer, null,
                            tint = SuccessGreen.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
                // Most studied subject
                uiState.mostStudiedSubject?.let { subjectStats ->
                    StatCard(
                        title = "Paling Sering",
                        value = subjectStats.subject,
                        subtitle = formatStudyTime(subjectStats.totalDuration),
                        valueColor = WarningYellow,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Filled.Star, null,
                                tint = WarningYellow.copy(alpha = 0.7f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Chart
            SectionHeader(title = "Aktivitas Minggu Ini")
            Spacer(modifier = Modifier.height(12.dp))

            StudyCard(modifier = Modifier.fillMaxWidth()) {
                // Total minggu ini
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Minggu Ini",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = formatStudyTime(uiState.weeklyTotalSeconds),
                        style = MaterialTheme.typography.titleSmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.weeklyStats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada data minggu ini",
                            style = MaterialTheme.typography.bodySmall,
                            color = TertiaryText
                        )
                    }
                } else {
                    WeeklyBarChart(
                        dailyStats = uiState.weeklyStats,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Per-subject breakdown (jika ada)
            if (uiState.mostStudiedSubject != null) {
                SectionHeader(title = "Detail Mata Pelajaran")
                Spacer(modifier = Modifier.height(12.dp))

                uiState.mostStudiedSubject?.let { stats ->
                    StudyCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            WarningYellow.copy(alpha = 0.15f),
                                            androidx.compose.foundation.shape.CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stats.subject.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = WarningYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = stats.subject,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PrimaryText,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${stats.sessionCount} sesi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryText
                                    )
                                }
                            }
                            Text(
                                text = formatStudyTime(stats.totalDuration),
                                style = MaterialTheme.typography.titleSmall,
                                color = WarningYellow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
