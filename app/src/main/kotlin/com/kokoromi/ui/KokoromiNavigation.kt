package com.kokoromi.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Home : Screen("home")
    // Screens are registered here as each milestone is built:
    // object CreateExperiment : Screen("create_experiment")
    // object CheckIn : Screen("check_in/{experimentId}")
    // object Reflection : Screen("reflection")
    // object Archive : Screen("archive")
    // object Settings : Screen("settings")
}

@Composable
fun KokoromiNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            // HomeScreen will be added in Milestone 3
        }
    }
}
