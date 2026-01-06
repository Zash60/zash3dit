package com.zash3dit.presentation.ui

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.zash3dit.data.local.SettingsDataStore
import com.zash3dit.presentation.navigation.Screen
import com.zash3dit.domain.model.ThemeMode
import com.zash3dit.presentation.ui.theme.Zash3ditTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val themeMode by dataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    Zash3ditTheme(themeMode = themeMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        Scaffold(
            bottomBar = {
                NavigationBar {
                    val items = listOf(Screen.Home, Screen.Editor, Screen.Settings)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                when (screen) {
                                    Screen.Home -> Icon(Icons.Default.Home, contentDescription = null)
                                    Screen.Editor -> Icon(Icons.Default.Edit, contentDescription = null)
                                    Screen.Settings -> Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            },
                            label = {
                                Text(
                                    when (screen) {
                                        Screen.Home -> "Home"
                                        Screen.Editor -> "Editor"
                                        Screen.Settings -> "Settings"
                                    }
                                )
                            },
                            selected = currentDestination?.hierarchy?.any {
                                when (screen) {
                                    Screen.Home -> it.route == Screen.Home.route
                                    Screen.Editor -> it.route?.startsWith(Screen.Editor.route) == true
                                    Screen.Settings -> it.route == Screen.Settings.route
                                    else -> false
                                }
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
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
                    route = "editor/{projectId}",
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
    }
}