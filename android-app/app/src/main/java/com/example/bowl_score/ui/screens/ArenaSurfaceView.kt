package com.example.bowl_score.ui.screens

import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.bowl_score.ui.FrameData

class ArenaSurfaceView(
    context: android.content.Context,
    val accentColor: Int,
    val onScoreUpdate: (Int) -> Unit,
    val onFinished: () -> Unit
) : SurfaceView(context), SurfaceHolder.Callback {

    private var isRunning = false
    private var drawThread: DrawThread? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var cachedFrames: List<FrameData> = emptyList()

    // Alpha-Beta-Gamma Filter State
    private var carX = 0f
    private var carY = 0f
    private var carVx = 0f
    private var carVy = 0f
    private var carAx = 0f
    private var carAy = 0f

    private val ALPHA = 0.5f
    private val BETA = 0.2f
    private val GAMMA = 0.05f
    private val FRICTION = 0.95f
    private val dt = 5f

    init {
        holder.addCallback(this)
    }

    fun setFrames(frames: List<FrameData>) {
        this.cachedFrames = frames
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        drawThread = DrawThread(holder).apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        var retry = true
        while (retry) {
            try {
                drawThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                // Try again
            }
        }
    }

    inner class DrawThread(private val surfaceHolder: SurfaceHolder) : Thread() {
        override fun run() {
            if (cachedFrames.isEmpty()) return

            val pinPaint = Paint().apply {
                color = accentColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val trajectoryPaint = Paint().apply {
                color = Color.CYAN
                style = Paint.Style.STROKE
                strokeWidth = 2f // Reduced from 12f
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }

            val carPath = Path()
            var isFirstCarPoint = true

            // Reset Filter State
            carX = 0f; carY = 0f
            carVx = 0f; carVy = 0f
            carAx = 0f; carAy = 0f

            var frameIndex = 0
            while (isRunning && frameIndex < cachedFrames.size) {
                val frame = cachedFrames[frameIndex]
                val canvas = try { surfaceHolder.lockCanvas() } catch (e: Exception) { null }
                
                if (canvas != null) {
                    try {
                        // Background for the whole SurfaceView (black bars)
                        canvas.drawColor(Color.BLACK)

                        val viewWidth = canvas.width.toFloat()
                        val viewHeight = canvas.height.toFloat()
                        val targetAspect = 16f / 9f
                        val viewAspect = viewWidth / viewHeight

                        val drawRect = if (viewAspect > targetAspect) {
                            val drawWidth = viewHeight * targetAspect
                            val left = (viewWidth - drawWidth) / 2
                            RectF(left, 0f, left + drawWidth, viewHeight)
                        } else {
                            val drawHeight = viewWidth / targetAspect
                            val top = (viewHeight - drawHeight) / 2
                            RectF(0f, top, viewWidth, top + drawHeight)
                        }

                        // 1. Draw the video frame
                        canvas.drawBitmap(frame.bitmap, null, drawRect, null)

                        // 2. Draw fallen pin boxes
                        frame.fallenPinRects.forEach { rect ->
                            val l = drawRect.left + (rect.left * drawRect.width())
                            val t = drawRect.top + (rect.top * drawRect.height())
                            val r = drawRect.left + (rect.right * drawRect.width())
                            val b = drawRect.top + (rect.bottom * drawRect.height())
                            canvas.drawRect(l, t, r, b, pinPaint)
                        }

                        // 3. Update car path (Alpha-Beta-Gamma Tracking)
                        val detection = frame.carPoint
                        if (frameIndex % 5 == 0 && detection != null) {
                            // Path A: The Keyframe (Measurement Update)
                            val cx = detection.x
                            val cy = detection.y

                            if (isFirstCarPoint) {
                                carX = cx
                                carY = cy
                                carVx = 0f; carVy = 0f
                                carAx = 0f; carAy = 0f
                                
                                val px = drawRect.left + (carX * drawRect.width())
                                val py = drawRect.top + (carY * drawRect.height())
                                carPath.moveTo(px, py)
                                isFirstCarPoint = false
                            } else {
                                // 1. Predict
                                val predX = carX + (carVx * dt) + (0.5f * carAx * dt * dt)
                                val predY = carY + (carVy * dt) + (0.5f * carAy * dt * dt)
                                val predVx = carVx + (carAx * dt)
                                val predVy = carVy + (carAy * dt)

                                // 2. Calculate Error
                                val ex = cx - predX
                                val ey = cy - predY

                                // 3. Update State
                                carX = predX + ALPHA * ex
                                carY = predY + ALPHA * ey
                                carVx = predVx + (BETA / dt) * ex
                                carVy = predVy + (BETA / dt) * ey
                                carAx = carAx + (2f * GAMMA / (dt * dt)) * ex
                                carAy = carAy + (2f * GAMMA / (dt * dt)) * ey

                                val px = drawRect.left + (carX * drawRect.width())
                                val py = drawRect.top + (carY * drawRect.height())
                                carPath.lineTo(px, py)
                            }
                        } else if (!isFirstCarPoint) {
                            // Path B: Intermediate Frames (Kinematic Prediction)
                            // 1. Apply acceleration to velocity
                            carVx += carAx
                            carVy += carAy

                            // 2. Apply damping for safety
                            carVx *= FRICTION
                            carVy *= FRICTION
                            carAx *= FRICTION
                            carAy *= FRICTION

                            // 3. Apply velocity to position
                            carX += carVx
                            carY += carVy

                            // 4. Draw if within bounds
                            if (carX >= 0f && carX <= 1f && carY >= 0f && carY <= 1f) {
                                val px = drawRect.left + (carX * drawRect.width())
                                val py = drawRect.top + (carY * drawRect.height())
                                carPath.lineTo(px, py)
                            }
                        }

                        if (!isFirstCarPoint) {
                            canvas.drawPath(carPath, trajectoryPaint)
                        }

                        // 4. Draw border exactly around the video (ON TOP of everything)
                        canvas.drawRect(drawRect, pinPaint)

                        // Update score on the main thread
                        mainHandler.post { onScoreUpdate(frame.standingPins) }

                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas)
                    }
                }
                
                frameIndex++
                try {
                    sleep(50) // Matches the ~20 fps extraction rate
                } catch (e: InterruptedException) {
                    break
                }
            }
            if (frameIndex >= cachedFrames.size) {
                mainHandler.post { onFinished() }
            }
        }
    }
}
