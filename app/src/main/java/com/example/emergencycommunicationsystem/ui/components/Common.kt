package com.example.emergencycommunicationsystem.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Data class to represent a bottom navigation item
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

// The Footer composable, which is a bottom navigation bar
@Composable
fun Footer(navController: NavController) {
    val items = listOf(
        // Use the route names you defined in your Screen sealed class
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Hotlines", Icons.Default.Call, "emergency_contacts"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination to avoid building up a large back stack
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}