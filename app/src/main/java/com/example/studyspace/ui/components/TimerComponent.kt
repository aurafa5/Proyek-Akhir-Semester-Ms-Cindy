package com.example.studyspace.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyspace.ui.theme.*
import com.example.studyspace.viewmodel.StudyMode
import com.example.studyspace.viewmodel.TimerState

// ====================================================
// TimerComponent - Circular Timer UI
// Menampilkan progress timer dalam bentuk lingkaran
// ====================================================

/**
 * Circular timer dengan animasi progress
 * @param progress Nilai 0.0 - 1.0 (progress timer)
 * @param timeText Teks waktu yang ditampilkan di tengah
 * @param timerState Status timer saat ini
 * @param mode Mode belajar aktif
 * @param size Ukuran lingkaran timer
 */
@Composable
fun CircularTimer(
    progress: Float,
    timeText: String,
    timerState: TimerState,
    mode: StudyMode,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp
) {
    // Warna berdasarkan mode
    val timerColor = when (mode) {
        StudyMode.FOCUS -> PrimaryBlue
        StudyMode.SHORT_BREAK -> SuccessGreen
        StudyMode.LONG_BREAK -> AccentPurple
    }

    // Animasi progress yang halus
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = LinearOutSlowInEasing
        ),
        label = "timer_progress"
    )

    // Animasi pulse ketika timer berjalan
    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Canvas untuk menggambar lingkaran timer
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Lingkaran background (track)
            drawArc(
                color = SecondarySurface,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Lingkaran progress dengan gradient
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to timerColor.copy(alpha = 0.5f),
                        1f to timerColor
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Efek glow ketika timer berjalan
            if (timerState == TimerState.RUNNING) {
                drawArc(
                    color = timerColor.copy(alpha = pulseAlpha * 0.2f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2 - 4.dp.toPx(), strokeWidth / 2 - 4.dp.toPx()),
                    size = Size(diameter + 8.dp.toPx(), diameter + 8.dp.toPx()),
                    style = Stroke(
                        width = strokeWidth + 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Konten di tengah lingkaran
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Teks waktu
            Text(
                text = timeText,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Label status
            Text(
                text = when (timerState) {
                    TimerState.IDLE -> mode.displayName
                    TimerState.RUNNING -> "Berjalan..."
                    TimerState.PAUSED -> "Dijeda"
                    TimerState.FINISHED -> "Selesai!"
                },
                style = MaterialTheme.typography.labelLarge,
                color = when (timerState) {
                    TimerState.RUNNING -> timerColor
                    TimerState.PAUSED -> WarningYellow
                    TimerState.FINISHED -> SuccessGreen
                    else -> SecondaryText
                }
            )
        }
    }
}
