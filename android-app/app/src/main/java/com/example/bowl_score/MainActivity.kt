package com.example.bowl_score

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bowl_score.ui.BowlingVisionApp
import com.example.bowl_score.ui.theme.BowlingVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BowlingVisionTheme {
                BowlingVisionApp()
            }
        }
    }
}