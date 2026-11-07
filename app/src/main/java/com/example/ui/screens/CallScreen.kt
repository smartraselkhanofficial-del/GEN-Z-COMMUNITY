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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CallAudioEngine
import com.example.data.model.CallSession
import com.example.data.repository.CommunityRepository
import com.example.ui.components.AudioWaveVisualizer
import com.example.ui.theme.DarkBackground
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CallScreen(
  callId: String,
  currentUserId: String,
  repository: CommunityRepository,
  onCallEnded: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val callSession by repository.observeCall(callId).collectAsState(initial = null)

  val audioEngine = remember { CallAudioEngine(context) }
  var isMuted by remember { mutableStateOf(false) }
  var isVideoEnabled by remember { mutableStateOf(true) }
  var isSpeakerOn by remember { mutableStateOf(true) }
  var callDuration by remember { mutableIntStateOf(0) }
  var audioLevel by remember { mutableStateOf(0.1f) }

  val call = callSession
  val isCaller = call?.callerId == currentUserId
  val peerName = if (isCaller) call?.receiverName ?: "Recipient" else call?.callerName ?: "Caller"
  val isConnected = call?.status == "accepted"

  // Start audio engine when call becomes accepted
  LaunchedEffect(isConnected) {
    if (isConnected) {
      audioEngine.onAudioLevel = { level ->
        audioLevel = level
      }
      audioEngine.startAudio(scope)
      while (true) {
        delay(1000)
        callDuration++
      }
    }
  }

  // Auto close if ended or rejected
  LaunchedEffect(call?.status) {
    if (call != null && (call?.status == "ended" || call?.status == "rejected")) {
      audioEngine.stopAudio()
      onCallEnded()
    }
  }

  DisposableEffect(callId) {
    onDispose {
      audioEngine.stopAudio()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("call_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp)
    ) {
      // Top info
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = if (call?.callType == "video") "GEN Z VIDEO CALL" else "GEN Z AUDIO CALL",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = peerName,
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = if (isConnected) {
            val mins = callDuration / 60
            val secs = callDuration % 60
            String.format("%02d:%02d", mins, secs)
          } else {
            "Ringing..."
          },
          fontSize = 15.sp,
          color = if (isConnected) NeonGreen else TextSecondary,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Middle: Video simulation window or glowing audio avatar
      if (call?.callType == "video" && isVideoEnabled) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .border(2.dp, NeonViolet, RoundedCornerShape(24.dp))
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Box(
                modifier = Modifier
                  .size(80.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.sweepGradient(listOf(NeonViolet, NeonCyan, NeonPink, NeonViolet))
                  )
                  .padding(2.dp)
                  .clip(CircleShape)
                  .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "User",
                  tint = NeonCyan,
                  modifier = Modifier.size(44.dp)
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Live Video Active",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
          }
        }
      } else {
        // Audio Call Avatar & Waveform
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(130.dp)
              .clip(CircleShape)
              .background(
                Brush.sweepGradient(listOf(NeonViolet, NeonPink, NeonCyan, NeonViolet))
              )
              .padding(3.dp)
              .clip(CircleShape)
              .background(DarkSurface),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Peer",
              tint = NeonCyan,
              modifier = Modifier.size(70.dp)
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          AudioWaveVisualizer(
            level = if (!isMuted && isConnected) audioLevel else 0.05f,
            barColor = NeonCyan,
            modifier = Modifier
              .width(200.dp)
              .height(36.dp)
          )
        }
      }

      // Bottom Control Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mute Mic Button
        IconButton(
          onClick = {
            isMuted = !isMuted
            audioEngine.setMuted(isMuted)
          },
          modifier = Modifier
            .size(56.dp)
            .background(if (isMuted) NeonPink else DarkSurfaceVariant, CircleShape)
            .testTag("call_mute_button")
        ) {
          Icon(
            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Mute Mic",
            tint = Color.White
          )
        }

        // Speaker Toggle Button
        IconButton(
          onClick = {
            isSpeakerOn = audioEngine.toggleSpeaker()
          },
          modifier = Modifier
            .size(56.dp)
            .background(if (isSpeakerOn) NeonViolet else DarkSurfaceVariant, CircleShape)
            .testTag("call_speaker_button")
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Toggle Speaker",
            tint = Color.White
          )
        }

        // Toggle Video Camera
        if (call?.callType == "video") {
          IconButton(
            onClick = { isVideoEnabled = !isVideoEnabled },
            modifier = Modifier
              .size(56.dp)
              .background(if (isVideoEnabled) NeonCyan else DarkSurfaceVariant, CircleShape)
              .testTag("call_video_toggle_button")
          ) {
            Icon(
              imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
              contentDescription = "Toggle Video",
              tint = if (isVideoEnabled) Color.Black else Color.White
            )
          }
        }

        // End Call Button (Red)
        IconButton(
          onClick = {
            scope.launch {
              repository.updateCallStatus(callId, "ended")
              audioEngine.stopAudio()
              onCallEnded()
            }
          },
          modifier = Modifier
            .size(62.dp)
            .background(NeonPink, CircleShape)
            .testTag("end_call_button")
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
          )
        }
      }
    }
  }
}
