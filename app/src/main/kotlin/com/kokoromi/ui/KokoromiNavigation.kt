package com.kokoromi.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kokoromi.ui.create.CreateExperimentScreen
import com.kokoromi.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    // Screens are registered here as each milestone is built:
    object CreateExperiment : Screen("create_experiment")
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
            HomeScreen(
                onCreateExperiment = { navController.navigate(Screen.CreateExperiment.route) },
                onCheckIn = { /* wired in Milestone 4 */ },
            )
        }
        composable(Screen.CreateExperiment.route) {
            CreateExperimentScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
    }
}
