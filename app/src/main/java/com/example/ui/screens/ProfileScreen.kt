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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.repository.CommunityRepository
import com.example.ui.components.signOut
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
  userProfile: UserProfile?,
  currentUserId: String,
  repository: CommunityRepository,
  onOpenAdmin: () -> Unit,
  onSignedOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val friendRequests by repository.observeFriendRequests().collectAsState(initial = emptyList())

  var showEditProfileDialog by remember { mutableStateOf(false) }

  val authUser = Firebase.auth.currentUser
  val isAdmin = userProfile?.role == "admin" || authUser?.email == "nurejannattv24@gmail.com"

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("profile_screen")
  ) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
      // Banner
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
              Brush.horizontalGradient(
                listOf(NeonViolet, NeonPink, NeonCyan)
              )
            )
        )
      }

      // Profile avatar + Name + Edit
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Box(
              modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(3.dp, DarkBackground, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              if (!userProfile?.avatarUrl.isNullOrBlank()) {
                AsyncImage(
                  model = userProfile?.avatarUrl,
                  contentDescription = "Avatar",
                  modifier = Modifier.size(90.dp),
                  contentScale = ContentScale.Crop
                )
              } else {
                Text(
                  text = (userProfile?.displayName ?: "Z").take(1).uppercase(),
                  fontSize = 36.sp,
                  fontWeight = FontWeight.Black,
                  color = NeonCyan
                )
              }
            }

            Button(
              onClick = { showEditProfileDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("edit_profile_button")
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Edit Profile", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = userProfile?.displayName ?: "Gen Z Member",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Text(
            text = "@${userProfile?.username ?: "genz_user"}",
            fontSize = 14.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = userProfile?.bio ?: "Vibing in GEN Z COMMUNITY 🚀",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 20.sp
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Stats row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(DarkSurface)
              .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            StatItem(label = "Friends", count = (userProfile?.friendsCount ?: 0).toString())
            StatItem(label = "Followers", count = (userProfile?.followersCount ?: 0).toString())
            StatItem(label = "Status", count = "Active")
          }

          Spacer(modifier = Modifier.height(20.dp))
        }
      }

      // Friend requests section
      if (friendRequests.isNotEmpty()) {
        item {
          Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
              text = "FRIEND REQUESTS (${friendRequests.size})",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = NeonPink,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
        }

        items(friendRequests) { req ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 4.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NeonViolet),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = req.senderName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = req.senderName, fontWeight = FontWeight.Bold, color = TextPrimary)
                  Text(text = "Sent you a request", fontSize = 11.sp, color = TextMuted)
                }
              }

              Row {
                IconButton(
                  onClick = {
                    scope.launch {
                      repository.respondFriendRequest(req.id, accept = true, senderUid = req.senderId)
                    }
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .background(NeonGreen, CircleShape)
                ) {
                  Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                  onClick = {
                    scope.launch {
                      repository.respondFriendRequest(req.id, accept = false, senderUid = req.senderId)
                    }
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .background(DarkSurfaceElevated, CircleShape)
                ) {
                  Icon(Icons.Default.Close, contentDescription = "Decline", tint = TextSecondary)
                }
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }

      // Actions section
      item {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
          if (isAdmin) {
            Button(
              onClick = onOpenAdmin,
              colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("open_admin_dashboard_button")
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin")
              Spacer(modifier = Modifier.width(8.dp))
              Text("Admin Dashboard & Moderation", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
          }

          Button(
            onClick = {
              signOut(credentialManager, scope, onSignOutComplete = onSignedOut)
            },
            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = NeonPink),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("sign_out_button")
          ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out", tint = NeonPink)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(20.dp))

          // App Version & Update status card
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("app_version_card")
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Up to date",
                  tint = NeonCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "GEN Z COMMUNITY v1.0.2",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "In-Place APK Updates Enabled • Same Signing Key",
                fontSize = 11.sp,
                color = TextSecondary
              )
              Text(
                text = "New APK versions install directly over this app without uninstalling.",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(90.dp))
        }
      }
    }

    if (showEditProfileDialog) {
      EditProfileModal(
        userProfile = userProfile,
        onDismiss = { showEditProfileDialog = false },
        onSave = { name, bio, avatar ->
          scope.launch {
            repository.updateUserProfile(currentUserId, name, bio, avatar)
          }
        }
      )
    }
  }
}

@Composable
private fun StatItem(label: String, count: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = count, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
    Spacer(modifier = Modifier.height(2.dp))
    Text(text = label, fontSize = 12.sp, color = TextMuted)
  }
}

@Composable
fun EditProfileModal(
  userProfile: UserProfile?,
  onDismiss: () -> Unit,
  onSave: (displayName: String, bio: String?, avatarUrl: String?) -> Unit
) {
  var displayName by remember { mutableStateOf(userProfile?.displayName ?: "") }
  var bio by remember { mutableStateOf(userProfile?.bio ?: "") }
  var avatarUrl by remember { mutableStateOf(userProfile?.avatarUrl ?: "") }

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
          text = "Edit Profile",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = displayName,
          onValueChange = { displayName = it },
          placeholder = { Text("Display Name", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("edit_display_name_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          placeholder = { Text("Bio / Status message", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonViolet,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          minLines = 3,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = avatarUrl,
          onValueChange = { avatarUrl = it },
          placeholder = { Text("Avatar Photo URL", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPink,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = {
            if (displayName.isNotBlank()) {
              onSave(displayName.trim(), bio.trim().ifBlank { null }, avatarUrl.trim().ifBlank { null })
              onDismiss()
            }
          },
          enabled = displayName.isNotBlank(),
          colors = ButtonDefaults.buttonColors(containerColor = NeonViolet, contentColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp).testTag("save_profile_button")
        ) {
          Text("Save Changes", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
