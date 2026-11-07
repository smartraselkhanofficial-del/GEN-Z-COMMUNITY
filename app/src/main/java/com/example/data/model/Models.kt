package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val username: String = "",
  val displayName: String = "",
  val email: String? = null,
  val avatarUrl: String? = null,
  val bio: String? = null,
  val role: String = "user",
  val isOnline: Boolean = true,
  val friendsCount: Int = 0,
  val followersCount: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Post(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorAvatar: String? = null,
  val content: String = "",
  val mediaUrl: String? = null,
  val mediaType: String? = null,
  val tag: String? = null,
  val likesCount: Int = 0,
  val commentsCount: Int = 0,
  val likedUserIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comment(
  @DocumentId val id: String = "",
  val postId: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorAvatar: String? = null,
  val text: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Moment(
  @DocumentId val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val authorAvatar: String? = null,
  val caption: String? = null,
  val mediaUrl: String? = null,
  val bgGradient: String? = null,
  val viewsCount: Int = 0,
  val reactionsCount: Int = 0,
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LiveRoom(
  @DocumentId val id: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val title: String = "",
  val description: String? = null,
  val category: String = "Chill",
  val isActive: Boolean = true,
  val participantCount: Int = 1,
  val speakers: List<String> = emptyList(),
  val listeners: List<String> = emptyList(),
  val handRaisedUserIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RoomChat(
  @DocumentId val id: String = "",
  val roomId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Chat(
  @DocumentId val id: String = "",
  val participantIds: List<String> = emptyList(),
  val isGroup: Boolean = false,
  val groupName: String? = null,
  val lastMessage: String? = null,
  val lastSenderId: String? = null,
  @ServerTimestamp val lastMessageTime: Timestamp? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val chatId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  val mediaUrl: String? = null,
  val mediaType: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CallSession(
  @DocumentId val id: String = "",
  val callerId: String = "",
  val callerName: String = "",
  val receiverId: String = "",
  val receiverName: String? = null,
  val callType: String = "audio",
  val status: String = "ringing",
  val sdpOffer: String? = null,
  val sdpAnswer: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FriendRequest(
  @DocumentId val id: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val receiverId: String = "",
  val receiverName: String? = null,
  val status: String = "pending",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Community(
  @DocumentId val id: String = "",
  val creatorId: String = "",
  val name: String = "",
  val description: String? = null,
  val category: String = "General",
  val membersCount: Int = 1,
  val memberIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Report(
  @DocumentId val id: String = "",
  val reporterId: String = "",
  val targetId: String = "",
  val targetType: String = "post",
  val reason: String = "",
  val status: String = "pending",
  @ServerTimestamp val createdAt: Timestamp? = null
)
