package com.example.bowl_score.ui.screens

import android.net.Uri
import android.os.Build
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.bowl_score.ui.BowlingViewModel
import com.example.bowl_score.ui.FrameData
import com.example.bowl_score.ui.theme.DeepViolet
import com.example.bowl_score.ui.theme.ElectricPurple
import com.example.bowl_score.ui.theme.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Composable
fun ArenaScreen(navController: NavHostController, viewModel: BowlingViewModel) {
    val context = LocalContext.current
    val isTwoPlayer = viewModel.player2VideoUri != null

    // Use a derived state to ensure the UI reacts to frame population
    val isReady = !viewModel.isProcessing && 
                  viewModel.player1Frames.isNotEmpty() && 
                  (!isTwoPlayer || viewModel.player2Frames.isNotEmpty())

    LaunchedEffect(Unit) {
        if (viewModel.player1Frames.isEmpty()) {
            viewModel.isProcessing = true
            withContext(Dispatchers.Default) {
                processVideoInternal(context, viewModel, 1)
                if (isTwoPlayer) {
                    processVideoInternal(context, viewModel, 2)
                }
            }
            viewModel.isProcessing = false
        }
    }

    fun navigateToCelebration() {
        val winner = if (!isTwoPlayer) {
            viewModel.player1Name
        } else {
            if (viewModel.player1MaxScore > viewModel.player2MaxScore) {
                viewModel.player1Name
            } else if (viewModel.player2MaxScore > viewModel.player1MaxScore) {
                viewModel.player2Name
            } else {
                // Scores are tied, check who reached it first
                val p1Time = viewModel.player1FirstTimeMaxScore ?: Long.MAX_VALUE
                val p2Time = viewModel.player2FirstTimeMaxScore ?: Long.MAX_VALUE
                if (p1Time <= p2Time) viewModel.player1Name else viewModel.player2Name
            }
        }
        navController.navigate(Screen.Celebration.createRoute(winner))
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (!isReady) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "PREPARING ARENA...",
                    style = MaterialTheme.typography.headlineLarge,
                    color = ElectricPurple
                )
                Spacer(modifier = Modifier.height(24.dp))
                LinearProgressIndicator(
                    progress = viewModel.processingProgress,
                    modifier = Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = ElectricPurple,
                    trackColor = ElectricPurple.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "${(viewModel.processingProgress * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StadiumScoreboard(viewModel, isTwoPlayer)

                Spacer(modifier = Modifier.height(16.dp))

                if (isTwoPlayer) {
                    Column(modifier = Modifier.weight(1f)) {
                        VideoContainer(
                            modifier = Modifier.weight(1f),
                            frames = viewModel.player1Frames,
                            accentColor = viewModel.player1AccentColor,
                            onScoreUpdate = { viewModel.player1Score = it },
                            onFinished = { navigateToCelebration() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        VideoContainer(
                            modifier = Modifier.weight(1f),
                            frames = viewModel.player2Frames,
                            accentColor = viewModel.player2AccentColor,
                            onScoreUpdate = { viewModel.player2Score = it },
                            onFinished = { /* Navigate only once */ }
                        )
                    }
                } else {
                    VideoContainer(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        frames = viewModel.player1Frames,
                        accentColor = viewModel.player1AccentColor,
                        onScoreUpdate = { viewModel.player1Score = it },
                        onFinished = { navigateToCelebration() }
                    )
                }
            }
        }
    }
}

fun processVideoInternal(context: android.content.Context, viewModel: BowlingViewModel, playerIndex: Int) {
    val uri = if (playerIndex == 1) viewModel.player1VideoUri else viewModel.player2VideoUri
    if (uri == null) return

    val inputSize = 640
    val modelBuffer = try { loadModelFileInternal(context, "best_int8.tflite") } catch (e: Exception) { return }
    val tflite = try { Interpreter(modelBuffer, Interpreter.Options().setNumThreads(4)) } catch (e: Exception) { return }

    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLong() ?: 0
        // Use 50ms (20fps) to balance tracking quality and memory/CPU usage
        val frameIntervalMs = 50L 
        val frameCount = (durationMs / frameIntervalMs).toInt()

        val frames = if (playerIndex == 1) viewModel.player1Frames else viewModel.player2Frames
        frames.clear()

        var lastCarPoint: PointF? = null
        var carVelocityX = 0f
        var carVelocityY = 0f
        var lastFallenPinRects = mutableListOf<RectF>()

        for (i in 0 until frameCount) {
            val timeUs = i * frameIntervalMs * 1000L
            
            // Optimization: use getScaledFrameAtTime (API 27+) to reduce memory pressure
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                try {
                    retriever.getScaledFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST, 640, 360)
                } catch (e: Exception) { null }
            } else {
                try {
                    retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST)?.let {
                        Bitmap.createScaledBitmap(it, 640, 360, true)
                    }
                } catch (e: Exception) { null }
            }

            if (bitmap != null) {
                var carPoint: PointF? = null
                var fallenPinRects = lastFallenPinRects.toMutableList()

                // Path A: The Keyframe (Inference every 5 frames @ 20fps = every 250ms)
                if (i % 5 == 0) {
                    val detections = runInferenceInternal(tflite, bitmap, inputSize)
                    
                    val newFallenPins = mutableListOf<RectF>()
                    var detectedCar: PointF? = null

                    detections.forEach { det ->
                        val (cx, cy, w, h, classId, _) = det
                        
                        // Sanity check: If coordinates are already normalized (usually < 1.1),
                        // don't divide by inputSize. Otherwise, normalize them.
                        val normX = if (cx > 1.1f) cx / inputSize else cx
                        val normY = if (cy > 1.1f) cy / inputSize else cy
                        val normW = if (w > 1.1f) w / inputSize else w
                        val normH = if (h > 1.1f) h / inputSize else h

                        // Skip invalid coordinates
                        if (normX.isNaN() || normY.isNaN() || normW.isNaN() || normH.isNaN()) return@forEach

                        when (classId) {
                            0 -> newFallenPins.add(RectF(normX - normW/2, normY - normH/2, normX + normW/2, normY + normH/2))
                            1 -> detectedCar = PointF(normX, normY)
                        }
                    }

                    detectedCar?.let { current ->
                        lastCarPoint?.let { last ->
                            carVelocityX = (current.x - last.x) / 5f
                            carVelocityY = (current.y - last.y) / 5f
                        }
                        lastCarPoint = current
                        carPoint = current
                    }
                    
                    fallenPinRects = newFallenPins
                    lastFallenPinRects = newFallenPins
                } else {
                    // Path B: Intermediate Frames (Kinematic Tracker)
                    lastCarPoint?.let { last ->
                        val nextX = last.x + carVelocityX
                        val nextY = last.y + carVelocityY
                        val newPoint = PointF(nextX, nextY)
                        carPoint = newPoint
                        lastCarPoint = newPoint
                    }
                }

                val currentScore = fallenPinRects.size
                frames.add(FrameData(bitmap, currentScore, carPoint, fallenPinRects))

                // Track max score and when it was first reached
                if (playerIndex == 1) {
                    if (currentScore > viewModel.player1MaxScore) {
                        viewModel.player1MaxScore = currentScore
                        viewModel.player1FirstTimeMaxScore = timeUs
                    }
                } else {
                    if (currentScore > viewModel.player2MaxScore) {
                        viewModel.player2MaxScore = currentScore
                        viewModel.player2FirstTimeMaxScore = timeUs
                    }
                }
            }
            
            val baseProgress = if (playerIndex == 2) 0.5f else 0f
            val factor = if (viewModel.player2VideoUri != null) 0.5f else 1.0f
            viewModel.processingProgress = baseProgress + (i.toFloat() / frameCount) * factor
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try { retriever.release() } catch (e: Exception) {}
        tflite.close()
    }
}

fun runInferenceInternal(tflite: Interpreter, bitmap: Bitmap, inputSize: Int): List<DetectionData> {
    val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
    val byteBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
    byteBuffer.order(ByteOrder.nativeOrder())
    
    val intValues = IntArray(inputSize * inputSize)
    resized.getPixels(intValues, 0, resized.width, 0, 0, resized.width, resized.height)
    
    for (pixel in intValues) {
        byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
        byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
        byteBuffer.putFloat((pixel and 0xFF) / 255f)
    }

    val output = Array(1) { Array(7) { FloatArray(8400) } }
    tflite.run(byteBuffer, output)

    val rawDetections = mutableListOf<DetectionData>()
    for (i in 0 until 8400) {
        val conf0 = output[0][4][i]
        val conf1 = output[0][5][i]
        val conf2 = output[0][6][i]
        val maxConf = maxOf(conf0, conf1, conf2)

        if (maxConf > 0.30f) {
            val classId = when (maxConf) {
                conf0 -> 0
                conf1 -> 1
                else -> 2
            }
            rawDetections.add(
                DetectionData(
                    output[0][0][i], output[0][1][i],
                    output[0][2][i], output[0][3][i],
                    classId, maxConf
                )
            )
        }
    }

    // Fix 2: NMS implementation to prevent exponential score growth
    val filteredDetections = mutableListOf<DetectionData>()
    val sortedDetections = rawDetections.sortedByDescending { it.conf }

    for (det in sortedDetections) {
        var keep = true
        for (kept in filteredDetections) {
            if (det.classId == kept.classId && calculateIoU(det, kept) > 0.45f) {
                keep = false
                break
            }
        }
        if (keep) filteredDetections.add(det)
    }

    return filteredDetections
}

fun calculateIoU(det1: DetectionData, det2: DetectionData): Float {
    val left1 = det1.cx - det1.w / 2
    val top1 = det1.cy - det1.h / 2
    val right1 = det1.cx + det1.w / 2
    val bottom1 = det1.cy + det1.h / 2

    val left2 = det2.cx - det2.w / 2
    val top2 = det2.cy - det2.h / 2
    val right2 = det2.cx + det2.w / 2
    val bottom2 = det2.cy + det2.h / 2

    val interLeft = maxOf(left1, left2)
    val interTop = maxOf(top1, top2)
    val interRight = minOf(right1, right2)
    val interBottom = minOf(bottom1, bottom2)

    if (interRight <= interLeft || interBottom <= interTop) return 0f

    val interArea = (interRight - interLeft) * (interBottom - interTop)
    val area1 = det1.w * det1.h
    val area2 = det2.w * det2.h
    
    return interArea / (area1 + area2 - interArea)
}


fun loadModelFileInternal(context: android.content.Context, modelPath: String): MappedByteBuffer {
    val fileDescriptor: AssetFileDescriptor = context.assets.openFd(modelPath)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

data class DetectionData(val cx: Float, val cy: Float, val w: Float, val h: Float, val classId: Int, val conf: Float)

@Composable
fun VideoContainer(
    modifier: Modifier,
    frames: List<FrameData>,
    accentColor: ComposeColor,
    onScoreUpdate: (Int) -> Unit,
    onFinished: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            factory = { context ->
                ArenaSurfaceView(
                    context = context,
                    accentColor = accentColor.toArgb(),
                    onScoreUpdate = onScoreUpdate,
                    onFinished = onFinished
                ).apply {
                    setFrames(frames)
                }
            },
            modifier = Modifier
                .aspectRatio(16f / 9f)
                .align(Alignment.Center)
        )
    }
}

@Composable
fun StadiumScoreboard(viewModel: BowlingViewModel, isTwoPlayer: Boolean) {
    val stadiumShape = GenericShape { size, _ ->
        moveTo(size.width * 0.1f, 0f)
        lineTo(size.width * 0.9f, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(stadiumShape),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = BorderStroke(2.dp, viewModel.player1AccentColor)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayerScoreItem(
                name = viewModel.player1Name,
                score = viewModel.player1Score,
                color = viewModel.player1AccentColor
            )

            if (isTwoPlayer) {
                Text("VS", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 24.sp)
                PlayerScoreItem(
                    name = viewModel.player2Name,
                    score = viewModel.player2Score,
                    color = viewModel.player2AccentColor
                )
            }
        }
    }
}

@Composable
fun PlayerScoreItem(name: String, score: Int, color: ComposeColor) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black
        )
    }
}

fun Modifier.glowingBorder(color: ComposeColor) = this.drawWithContent {
    drawContent()
    drawIntoCanvas { _ ->
        // Simplified glow effect using a semi-transparent brush
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.4f), ComposeColor.Transparent),
                center = center,
                radius = size.maxDimension
            ),
            blendMode = BlendMode.Screen
        )
    }
}
