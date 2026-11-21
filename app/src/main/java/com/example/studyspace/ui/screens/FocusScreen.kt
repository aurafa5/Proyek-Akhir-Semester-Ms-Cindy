package com.example.studyspace.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspace.ui.components.*
import com.example.studyspace.ui.theme.*
import com.example.studyspace.viewmodel.*

// ====================================================
// FocusScreen - Layar Timer Belajar
// ====================================================

@Composable
fun FocusScreen(viewModel: FocusViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Dialog ringkasan sesi
    if (uiState.showSessionSummary && uiState.completedSession != null) {
        SessionSummaryDialog(
            session = uiState.completedSession!!,
            onDismiss = viewModel::dismissSessionSummary
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Focus Timer",
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryText,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode Selector (Focus / Short Break / Long Break)
        StudyModeSelector(
            selectedMode = uiState.selectedMode,
            onModeSelected = viewModel::setMode,
            enabled = uiState.timerState == TimerState.IDLE
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Circular Timer
        CircularTimer(
            progress = uiState.progress,
            timeText = uiState.formattedRemainingTime,
            timerState = uiState.timerState,
            mode = uiState.selectedMode
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Duration Selector (hanya tampil saat IDLE)
        AnimatedVisibility(
            visible = uiState.timerState == TimerState.IDLE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            DurationSelector(
                selectedMinutes = uiState.selectedDurationMinutes,
                onDurationSelected = viewModel::setDuration,
                onCustomDuration = viewModel::setCustomDuration
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input mata pelajaran
        AnimatedVisibility(
            visible = uiState.timerState == TimerState.IDLE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.subject,
                    onValueChange = viewModel::setSubject,
                    label = { Text("Mata Pelajaran", color = SecondaryText) },
                    leadingIcon = {
                        Icon(Icons.Filled.School, null, tint = SecondaryText)
                    },
                    isError = uiState.error != null,
                    supportingText = uiState.error?.let { { Text(it, color = ErrorRed) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::setNotes,
                    label = { Text("Catatan (opsional)", color = SecondaryText) },
                    leadingIcon = {
                        Icon(Icons.Filled.Notes, null, tint = SecondaryText)
                    },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = PrimaryBlue,
                        cursorColor = PrimaryBlue,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Info saat timer berjalan
        AnimatedVisibility(
            visible = uiState.timerState != TimerState.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            StudyCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mata Pelajaran", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(uiState.subject, style = MaterialTheme.typography.titleSmall, color = PrimaryText)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Mode", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        Text(uiState.selectedMode.displayName, style = MaterialTheme.typography.titleSmall, color = PrimaryBlue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Control Buttons
        TimerControls(
            timerState = uiState.timerState,
            onStart = viewModel::startTimer,
            onPause = viewModel::pauseTimer,
            onResume = viewModel::resumeTimer,
            onReset = viewModel::resetTimer
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun StudyModeSelector(
    selectedMode: StudyMode,
    onModeSelected: (StudyMode) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudyMode.entries.forEach { mode ->
            val isSelected = selectedMode == mode
            FilterChip(
                selected = isSelected,
                onClick = { if (enabled) onModeSelected(mode) },
                label = {
                    Text(
                        text = mode.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                    selectedLabelColor = PrimaryBlue,
                    containerColor = SecondarySurface,
                    labelColor = SecondaryText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    selectedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                    borderColor = CardBorder
                )
            )
        }
    }
}

@Composable
private fun DurationSelector(
    selectedMinutes: Int,
    onDurationSelected: (Int) -> Unit,
    onCustomDuration: (Int) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val presetDurations = listOf(25, 30, 45, 60)

    if (showCustomDialog) {
        CustomDurationDialog(
            currentMinutes = selectedMinutes,
            onConfirm = { minutes ->
                onCustomDuration(minutes)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Durasi",
            style = MaterialTheme.typography.labelMedium,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetDurations.forEach { minutes ->
                val isSelected = selectedMinutes == minutes
                FilterChip(
                    selected = isSelected,
                    onClick = { onDurationSelected(minutes) },
                    label = {
                        Text(
                            "${minutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPurple.copy(alpha = 0.2f),
                        selectedLabelColor = AccentPurple,
                        containerColor = SecondarySurface,
                        labelColor = SecondaryText
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        selectedBorderColor = AccentPurple.copy(alpha = 0.5f),
                        borderColor = CardBorder
                    )
                )
            }
            // Tombol custom duration
            FilterChip(
                selected = selectedMinutes !in presetDurations,
                onClick = { showCustomDialog = true },
                label = {
                    Text(
                        text = if (selectedMinutes !in presetDurations) "${selectedMinutes}m" else "Kustom",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WarningYellow.copy(alpha = 0.2f),
                    selectedLabelColor = WarningYellow,
                    containerColor = SecondarySurface,
                    labelColor = SecondaryText
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedMinutes !in presetDurations,
                    selectedBorderColor = WarningYellow.copy(alpha = 0.5f),
                    borderColor = CardBorder
                )
            )
        }
    }
}

@Composable
private fun TimerControls(
    timerState: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset button (tampil saat bukan IDLE)
        AnimatedVisibility(
            visible = timerState != TimerState.IDLE,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            OutlinedButton(
                onClick = onReset,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SecondaryText
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.height(52.dp)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // Main action button
        Button(
            onClick = {
                when (timerState) {
                    TimerState.IDLE -> onStart()
                    TimerState.RUNNING -> onPause()
                    TimerState.PAUSED -> onResume()
                    TimerState.FINISHED -> onReset()
                }
            },
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (timerState) {
                    TimerState.RUNNING -> WarningYellow
                    TimerState.PAUSED -> SuccessGreen
                    TimerState.FINISHED -> PrimaryBlue
                    else -> PrimaryBlue
                },
                contentColor = when (timerState) {
                    TimerState.RUNNING -> Background
                    else -> PrimaryText
                }
            )
        ) {
            Icon(
                imageVector = when (timerState) {
                    TimerState.IDLE -> Icons.Filled.PlayArrow
                    TimerState.RUNNING -> Icons.Filled.Pause
                    TimerState.PAUSED -> Icons.Filled.PlayArrow
                    TimerState.FINISHED -> Icons.Filled.Refresh
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (timerState) {
                    TimerState.IDLE -> "Mulai"
                    TimerState.RUNNING -> "Jeda"
                    TimerState.PAUSED -> "Lanjutkan"
                    TimerState.FINISHED -> "Reset"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomDurationDialog(
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf(currentMinutes.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        title = {
            Text("Durasi Kustom", color = PrimaryText, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column {
                Text(
                    text = "Masukkan durasi dalam menit (1-180)",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        isError = false
                    },
                    isError = isError,
                    suffix = { Text("menit", color = SecondaryText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (isError) {
                    Text(
                        text = "Masukkan angka antara 1-180",
                        color = ErrorRed,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minutes = inputText.toIntOrNull()
                    if (minutes != null && minutes in 1..180) {
                        onConfirm(minutes)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Konfirmasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SecondaryText)
            }
        }
    )
}

@Composable
private fun SessionSummaryDialog(
    session: com.example.studyspace.data.database.StudySession,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(SuccessGreen.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sesi Selesai!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = session.subject,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        label = "Durasi",
                        value = formatStudyTime(session.duration)
                    )
                    SummaryItem(
                        label = "Cahaya Rata-rata",
                        value = "${session.averageLight.toInt()} lux"
                    )
                    SummaryItem(
                        label = "Gerakan",
                        value = "${session.movementCount}x"
                    )
                }

                if (session.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Catatan:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Tutup", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryText
        )
    }
}
