package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.Comment
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
  postId: String,
  comments: List<Comment>,
  onDismiss: () -> Unit,
  onSendComment: (String) -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var textInput by remember { mutableStateOf("") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DarkSurface,
    contentColor = TextPrimary
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.7f)
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("comments_bottom_sheet")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Comments (${comments.size})",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
      ) {
        if (comments.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No comments yet. Start the conversation!",
                color = TextMuted,
                fontSize = 14.sp
              )
            }
          }
        } else {
          items(comments) { comment ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = comment.authorName.take(1).uppercase(),
                  color = NeonCyan,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = comment.authorName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = NeonViolet
                )
                Text(
                  text = comment.text,
                  fontSize = 14.sp,
                  color = TextPrimary,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Comment input field
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = textInput,
          onValueChange = { textInput = it },
          placeholder = { Text("Add a comment...", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceElevated,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("comment_input_field")
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (textInput.isNotBlank()) {
              onSendComment(textInput.trim())
              textInput = ""
            }
          },
          modifier = Modifier
            .size(46.dp)
            .background(NeonViolet, CircleShape)
            .testTag("send_comment_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Comment",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun CreatePostDialog(
  onDismiss: () -> Unit,
  onPublish: (content: String, mediaUrl: String?, tag: String?) -> Unit
) {
  var content by remember { mutableStateOf("") }
  var mediaUrl by remember { mutableStateOf("") }
  var selectedTag by remember { mutableStateOf("#General") }
  val tags = listOf("#General", "#Cyber", "#Tech", "#Vibes", "#Music", "#Gaming", "#Debate")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonViolet.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        .testTag("create_post_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Create Post",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          placeholder = { Text("What's on your mind? Share thoughts, links, questions...", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonViolet,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          minLines = 4,
          maxLines = 6,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_post_content_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Optional Media URL
        OutlinedTextField(
          value = mediaUrl,
          onValueChange = { mediaUrl = it },
          placeholder = { Text("Image URL (optional)", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_post_media_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tags picker
        Text(text = "Choose Topic Tag", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow {
          items(tags) { tag ->
            val isSelected = tag == selectedTag
            Box(
              modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) NeonViolet else DarkSurfaceVariant)
                .clickable { selectedTag = tag }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = tag,
                color = if (isSelected) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (content.isNotBlank()) {
              onPublish(content.trim(), mediaUrl.trim().ifBlank { null }, selectedTag)
              onDismiss()
            }
          },
          enabled = content.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonViolet,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("publish_post_button")
        ) {
          Text("Publish Post", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }
  }
}

@Composable
fun CreateMomentDialog(
  onDismiss: () -> Unit,
  onPublish: (caption: String?, mediaUrl: String?, gradient: String) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var mediaUrl by remember { mutableStateOf("") }
  var selectedGradient by remember { mutableStateOf("neon_violet") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonPink.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        .testTag("create_moment_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Share Moment (24h)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = caption,
          onValueChange = { caption = it },
          placeholder = { Text("What's happening right now?", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonPink,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          minLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_moment_caption_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = mediaUrl,
          onValueChange = { mediaUrl = it },
          placeholder = { Text("Photo URL (optional)", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            onPublish(caption.trim().ifBlank { null }, mediaUrl.trim().ifBlank { null }, selectedGradient)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonPink,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("publish_moment_button")
        ) {
          Text("Post Moment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }
  }
}

@Composable
fun CreateLiveRoomDialog(
  onDismiss: () -> Unit,
  onCreate: (title: String, description: String?, category: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Chill") }
  val categories = listOf("Chill", "Music", "Tech", "Gaming", "Debate", "Podcasting")

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurface),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
        .testTag("create_live_room_dialog")
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Go Live (Audio Stage)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("Stage Title (e.g. Late Night Beats)", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_room_title_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          placeholder = { Text("Description / Rules for listeners", color = TextMuted) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonViolet,
            unfocusedBorderColor = DarkSurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          minLines = 2,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Category", fontSize = 12.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow {
          items(categories) { cat ->
            val isSelected = cat == selectedCategory
            Box(
              modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) NeonCyan else DarkSurfaceVariant)
                .clickable { selectedCategory = cat }
                .padding(horizontal = 10.dp, vertical = 6.dp)
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

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            if (title.isNotBlank()) {
              onCreate(title.trim(), description.trim().ifBlank { null }, selectedCategory)
              onDismiss()
            }
          },
          enabled = title.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan,
            contentColor = Color.Black
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("start_stage_button")
        ) {
          Text("Launch Live Stage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }
  }
}
