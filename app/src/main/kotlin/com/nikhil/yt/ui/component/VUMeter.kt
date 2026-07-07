package com.nikhil.yt.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nikhil.yt.LocalPlayerConnection
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VuMeter(
    modifier: Modifier = Modifier,
    isPlayerExpanded: Boolean = true,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val service = playerConnection.service
    val isPlaying by playerConnection.isPlaying.collectAsState()

    var leftLevel by remember { mutableStateOf(0f) }
    var rightLevel by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying, isPlayerExpanded) {
        if (isPlaying && isPlayerExpanded) {
            while (true) {
                // Poll the amplitude values from our custom audio processor
                val rawL = service.amplitudeProcessor.latestAmplitudeL
                val rawR = service.amplitudeProcessor.latestAmplitudeR

                // Scale it by the current player volume (which is 0f to 1f)
                val currentVol = service.player.volume
                
                // Add a small random jitter to make it feel organic and warm
                val jitterL = if (rawL > 0.05f) (Math.random().toFloat() - 0.5f) * 0.02f else 0f
                val jitterR = if (rawR > 0.05f) (Math.random().toFloat() - 0.5f) * 0.02f else 0f

                leftLevel = (rawL * currentVol + jitterL).coerceIn(0f, 1f)
                rightLevel = (rawR * currentVol + jitterR).coerceIn(0f, 1f)

                delay(16) // ~60 FPS polling rate
            }
        } else {
            // Decay to zero when paused
            while (leftLevel > 0f || rightLevel > 0f) {
                leftLevel = (leftLevel * 0.8f - 0.02f).coerceIn(0f, 1f)
                rightLevel = (rightLevel * 0.8f - 0.02f).coerceIn(0f, 1f)
                delay(16)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(com.nikhil.yt.R.drawable.vu_meter_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            drawVintageNeedles(
                leftLevel = leftLevel,
                rightLevel = rightLevel,
                isActive = isPlayerExpanded && isPlaying,
            )
        }
    }
}

private fun DrawScope.drawVintageNeedles(
    leftLevel: Float,
    rightLevel: Float,
    isActive: Boolean,
) {
    val w = size.width
    val h = size.height
    
    // Pivot at bottom center (90% of height)
    val cx = w / 2f
    val cy = h * 0.9f
    val needleLen = h * 0.75f
    
    // Matte Red (Left) and Matte Blue (Right) vintage needles
    val leftNeedleColor = Color(0xFFE53935)
    val rightNeedleColor = Color(0xFF1E88E5)
    
    // Sweep from left (-145 deg) to right (-35 deg)
    val startAngle = -145f
    val sweepAngle = 110f
    
    fun drawNeedle(level: Float, color: Color) {
        val clamped = level.coerceIn(0f, 1f)
        val angleDeg = startAngle + clamped * sweepAngle
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val tipX = cx + (needleLen * cos(angleRad)).toFloat()
        val tipY = cy + (needleLen * sin(angleRad)).toFloat()
        
        // Shadow for depth
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(cx + 5f, cy + 5f),
            end = Offset(tipX + 5f, tipY + 5f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
        
        // Main needle line
        drawLine(
            color = color,
            start = Offset(cx, cy),
            end = Offset(tipX, tipY),
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
    }
    
    drawNeedle(leftLevel, leftNeedleColor)
    drawNeedle(rightLevel, rightNeedleColor)
    
    // Pivot cap covering the needle base
    drawCircle(Color(0xFF1A1A1A), radius = w * 0.065f, center = Offset(cx, cy))
    drawCircle(Color(0xFF333333), radius = w * 0.03f, center = Offset(cx, cy))
}
