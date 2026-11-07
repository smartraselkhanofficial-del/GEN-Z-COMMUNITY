package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.CallSession
import com.example.data.model.Chat
import com.example.data.model.LiveRoom
import com.example.data.model.UserProfile
import com.example.data.repository.CommunityRepository
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.NavDestination
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatConversationScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.LiveAudioRoomSheet
import com.example.ui.screens.LiveRoomsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        MainAppRoot()
      }
    }
  }
}

@Composable
fun MainAppRoot() {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  val repository = remember { CommunityRepository(context) }

  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  // Attempt silent auto-sign in on startup
  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = { currentUser = Firebase.auth.currentUser },
      onUnauthenticated = {},
      scope = scope
    )
  }

  if (currentUser == null) {
    AuthScreen(
      onAuthSuccess = { currentUser = Firebase.auth.currentUser }
    )
  } else {
    val uid = currentUser!!.uid
    key(uid) {
      AuthenticatedApp(
        currentUserId = uid,
        repository = repository,
        onSignedOut = { currentUser = null }
      )
    }
  }
}

@Composable
fun AuthenticatedApp(
  currentUserId: String,
  repository: CommunityRepository,
  onSignedOut: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var currentDestination by remember { mutableStateOf(NavDestination.FEED) }
  var userProfile by remember { mutableStateOf<UserProfile?>(null) }

  // Subscreen navigation states
  var activeLiveRoom by remember { mutableStateOf<LiveRoom?>(null) }
  var activeChat by remember { mutableStateOf<Chat?>(null) }
  var activeCallId by remember { mutableStateOf<String?>(null) }
  var showAdminDashboard by remember { mutableStateOf(false) }

  // Incoming call state
  val incomingCalls by repository.observeActiveCalls().collectAsState(initial = emptyList())
  val pendingIncomingCall = incomingCalls.firstOrNull { it.status == "ringing" }

  // Initialize or fetch user profile
  LaunchedEffect(currentUserId) {
    val authUser = Firebase.auth.currentUser
    val name = authUser?.displayName ?: "Gen Z User"
    val email = authUser?.email
    val photo = authUser?.photoUrl?.toString()
    val username = email?.substringBefore("@") ?: "genz_${currentUserId.take(5)}"

    val profile = repository.getOrCreateUserProfile(
      uid = currentUserId,
      username = username,
      displayName = name,
      email = email,
      photoUrl = photo
    )
    userProfile = profile
  }

  // BackHandler to handle custom subscreen pop navigation
  BackHandler(
    enabled = activeLiveRoom != null || activeChat != null || activeCallId != null || showAdminDashboard || currentDestination != NavDestination.FEED
  ) {
    when {
      activeCallId != null -> {
        // Ending call handled inside CallScreen
      }
      activeLiveRoom != null -> activeLiveRoom = null
      activeChat != null -> activeChat = null
      showAdminDashboard -> showAdminDashboard = false
      currentDestination != NavDestination.FEED -> currentDestination = NavDestination.FEED
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      topBar = {
        if (activeCallId == null && activeLiveRoom == null && activeChat == null && !showAdminDashboard) {
          AppTopBar(
            userAvatarUrl = userProfile?.avatarUrl,
            unreadNotificationCount = 2,
            onSearchClick = { currentDestination = NavDestination.DISCOVER },
            onNotificationClick = { currentDestination = NavDestination.FEED },
            onProfileClick = { currentDestination = NavDestination.PROFILE }
          )
        }
      },
      bottomBar = {
        if (activeCallId == null && activeLiveRoom == null && activeChat == null && !showAdminDashboard) {
          AppBottomBar(
            currentDestination = currentDestination,
            onNavigate = { currentDestination = it }
          )
        }
      },
      containerColor = DarkBackground,
      contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentDestination) {
          NavDestination.FEED -> FeedScreen(
            repository = repository,
            currentUserId = currentUserId
          )
          NavDestination.STAGES -> LiveRoomsScreen(
            repository = repository,
            currentUserId = currentUserId,
            onOpenRoom = { room -> activeLiveRoom = room }
          )
          NavDestination.DISCOVER -> DiscoverScreen(
            repository = repository,
            currentUserId = currentUserId
          )
          NavDestination.CHATS -> ChatsScreen(
            repository = repository,
            currentUserId = currentUserId,
            onOpenChat = { chat -> activeChat = chat }
          )
          NavDestination.PROFILE -> ProfileScreen(
            userProfile = userProfile,
            currentUserId = currentUserId,
            repository = repository,
            onOpenAdmin = { showAdminDashboard = true },
            onSignedOut = onSignedOut
          )
        }

        // Subscreen: Chat Conversation
        if (activeChat != null) {
          ChatConversationScreen(
            chat = activeChat!!,
            currentUserId = currentUserId,
            repository = repository,
            onBack = { activeChat = null },
            onStartCall = { callType ->
              val otherUid = activeChat!!.participantIds.firstOrNull { it != currentUserId } ?: "user"
              scope.launch {
                val callId = repository.initiateCall(otherUid, "Peer", callType)
                activeCallId = callId
              }
            }
          )
        }

        // Subscreen: Live Audio Stage
        if (activeLiveRoom != null) {
          LiveAudioRoomSheet(
            room = activeLiveRoom!!,
            currentUserId = currentUserId,
            repository = repository,
            onClose = { activeLiveRoom = null }
          )
        }

        // Subscreen: Admin Dashboard
        if (showAdminDashboard) {
          AdminDashboardScreen(
            repository = repository,
            onBack = { showAdminDashboard = false }
          )
        }

        // Subscreen: Active Audio/Video Call Screen
        if (activeCallId != null) {
          CallScreen(
            callId = activeCallId!!,
            currentUserId = currentUserId,
            repository = repository,
            onCallEnded = { activeCallId = null }
          )
        }
      }
    }

    // Incoming Call Banner Alert
    AnimatedVisibility(
      visible = pendingIncomingCall != null && activeCallId == null,
      enter = slideInVertically() + fadeIn(),
      exit = slideOutVertically() + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(16.dp)
    ) {
      if (pendingIncomingCall != null) {
        IncomingCallBanner(
          call = pendingIncomingCall,
          onAccept = {
            scope.launch {
              repository.updateCallStatus(pendingIncomingCall.id, "accepted")
              activeCallId = pendingIncomingCall.id
            }
          },
          onDecline = {
            scope.launch {
              repository.updateCallStatus(pendingIncomingCall.id, "rejected")
            }
          }
        )
      }
    }
  }
}

@Composable
fun IncomingCallBanner(
  call: CallSession,
  onAccept: () -> Unit,
  onDecline: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.5.dp, NeonCyan, RoundedCornerShape(20.dp))
      .testTag("incoming_call_banner")
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(NeonViolet),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Person, contentDescription = "Caller", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = call.callerName,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )
          Text(
            text = "Incoming ${call.callType} call...",
            fontSize = 12.sp,
            color = NeonCyan
          )
        }
      }

      Row {
        // Accept (Green)
        IconButton(
          onClick = onAccept,
          modifier = Modifier
            .size(42.dp)
            .background(NeonGreen, CircleShape)
            .testTag("accept_call_button")
        ) {
          Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.Black)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Decline (Red)
        IconButton(
          onClick = onDecline,
          modifier = Modifier
            .size(42.dp)
            .background(NeonPink, CircleShape)
            .testTag("decline_call_button")
        ) {
          Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
        }
      }
    }
  }
}
