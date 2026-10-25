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
const CHARLIE_UID = "charlie_789";

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

test("Unauthenticated access is denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("emergency_alerts").get());
});

test("Alice can create her own emergency alert with authorized contacts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const alertDoc = aliceDb.collection("emergency_alerts").doc("alert_1");

  await assertSucceeds(
    alertDoc.set({
      alertId: "alert_1",
      senderId: ALICE_UID,
      senderName: "Alice Smith",
      senderPhone: "+91 98450 11111",
      status: "ACTIVE",
      latitude: 12.9716,
      longitude: 77.5946,
      accuracy: 10.0,
      address: "Bangalore, India",
      triggerMethod: "MANUAL_SOS",
      authorizedUids: [BOB_UID],
      acknowledgments: {},
      deliveryStatuses: {},
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authorized contact Bob can read Alice's emergency alert and acknowledge it", async () => {
  // Pre-seed alert as Alice
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const alertDoc = aliceDb.collection("emergency_alerts").doc("alert_2");
  const createdAt = new Date();

  await alertDoc.set({
    alertId: "alert_2",
    senderId: ALICE_UID,
    senderName: "Alice Smith",
    status: "ACTIVE",
    latitude: 12.9716,
    longitude: 77.5946,
    triggerMethod: "MANUAL_SOS",
    authorizedUids: [BOB_UID],
    acknowledgments: {},
    createdAt: createdAt,
    updatedAt: createdAt,
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const bobDoc = bobDb.collection("emergency_alerts").doc("alert_2");

  // Bob can read
  await assertSucceeds(bobDoc.get());

  // Bob can post an acknowledgment
  await assertSucceeds(
    bobDoc.update({
      acknowledgments: {
        [BOB_UID]: {
          name: "Bob",
          message: "I am calling you now",
          timestamp: new Date(),
        },
      },
      updatedAt: new Date(),
    })
  );
});

test("Unauthorized Charlie cannot read or modify Alice's emergency alert", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const alertDoc = aliceDb.collection("emergency_alerts").doc("alert_3");
  const createdAt = new Date();

  await alertDoc.set({
    alertId: "alert_3",
    senderId: ALICE_UID,
    senderName: "Alice Smith",
    status: "ACTIVE",
    latitude: 12.9716,
    longitude: 77.5946,
    triggerMethod: "MANUAL_SOS",
    authorizedUids: [BOB_UID],
    createdAt: createdAt,
  });

  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const charlieDoc = charlieDb.collection("emergency_alerts").doc("alert_3");

  // Read denied
  await assertFails(charlieDoc.get());

  // Update denied
  await assertFails(
    charlieDoc.update({
      status: "RESOLVED",
    })
  );
});
