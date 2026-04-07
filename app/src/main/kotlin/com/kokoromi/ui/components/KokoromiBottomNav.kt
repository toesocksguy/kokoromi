package com.kokoromi.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home),
    BottomNavItem("Notes", Icons.Filled.Note),
    BottomNavItem("Archive", Icons.Filled.Archive),
    BottomNavItem("Settings", Icons.Filled.Settings),
)

@Composable
fun KokoromiBottomNav(
    selectedIndex: Int,
    onNavigate: (Int) -> Unit,
) {
    NavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { if (selectedIndex != index) onNavigate(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
