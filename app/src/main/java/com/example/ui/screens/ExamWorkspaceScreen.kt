package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.ui.components.ExamMetadataDialog
import com.example.ui.components.QuestionEditorSheet
import com.example.ui.components.QuestionTypeBadge
import com.example.ui.viewmodel.ExamWorkspaceViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamWorkspaceScreen(
    viewModel: ExamWorkspaceViewModel,
    onBack: () -> Unit,
    onOpenAiGenerator: (subject: String, gradeLevel: String) -> Unit
) {
    val examState by viewModel.examState.collectAsStateWithLifecycle()
    val questionsState by viewModel.questionsState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(0) }
    var selectedTypeFilter by remember { mutableStateOf<QuestionType?>(null) }
    var showHeaderDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionItem?>(null) }
    var showQuestionSheet by remember { mutableStateOf(false) }
    var questionPendingDelete by remember { mutableStateOf<QuestionItem?>(null) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val exam = (examState as? UiState.Success)?.data
    val questions = (questionsState as? UiState.Success)?.data.orEmpty()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = exam?.title ?: "Memuat Naskah...",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (exam != null) {
                            Text(
                                text = "${exam.subject} • ${exam.gradeLevel} • ${questions.size} soal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    if (exam != null) {
                        IconButton(
                            onClick = { showHeaderDialog = true },
                            modifier = Modifier.testTag("edit_exam_header_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit identitas naskah")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Susun Soal (${questions.size})") },
                    modifier = Modifier.testTag("tab_susun_soal")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Pratinjau & Kunci") },
                    modifier = Modifier.testTag("tab_pratinjau_kunci")
                )
            }

            when {
                examState is UiState.Loading || questionsState is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                examState is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = (examState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                exam == null -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Naskah tidak ditemukan.")
                    }
                }
                activeTab == 0 -> {
                    QuestionBuilderTab(
                        exam = exam,
                        questions = questions,
                        selectedTypeFilter = selectedTypeFilter,
                        onSelectFilter = { selectedTypeFilter = it },
                        onAddManual = {
                            editingQuestion = null
                            showQuestionSheet = true
                        },
                        onLaunchAi = {
                            onOpenAiGenerator(exam.subject, exam.gradeLevel)
                        },
                        onEditQuestion = { q ->
                            editingQuestion = q
                            showQuestionSheet = true
                        },
                        onDeleteQuestion = { q -> questionPendingDelete = q },
                        onMoveUp = { idx ->
                            if (idx > 0) viewModel.moveQuestion(questions, idx, idx - 1)
                        },
                        onMoveDown = { idx ->
                            if (idx < questions.lastIndex) viewModel.moveQuestion(questions, idx, idx + 1)
                        }
                    )
                }
                else -> {
                    ExamPreviewAndKeyTab(
                        exam = exam,
                        questions = questions,
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    )
                }
            }
        }
    }

    if (showHeaderDialog && exam != null) {
        ExamMetadataDialog(
            initialExam = exam,
            isBusy = isSaving,
            onDismiss = { showHeaderDialog = false },
            onConfirm = { title, schoolName, subject, gradeLevel, semester, academicYear, duration, instructions, _ ->
                viewModel.updateExamHeader(
                    exam.copy(
                        title = title,
                        schoolName = schoolName,
                        subject = subject,
                        gradeLevel = gradeLevel,
                        semester = semester,
                        academicYear = academicYear,
                        durationMinutes = duration,
                        instructions = instructions
                    ),
                    onDone = { showHeaderDialog = false }
                )
            }
        )
    }

    if (showQuestionSheet) {
        val nextOrder = (questions.maxOfOrNull { it.orderIndex } ?: -1L) + 1L
        QuestionEditorSheet(
            initialQuestion = editingQuestion,
            defaultOrderIndex = nextOrder,
            isSaving = isSaving,
            onDismiss = { showQuestionSheet = false },
            onSave = { item ->
                viewModel.saveQuestion(item) {
                    showQuestionSheet = false
                }
            }
        )
    }

    questionPendingDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { questionPendingDelete = null },
            title = { Text("Hapus Soal?") },
            text = { Text("Soal ini beserta kunci jawabannya akan dihapus dari naskah.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(target.id)
                        questionPendingDelete = null
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { questionPendingDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun QuestionBuilderTab(
    exam: ExamDocument,
    questions: List<QuestionItem>,
    selectedTypeFilter: QuestionType?,
    onSelectFilter: (QuestionType?) -> Unit,
    onAddManual: () -> Unit,
    onLaunchAi: () -> Unit,
    onEditQuestion: (QuestionItem) -> Unit,
    onDeleteQuestion: (QuestionItem) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    val filtered = remember(questions, selectedTypeFilter) {
        if (selectedTypeFilter == null) questions
        else questions.filter { it.questionType == selectedTypeFilter }
    }

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
        ) {
            // Action bar: Manual + AI Generator
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = onAddManual,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_manual_question_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Manual")
                }

                OutlinedButton(
                    onClick = onLaunchAi,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("launch_ai_generator_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Soal AI")
                }
            }

            // Type distribution filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { onSelectFilter(null) },
                    label = { Text("Semua (${questions.size})") }
                )
                QuestionType.entries.forEach { qType ->
                    val count = questions.count { it.questionType == qType }
                    FilterChip(
                        selected = selectedTypeFilter == qType,
                        onClick = { onSelectFilter(if (selectedTypeFilter == qType) null else qType) },
                        label = { Text("${qType.shortLabel}: $count") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filtered.isEmpty()) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = if (questions.isEmpty()) "Naskah Masih Kosong" else "Tidak Ada Soal Tipe Ini",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tambahkan soal Pilihan Ganda, PG Kompleks, Benar/Salah, Isian, atau Esai secara manual maupun otomatis via AI.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(filtered, key = { _, item -> item.id }) { _, q ->
                        val actualIndex = questions.indexOfFirst { it.id == q.id }
                        QuestionRowCard(
                            number = actualIndex + 1,
                            question = q,
                            canMoveUp = selectedTypeFilter == null && actualIndex > 0,
                            canMoveDown = selectedTypeFilter == null && actualIndex in 0 until questions.lastIndex,
                            onMoveUp = { onMoveUp(actualIndex) },
                            onMoveDown = { onMoveDown(actualIndex) },
                            onEdit = { onEditQuestion(q) },
                            onDelete = { onDeleteQuestion(q) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionRowCard(
    number: Int,
    question: QuestionItem,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "#$number",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    QuestionTypeBadge(type = question.questionType, useShortLabel = true)
                    Text(
                        text = "${question.points} poin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canMoveUp) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Naikkan", modifier = Modifier.size(18.dp))
                        }
                    }
                    if (canMoveDown) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Turunkan", modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Sunting soal", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus soal", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = question.prompt,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Kunci: ${question.formatAnswerKey()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (question.topicTag.isNotBlank()) {
                Text(
                    text = "Materi: ${question.topicTag}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
