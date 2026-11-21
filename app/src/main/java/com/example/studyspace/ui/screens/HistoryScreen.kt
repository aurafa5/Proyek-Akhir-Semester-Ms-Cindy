package com.example.studyspace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studyspace.data.database.StudySession
import com.example.studyspace.ui.components.*
import com.example.studyspace.ui.theme.*
import com.example.studyspace.viewmodel.HistoryViewModel

// ====================================================
// HistoryScreen - Layar Riwayat Belajar
// ====================================================

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog konfirmasi hapus
    uiState.sessionToDelete?.let { session ->
        DeleteConfirmationDialog(
            session = session,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }

    // Dialog detail sesi
    uiState.selectedSession?.let { session ->
        SessionDetailDialog(
            session = session,
            onDismiss = viewModel::hideSessionDetail,
            onSaveNotes = { newNotes ->
                viewModel.updateSessionNotes(session, newNotes)
            },
            onDelete = {
                viewModel.hideSessionDetail()
                viewModel.showDeleteConfirmation(session)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Riwayat Belajar",
                style = MaterialTheme.typography.headlineSmall,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chip berdasarkan mata pelajaran
            if (uiState.subjects.isNotEmpty()) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Filter chips (scrollable horizontal)
        if (uiState.subjects.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Semua" chip
                item {
                    FilterChip(
                        selected = uiState.selectedSubject == null,
                        onClick = { viewModel.filterBySubject(null) },
                        label = { Text("Semua", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryBlue,
                            containerColor = SecondarySurface,
                            labelColor = SecondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = uiState.selectedSubject == null,
                            selectedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                            borderColor = CardBorder
                        )
                    )
                }
                items(uiState.subjects) { subject ->
                    val isSelected = uiState.selectedSubject == subject
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterBySubject(subject) },
                        label = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                            selectedLabelColor = PrimaryBlue,
                            containerColor = SecondarySurface,
                            labelColor = SecondaryText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = PrimaryBlue.copy(alpha = 0.5f),
                            borderColor = CardBorder
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Daftar sesi
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.sessions.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.HistoryEdu,
                        contentDescription = null,
                        tint = TertiaryText,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (uiState.selectedSubject != null)
                            "Tidak ada sesi untuk mata pelajaran ini"
                        else
                            "Belum ada riwayat belajar",
                        style = MaterialTheme.typography.titleSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = "Mulai sesi belajar di menu Focus",
                        style = MaterialTheme.typography.bodySmall,
                        color = TertiaryText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.sessions,
                    key = { session -> session.id }
                ) { session ->
                    HistorySessionCard(
                        session = session,
                        onClick = { viewModel.showSessionDetail(session) },
                        onDelete = { viewModel.showDeleteConfirmation(session) }
                    )
                }
                // Space for bottom nav
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: StudySession,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    StudyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject initial
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AccentPurple.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.subject.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentPurple,
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
                if (session.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TertiaryText,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatStudyTime(session.duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Mode badge
                Text(
                    text = when (session.mode) {
                        "focus" -> "Focus"
                        "short_break" -> "Break"
                        "long_break" -> "Long Break"
                        else -> "Focus"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = ErrorRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    session: StudySession,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "Hapus Sesi?",
                color = PrimaryText,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                "Sesi belajar \"${session.subject}\" akan dihapus secara permanen. Tindakan ini tidak dapat dibatalkan.",
                color = SecondaryText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Hapus")
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
private fun SessionDetailDialog(
    session: StudySession,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onDelete: () -> Unit
) {
    var editableNotes by remember(session.id) { mutableStateOf(session.notes) }
    var isEditingNotes by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.subject,
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, null, tint = SecondaryText)
                    }
                }

                Text(
                    text = formatSessionDate(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryText
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Detail stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailItem("Durasi", formatStudyTime(session.duration))
                    DetailItem("Mode", session.mode.replace("_", " ").replaceFirstChar { it.uppercase() })
                    DetailItem("Gerakan", "${session.movementCount}x")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sensor data
                if (session.averageLight > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailItem("Cahaya Avg", "${session.averageLight.toInt()} lux")
                        DetailItem("Cahaya Min", "${session.minLight.toInt()} lux")
                        DetailItem("Cahaya Max", "${session.maxLight.toInt()} lux")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Catatan section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Catatan",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryText
                    )
                    if (!isEditingNotes) {
                        TextButton(
                            onClick = { isEditingNotes = true }
                        ) {
                            Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (isEditingNotes) {
                    OutlinedTextField(
                        value = editableNotes,
                        onValueChange = { editableNotes = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = PrimaryText,
                            unfocusedTextColor = PrimaryText,
                            cursorColor = PrimaryBlue
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { isEditingNotes = false; editableNotes = session.notes }) {
                            Text("Batal", color = SecondaryText)
                        }
                        Button(
                            onClick = {
                                onSaveNotes(editableNotes)
                                isEditingNotes = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Simpan")
                        }
                    }
                } else {
                    Text(
                        text = if (editableNotes.isEmpty()) "Tidak ada catatan" else editableNotes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (editableNotes.isEmpty()) TertiaryText else PrimaryText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus Sesi")
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = PrimaryText,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryText
        )
    }
}
