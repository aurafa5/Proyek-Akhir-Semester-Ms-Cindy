package com.example.studyspace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.studyspace.ui.navigation.Screen
import com.example.studyspace.ui.navigation.bottomNavItems
import com.example.studyspace.ui.theme.*

// ====================================================
// BottomNavigation - Komponen Bottom Navigation Bar
// ====================================================

/**
 * Bottom navigation bar dengan 5 menu utama
 */
@Composable
fun StudyBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Surface,
        contentColor = SecondaryText,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    // Navigasi hanya jika bukan halaman yang sama
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            // Pop back stack ke home untuk mencegah penumpukan
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = SecondaryText,
                    unselectedTextColor = TertiaryText,
                    indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                )
            )
        }
    }
}
