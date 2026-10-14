package com.example.projekt.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.projekt.R

data class BottomNavItem(
    val screen   : Screen,
    val icon     : ImageVector,
    val labelRes : Int
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,     Icons.Default.Home,     R.string.nav_portfolio),
    BottomNavItem(Screen.History,  Icons.Default.List,     R.string.nav_history),
    BottomNavItem(Screen.Settings, Icons.Default.Settings, R.string.nav_settings)
)

private val BgDark     = Color(0xFF161B22)
private val TealAccent = Color(0xFF1AAB8B)
private val TextMuted  = Color(0xFF8B949E)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute      = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = BgDark,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon  = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(stringResource(item.labelRes)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = TealAccent,
                    selectedTextColor   = TealAccent,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor      = TealAccent.copy(alpha = 0.12f)
                )
            )
        }
    }
}