package com.example.bowl_score.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bowl_score.ui.BowlingViewModel

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
            com.example.bowl_score.ui.screens.PlayerModeScreen(navController)
        }
        composable(Screen.Setup.route) { backStackEntry ->
            val modeString = backStackEntry.arguments?.getString("mode") ?: "1"
            val mode = modeString.toIntOrNull() ?: 1
            com.example.bowl_score.ui.screens.SetupScreen(navController, mode, viewModel)
        }
        composable(Screen.Arena.route) {
            com.example.bowl_score.ui.screens.ArenaScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable(Screen.Celebration.route) { backStackEntry ->
            val winner = backStackEntry.arguments?.getString("winner") ?: "Unknown"
            com.example.bowl_score.ui.screens.CelebrationScreen(navController, winner)
        }
    }
}