package com.example.bowl_score.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bowl_score.ui.theme.Screen
import com.example.bowl_score.ui.theme.DeepViolet
import com.example.bowl_score.ui.theme.ElectricPurple

@Composable
fun FunkyButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(60.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "BowlingVision",
                style = MaterialTheme.typography.displayLarge,
                color = ElectricPurple
            )
            Spacer(modifier = Modifier.height(48.dp))

            FunkyButton("Live Camera") {
                Toast.makeText(context, "Coming Soon in V2!", Toast.LENGTH_SHORT).show()
            }
            Spacer(modifier = Modifier.height(16.dp))

            FunkyButton("Load from Gallery") {
                navController.navigate(Screen.PlayerMode.route)
            }
        }
    }
}