package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.Moment
import com.example.data.model.Post
import com.example.data.repository.CommunityRepository
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.CreateMomentDialog
import com.example.ui.components.CreatePostDialog
import com.example.ui.components.MomentStoryBar
import com.example.ui.components.PostCard
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
fun FeedScreen(
  repository: CommunityRepository,
  currentUserId: String,
  modifier: Modifier = Modifier
) {
  val scope = rememberCoroutineScope()
  val posts by repository.observePosts().collectAsState(initial = emptyList())
  val moments by repository.observeMoments().collectAsState(initial = emptyList())

  var showCreatePostDialog by remember { mutableStateOf(false) }
  var showCreateMomentDialog by remember { mutableStateOf(false) }
  var selectedPostForComments by remember { mutableStateOf<String?>(null) }
  var selectedMomentForViewing by remember { mutableStateOf<Moment?>(null) }
  var selectedCategory by remember { mutableStateOf("All") }
  val categories = listOf("All", "#Tech", "#Vibes", "#Music", "#Gaming", "#Cyber", "#Debate")

  val comments by if (selectedPostForComments != null) {
    repository.observeComments(selectedPostForComments!!).collectAsState(initial = emptyList())
  } else {
    remember { mutableStateOf<List<Comment>>(emptyList()) }
  }

  val filteredPosts = if (selectedCategory == "All") {
    posts
  } else {
    posts.filter { it.tag.equals(selectedCategory, ignoreCase = true) }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .testTag("feed_screen")
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize()
    ) {
      // Stories / Moments Bar
      item {
        MomentStoryBar(
          moments = moments,
          onAddStoryClick = { showCreateMomentDialog = true },
          onMomentClick = { selectedMomentForViewing = it }
        )
      }

      // Quick Post trigger composer box
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
            .clickable { showCreatePostDialog = true }
            .testTag("quick_create_post_card")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(NeonViolet.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Create",
                tint = NeonViolet,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "What's on your mind? Share with Gen Z...",
              color = TextMuted,
              fontSize = 14.sp
            )
          }
        }
      }

      // Filter tags row
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
          items(categories) { cat ->
            val isSelected = cat == selectedCategory
            Box(
              modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) NeonViolet else DarkSurfaceVariant)
                .clickable { selectedCategory = cat }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Posts list
      if (filteredPosts.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(48.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No posts found in $selectedCategory. Be the first to post!",
              color = TextMuted,
              fontSize = 14.sp
            )
          }
        }
      } else {
        items(filteredPosts, key = { it.id }) { post ->
          PostCard(
            post = post,
            currentUserId = currentUserId,
            onLikeClick = {
              scope.launch {
                repository.toggleLikePost(post.id)
              }
            },
            onCommentClick = { selectedPostForComments = post.id },
            onReportClick = {
              scope.launch {
                repository.createReport(post.id, "post", "Inappropriate content reported by user")
              }
            },
            onDeleteClick = {
              scope.launch {
                repository.deletePost(post.id)
              }
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }

    // Create Post Dialog
    if (showCreatePostDialog) {
      CreatePostDialog(
        onDismiss = { showCreatePostDialog = false },
        onPublish = { content, mediaUrl, tag ->
          scope.launch {
            repository.createPost(content, mediaUrl, tag)
          }
        }
      )
    }

    // Create Moment Dialog
    if (showCreateMomentDialog) {
      CreateMomentDialog(
        onDismiss = { showCreateMomentDialog = false },
        onPublish = { caption, mediaUrl, gradient ->
          scope.launch {
            repository.createMoment(caption, mediaUrl, gradient)
          }
        }
      )
    }

    // Comments Bottom Sheet
    if (selectedPostForComments != null) {
      CommentsBottomSheet(
        postId = selectedPostForComments!!,
        comments = comments,
        onDismiss = { selectedPostForComments = null },
        onSendComment = { text ->
          scope.launch {
            repository.addComment(selectedPostForComments!!, text)
          }
        }
      )
    }

    // Moment Story Viewer Dialog
    if (selectedMomentForViewing != null) {
      val moment = selectedMomentForViewing!!
      Dialog(onDismissRequest = { selectedMomentForViewing = null }) {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, NeonPink, RoundedCornerShape(24.dp))
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NeonViolet),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = moment.authorName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(text = moment.authorName, fontWeight = FontWeight.Bold, color = TextPrimary)
                  Text(text = "24h Moment", fontSize = 11.sp, color = NeonPink)
                }
              }
              IconButton(onClick = { selectedMomentForViewing = null }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (!moment.mediaUrl.isNullOrBlank()) {
              AsyncImage(
                model = moment.mediaUrl,
                contentDescription = "Moment image",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(240.dp)
                  .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.height(14.dp))
            }

            if (!moment.caption.isNullOrBlank()) {
              Text(
                text = moment.caption,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 8.dp)
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = { selectedMomentForViewing = null },
              colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("React to Moment")
              }
            }
          }
        }
      }
    }
  }
}
