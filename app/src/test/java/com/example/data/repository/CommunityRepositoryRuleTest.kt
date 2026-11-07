package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CommunityRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createPost_validPayload_createsDocumentAndReturnsId() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = CommunityRepository(firestore)

    val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createPost(content = "Gen Z rules the digital world!", tag = "#Tech")
    }
    assertTrue("Post ID should not be empty", postId.isNotEmpty())
  }

  @Test
  fun observePosts_authenticatedUser_emitsPosts() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = CommunityRepository(firestore)

    val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createPost(content = "Vibes are immaculate today!")
    }

    val posts = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observePosts().first { list -> list.any { it.id == postId } }
    }
    assertTrue("Should emit created post", posts.any { it.id == postId })
  }

  @Test
  fun toggleLikePost_authenticatedUser_updatesLike() = runBlocking {
    signInTestUser(ALICE_EMAIL)
    val repository = CommunityRepository(firestore)
    val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.createPost(content = "Hit like if you love music!")
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = CommunityRepository(firestore)
    val liked = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.toggleLikePost(postId)
    }
    assertTrue("Bob should be able to like post", liked)
  }

  @Test
  fun observePosts_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = CommunityRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observePosts().first()
      }
      fail("Expected exception when unauthenticated")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      // Either direct IllegalStateException from requireUserId or Firestore PERMISSION_DENIED
      assertTrue(e is IllegalStateException || firestoreException?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@test.com"
    const val BOB_EMAIL = "bob@test.com"
    const val DEFAULT_TIMEOUT_MS = 8000L
    const val FLOW_TIMEOUT_MS = 6000L
  }
}
