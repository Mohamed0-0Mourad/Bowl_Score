package com.example.bowl_score.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SetupScreen(navController: NavHostController, mode: Int) {
    // Mode will be 1 or 2 based on player count
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Setup Arena", style = MaterialTheme.typography.displayLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Player details and Video Picker will go here...", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { /* Launch Picker, then navigate to Arena */ }) {
            Text("Select Video(s) & Enter Arena")
        }
    }
}