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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Community
import com.example.data.model.UserProfile
import com.example.data.repository.CommunityRepository
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun DiscoverScreen(
  repository: CommunityRepository,
  currentUserId: String,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  var searchQuery by remember { mutableStateOf("") }
  var searchResults by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
  var requestSentMap by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
  var showCreateCommunityDialog by remember { mutableStateOf(false) }

  val communities by repository.observeCommunities().collectAsState(initial = emptyList())

  val trendingTopics = listOf(
    "#AI_Art" to "4.2k vibes",
    "#Hyperpop" to "2.8k vibes",
    "#Cyberpunk" to "1.9k vibes",
    "#LateNightDebate" to "850 vibes",
    "#GamingClips" to "3.1k vibes"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("discover_screen")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "DISCOVER & CONNECT",
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
        Text(
          text = "Search Gen Z members, trending topics & communities",
          fontSize = 12.sp,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = {
            searchQuery = it
            scope.launch {
              searchResults = repository.searchUsers(it)
            }
          },
          placeholder = { Text("Search users by @handle or name...", color = TextMuted) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonCyan)
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth().testTag("discover_search_input")
        )

        Spacer(modifier = Modifier.height(18.dp))
      }

      // User Search Results
      if (searchQuery.isNotBlank()) {
        item {
          Text(
            text = "SEARCH RESULTS (${searchResults.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        if (searchResults.isEmpty()) {
          item {
            Text(text = "No users found matching '$searchQuery'", color = TextMuted, fontSize = 13.sp)
          }
        } else {
          items(searchResults) { user ->
            if (user.userId != currentUserId) {
              val isSent = requestSentMap[user.userId] == true
              Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
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
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.displayName, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "@${user.username}", fontSize = 12.sp, color = NeonCyan)
                  }
                  Button(
                    onClick = {
                      scope.launch {
                        repository.sendFriendRequest(user.userId, user.displayName)
                        requestSentMap = requestSentMap + (user.userId to true)
                      }
                    },
                    enabled = !isSent,
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isSent) DarkSurfaceElevated else NeonViolet,
                      contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Text(if (isSent) "Sent" else "Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(20.dp))
        }
      }

      // Trending Topics Section
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.TrendingUp, contentDescription = "Trending", tint = NeonPink, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "TRENDING ON GEN Z",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonPink,
            letterSpacing = 1.sp
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      items(trendingTopics) { (tag, vibes) ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = tag, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = vibes, color = TextMuted, fontSize = 12.sp)
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(22.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Group, contentDescription = "Communities", tint = NeonCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "FEATURED COMMUNITIES",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              letterSpacing = 1.sp
            )
          }
          IconButton(onClick = { showCreateCommunityDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Create Community", tint = NeonCyan)
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Communities list
      items(communities) { comm ->
        val isMember = comm.memberIds.contains(currentUserId)
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeonViolet.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = comm.name.take(1).uppercase(),
                color = NeonCyan,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = comm.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
              Text(text = "${comm.membersCount} members • ${comm.category}", fontSize = 12.sp, color = TextMuted)
            }
            Button(
              onClick = {
                scope.launch {
                  repository.joinCommunity(comm.id)
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isMember) DarkSurfaceElevated else NeonCyan,
                contentColor = if (isMember) TextPrimary else Color.Black
              ),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(if (isMember) "Joined" else "Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(90.dp))
      }
    }

    if (showCreateCommunityDialog) {
      CreateCommunityModal(
        onDismiss = { showCreateCommunityDialog = false },
        onCreate = { name, desc, cat ->
          scope.launch {
            repository.createCommunity(name, desc, cat)
          }
        }
      )
    }
  }
}

@Composable
fun CreateCommunityModal(
  onDismiss: () -> Unit,
  onCreate: (name: String, desc: String?, category: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Tech & Coding") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
        .padding(4.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Text(
          text = "Create Gen Z Community",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          placeholder = { Text("Community Name", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = desc,
          onValueChange = { desc = it },
          placeholder = { Text("What is this community about?", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonViolet,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            if (name.isNotBlank()) {
              onCreate(name.trim(), desc.trim().ifBlank { null }, category)
              onDismiss()
            }
          },
          enabled = name.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
          Text("Create Community", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
