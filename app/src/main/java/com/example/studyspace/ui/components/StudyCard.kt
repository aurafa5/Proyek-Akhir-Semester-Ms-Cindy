package com.example.studyspace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studyspace.ui.theme.*

// ====================================================
// StudyCard - Reusable Card Component
// Komponen kartu dasar untuk seluruh aplikasi
// ====================================================

/**
 * Kartu utama dengan styling dark premium
 */
@Composable
fun StudyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = CardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Kartu statistik dengan nilai besar dan label
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String = "",
    valueColor: Color = PrimaryText,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    StudyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SecondaryText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = valueColor,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
            icon?.invoke()
        }
    }
}

/**
 * Divider untuk memisahkan konten
 */
@Composable
fun StudyDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = CardBorder,
        thickness = 1.dp
    )
}

/**
 * Section header dengan label
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryText,
            fontWeight = FontWeight.SemiBold
        )
        action?.invoke()
    }
}
