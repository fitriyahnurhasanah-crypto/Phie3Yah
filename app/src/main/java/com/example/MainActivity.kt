package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.firebase.ExamRepository
import com.example.data.firebase.configureAppCheck
import com.example.model.CurriculumChapter
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.CurriculumBankScreen
import com.example.ui.screens.ExamWorkspaceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.NaskahSoalTheme
import com.example.ui.viewmodel.AiGeneratorViewModel
import com.example.ui.viewmodel.ExamListViewModel
import com.example.ui.viewmodel.ExamWorkspaceViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            NaskahSoalTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedNavHost(
                currentUserId = user.uid,
                userEmail = user.email
            )
        }
    }
}

private const val ROUTE_HOME = "home"
private const val ROUTE_CURRICULUM = "curriculum"
private const val ROUTE_WORKSPACE = "workspace/{examId}"
private const val ROUTE_AI_GEN = "ai_gen/{examId}?subject={subject}&grade={grade}&chapterId={chapterId}"

@Composable
private fun AuthenticatedNavHost(
    currentUserId: String,
    userEmail: String?
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val listViewModel: ExamListViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ExamListViewModel(ExamRepository(db), currentUserId)
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME
    ) {
        composable(ROUTE_HOME) {
            HomeScreen(
                viewModel = listViewModel,
                userEmail = userEmail,
                onOpenExam = { examId ->
                    navController.navigate("workspace/$examId")
                },
                onOpenCurriculumBank = {
                    navController.navigate(ROUTE_CURRICULUM)
                },
                onSignOut = {
                    signOut(credentialManager, scope)
                }
            )
        }

        composable(ROUTE_CURRICULUM) {
            CurriculumBankScreen(
                onBack = { navController.popBackStack() },
                onCreateExamFromChapter = { chapter: CurriculumChapter ->
                    listViewModel.createExam(
                        title = "UAS ${chapter.subject} (${chapter.chapterLabel})",
                        schoolName = "SD Negeri Nusantara",
                        subject = chapter.subject,
                        gradeLevel = "Kelas 5 SD",
                        semester = "Semester 1 (Ganjil)",
                        academicYear = "2026/2027",
                        durationMinutes = 90L,
                        instructions = "Kerjakan soal dengan teliti sesuai petunjuk masing-masing bagian.",
                        withStarterTemplate = false,
                        onCreated = { newExamId ->
                            val encSubj = URLEncoder.encode(chapter.subject, "UTF-8")
                            val encGrade = URLEncoder.encode("Kelas 5 SD", "UTF-8")
                            navController.navigate("workspace/$newExamId") {
                                popUpTo(ROUTE_HOME)
                            }
                            navController.navigate(
                                "ai_gen/$newExamId?subject=$encSubj&grade=$encGrade&chapterId=${chapter.id}"
                            )
                        }
                    )
                }
            )
        }

        composable(
            route = ROUTE_WORKSPACE,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId").orEmpty()
            val workspaceViewModel: ExamWorkspaceViewModel = viewModel(
                key = "${currentUserId}_$examId",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        ExamWorkspaceViewModel(ExamRepository(db), examId)
                    }
                }
            )

            ExamWorkspaceScreen(
                viewModel = workspaceViewModel,
                onBack = { navController.popBackStack() },
                onOpenAiGenerator = { subject, gradeLevel ->
                    val encSubj = URLEncoder.encode(subject, "UTF-8")
                    val encGrade = URLEncoder.encode(gradeLevel, "UTF-8")
                    navController.navigate("ai_gen/$examId?subject=$encSubj&grade=$encGrade")
                }
            )
        }

        composable(
            route = ROUTE_AI_GEN,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("subject") { type = NavType.StringType; defaultValue = "IPAS" },
                navArgument("grade") { type = NavType.StringType; defaultValue = "Kelas 5 SD" },
                navArgument("chapterId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId").orEmpty()
            val subject = URLDecoder.decode(
                backStackEntry.arguments?.getString("subject") ?: "IPAS",
                "UTF-8"
            )
            val grade = URLDecoder.decode(
                backStackEntry.arguments?.getString("grade") ?: "Kelas 5 SD",
                "UTF-8"
            )
            val chapterId = backStackEntry.arguments?.getString("chapterId")

            val workspaceViewModel: ExamWorkspaceViewModel = viewModel(
                key = "${currentUserId}_$examId",
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        ExamWorkspaceViewModel(ExamRepository(db), examId)
                    }
                }
            )
            val aiViewModel: AiGeneratorViewModel = viewModel(key = "${currentUserId}_ai_$examId")

            AiGeneratorScreen(
                viewModel = aiViewModel,
                initialSubject = subject,
                initialGrade = grade,
                initialChapterId = chapterId,
                onBack = { navController.popBackStack() },
                onInsertQuestions = { generatedQuestions ->
                    workspaceViewModel.addGeneratedQuestions(generatedQuestions) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
