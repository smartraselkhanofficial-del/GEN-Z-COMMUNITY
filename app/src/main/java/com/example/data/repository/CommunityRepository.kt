package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CallSession
import com.example.data.model.Chat
import com.example.data.model.ChatMessage
import com.example.data.model.Comment
import com.example.data.model.Community
import com.example.data.model.FriendRequest
import com.example.data.model.LiveRoom
import com.example.data.model.Moment
import com.example.data.model.Post
import com.example.data.model.Report
import com.example.data.model.RoomChat
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityRepository(val db: FirebaseFirestore) {

  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private val auth = Firebase.auth

  fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  // --- USER PROFILES ---

  suspend fun getOrCreateUserProfile(
    uid: String,
    username: String,
    displayName: String,
    email: String?,
    photoUrl: String?
  ): UserProfile {
    val docRef = db.collection("users").document(uid)
    val snap = docRef.get().await()
    if (snap.exists()) {
      return snap.toObject(UserProfile::class.java) ?: UserProfile(userId = uid, username = username, displayName = displayName)
    }

    val cleanUsername = username.filter { it.isLetterOrDigit() || it == '_' }
      .take(20).ifBlank { "user_${uid.take(6)}" }
    val cleanDisplayName = displayName.ifBlank { "Gen Z User" }.take(40)

    val payload = mutableMapOf<String, Any>(
      "userId" to uid,
      "username" to cleanUsername,
      "displayName" to cleanDisplayName,
      "role" to "user",
      "isOnline" to true,
      "friendsCount" to 0,
      "followersCount" to 0,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (!email.isNullOrBlank()) payload["email"] = email
    if (!photoUrl.isNullOrBlank()) payload["avatarUrl"] = photoUrl

    try {
      docRef.set(payload).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }

    return UserProfile(
      userId = uid,
      username = cleanUsername,
      displayName = cleanDisplayName,
      email = email,
      avatarUrl = photoUrl,
      role = "user",
      isOnline = true
    )
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
    val docRef = db.collection("users").document(userId)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        close(error)
        return@addSnapshotListener
      }
      trySend(snapshot?.toObject(UserProfile::class.java))
    }
    awaitClose { registration.remove() }
  }

  suspend fun updateUserProfile(
    userId: String,
    displayName: String,
    bio: String?,
    avatarUrl: String?
  ) {
    val uid = requireUserId()
    if (uid != userId) throw IllegalStateException("Cannot update another user's profile")
    val docRef = db.collection("users").document(uid)
    val updates = mutableMapOf<String, Any>(
      "displayName" to displayName.take(40),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (bio != null) updates["bio"] = bio.take(250)
    if (avatarUrl != null) updates["avatarUrl"] = avatarUrl.take(800)

    try {
      docRef.update(updates).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun searchUsers(queryText: String): List<UserProfile> {
    val query = queryText.trim()
    if (query.isBlank()) return emptyList()
    return try {
      val snap = db.collection("users")
        .limit(20)
        .get()
        .await()
      snap.toObjects(UserProfile::class.java).filter {
        it.username.contains(query, ignoreCase = true) ||
          it.displayName.contains(query, ignoreCase = true)
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, "users")
      emptyList()
    }
  }

  // --- POSTS ---

  fun observePosts(): Flow<List<Post>> = flow {
    emitAll(
      db.collection("posts")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Post::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "posts")
          throw error
        }
    )
  }

  suspend fun createPost(content: String, mediaUrl: String? = null, tag: String? = null): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val authorName = userDoc.getString("displayName") ?: "Gen Z Member"
    val authorAvatar = userDoc.getString("avatarUrl")

    val docRef = db.collection("posts").document()
    val payload = mutableMapOf<String, Any>(
      "authorId" to uid,
      "authorName" to authorName,
      "content" to content.take(1900),
      "likesCount" to 0,
      "commentsCount" to 0,
      "likedUserIds" to emptyList<String>(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (!authorAvatar.isNullOrBlank()) payload["authorAvatar"] = authorAvatar
    if (!mediaUrl.isNullOrBlank()) {
      payload["mediaUrl"] = mediaUrl
      payload["mediaType"] = "image"
    }
    if (!tag.isNullOrBlank()) payload["tag"] = tag.take(40)

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun toggleLikePost(postId: String): Boolean {
    val uid = requireUserId()
    val docRef = db.collection("posts").document(postId)
    val snap = docRef.get().await()
    val post = snap.toObject(Post::class.java) ?: return false

    val isLiked = post.likedUserIds.contains(uid)
    val newLikedList = if (isLiked) {
      post.likedUserIds - uid
    } else {
      (post.likedUserIds + uid).take(50)
    }
    val newCount = newLikedList.size

    val updates = mapOf(
      "likesCount" to newCount,
      "likedUserIds" to newLikedList,
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      docRef.update(updates).await()
      return !isLiked
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deletePost(postId: String) {
    val uid = requireUserId()
    val docRef = db.collection("posts").document(postId)
    val snap = docRef.get().await()
    if (snap.getString("authorId") != uid) {
      throw IllegalStateException("Only author can delete post")
    }
    try {
      docRef.delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  // --- COMMENTS ---

  fun observeComments(postId: String): Flow<List<Comment>> = flow {
    emitAll(
      db.collection("posts").document(postId).collection("comments")
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Comment::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "posts/$postId/comments")
          throw error
        }
    )
  }

  suspend fun addComment(postId: String, text: String): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val authorName = userDoc.getString("displayName") ?: "Gen Z Member"
    val authorAvatar = userDoc.getString("avatarUrl")

    val commentDoc = db.collection("posts").document(postId).collection("comments").document()
    val payload = mutableMapOf<String, Any>(
      "postId" to postId,
      "authorId" to uid,
      "authorName" to authorName,
      "text" to text.take(450),
      "createdAt" to FieldValue.serverTimestamp()
    )
    if (!authorAvatar.isNullOrBlank()) payload["authorAvatar"] = authorAvatar

    try {
      commentDoc.set(payload).await()
      // Increment comments count on post
      db.collection("posts").document(postId).update(
        mapOf(
          "commentsCount" to FieldValue.increment(1),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      return commentDoc.id
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, commentDoc.path)
      throw e
    }
  }

  // --- MOMENTS / STORIES ---

  fun observeMoments(): Flow<List<Moment>> = flow {
    emitAll(
      db.collection("moments")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(30)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Moment::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "moments")
          throw error
        }
    )
  }

  suspend fun createMoment(caption: String?, mediaUrl: String?, bgGradient: String?): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val authorName = userDoc.getString("displayName") ?: "Gen Z Member"
    val authorAvatar = userDoc.getString("avatarUrl")

    val docRef = db.collection("moments").document()
    val payload = mutableMapOf<String, Any>(
      "authorId" to uid,
      "authorName" to authorName,
      "viewsCount" to 1,
      "reactionsCount" to 0,
      "createdAt" to FieldValue.serverTimestamp()
    )
    if (!authorAvatar.isNullOrBlank()) payload["authorAvatar"] = authorAvatar
    if (!caption.isNullOrBlank()) payload["caption"] = caption.take(280)
    if (!mediaUrl.isNullOrBlank()) payload["mediaUrl"] = mediaUrl
    if (!bgGradient.isNullOrBlank()) payload["bgGradient"] = bgGradient

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  // --- LIVE AUDIO ROOMS ---

  fun observeLiveRooms(): Flow<List<LiveRoom>> = flow {
    emitAll(
      db.collection("liveRooms")
        .whereEqualTo("isActive", true)
        .limit(25)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(LiveRoom::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "liveRooms")
          throw error
        }
    )
  }

  fun observeLiveRoom(roomId: String): Flow<LiveRoom?> = callbackFlow {
    val docRef = db.collection("liveRooms").document(roomId)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        close(error)
        return@addSnapshotListener
      }
      trySend(snapshot?.toObject(LiveRoom::class.java))
    }
    awaitClose { registration.remove() }
  }

  suspend fun createLiveRoom(title: String, description: String?, category: String): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val hostName = userDoc.getString("displayName") ?: "Host"

    val docRef = db.collection("liveRooms").document()
    val payload = mutableMapOf<String, Any>(
      "hostId" to uid,
      "hostName" to hostName,
      "title" to title.take(80),
      "category" to category.take(40),
      "isActive" to true,
      "participantCount" to 1,
      "speakers" to listOf(uid),
      "listeners" to emptyList<String>(),
      "handRaisedUserIds" to emptyList<String>(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (!description.isNullOrBlank()) payload["description"] = description.take(280)

    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun joinLiveRoom(roomId: String) {
    val uid = requireUserId()
    val docRef = db.collection("liveRooms").document(roomId)
    val snap = docRef.get().await()
    val room = snap.toObject(LiveRoom::class.java) ?: return

    if (room.speakers.contains(uid)) return
    val newListeners = (room.listeners + uid).distinct().take(50)
    val newCount = (room.speakers + newListeners).distinct().size

    docRef.update(
      mapOf(
        "listeners" to newListeners,
        "participantCount" to newCount,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  suspend fun leaveLiveRoom(roomId: String) {
    val uid = requireUserId()
    val docRef = db.collection("liveRooms").document(roomId)
    val snap = docRef.get().await()
    val room = snap.toObject(LiveRoom::class.java) ?: return

    val newSpeakers = room.speakers - uid
    val newListeners = room.listeners - uid
    val newHands = room.handRaisedUserIds - uid
    val newCount = (newSpeakers + newListeners).size

    val updates = mutableMapOf<String, Any>(
      "speakers" to newSpeakers,
      "listeners" to newListeners,
      "handRaisedUserIds" to newHands,
      "participantCount" to newCount,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (room.hostId == uid) {
      updates["isActive"] = false
    }

    docRef.update(updates).await()
  }

  suspend fun raiseHandInRoom(roomId: String) {
    val uid = requireUserId()
    val docRef = db.collection("liveRooms").document(roomId)
    val snap = docRef.get().await()
    val room = snap.toObject(LiveRoom::class.java) ?: return
    if (room.speakers.contains(uid) || room.handRaisedUserIds.contains(uid)) return

    docRef.update(
      mapOf(
        "handRaisedUserIds" to (room.handRaisedUserIds + uid).take(50),
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  suspend fun acceptSpeaker(roomId: String, targetUid: String) {
    val uid = requireUserId()
    val docRef = db.collection("liveRooms").document(roomId)
    val snap = docRef.get().await()
    val room = snap.toObject(LiveRoom::class.java) ?: return
    if (room.hostId != uid) return

    val newSpeakers = (room.speakers + targetUid).distinct().take(20)
    val newListeners = room.listeners - targetUid
    val newHands = room.handRaisedUserIds - targetUid

    docRef.update(
      mapOf(
        "speakers" to newSpeakers,
        "listeners" to newListeners,
        "handRaisedUserIds" to newHands,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  // Live Room Chat
  fun observeRoomChats(roomId: String): Flow<List<RoomChat>> = flow {
    emitAll(
      db.collection("liveRooms").document(roomId).collection("roomChats")
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .limit(50)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(RoomChat::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "liveRooms/$roomId/roomChats")
          throw error
        }
    )
  }

  suspend fun sendRoomChat(roomId: String, text: String) {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val senderName = userDoc.getString("displayName") ?: "Listener"

    val chatRef = db.collection("liveRooms").document(roomId).collection("roomChats").document()
    val payload = mapOf(
      "roomId" to roomId,
      "senderId" to uid,
      "senderName" to senderName,
      "text" to text.take(250),
      "createdAt" to FieldValue.serverTimestamp()
    )
    chatRef.set(payload).await()
  }

  // --- CHATS & MESSAGES ---

  fun observeUserChats(): Flow<List<Chat>> = flow {
    val uid = requireUserId()
    emitAll(
      db.collection("chats")
        .whereArrayContains("participantIds", uid)
        .limit(30)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Chat::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "chats")
          throw error
        }
    )
  }

  suspend fun getOrCreateDirectChat(targetUid: String): String {
    val uid = requireUserId()
    if (uid == targetUid) throw IllegalArgumentException("Cannot chat with yourself")

    // Check existing
    val existingSnap = db.collection("chats")
      .whereArrayContains("participantIds", uid)
      .get()
      .await()

    val matched = existingSnap.toObjects(Chat::class.java).firstOrNull {
      !it.isGroup && it.participantIds.contains(targetUid)
    }
    if (matched != null && matched.id.isNotBlank()) {
      return matched.id
    }

    val newDoc = db.collection("chats").document()
    val payload = mapOf(
      "participantIds" to listOf(uid, targetUid),
      "isGroup" to false,
      "lastMessage" to "Started conversation",
      "lastSenderId" to uid,
      "lastMessageTime" to FieldValue.serverTimestamp(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    newDoc.set(payload).await()
    return newDoc.id
  }

  fun observeChatMessages(chatId: String): Flow<List<ChatMessage>> = flow {
    emitAll(
      db.collection("chats").document(chatId).collection("messages")
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .limit(100)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(ChatMessage::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "chats/$chatId/messages")
          throw error
        }
    )
  }

  suspend fun sendChatMessage(chatId: String, text: String, mediaUrl: String? = null) {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val senderName = userDoc.getString("displayName") ?: "User"

    val msgDoc = db.collection("chats").document(chatId).collection("messages").document()
    val payload = mutableMapOf<String, Any>(
      "chatId" to chatId,
      "senderId" to uid,
      "senderName" to senderName,
      "text" to text.take(900),
      "createdAt" to FieldValue.serverTimestamp()
    )
    if (!mediaUrl.isNullOrBlank()) {
      payload["mediaUrl"] = mediaUrl
      payload["mediaType"] = "image"
    }

    msgDoc.set(payload).await()

    // Update parent chat snippet
    db.collection("chats").document(chatId).update(
      mapOf(
        "lastMessage" to text.take(100),
        "lastSenderId" to uid,
        "lastMessageTime" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).await()
  }

  // --- CALL SESSIONS ---

  fun observeActiveCalls(): Flow<List<CallSession>> = flow {
    val uid = requireUserId()
    emitAll(
      db.collection("calls")
        .whereEqualTo("receiverId", uid)
        .whereEqualTo("status", "ringing")
        .limit(10)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(CallSession::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "calls")
          throw error
        }
    )
  }

  fun observeCall(callId: String): Flow<CallSession?> = callbackFlow {
    val docRef = db.collection("calls").document(callId)
    val registration = docRef.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, docRef.path)
        close(error)
        return@addSnapshotListener
      }
      trySend(snapshot?.toObject(CallSession::class.java))
    }
    awaitClose { registration.remove() }
  }

  suspend fun initiateCall(targetUid: String, targetName: String, callType: String): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val callerName = userDoc.getString("displayName") ?: "Caller"

    val docRef = db.collection("calls").document()
    val payload = mapOf(
      "callerId" to uid,
      "callerName" to callerName,
      "receiverId" to targetUid,
      "receiverName" to targetName.take(50),
      "callType" to callType,
      "status" to "ringing",
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    return docRef.id
  }

  suspend fun updateCallStatus(callId: String, status: String, sdpAnswer: String? = null) {
    val docRef = db.collection("calls").document(callId)
    val updates = mutableMapOf<String, Any>(
      "status" to status,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (sdpAnswer != null) updates["sdpAnswer"] = sdpAnswer
    docRef.update(updates).await()
  }

  // --- FRIEND SYSTEM ---

  fun observeFriendRequests(): Flow<List<FriendRequest>> = flow {
    val uid = requireUserId()
    emitAll(
      db.collection("friendRequests")
        .whereEqualTo("receiverId", uid)
        .whereEqualTo("status", "pending")
        .limit(30)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(FriendRequest::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "friendRequests")
          throw error
        }
    )
  }

  suspend fun sendFriendRequest(targetUid: String, targetName: String): String {
    val uid = requireUserId()
    val userDoc = db.collection("users").document(uid).get().await()
    val senderName = userDoc.getString("displayName") ?: "Friend"

    val docRef = db.collection("friendRequests").document()
    val payload = mapOf(
      "senderId" to uid,
      "senderName" to senderName,
      "receiverId" to targetUid,
      "receiverName" to targetName,
      "status" to "pending",
      "createdAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
    return docRef.id
  }

  suspend fun respondFriendRequest(requestId: String, accept: Boolean, senderUid: String) {
    val uid = requireUserId()
    val docRef = db.collection("friendRequests").document(requestId)
    val status = if (accept) "accepted" else "rejected"
    docRef.update("status", status).await()

    if (accept) {
      // Increment friendsCount on both users
      db.collection("users").document(uid).update("friendsCount", FieldValue.increment(1)).await()
      db.collection("users").document(senderUid).update("friendsCount", FieldValue.increment(1)).await()
    }
  }

  // --- COMMUNITIES ---

  fun observeCommunities(): Flow<List<Community>> = flow {
    emitAll(
      db.collection("communities")
        .orderBy("membersCount", Query.Direction.DESCENDING)
        .limit(30)
        .snapshots()
        .map { snapshot -> snapshot.toObjects(Community::class.java) }
        .catch { error ->
          handleFirestoreError(error, OperationType.LIST, "communities")
          throw error
        }
    )
  }

  suspend fun createCommunity(name: String, description: String?, category: String): String {
    val uid = requireUserId()
    val docRef = db.collection("communities").document()
    val payload = mutableMapOf<String, Any>(
      "creatorId" to uid,
      "name" to name.take(50),
      "category" to category.take(30),
      "membersCount" to 1,
      "memberIds" to listOf(uid),
      "createdAt" to FieldValue.serverTimestamp()
    )
    if (!description.isNullOrBlank()) payload["description"] = description.take(250)
    docRef.set(payload).await()
    return docRef.id
  }

  suspend fun joinCommunity(communityId: String) {
    val uid = requireUserId()
    val docRef = db.collection("communities").document(communityId)
    val snap = docRef.get().await()
    val comm = snap.toObject(Community::class.java) ?: return
    if (comm.memberIds.contains(uid)) return

    val newMembers = (comm.memberIds + uid).take(100)
    docRef.update(
      mapOf(
        "memberIds" to newMembers,
        "membersCount" to newMembers.size
      )
    ).await()
  }

  // --- REPORTS & ADMIN ---

  suspend fun createReport(targetId: String, targetType: String, reason: String) {
    val uid = requireUserId()
    val docRef = db.collection("reports").document()
    val payload = mapOf(
      "reporterId" to uid,
      "targetId" to targetId,
      "targetType" to targetType,
      "reason" to reason.take(400),
      "status" to "pending",
      "createdAt" to FieldValue.serverTimestamp()
    )
    docRef.set(payload).await()
  }

  suspend fun getAdminMetrics(): Map<String, Long> {
    return try {
      val usersCount = db.collection("users").get().await().size().toLong()
      val postsCount = db.collection("posts").get().await().size().toLong()
      val roomsCount = db.collection("liveRooms").get().await().size().toLong()
      val communitiesCount = db.collection("communities").get().await().size().toLong()
      mapOf(
        "users" to usersCount,
        "posts" to postsCount,
        "rooms" to roomsCount,
        "communities" to communitiesCount
      )
    } catch (e: Exception) {
      mapOf("users" to 0L, "posts" to 0L, "rooms" to 0L, "communities" to 0L)
    }
  }
}
