package com.example.bowl_score.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.bowl_score.ui.BowlingViewModel
import com.example.bowl_score.ui.theme.Screen
import com.example.bowl_score.ui.theme.DeepViolet
import com.example.bowl_score.ui.theme.ElectricPurple
import com.example.bowl_score.ui.theme.NeonPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(navController: NavHostController, mode: Int, viewModel: BowlingViewModel) {
    val scrollState = rememberScrollState()

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> viewModel.player1VideoUri = uri }
    )

    val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> 
            if (viewModel.player1VideoUri == null) {
                viewModel.player1VideoUri = uri
            } else {
                viewModel.player2VideoUri = uri
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Setup Arena",
                style = MaterialTheme.typography.displayMedium,
                color = ElectricPurple
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Player 1 Section
            PlayerSetupSection(
                title = "Player 1",
                name = viewModel.player1Name,
                onNameChange = { viewModel.player1Name = it },
                selectedColor = viewModel.player1AccentColor,
                onColorSelected = { viewModel.player1AccentColor = it }
            )

            if (mode == 2) {
                Spacer(modifier = Modifier.height(24.dp))
                // Player 2 Section
                PlayerSetupSection(
                    title = "Player 2",
                    name = viewModel.player2Name,
                    onNameChange = { viewModel.player2Name = it },
                    selectedColor = viewModel.player2AccentColor,
                    onColorSelected = { viewModel.player2AccentColor = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = if (viewModel.player1VideoUri == null) "Select Video (Player 1)" else "Video 1 Selected!",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (mode == 2) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        multiPhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = if (viewModel.player2VideoUri == null) "Select Video (Player 2)" else "Video 2 Selected!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canEnterArena = if (mode == 1) {
                viewModel.player1VideoUri != null
            } else {
                viewModel.player1VideoUri != null && viewModel.player2VideoUri != null
            }

            Button(
                onClick = { navController.navigate(Screen.Arena.route) },
                enabled = canEnterArena,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPink,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(text = "ENTER ARENA", style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSetupSection(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(ElectricPurple, NeonPink, Color.Cyan, Color.Green, Color.Yellow)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title, 
            style = MaterialTheme.typography.headlineSmall, 
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 4.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}
