package com.nikhil.yt.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VuMeter(
    modifier: Modifier = Modifier,
    isPlayerExpanded: Boolean = true,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val playerVolume by playerConnection.service.playerVolume.collectAsState()

    val targetLevel = if (isPlayerExpanded) playerVolume else 0f
    val animatedLevel by animateFloatAsState(
        targetValue = targetLevel,
        animationSpec = spring(
            dampingRatio = 0.3f,
            stiffness = 100f,
        ),
        label = "vuLevel",
    )

    val leftLevel = animatedLevel.coerceIn(0f, 1f)
    val rightLevel = (animatedLevel * 0.95f + 0.05f).coerceIn(0f, 1f)

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
                isActive = isPlayerExpanded,
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
    
    // Warm-colored vintage needles (classic orange-red and yellow-orange or black/red)
    val leftNeedleColor = Color(0xFFE53935)  // Matte Red
    val rightNeedleColor = Color(0xFF1E88E5) // Matte Blue
    
    // Sweep from left (-145 deg) to right (-35 deg)
    val startAngle = -145f
    val sweepAngle = 110f
    
    fun drawNeedle(level: Float, color: Color) {
        val clamped = level.coerceIn(0f, 1f)
        val angleDeg = startAngle + clamped * sweepAngle
        val angleRad = Math.toRadians(angleDeg.toDouble())
        val tipX = cx + (needleLen * cos(angleRad)).toFloat()
        val tipY = cy + (needleLen * sin(angleRad)).toFloat()
        
        // Shadow (subtle black offset line for 3D depth)
        if (isActive) {
            drawLine(
                color = Color.Black.copy(alpha = 0.3f),
                start = Offset(cx + 4f, cy + 4f),
                end = Offset(tipX + 4f, tipY + 4f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }
        
        // Main Needle Line
        drawLine(
            color = if (isActive) color else color.copy(alpha = 0.4f),
            start = Offset(cx, cy),
            end = Offset(tipX, tipY),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
    
    drawNeedle(leftLevel, leftNeedleColor)
    drawNeedle(rightLevel, rightNeedleColor)
    
    // Pivot cap
    drawCircle(Color(0xFF222222), radius = w * 0.06f, center = Offset(cx, cy))
    drawCircle(Color(0xFF444444), radius = w * 0.03f, center = Offset(cx, cy))
}
