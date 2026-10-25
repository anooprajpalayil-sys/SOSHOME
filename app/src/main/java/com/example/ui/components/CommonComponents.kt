package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.GuardianGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PureWhite

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    amplitude: Int,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    activeColor: Color = CrimsonRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "factor"
    )

    val normalizedAmp = (amplitude.toFloat() / 32767f).coerceIn(0.1f, 1.0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val offset = kotlin.math.sin((i.toDouble() / barCount) * Math.PI).toFloat()
            val heightFraction = if (isRecording) {
                (normalizedAmp * offset * animFactor).coerceIn(0.15f, 1.0f)
            } else {
                0.12f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((40 * heightFraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isRecording) activeColor else Color.Gray.copy(alpha = 0.4f))
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDanger) CrimsonRed.copy(alpha = 0.15f) else GuardianGreen.copy(alpha = 0.15f)
    val textColor = if (isDanger) CrimsonRed else GuardianGreen

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
