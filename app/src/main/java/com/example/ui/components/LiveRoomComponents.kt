package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveRoom
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveRoomCard(
  room: LiveRoom,
  onJoinClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val transition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .border(
        width = 1.dp,
        brush = Brush.horizontalGradient(listOf(NeonViolet.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.3f))),
        shape = RoundedCornerShape(22.dp)
      )
      .testTag("live_room_card_${room.id}")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Top bar: Category + Live Pulsing Beacon + Participant Counter
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = room.category,
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Live badge
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(NeonPink.copy(alpha = pulseAlpha))
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE",
            color = NeonPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
          )
          Spacer(modifier = Modifier.width(12.dp))
          Icon(
            imageVector = Icons.Default.Headphones,
            contentDescription = "Listeners",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = room.participantCount.toString(),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Room Title
      Text(
        text = room.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary,
        lineHeight = 24.sp
      )

      if (!room.description.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = room.description,
          fontSize = 13.sp,
          color = TextMuted,
          maxLines = 2
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Speakers row & Join Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(NeonViolet.copy(alpha = 0.3f))
              .border(1.5.dp, NeonGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Host",
              tint = NeonGreen,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = room.hostName,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Host",
              fontSize = 11.sp,
              color = NeonGreen
            )
          }
        }

        Button(
          onClick = onJoinClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonViolet,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("join_room_button_${room.id}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = "Tune in",
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Tune In",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun AudioWaveVisualizer(
  level: Float,
  modifier: Modifier = Modifier,
  barColor: Color = NeonCyan
) {
  val transition = rememberInfiniteTransition(label = "waveAnim")
  val phase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wavePhase"
  )

  Canvas(modifier = modifier) {
    val barCount = 16
    val barWidth = size.width / (barCount * 1.6f)
    val spacing = barWidth * 0.6f

    for (i in 0 until barCount) {
      val sinOffset = kotlin.math.sin(phase + i * 0.4f).toFloat()
      val dynamicHeight = (size.height * 0.2f + (size.height * 0.7f * (level * 1.5f + abs(sinOffset) * 0.3f))).coerceIn(4f, size.height)
      val x = i * (barWidth + spacing)
      val y = (size.height - dynamicHeight) / 2f

      drawRoundRect(
        color = barColor,
        topLeft = Offset(x, y),
        size = Size(barWidth, dynamicHeight),
        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
      )
    }
  }
}

private fun abs(value: Float): Float = if (value < 0f) -value else value
