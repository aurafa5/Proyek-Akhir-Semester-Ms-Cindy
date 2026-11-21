package com.example.studyspace.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyspace.data.database.DailyStats
import com.example.studyspace.ui.theme.*
import kotlin.math.max

// ====================================================
// StatisticsChart - Grafik statistik belajar
// Menggunakan Canvas untuk rendering chart kustom
// ====================================================

/**
 * Grafik batang untuk durasi belajar harian
 * @param dailyStats Data statistik per hari
 * @param maxDuration Nilai maksimum untuk skala (dalam detik)
 */
@Composable
fun WeeklyBarChart(
    dailyStats: List<DailyStats>,
    modifier: Modifier = Modifier
) {
    if (dailyStats.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val maxDuration = dailyStats.maxOf { it.totalDuration }.toFloat().coerceAtLeast(1f)

    // Mapping nama hari
    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")

    Column(modifier = modifier) {
        // Label Y axis
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = formatDuration(maxDuration.toInt()),
                style = MaterialTheme.typography.labelSmall,
                color = TertiaryText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = dailyStats.size.coerceAtLeast(1)
                val totalWidth = size.width
                val totalHeight = size.height
                val barSpacing = 8.dp.toPx()
                val barWidth = (totalWidth - barSpacing * (barCount + 1)) / barCount

                // Garis horizontal referensi
                for (i in 0..3) {
                    val y = totalHeight * (1f - i / 4f)
                    drawLine(
                        color = CardBorder,
                        start = Offset(0f, y),
                        end = Offset(totalWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Gambar batang
                dailyStats.forEachIndexed { index, stat ->
                    val barHeight = (stat.totalDuration / maxDuration) * totalHeight
                    val x = barSpacing + index * (barWidth + barSpacing)
                    val y = totalHeight - barHeight

                    // Warna batang dengan gradient
                    val barColor = when {
                        index == dailyStats.size - 1 -> PrimaryBlue // Hari ini highlight
                        barHeight > totalHeight * 0.7f -> AccentPurple
                        else -> PrimaryBlue.copy(alpha = 0.6f)
                    }

                    if (barHeight > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.4f)),
                                startY = y,
                                endY = totalHeight
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    } else {
                        // Batang minimal untuk hari tanpa data
                        drawRoundRect(
                            color = CardBorder,
                            topLeft = Offset(x, totalHeight - 4.dp.toPx()),
                            size = Size(barWidth, 4.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label hari di bawah chart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dailyStats.forEach { stat ->
                // Ambil nama hari dari tanggal (format YYYY-MM-DD)
                val dayName = getDayName(stat.date)
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Grafik garis untuk data sensor cahaya
 * @param values List nilai intensitas cahaya dalam urutan waktu
 */
@Composable
fun LightIntensityChart(
    values: List<Float>,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) {
        Box(
            modifier = modifier.height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Kumpulkan lebih banyak data...",
                style = MaterialTheme.typography.labelSmall,
                color = TertiaryText
            )
        }
        return
    }

    val maxValue = values.max().coerceAtLeast(1f)
    val minValue = values.min()

    Canvas(modifier = modifier.height(80.dp).fillMaxWidth()) {
        val totalWidth = size.width
        val totalHeight = size.height
        val pointSpacing = totalWidth / (values.size - 1).coerceAtLeast(1)

        // Hitung posisi setiap titik
        val points = values.mapIndexed { index, value ->
            val x = index * pointSpacing
            val normalizedValue = if (maxValue > minValue) {
                (value - minValue) / (maxValue - minValue)
            } else 0.5f
            val y = totalHeight * (1f - normalizedValue)
            Offset(x, y)
        }

        // Gambar garis
        for (i in 0 until points.size - 1) {
            drawLine(
                color = WarningYellow,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx()
            )
        }

        // Titik terakhir
        if (points.isNotEmpty()) {
            drawCircle(
                color = WarningYellow,
                radius = 4.dp.toPx(),
                center = points.last()
            )
        }
    }
}

/**
 * Placeholder ketika belum ada data chart
 */
@Composable
private fun EmptyChartPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(140.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Belum ada data untuk ditampilkan",
            style = MaterialTheme.typography.bodySmall,
            color = TertiaryText
        )
    }
}

/** Format durasi (detik) ke format HH:mm */
fun formatDuration(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) {
        "${hours}j ${minutes}m"
    } else {
        "${minutes}m"
    }
}

/** Dapatkan nama hari singkat dari tanggal format YYYY-MM-DD */
private fun getDayName(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val calendar = java.util.Calendar.getInstance()
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                java.util.Calendar.MONDAY -> "Sen"
                java.util.Calendar.TUESDAY -> "Sel"
                java.util.Calendar.WEDNESDAY -> "Rab"
                java.util.Calendar.THURSDAY -> "Kam"
                java.util.Calendar.FRIDAY -> "Jum"
                java.util.Calendar.SATURDAY -> "Sab"
                java.util.Calendar.SUNDAY -> "Min"
                else -> dateStr.takeLast(2)
            }
        } else dateStr.takeLast(2)
    } catch (e: Exception) {
        dateStr.takeLast(2)
    }
}
