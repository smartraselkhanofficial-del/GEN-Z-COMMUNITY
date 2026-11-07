package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Moment
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary

@Composable
fun MomentStoryBar(
  moments: List<Moment>,
  onAddStoryClick: () -> Unit,
  onMomentClick: (Moment) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("moments_story_bar")
  ) {
    // Add Your Story Item
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(end = 14.dp)
          .clickable { onAddStoryClick() }
          .testTag("add_story_button")
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .border(2.dp, NeonViolet, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Moment",
            tint = NeonCyan,
            modifier = Modifier.size(28.dp)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Your Story",
          fontSize = 11.sp,
          color = TextPrimary,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Moments list
    items(moments) { moment ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(end = 14.dp)
          .clickable { onMomentClick(moment) }
          .testTag("moment_item_${moment.id}")
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(
              Brush.sweepGradient(
                listOf(NeonPink, NeonViolet, NeonCyan, NeonPink)
              )
            )
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(DarkSurface),
          contentAlignment = Alignment.Center
        ) {
          if (!moment.authorAvatar.isNullOrBlank()) {
            AsyncImage(
              model = moment.authorAvatar,
              contentDescription = moment.authorName,
              modifier = Modifier.size(58.dp),
              contentScale = ContentScale.Crop
            )
          } else {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = moment.authorName,
              tint = NeonCyan,
              modifier = Modifier.size(30.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = moment.authorName.take(9),
          fontSize = 11.sp,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
