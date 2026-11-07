package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit
) {
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(24.dp)
      .testTag("auth_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      // 3D Neon Glowing Logo
      Box(
        modifier = Modifier
          .size(90.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(
            Brush.linearGradient(
              listOf(NeonViolet, NeonPink, NeonCyan)
            )
          )
          .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Z",
          fontSize = 54.sp,
          fontWeight = FontWeight.Black,
          color = Color.White
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "GEN Z COMMUNITY",
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        color = TextPrimary,
        letterSpacing = 1.5.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Next-Gen Social Network • Live Audio Stages • Calling",
        fontSize = 14.sp,
        color = TextMuted,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Feature highlights
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(DarkSurface)
          .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
          .padding(18.dp)
      ) {
        FeatureRow(icon = Icons.Default.GraphicEq, color = NeonViolet, title = "Kuki-Style Audio Stages", subtitle = "Host or speak on live voice rooms with real participants")
        Spacer(modifier = Modifier.height(14.dp))
        FeatureRow(icon = Icons.Default.VideoCall, color = NeonCyan, title = "Audio & Video Calling", subtitle = "Crystal clear calling and instant connection")
        Spacer(modifier = Modifier.height(14.dp))
        FeatureRow(icon = Icons.Default.Message, color = NeonPink, title = "Realtime Chat & Feed", subtitle = "Moments, stories, reactions, and direct messaging")
      }

      Spacer(modifier = Modifier.height(36.dp))

      // Interactive Google Sign-In Button
      GoogleSignInButton(
        onAuthSuccess = onAuthSuccess,
        onAuthError = { errorMessage = it }
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "Quick & secure sign-in with any Google / Gmail account.\nYour profile name and handle can be customized anytime.",
        fontSize = 12.sp,
        color = TextMuted,
        textAlign = TextAlign.Center,
        lineHeight = 16.sp
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = errorMessage!!,
          color = NeonPink,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Composable
private fun FeatureRow(
  icon: ImageVector,
  color: Color,
  title: String,
  subtitle: String
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = color,
        modifier = Modifier.size(20.dp)
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column {
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = TextPrimary
      )
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = TextMuted
      )
    }
  }
}
