package com.example.studyspace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studyspace.sensor.LightCategory
import com.example.studyspace.sensor.LightData
import com.example.studyspace.sensor.AccelerometerData
import com.example.studyspace.ui.theme.*

// ====================================================
// SensorCard - Komponen kartu tampilan data sensor
// ====================================================

/**
 * Kartu data Light Sensor
 * Menampilkan nilai lux, kategori, dan rekomendasi
 */
@Composable
fun LightSensorCard(
    lightData: LightData,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    StudyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sensor Cahaya",
                style = MaterialTheme.typography.titleSmall,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Filled.LightMode,
                contentDescription = "Light Sensor",
                tint = WarningYellow,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAvailable) {
            SensorUnavailableMessage(sensorName = "Light Sensor")
        } else {
            // Nilai lux besar di tengah
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.1f", lightData.lux),
                    style = MaterialTheme.typography.headlineLarge,
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "lux",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SecondaryText,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badge kategori
            LightCategoryBadge(category = lightData.category)

            Spacer(modifier = Modifier.height(12.dp))

            // Rekomendasi
            if (lightData.recommendation.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(14.dp).padding(top = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lightData.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

/**
 * Badge untuk kategori pencahayaan
 */
@Composable
fun LightCategoryBadge(category: LightCategory) {
    val (badgeColor, textColor) = when (category) {
        LightCategory.VERY_DARK -> Pair(ErrorRed.copy(alpha = 0.2f), ErrorRed)
        LightCategory.DARK -> Pair(WarningYellow.copy(alpha = 0.2f), WarningYellow)
        LightCategory.DIM -> Pair(Color(0xFFFB923C).copy(alpha = 0.2f), Color(0xFFFB923C))
        LightCategory.MODERATE -> Pair(SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
        LightCategory.BRIGHT -> Pair(PrimaryBlue.copy(alpha = 0.2f), PrimaryBlue)
        LightCategory.VERY_BRIGHT -> Pair(AccentPurple.copy(alpha = 0.2f), AccentPurple)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = badgeColor
    ) {
        Text(
            text = category.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Kartu data Accelerometer
 * Menampilkan nilai X, Y, Z dan jumlah gerakan
 */
@Composable
fun AccelerometerCard(
    data: AccelerometerData,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    StudyCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sensor Gerak (Accelerometer)",
                style = MaterialTheme.typography.titleSmall,
                color = SecondaryText,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Filled.Vibration,
                contentDescription = "Accelerometer",
                tint = AccentPurple,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAvailable) {
            SensorUnavailableMessage(sensorName = "Accelerometer")
        } else {
            // Nilai X, Y, Z
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AxisValue(label = "X", value = data.x, modifier = Modifier.weight(1f))
                AxisValue(label = "Y", value = data.y, modifier = Modifier.weight(1f))
                AxisValue(label = "Z", value = data.z, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            StudyDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Jumlah gerakan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gerakan Terdeteksi",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText
                    )
                    Text(
                        text = "${data.movementCount}x",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (data.movementCount > 50) WarningYellow else PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Indikator aktif/diam
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (data.isMoving) {
                        WarningYellow.copy(alpha = 0.2f)
                    } else {
                        SuccessGreen.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (data.isMoving) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (data.isMoving) WarningYellow else SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (data.isMoving) "Bergerak" else "Stabil",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (data.isMoving) WarningYellow else SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Catatan tentang sensor
            Text(
                text = "* Data ini menunjukkan aktivitas gerakan perangkat, bukan ukuran fokus manusia.",
                style = MaterialTheme.typography.labelSmall,
                color = TertiaryText
            )
        }
    }
}

/**
 * Tampilan nilai sumbu accelerometer (X, Y, atau Z)
 */
@Composable
private fun AxisValue(
    label: String,
    value: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SecondaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Pesan ketika sensor tidak tersedia di perangkat
 */
@Composable
fun SensorUnavailableMessage(sensorName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.SensorsOff,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$sensorName tidak tersedia di perangkat ini",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText
        )
    }
}
