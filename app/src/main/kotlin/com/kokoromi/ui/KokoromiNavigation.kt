package com.kokoromi.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kokoromi.ui.checkin.CheckInScreen
import com.kokoromi.ui.create.CreateExperimentScreen
import com.kokoromi.ui.home.HomeScreen
import java.time.LocalDate

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CreateExperiment : Screen("create_experiment")
    object CheckIn : Screen("check_in/{experimentId}/{initialCompleted}?date={date}") {
        fun route(experimentId: String, initialCompleted: Boolean, date: LocalDate? = null): String {
            val base = "check_in/$experimentId/$initialCompleted"
            return if (date != null) "$base?date=$date" else base
        }
    }
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
                onCheckIn = { experimentId, initialCompleted ->
                    navController.navigate(Screen.CheckIn.route(experimentId, initialCompleted))
                },
            )
        }
        composable(Screen.CreateExperiment.route) {
            CreateExperimentScreen(
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.CheckIn.route,
            arguments = listOf(
                navArgument("experimentId") { type = NavType.StringType },
                navArgument("initialCompleted") { type = NavType.BoolType },
                navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) {
            CheckInScreen(onBack = { navController.popBackStack() })
        }
    }
}
