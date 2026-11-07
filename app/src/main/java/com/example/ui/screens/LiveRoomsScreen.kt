package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LiveRoom
import com.example.data.repository.CommunityRepository
import com.example.ui.components.CreateLiveRoomDialog
import com.example.ui.components.LiveRoomCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LiveRoomsScreen(
  repository: CommunityRepository,
  currentUserId: String,
  onOpenRoom: (LiveRoom) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val rooms by repository.observeLiveRooms().collectAsState(initial = emptyList())
  var showCreateDialog by remember { mutableStateOf(false) }
  var selectedCategory by remember { mutableStateOf("All") }
  val categories = listOf("All", "Chill", "Music", "Tech", "Gaming", "Debate")

  val filteredRooms = if (selectedCategory == "All") {
    rooms
  } else {
    rooms.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("live_rooms_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // Header Banner
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = "Audio Stages",
              tint = NeonCyan,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
              text = "AUDIO STAGES",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Kuki-style live rooms • Drop in, listen, or raise hand to speak",
            fontSize = 13.sp,
            color = TextMuted
          )
        }
      }

      // Categories
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
        ) {
          items(categories) { cat ->
            val isSelected = cat == selectedCategory
            Box(
              modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) NeonCyan else DarkSurfaceVariant)
                .clickable { selectedCategory = cat }
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                color = if (isSelected) Color.Black else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Rooms list
      if (filteredRooms.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(50.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "No live stages currently in $selectedCategory",
                color = TextMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Be the first to launch a stage!",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      } else {
        items(filteredRooms, key = { it.id }) { room ->
          LiveRoomCard(
            room = room,
            onJoinClick = { onOpenRoom(room) }
          )
        }
      }
    }

    // Go Live FAB
    FloatingActionButton(
      onClick = { showCreateDialog = true },
      containerColor = NeonViolet,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 76.dp, end = 20.dp)
        .testTag("go_live_fab")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Go Live")
        Spacer(modifier = Modifier.size(6.dp))
        Text("Go Live", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }

    if (showCreateDialog) {
      CreateLiveRoomDialog(
        onDismiss = { showCreateDialog = false },
        onCreate = { title, desc, cat ->
          scope.launch {
            val roomId = repository.createLiveRoom(title, desc, cat)
            // Retrieve created room to open
            val newRoom = LiveRoom(
              id = roomId,
              hostId = currentUserId,
              hostName = "Host",
              title = title,
              description = desc,
              category = cat,
              isActive = true,
              speakers = listOf(currentUserId)
            )
            onOpenRoom(newRoom)
          }
        }
      )
    }
  }
}
