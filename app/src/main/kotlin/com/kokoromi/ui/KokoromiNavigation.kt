package com.kokoromi.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kokoromi.ui.archive.ArchiveScreen
import com.kokoromi.ui.checkin.CheckInScreen
import com.kokoromi.ui.completion.CompletionScreen
import com.kokoromi.ui.create.CreateExperimentScreen
import com.kokoromi.ui.detail.ExperimentDetailScreen
import com.kokoromi.ui.home.HomeScreen
import com.kokoromi.ui.notes.AddEditNoteScreen
import com.kokoromi.ui.notes.FieldNotesScreen
import com.kokoromi.ui.reflection.ReflectionScreen
import com.kokoromi.ui.settings.SettingsScreen
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
    object Completion : Screen("completion/{experimentId}") {
        fun route(experimentId: String) = "completion/$experimentId"
    }
    object Reflection : Screen("reflection/{experimentId}") {
        fun route(experimentId: String) = "reflection/$experimentId"
    }
    object ExperimentDetail : Screen("experiment_detail/{experimentId}") {
        fun route(experimentId: String) = "experiment_detail/$experimentId"
    }
    object FieldNotes : Screen("field_notes")
    object AddEditNote : Screen("add_edit_note?noteId={noteId}") {
        fun route(noteId: String? = null) =
            if (noteId != null) "add_edit_note?noteId=$noteId" else "add_edit_note"
    }
    object Archive : Screen("archive")
    object Settings : Screen("settings")
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
                onNavigateToCompletion = { experimentId ->
                    navController.navigate(Screen.Completion.route(experimentId))
                },
                onNavigateToReflection = { experimentId ->
                    navController.navigate(Screen.Reflection.route(experimentId))
                },
                onNavigateToDetail = { experimentId ->
                    navController.navigate(Screen.ExperimentDetail.route(experimentId))
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
        ) { backStackEntry ->
            val experimentId = backStackEntry.arguments?.getString("experimentId") ?: ""
            CheckInScreen(
                onBack = { navController.popBackStack() },
                onReflect = { navController.navigate(Screen.Reflection.route(experimentId)) },
            )
        }
        composable(
            route = Screen.Completion.route,
            arguments = listOf(
                navArgument("experimentId") { type = NavType.StringType },
            ),
        ) {
            CompletionScreen(
                onNavigateToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onNavigateToCreate = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                    navController.navigate(Screen.CreateExperiment.route)
                },
            )
        }
        composable(
            route = Screen.Reflection.route,
            arguments = listOf(
                navArgument("experimentId") { type = NavType.StringType },
            ),
        ) {
            ReflectionScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.ExperimentDetail.route,
            arguments = listOf(
                navArgument("experimentId") { type = NavType.StringType },
            ),
        ) {
            ExperimentDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateToDetail = { experimentId ->
                    navController.navigate(Screen.ExperimentDetail.route(experimentId))
                },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.FieldNotes.route) {
            FieldNotesScreen(
                onCreateNote = { navController.navigate(Screen.AddEditNote.route()) },
                onOpenNote = { noteId -> navController.navigate(Screen.AddEditNote.route(noteId)) },
            )
        }
        composable(
            route = Screen.AddEditNote.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType; nullable = true; defaultValue = null },
            ),
        ) {
            AddEditNoteScreen(onBack = { navController.popBackStack() })
        }
    }
}
