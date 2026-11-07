package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PostCard(
  post: Post,
  currentUserId: String,
  onLikeClick: () -> Unit,
  onCommentClick: () -> Unit,
  onReportClick: () -> Unit,
  onDeleteClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isLiked = post.likedUserIds.contains(currentUserId)
  var menuExpanded by remember { mutableStateOf(false) }

  val heartTint by animateColorAsState(
    targetValue = if (isLiked) NeonPink else TextSecondary,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "heartTint"
  )

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(20.dp))
      .testTag("post_card_${post.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Avatar, Name, Handle, Menu
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(DarkSurfaceVariant)
            .border(1.dp, NeonViolet.copy(alpha = 0.5f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          if (!post.authorAvatar.isNullOrBlank()) {
            AsyncImage(
              model = post.authorAvatar,
              contentDescription = post.authorName,
              modifier = Modifier.size(44.dp),
              contentScale = ContentScale.Crop
            )
          } else {
            Text(
              text = post.authorName.take(1).uppercase(),
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = post.authorName,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )
          Text(
            text = "Gen Z Member",
            fontSize = 12.sp,
            color = TextMuted
          )
        }

        // More menu
        Box {
          IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(32.dp).testTag("post_menu_button_${post.id}")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Post Options",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text("Report Post") },
              onClick = {
                menuExpanded = false
                onReportClick()
              }
            )
            if (post.authorId == currentUserId && onDeleteClick != null) {
              DropdownMenuItem(
                text = { Text("Delete Post", color = NeonPink) },
                onClick = {
                  menuExpanded = false
                  onDeleteClick()
                }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Post text content
      Text(
        text = post.content,
        fontSize = 15.sp,
        color = TextPrimary,
        lineHeight = 22.sp
      )

      // Tag pill
      if (!post.tag.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NeonViolet.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = post.tag,
            color = NeonViolet,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      // Media preview
      if (!post.mediaUrl.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        AsyncImage(
          model = post.mediaUrl,
          contentDescription = "Post Media",
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp)),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action row: Like, Comment, Share
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Like Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { onLikeClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .testTag("post_like_button_${post.id}")
        ) {
          Icon(
            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = heartTint,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = post.likesCount.toString(),
            fontSize = 13.sp,
            color = if (isLiked) NeonPink else TextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Comment Button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { onCommentClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .testTag("post_comment_button_${post.id}")
        ) {
          Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = "Comments",
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = post.commentsCount.toString(),
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Share button
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Share",
            fontSize = 13.sp,
            color = TextSecondary
          )
        }
      }
    }
  }
}
