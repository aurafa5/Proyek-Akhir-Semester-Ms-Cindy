package com.example.studyspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspace.data.database.StudySession
import com.example.studyspace.ui.components.*
import com.example.studyspace.ui.theme.*
import com.example.studyspace.viewmodel.HomeUiState
import com.example.studyspace.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

// ====================================================
// HomeScreen - Layar Dashboard
// ====================================================

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartSession: () -> Unit
) {
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

        // Header: Greeting & Date
        HomeHeader()

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Waktu Belajar",
                    value = formatStudyTime(uiState.totalStudyTimeToday),
                    subtitle = "hari ini",
                    valueColor = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = PrimaryBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                StatCard(
                    title = "Sesi Belajar",
                    value = "${uiState.sessionCountToday}",
                    subtitle = "hari ini",
                    valueColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Filled.School,
                            contentDescription = null,
                            tint = AccentPurple.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Goal Progress
            DailyGoalCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Start Action
            QuickStartCard(onStartSession = onStartSession)

            Spacer(modifier = Modifier.height(16.dp))

            // Recent Sessions
            SectionHeader(title = "Sesi Terakhir")
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.recentSessions.isEmpty()) {
                EmptySessionsCard()
            } else {
                uiState.recentSessions.forEach { session ->
                    RecentSessionItem(session = session)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
    }
}

@Composable
private fun HomeHeader() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Selamat Pagi"
        in 12..14 -> "Selamat Siang"
        in 15..17 -> "Selamat Sore"
        else -> "Selamat Malam"
    }

    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
    val todayDate = dateFormat.format(Date())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = todayDate,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText
            )
        }
        // App icon/logo placeholder
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoStories,
                contentDescription = "StudySpace",
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DailyGoalCard(uiState: HomeUiState) {
    StudyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Harian",
                style = MaterialTheme.typography.titleSmall,
                color = SecondaryText
            )
            Text(
                text = if (uiState.isGoalAchieved) "Tercapai!" else "${(uiState.goalProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = if (uiState.isGoalAchieved) SuccessGreen else PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { uiState.goalProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (uiState.isGoalAchieved) SuccessGreen else PrimaryBlue,
            trackColor = CardBorder,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatStudyTime(uiState.totalStudyTimeToday),
                style = MaterialTheme.typography.labelSmall,
                color = PrimaryText
            )
            Text(
                text = "dari ${formatStudyTime(uiState.dailyGoalSeconds)}",
                style = MaterialTheme.typography.labelSmall,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun QuickStartCard(onStartSession: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.3f),
                            AccentPurple.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mulai Belajar",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fokus dengan timer Pomodoro",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
                Button(
                    onClick = onStartSession,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mulai", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun RecentSessionItem(session: StudySession) {
    StudyCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject initial icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.subject.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.subject,
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryText,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatSessionDate(session.startTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText
                )
            }

            Text(
                text = formatStudyTime(session.duration),
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptySessionsCard() {
    StudyCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.School,
                contentDescription = null,
                tint = TertiaryText,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada sesi belajar",
                style = MaterialTheme.typography.titleSmall,
                color = SecondaryText
            )
            Text(
                text = "Mulai sesi pertamamu sekarang!",
                style = MaterialTheme.typography.bodySmall,
                color = TertiaryText
            )
        }
    }
}

/** Format detik ke format waktu yang mudah dibaca */
fun formatStudyTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}j ${minutes}m"
        minutes > 0 -> "${minutes}m ${secs}d"
        else -> "${secs}d"
    }
}

/** Format Unix timestamp ke tanggal yang mudah dibaca */
fun formatSessionDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}
