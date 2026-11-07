package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class NavDestination(val label: String) {
  FEED("Feed"),
  STAGES("Live Stages"),
  DISCOVER("Discover"),
  CHATS("Chats"),
  PROFILE("Profile")
}

@Composable
fun AppTopBar(
  userAvatarUrl: String?,
  unreadNotificationCount: Int = 2,
  onSearchClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onProfileClick: () -> Unit = {}
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(DarkBackground)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Logo with neon gradient
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(
            Brush.linearGradient(
              listOf(NeonViolet, NeonPink, NeonCyan)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Z",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 20.sp
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "GEN Z",
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = TextPrimary,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.weight(1f))

    // Search Icon
    IconButton(
      onClick = onSearchClick,
      modifier = Modifier
        .size(42.dp)
        .testTag("top_bar_search_button")
    ) {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Search",
        tint = TextPrimary
      )
    }

    // Notifications Badge Icon
    IconButton(
      onClick = onNotificationClick,
      modifier = Modifier
        .size(42.dp)
        .testTag("top_bar_notification_button")
    ) {
      BadgedBox(
        badge = {
          if (unreadNotificationCount > 0) {
            Badge(
              containerColor = NeonPink,
              contentColor = Color.White
            ) {
              Text(
                text = unreadNotificationCount.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Notifications",
          tint = TextPrimary
        )
      }
    }

    Spacer(modifier = Modifier.width(4.dp))

    // User Avatar
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .border(1.5.dp, Brush.horizontalGradient(listOf(NeonViolet, NeonCyan)), CircleShape)
        .clickable { onProfileClick() }
        .testTag("top_bar_profile_avatar"),
      contentAlignment = Alignment.Center
    ) {
      if (!userAvatarUrl.isNullOrBlank()) {
        AsyncImage(
          model = userAvatarUrl,
          contentDescription = "User Avatar",
          modifier = Modifier.size(36.dp),
          contentScale = ContentScale.Crop
        )
      } else {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User Avatar",
          tint = NeonCyan,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}

@Composable
fun AppBottomBar(
  currentDestination: NavDestination,
  onNavigate: (NavDestination) -> Unit
) {
  NavigationBar(
    containerColor = DarkSurface,
    contentColor = TextPrimary,
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("main_navigation_bar")
  ) {
    NavDestination.values().forEach { destination ->
      val selected = destination == currentDestination
      val icon = when (destination) {
        NavDestination.FEED -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
        NavDestination.STAGES -> if (selected) Icons.Filled.Mic else Icons.Outlined.Mic
        NavDestination.DISCOVER -> if (selected) Icons.Filled.Explore else Icons.Outlined.Explore
        NavDestination.CHATS -> if (selected) Icons.Filled.Chat else Icons.Outlined.Chat
        NavDestination.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
      }

      NavigationBarItem(
        selected = selected,
        onClick = { onNavigate(destination) },
        icon = {
          Icon(
            imageVector = icon,
            contentDescription = destination.label,
            modifier = Modifier.size(24.dp)
          )
        },
        label = {
          Text(
            text = destination.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = Color.White,
          selectedTextColor = NeonCyan,
          indicatorColor = NeonViolet.copy(alpha = 0.35f),
          unselectedIconColor = TextSecondary,
          unselectedTextColor = TextSecondary
        ),
        modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
      )
    }
  }
}
