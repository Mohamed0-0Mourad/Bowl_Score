package com.example.bowl_score.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlayerMode : Screen("player_mode")
    object Setup : Screen("setup/{mode}") {
        fun createRoute(mode: Int) = "setup/$mode"
    }
    object Arena : Screen("arena")
    object Celebration : Screen("celebration/{winner}") {
        fun createRoute(winner: String) = "celebration/$winner"
    }
}

@Composable
fun BowlingVisionApp() {
    val navController = rememberNavController()
    val viewModel: BowlingViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            com.example.bowl_score.ui.screens.HomeScreen(navController)
        }
        composable(Screen.PlayerMode.route) {
            // We will build this one later, keep it empty for now!
        }
        composable(Screen.Setup.route + "/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")?.toInt() ?: 1
            com.example.bowl_score.ui.screens.SetupScreen(navController, mode)
        }
        composable(Screen.Arena.route) {
            // The massive SurfaceView logic will go here
        }
        composable(Screen.Celebration.route + "/{winner}") { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner") ?: "Unknown"
            com.example.bowl_score.ui.screens.CelebrationScreen(navController, winner)
        }
    }
}