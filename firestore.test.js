const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. Unauthenticated checks
test("Unauthenticated user: cannot read posts or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

// 2. User profiles
test("Authenticated user: can create their own profile, but cannot tamper with other profiles", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    username: "alice_vibes",
    displayName: "Alice In Tech",
    createdAt: new Date(),
    isOnline: true,
    friendsCount: 0,
    followersCount: 0
  }));

  // Alice cannot overwrite Bob's profile
  await assertFails(aliceDb.collection("users").doc(BOB_UID).set({
    userId: BOB_UID,
    username: "bob_hacked",
    displayName: "Hacked",
    createdAt: new Date()
  }));
});

// 3. Posts
test("Authenticated user: can create a post and Bob can read it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const postRef = aliceDb.collection("posts").doc("post_1");
  await assertSucceeds(postRef.set({
    authorId: ALICE_UID,
    authorName: "Alice In Tech",
    content: "Welcome to GEN Z COMMUNITY!",
    likesCount: 0,
    commentsCount: 0,
    likedUserIds: [],
    createdAt: new Date()
  }));

  // Bob can read
  await assertSucceeds(bobDb.collection("posts").doc("post_1").get());

  // Bob can like the post (updating likesCount and likedUserIds)
  await assertSucceeds(bobDb.collection("posts").doc("post_1").update({
    likesCount: 1,
    likedUserIds: [BOB_UID],
    commentsCount: 0
  }));

  // Bob cannot change the post's content
  await assertFails(bobDb.collection("posts").doc("post_1").update({
    content: "Tampered content"
  }));
});

// 4. Chat Privacy
test("Chat isolation: Non-participant cannot read private chats", async () => {
  // Create chat between Alice and Charlie
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("chats").doc("chat_ac").set({
      participantIds: [ALICE_UID, "charlie_789"],
      isGroup: false,
      createdAt: new Date()
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("chats").doc("chat_ac").get());
  await assertFails(bobDb.collection("chats").doc("chat_ac").get());
});

// 5. Live Audio Rooms
test("Live Audio Rooms: Host can create room, participants can join", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const roomRef = aliceDb.collection("liveRooms").doc("room_chill");
  await assertSucceeds(roomRef.set({
    hostId: ALICE_UID,
    hostName: "Alice",
    title: "Late Night Chill Beats",
    category: "Music",
    isActive: true,
    participantCount: 1,
    speakers: [ALICE_UID],
    listeners: [],
    handRaisedUserIds: [],
    createdAt: new Date()
  }));

  // Bob can join as listener
  await assertSucceeds(bobDb.collection("liveRooms").doc("room_chill").update({
    participantCount: 2,
    speakers: [ALICE_UID],
    listeners: [BOB_UID],
    handRaisedUserIds: [],
    isActive: true
  }));
});
