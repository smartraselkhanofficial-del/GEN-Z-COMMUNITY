package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CallAudioEngine
import com.example.data.model.LiveRoom
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
import kotlinx.coroutines.launch

@Composable
fun LiveAudioRoomSheet(
  room: LiveRoom,
  currentUserId: String,
  repository: CommunityRepository,
  onClose: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val activeRoom by repository.observeLiveRoom(room.id).collectAsState(initial = room)
  val roomChats by repository.observeRoomChats(room.id).collectAsState(initial = emptyList())

  val audioEngine = remember { CallAudioEngine(context) }
  var audioLevel by remember { mutableFloatStateOf(0.1f) }
  var isMicMuted by remember { mutableStateOf(false) }
  var isChatExpanded by remember { mutableStateOf(false) }
  var chatInput by remember { mutableStateOf("") }

  val currentRoom = activeRoom ?: room
  val isHost = currentRoom.hostId == currentUserId
  val isSpeaker = isHost || currentRoom.speakers.contains(currentUserId)
  val isHandRaised = currentRoom.handRaisedUserIds.contains(currentUserId)

  // Join room on launch
  LaunchedEffect(room.id) {
    repository.joinLiveRoom(room.id)
    audioEngine.onAudioLevel = { level ->
      audioLevel = level
    }
    if (isSpeaker) {
      audioEngine.startAudio(scope)
    }
  }

  DisposableEffect(room.id) {
    onDispose {
      audioEngine.stopAudio()
      scope.launch {
        repository.leaveLiveRoom(room.id)
      }
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(16.dp)
      .testTag("live_audio_room_sheet")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(NeonViolet.copy(alpha = 0.3f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = currentRoom.category,
              color = NeonCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• ${currentRoom.participantCount} listening",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = currentRoom.title,
          fontSize = 18.sp,
          fontWeight = FontWeight.ExtraBold,
          color = TextPrimary
        )
      }

      IconButton(
        onClick = onClose,
        modifier = Modifier
          .size(38.dp)
          .background(DarkSurfaceVariant, CircleShape)
          .testTag("close_room_button")
      ) {
        Icon(Icons.Default.Close, contentDescription = "Minimize", tint = TextPrimary)
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Audio Visualizer
    AudioWaveVisualizer(
      level = if (isSpeaker && !isMicMuted) audioLevel else 0.05f,
      barColor = if (isSpeaker && !isMicMuted) NeonGreen else NeonCyan,
      modifier = Modifier
        .fillMaxWidth()
        .height(38.dp)
        .padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Stage Speakers Section
    Text(
      text = "STAGE SPEAKERS (${currentRoom.speakers.size})",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = NeonCyan,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      modifier = Modifier
        .fillMaxWidth()
        .height(160.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(currentRoom.speakers) { speakerUid ->
        val isThisUserHost = speakerUid == currentRoom.hostId
        val isThisUser = speakerUid == currentUserId

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.testTag("speaker_item_$speakerUid")
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(DarkSurfaceElevated)
              .border(
                width = 2.dp,
                color = if (isThisUser && !isMicMuted) NeonGreen else NeonViolet,
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Speaker",
              tint = if (isThisUserHost) NeonCyan else TextPrimary,
              modifier = Modifier.size(32.dp)
            )

            // Mic badge
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isThisUser && isMicMuted) NeonPink else NeonGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isThisUser && isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mic Status",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (isThisUserHost) "${currentRoom.hostName} (Host)" else if (isThisUser) "You" else "Speaker",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isThisUserHost) NeonCyan else TextPrimary,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    // Host Speaker Requests Queue
    if (isHost && currentRoom.handRaisedUserIds.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Hand Raised Requests (${currentRoom.handRaisedUserIds.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NeonPink
          )
          Spacer(modifier = Modifier.height(6.dp))
          currentRoom.handRaisedUserIds.take(3).forEach { reqUid ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "User ${reqUid.take(6)} wants to speak", fontSize = 12.sp, color = TextPrimary)
              Button(
                onClick = {
                  scope.launch {
                    repository.acceptSpeaker(currentRoom.id, reqUid)
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Approve", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Listeners section
    Text(
      text = "LISTENERS (${currentRoom.listeners.size})",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Live Room Chat & Overlay
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(DarkSurface)
        .padding(12.dp)
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        Text(
          text = "Stage Chat",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NeonViolet
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
          modifier = Modifier.weight(1f)
        ) {
          items(roomChats) { chat ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
              Text(
                text = "${chat.senderName}: ",
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
              Text(
                text = chat.text,
                color = TextPrimary,
                fontSize = 12.sp
              )
            }
          }
        }

        // Room Chat input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = chatInput,
            onValueChange = { chatInput = it },
            placeholder = { Text("Chat in room...", fontSize = 12.sp, color = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonViolet,
              unfocusedBorderColor = DarkSurfaceVariant,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f).height(48.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          IconButton(
            onClick = {
              if (chatInput.isNotBlank()) {
                scope.launch {
                  repository.sendRoomChat(currentRoom.id, chatInput.trim())
                  chatInput = ""
                }
              }
            },
            modifier = Modifier.size(42.dp).background(NeonViolet, CircleShape)
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Control bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isSpeaker) {
        // Mute / Unmute
        IconButton(
          onClick = {
            isMicMuted = !isMicMuted
            audioEngine.setMuted(isMicMuted)
          },
          modifier = Modifier
            .size(48.dp)
            .background(if (isMicMuted) NeonPink else NeonGreen, CircleShape)
            .testTag("stage_mic_toggle")
        ) {
          Icon(
            imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Toggle Mic",
            tint = Color.White
          )
        }
      } else {
        // Raise hand button
        Button(
          onClick = {
            scope.launch {
              repository.raiseHandInRoom(currentRoom.id)
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isHandRaised) NeonPink else DarkSurfaceElevated,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("stage_raise_hand_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FrontHand, contentDescription = "Raise Hand", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isHandRaised) "Hand Raised" else "Raise Hand")
          }
        }
      }

      // Leave / End Stage
      Button(
        onClick = {
          scope.launch {
            repository.leaveLiveRoom(currentRoom.id)
            onClose()
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = DarkSurfaceElevated,
          contentColor = NeonPink
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.testTag("leave_stage_button")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.ExitToApp, contentDescription = "Leave", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isHost) "End Stage" else "Leave Quietly")
        }
      }
    }
  }
}
