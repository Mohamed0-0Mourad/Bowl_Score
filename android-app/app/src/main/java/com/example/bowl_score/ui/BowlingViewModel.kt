package com.example.bowl_score.ui

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bowl_score.ui.theme.ElectricPurple
import com.example.bowl_score.ui.theme.NeonPink

data class FrameData(
    val bitmap: Bitmap,
    val standingPins: Int,
    val carPoint: PointF?,
    val fallenPinRects: List<RectF>
)

class BowlingViewModel : ViewModel() {
    var player1Name by mutableStateOf("Player 1")
    var player2Name by mutableStateOf("Player 2")

    var player1VideoUri by mutableStateOf<Uri?>(null)
    var player2VideoUri by mutableStateOf<Uri?>(null)

    var player1AccentColor by mutableStateOf(ElectricPurple)
    var player2AccentColor by mutableStateOf(NeonPink)

    var player1Score by mutableIntStateOf(0)
    var player2Score by mutableIntStateOf(0)

    var player1MaxScore by mutableIntStateOf(0)
    var player2MaxScore by mutableIntStateOf(0)

    var player1FirstTimeMaxScore by mutableStateOf<Long?>(null)
    var player2FirstTimeMaxScore by mutableStateOf<Long?>(null)

    var isProcessing by mutableStateOf(false)
    var processingProgress by mutableFloatStateOf(0f)
    
    var player1Frames = mutableListOf<FrameData>()
    var player2Frames = mutableListOf<FrameData>()

    fun reset() {
        player1Name = "Player 1"
        player2Name = "Player 2"
        player1VideoUri = null
        player2VideoUri = null
        player1AccentColor = ElectricPurple
        player2AccentColor = NeonPink
        player1Score = 0
        player2Score = 0
        player1MaxScore = 0
        player2MaxScore = 0
        player1FirstTimeMaxScore = null
        player2FirstTimeMaxScore = null
        isProcessing = false
        processingProgress = 0f
        player1Frames.clear()
        player2Frames.clear()
    }
}
