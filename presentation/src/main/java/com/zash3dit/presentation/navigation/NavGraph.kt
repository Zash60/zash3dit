package com.zash3dit.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zash3dit.presentation.ui.EditorScreen
import com.zash3dit.presentation.ui.HomeScreen
import com.zash3dit.presentation.ui.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Editor : Screen("editor") {
        fun createRoute(projectId: Long) = "editor/$projectId"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(onProjectSelected = { projectId ->
                navController.navigate(Screen.Editor.createRoute(projectId))
            })
        }
        composable(Screen.Editor.route) {
            EditorScreen(
                projectId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Editor.createRoute(0).replace("0", "{projectId}"),
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            EditorScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}