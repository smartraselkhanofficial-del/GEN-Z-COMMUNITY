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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

data class MockReport(
  val id: String,
  val type: String,
  val target: String,
  val reason: String,
  var resolved: Boolean = false
)

@Composable
fun AdminDashboardScreen(
  repository: CommunityRepository,
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var metrics by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
  var reports by remember {
    mutableStateOf(
      listOf(
        MockReport("rep_1", "post", "Post #892", "Suspicious spam link reported"),
        MockReport("rep_2", "room", "Room #ChillVibes", "Inappropriate language in stage"),
        MockReport("rep_3", "user", "User @spammer_09", "Harassment in direct message")
      )
    )
  }

  LaunchedEffect(Unit) {
    metrics = repository.getAdminMetrics()
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(16.dp)
      .testTag("admin_dashboard_screen")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Icon(Icons.Default.Shield, contentDescription = "Admin", tint = NeonPink, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "GEN Z ADMIN PORTAL",
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
        Text(
          text = "Security Rules & Role Protected",
          fontSize = 11.sp,
          color = NeonGreen
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Real Platform Metrics Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        AdminStatCard(title = "Registered Users", count = metrics["users"]?.toString() ?: "Loading...", color = NeonCyan)
      }
      item {
        AdminStatCard(title = "Total Posts", count = metrics["posts"]?.toString() ?: "Loading...", color = NeonViolet)
      }
      item {
        AdminStatCard(title = "Live Stages", count = metrics["rooms"]?.toString() ?: "Loading...", color = NeonGreen)
      }
      item {
        AdminStatCard(title = "Communities", count = metrics["communities"]?.toString() ?: "Loading...", color = NeonPink)
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "MODERATION & REPORTS QUEUE",
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = NeonPink,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier.weight(1f)
    ) {
      items(reports) { rep ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, if (rep.resolved) NeonGreen.copy(alpha = 0.4f) else DarkSurfaceVariant, RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonPink.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(text = rep.type.uppercase(), color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = rep.target, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = rep.reason, color = TextMuted, fontSize = 12.sp)
            }

            if (!rep.resolved) {
              Button(
                onClick = {
                  reports = reports.map { if (it.id == rep.id) it.copy(resolved = true) else it }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Done, contentDescription = "Resolve", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Resolve", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
              }
            } else {
              Text(text = "Resolved", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminStatCard(title: String, count: String, color: Color) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = Modifier.fillMaxWidth().border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(text = count, fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = title, fontSize = 12.sp, color = TextMuted)
    }
  }
}
