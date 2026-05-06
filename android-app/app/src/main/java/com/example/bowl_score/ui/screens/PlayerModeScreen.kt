package com.example.bowl_score.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bowl_score.ui.theme.Screen
import com.example.bowl_score.ui.theme.DeepViolet
import com.example.bowl_score.ui.theme.ElectricPurple
import com.example.bowl_score.ui.theme.NeonPink

@Composable
fun PlayerModeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "CHOOSE MODE",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(64.dp))

            ModeButton(
                text = "1 PLAYER",
                color = ElectricPurple,
                onClick = { navController.navigate(Screen.Setup.createRoute(1)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ModeButton(
                text = "2 PLAYERS",
                color = NeonPink,
                onClick = { navController.navigate(Screen.Setup.createRoute(2)) }
            )
        }
    }
}

@Composable
fun ModeButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}
