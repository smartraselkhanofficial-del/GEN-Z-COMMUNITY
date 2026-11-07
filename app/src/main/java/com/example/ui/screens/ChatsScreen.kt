package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Chat
import com.example.data.model.UserProfile
import com.example.data.repository.CommunityRepository
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
fun ChatsScreen(
  repository: CommunityRepository,
  currentUserId: String,
  onOpenChat: (Chat) -> Unit,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val chats by repository.observeUserChats().collectAsState(initial = emptyList())
  var showNewChatDialog by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("chats_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "MESSAGES",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.sp
          )
          Text(
            text = "Realtime Messenger • Audio & Video Calling",
            fontSize = 12.sp,
            color = TextMuted
          )
        }
      }

      if (chats.isEmpty()) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(40.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "No conversations yet",
              fontSize = 16.sp,
              color = TextMuted,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Tap + below to start a chat!",
              fontSize = 13.sp,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 16.dp)
        ) {
          items(chats, key = { it.id }) { chat ->
            val otherParticipantId = chat.participantIds.firstOrNull { it != currentUserId } ?: "User"

            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onOpenChat(chat) }
                .testTag("chat_item_${chat.id}")
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.5.dp, NeonViolet, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    tint = NeonCyan,
                    modifier = Modifier.size(26.dp)
                  )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (chat.isGroup) chat.groupName ?: "Group" else "Member (${otherParticipantId.take(6)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = chat.lastMessage ?: "Tap to message",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                )
              }
            }
          }
        }
      }
    }

    // New Chat FAB
    FloatingActionButton(
      onClick = { showNewChatDialog = true },
      containerColor = NeonViolet,
      contentColor = Color.White,
      shape = CircleShape,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 76.dp, end = 20.dp)
        .testTag("new_chat_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "New Chat")
    }

    if (showNewChatDialog) {
      NewChatModal(
        repository = repository,
        onDismiss = { showNewChatDialog = false },
        onSelectUser = { targetUser ->
          showNewChatDialog = false
          scope.launch {
            val chatId = repository.getOrCreateDirectChat(targetUser.userId)
            val newChat = Chat(
              id = chatId,
              participantIds = listOf(currentUserId, targetUser.userId),
              isGroup = false
            )
            onOpenChat(newChat)
          }
        }
      )
    }
  }
}

@Composable
fun NewChatModal(
  repository: CommunityRepository,
  onDismiss: () -> Unit,
  onSelectUser: (UserProfile) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
  val scope = rememberCoroutineScope()

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
        .padding(4.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(
          text = "Start Conversation",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = {
            searchQuery = it
            scope.launch {
              searchResults = repository.searchUsers(it)
            }
          },
          placeholder = { Text("Search username or name...", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("search_user_chat_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.height(200.dp)) {
          if (searchResults.isEmpty()) {
            item {
              Text(
                text = if (searchQuery.isBlank()) "Type to find Gen Z community friends" else "No users found",
                fontSize = 13.sp,
                color = TextMuted,
                modifier = Modifier.padding(16.dp)
              )
            }
          } else {
            items(searchResults) { user ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onSelectUser(user) }
                  .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonViolet),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = user.displayName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = user.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                  Text(text = "@${user.username}", fontSize = 12.sp, color = NeonCyan)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun ChatConversationScreen(
  chat: Chat,
  currentUserId: String,
  repository: CommunityRepository,
  onBack: () -> Unit,
  onStartCall: (callType: String) -> Unit
) {
  val scope = rememberCoroutineScope()
  val messages by repository.observeChatMessages(chat.id).collectAsState(initial = emptyList())
  var textInput by remember { mutableStateOf("") }

  val otherParticipantId = chat.participantIds.firstOrNull { it != currentUserId } ?: "Member"

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("chat_conversation_screen")
  ) {
    // Header with calling triggers
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurface)
        .padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }

      Spacer(modifier = Modifier.width(6.dp))

      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(DarkSurfaceVariant)
          .border(1.dp, NeonCyan, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Person, contentDescription = "User", tint = NeonCyan, modifier = Modifier.size(20.dp))
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (chat.isGroup) chat.groupName ?: "Group" else "Member (${otherParticipantId.take(6)})",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = TextPrimary
        )
        Text(
          text = "Online",
          fontSize = 11.sp,
          color = NeonGreen
        )
      }

      // Audio Call Button
      IconButton(
        onClick = { onStartCall("audio") },
        modifier = Modifier.testTag("start_audio_call_button")
      ) {
        Icon(Icons.Default.Call, contentDescription = "Audio Call", tint = NeonCyan)
      }

      // Video Call Button
      IconButton(
        onClick = { onStartCall("video") },
        modifier = Modifier.testTag("start_video_call_button")
      ) {
        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = NeonViolet)
      }
    }

    // Message history
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        val isMine = msg.senderId == currentUserId

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
        ) {
          Box(
            modifier = Modifier
              .clip(
                RoundedCornerShape(
                  topStart = 16.dp,
                  topEnd = 16.dp,
                  bottomStart = if (isMine) 16.dp else 2.dp,
                  bottomEnd = if (isMine) 2.dp else 16.dp
                )
              )
              .background(if (isMine) NeonViolet else DarkSurface)
              .border(1.dp, if (isMine) NeonViolet else DarkSurfaceVariant, RoundedCornerShape(16.dp))
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Text(
              text = msg.text,
              color = if (isMine) Color.White else TextPrimary,
              fontSize = 14.sp,
              lineHeight = 20.sp
            )
          }
        }
      }
    }

    // Input row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(DarkSurface)
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        placeholder = { Text("Type a message...", color = TextMuted) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = DarkSurfaceVariant,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("chat_message_input")
      )

      Spacer(modifier = Modifier.width(8.dp))

      IconButton(
        onClick = {
          if (textInput.isNotBlank()) {
            scope.launch {
              repository.sendChatMessage(chat.id, textInput.trim())
              textInput = ""
            }
          }
        },
        modifier = Modifier
          .size(46.dp)
          .background(NeonViolet, CircleShape)
          .testTag("send_chat_message_button")
      ) {
        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
      }
    }
  }
}
