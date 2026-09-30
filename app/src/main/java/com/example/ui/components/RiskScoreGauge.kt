package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ThreatClassification
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.ThreatRed
import com.example.ui.theme.WarningAmber

@Composable
fun RiskScoreGauge(
    score: Int,
    classification: ThreatClassification,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedScore = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val (primaryColor, gradientColors) = when (classification) {
        ThreatClassification.PHISHING -> Pair(
            ThreatRed,
            listOf(ThreatRed, Color(0xFFFF5252), Color(0xFFFF1744))
        )
        ThreatClassification.SUSPICIOUS -> Pair(
            WarningAmber,
            listOf(WarningAmber, Color(0xFFFBBF24), Color(0xFFD97706))
        )
        ThreatClassification.SAFE -> Pair(
            SafeGreen,
            listOf(SafeGreen, Color(0xFF34D399), Color(0xFF059669))
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Background full track (260 degree arc)
            drawArc(
                color = trackColor,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = stroke
            )

            // Dynamic progress sweep
            val currentSweep = (animatedScore.value / 100f) * 260f
            if (currentSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = 140f,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    style = stroke
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedScore.value.toInt()}",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Text(
                text = "/ 100",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when (classification) {
                    ThreatClassification.PHISHING -> "HIGH RISK"
                    ThreatClassification.SUSPICIOUS -> "SUSPICIOUS"
                    ThreatClassification.SAFE -> "SAFE"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
        }
    }
}
