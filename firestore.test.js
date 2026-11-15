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

function serverTimestamp() {
  return new Date(Date.now() - 2000);
}

function validExamPayload(uid) {
  return {
    userId: uid,
    title: "Penilaian Akhir Semester I",
    schoolName: "SD Negeri Nusantara 01",
    subject: "IPAS",
    gradeLevel: "Kelas 5 SD",
    semester: "Semester 1 (Ganjil)",
    academicYear: "2026/2027",
    durationMinutes: 90,
    instructions: "Kerjakan dengan teliti.",
    questionCount: 0,
    totalPoints: 0,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validQuestionPayload(uid, examId) {
  return {
    userId: uid,
    examId: examId,
    orderIndex: 0,
    type: "PILIHAN_GANDA",
    prompt: "Bagian tumbuhan yang berperan melepaskan uap air ke udara disebut...",
    options: ["Akar", "Batang", "Daun", "Biji"],
    correctIndices: [2],
    statements: [],
    statementAnswers: [],
    shortAnswerKey: "",
    essayRubric: "",
    explanation: "Tumbuhan melepaskan uap air melalui daun dalam proses transpirasi.",
    topicTag: "Bab 4: Air Sumber Kehidupan",
    points: 2,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

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

test("Unauthenticated user cannot list or write exams", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("exams").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1").set(validExamPayload(ALICE_UID))
  );
});

test("Owner can create, list, update, and delete exam and questions", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const examRef = aliceDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1");

  await assertSucceeds(examRef.set(validExamPayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("exams").get());

  const qRef = examRef.collection("questions").doc("q_1");
  await assertSucceeds(qRef.set(validQuestionPayload(ALICE_UID, "exam_1")));
  await assertSucceeds(
    qRef.update({
      prompt: "Di manakah proses fotosintesis dan transpirasi terjadi?",
      points: 3,
      updatedAt: serverTimestamp(),
    })
  );
  await assertSucceeds(examRef.update({ questionCount: 1, totalPoints: 3, updatedAt: serverTimestamp() }));
  await assertSucceeds(qRef.delete());
  await assertSucceeds(examRef.delete());
});

test("Cross-user access to exams and questions is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const examRef = aliceDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1");
  await assertSucceeds(examRef.set(validExamPayload(ALICE_UID)));
  await assertSucceeds(examRef.collection("questions").doc("q_1").set(validQuestionPayload(ALICE_UID, "exam_1")));

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("exams").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1").collection("questions").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1").update({
      title: "Hacked",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Shadow update with ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const examRef = aliceDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1");
  await assertFails(
    examRef.set({
      ...validExamPayload(ALICE_UID),
      isAdmin: true,
    })
  );
});

test("Invalid question type or orphan question is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  // Orphan question without parent exam
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("exams")
      .doc("missing_exam")
      .collection("questions")
      .doc("q_1")
      .set(validQuestionPayload(ALICE_UID, "missing_exam"))
  );

  // Create exam first
  const examRef = aliceDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1");
  await assertSucceeds(examRef.set(validExamPayload(ALICE_UID)));

  // Invalid question type
  await assertFails(
    examRef.collection("questions").doc("q_bad").set({
      ...validQuestionPayload(ALICE_UID, "exam_1"),
      type: "UNKNOWN_TYPE",
    })
  );
});

test("Immutable userId or createdAt tampering on update is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const examRef = aliceDb.collection("users").doc(ALICE_UID).collection("exams").doc("exam_1");
  await assertSucceeds(examRef.set(validExamPayload(ALICE_UID)));

  await assertFails(
    examRef.update({
      userId: BOB_UID,
      updatedAt: serverTimestamp(),
    })
  );
});
