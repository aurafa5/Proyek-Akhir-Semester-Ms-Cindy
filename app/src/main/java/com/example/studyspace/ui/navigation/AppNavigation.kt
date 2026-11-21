package com.example.studyspace.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

// ====================================================
// AppNavigation - Navigasi dan Route Definitions
// ====================================================

/** Daftar route untuk navigasi */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Focus : Screen("focus")
    object Environment : Screen("environment")
    object History : Screen("history")
    object Statistics : Screen("statistics")
}

/** Item menu bottom navigation */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

/** Daftar item bottom navigation */
val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        label = "Home",
        icon = Icons.Filled.Home,
    ),
    BottomNavItem(
        screen = Screen.Focus,
        label = "Focus",
        icon = Icons.Filled.Timer,
    ),
    BottomNavItem(
        screen = Screen.Environment,
        label = "Environment",
        icon = Icons.Filled.Sensors,
    ),
    BottomNavItem(
        screen = Screen.History,
        label = "History",
        icon = Icons.Filled.History,
    ),
    BottomNavItem(
        screen = Screen.Statistics,
        label = "Statistics",
        icon = Icons.Filled.BarChart,
    )
)
