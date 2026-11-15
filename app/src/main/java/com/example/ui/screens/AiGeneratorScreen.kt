package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.GenerationRequest
import com.example.model.CurriculumBank
import com.example.model.QuestionItem
import com.example.ui.components.QuestionTypeBadge
import com.example.ui.viewmodel.AiGeneratorState
import com.example.ui.viewmodel.AiGeneratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGeneratorScreen(
    viewModel: AiGeneratorViewModel,
    initialSubject: String,
    initialGrade: String,
    initialChapterId: String?,
    onBack: () -> Unit,
    onInsertQuestions: (List<QuestionItem>) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var sourceMode by remember {
        mutableIntStateOf(if (initialChapterId != null) 0 else 0)
    }
    var subject by remember { mutableStateOf(initialSubject.ifBlank { "IPAS" }) }
    var gradeLevel by remember { mutableStateOf(initialGrade.ifBlank { "Kelas 5 SD" }) }
    var difficulty by remember { mutableStateOf("Sedang") }
    var customTopic by remember { mutableStateOf("") }

    var selectedSubjectFilter by remember {
        mutableStateOf(
            CurriculumBank.subjects.firstOrNull {
                initialSubject.contains(it, ignoreCase = true)
            } ?: CurriculumBank.subjects.first()
        )
    }
    val selectedChapterIds = remember {
        mutableStateListOf<String>().apply {
            if (!initialChapterId.isNullOrBlank()) {
                add(initialChapterId)
                CurriculumBank.chapters.firstOrNull { it.id == initialChapterId }?.let {
                    selectedSubjectFilter = it.subject
                    subject = it.subject
                }
            }
        }
    }

    var pgCount by remember { mutableIntStateOf(3) }
    var pgkCount by remember { mutableIntStateOf(2) }
    var bsCount by remember { mutableIntStateOf(2) }
    var isianCount by remember { mutableIntStateOf(2) }
    var esaiCount by remember { mutableIntStateOf(1) }

    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentBytes by remember { mutableStateOf<ByteArray?>(null) }
    var attachmentMime by remember { mutableStateOf<String?>(null) }
    var attachmentError by remember { mutableStateOf<String?>(null) }

    val selectedResultIndices = remember { mutableStateListOf<Int>() }

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            readAttachmentFromUri(context, it, "application/pdf") { name, bytes, mime, err ->
                attachmentName = name
                attachmentBytes = bytes
                attachmentMime = mime
                attachmentError = err
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            val mime = context.contentResolver.getType(it) ?: "image/jpeg"
            readAttachmentFromUri(context, it, mime) { name, bytes, resolvedMime, err ->
                attachmentName = name
                attachmentBytes = bytes
                attachmentMime = resolvedMime
                attachmentError = err
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                title = { Text("Generator Soal AI") }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
            ) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("1. Sumber Materi Ujian", style = MaterialTheme.typography.titleMedium)

                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                listOf("Materi Kelas 5", "PDF / Foto", "Topik Bebas").forEachIndexed { idx, label ->
                                    SegmentedButton(
                                        selected = sourceMode == idx,
                                        onClick = { sourceMode = idx },
                                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 3)
                                    ) {
                                        Text(label, maxLines = 1)
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = subject,
                                    onValueChange = { subject = it },
                                    label = { Text("Mata Pelajaran") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = gradeLevel,
                                    onValueChange = { gradeLevel = it },
                                    label = { Text("Kelas") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            when (sourceMode) {
                                0 -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        CurriculumBank.subjects.forEach { subj ->
                                            FilterChip(
                                                selected = selectedSubjectFilter == subj,
                                                onClick = {
                                                    selectedSubjectFilter = subj
                                                    subject = subj
                                                },
                                                label = { Text(subj) }
                                            )
                                        }
                                    }

                                    val chapters = CurriculumBank.chapters.filter { it.subject == selectedSubjectFilter }
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        chapters.forEach { chapter ->
                                            val checked = selectedChapterIds.contains(chapter.id)
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (checked) {
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (checked) selectedChapterIds.remove(chapter.id)
                                                        else selectedChapterIds.add(chapter.id)
                                                    }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                                ) {
                                                    Checkbox(
                                                        checked = checked,
                                                        onCheckedChange = {
                                                            if (it) selectedChapterIds.add(chapter.id)
                                                            else selectedChapterIds.remove(chapter.id)
                                                        }
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "${chapter.chapterLabel}: ${chapter.title}",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = chapter.keyPoints.joinToString(" • "),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                1 -> {
                                    Text(
                                        text = "Unggah dokumen PDF rangkuman/modul atau foto halaman buku pelajaran (maks. 19 MB):",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedButton(
                                            onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pilih PDF")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                photoLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Pilih Foto")
                                        }
                                    }
                                    if (attachmentName != null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "$attachmentName (${(attachmentBytes?.size ?: 0) / 1024} KB)",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        attachmentName = null
                                                        attachmentBytes = null
                                                        attachmentMime = null
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Hapus lampiran", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                    if (!attachmentError.isNullOrBlank()) {
                                        Text(
                                            text = attachmentError!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                else -> {
                                    OutlinedTextField(
                                        value = customTopic,
                                        onValueChange = { customTopic = it },
                                        label = { Text("Kisi-Kisi / Materi Pokok *") },
                                        placeholder = { Text("Contoh: Siklus air, ekosistem rantai makanan, dan sifat cahaya...") },
                                        minLines = 3,
                                        maxLines = 6,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("2. Komposisi 5 Tipe Soal & Kesulitan", style = MaterialTheme.typography.titleMedium)

                            CountStepperRow("Pilihan Ganda (PG)", pgCount) { pgCount = it }
                            CountStepperRow("Pilihan Ganda Kompleks", pgkCount) { pgkCount = it }
                            CountStepperRow("Benar / Salah (B/S)", bsCount) { bsCount = it }
                            CountStepperRow("Isian Singkat", isianCount) { isianCount = it }
                            CountStepperRow("Esai / Uraian", esaiCount) { esaiCount = it }

                            HorizontalDivider()

                            Text("Tingkat Kesulitan", style = MaterialTheme.typography.labelLarge)
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                listOf("Mudah", "Sedang", "HOTS").forEachIndexed { idx, level ->
                                    SegmentedButton(
                                        selected = difficulty == level,
                                        onClick = { difficulty = level },
                                        shape = SegmentedButtonDefaults.itemShape(index = idx, count = 3)
                                    ) {
                                        Text(level)
                                    }
                                }
                            }

                            val totalRequested = pgCount + pgkCount + bsCount + isianCount + esaiCount
                            Button(
                                enabled = state !is AiGeneratorState.Loading && totalRequested > 0,
                                onClick = {
                                    val materialContext = when (sourceMode) {
                                        0 -> CurriculumBank.chapters
                                            .filter { selectedChapterIds.contains(it.id) }
                                            .joinToString("\n\n") { it.summaryPromptContext }
                                            .ifBlank {
                                                CurriculumBank.chapters
                                                    .filter { it.subject == selectedSubjectFilter }
                                                    .take(2)
                                                    .joinToString("\n\n") { it.summaryPromptContext }
                                            }
                                        1 -> customTopic.ifBlank { "Buat soal berdasarkan file materi terlampir." }
                                        else -> customTopic
                                    }

                                    selectedResultIndices.clear()
                                    viewModel.generate(
                                        GenerationRequest(
                                            subject = subject.ifBlank { "Umum" },
                                            gradeLevel = gradeLevel.ifBlank { "Kelas 5 SD" },
                                            materialContext = materialContext,
                                            difficulty = difficulty,
                                            pgCount = pgCount,
                                            pgKompleksCount = pgkCount,
                                            benarSalahCount = bsCount,
                                            isianCount = isianCount,
                                            esaiCount = esaiCount,
                                            attachmentBytes = if (sourceMode == 1) attachmentBytes else null,
                                            attachmentMimeType = if (sourceMode == 1) attachmentMime else null
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("generate_ai_questions_button")
                            ) {
                                if (state is AiGeneratorState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Menyusun $totalRequested soal...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate $totalRequested Soal & Kunci")
                                }
                            }
                        }
                    }
                }

                when (val current = state) {
                    is AiGeneratorState.Error -> {
                        item {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = current.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    is AiGeneratorState.Generated -> {
                        if (selectedResultIndices.isEmpty() && current.questions.isNotEmpty()) {
                            selectedResultIndices.addAll(current.questions.indices)
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Hasil AI (${current.questions.size} Soal)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Button(
                                    enabled = selectedResultIndices.isNotEmpty(),
                                    onClick = {
                                        val chosen = selectedResultIndices.sorted()
                                            .mapNotNull { idx -> current.questions.getOrNull(idx) }
                                        onInsertQuestions(chosen)
                                    },
                                    modifier = Modifier.testTag("insert_ai_questions_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Masukkan (${selectedResultIndices.size})")
                                }
                            }
                        }

                        itemsIndexed(current.questions) { idx, q ->
                            val isSelected = selectedResultIndices.contains(idx)
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) selectedResultIndices.remove(idx)
                                        else selectedResultIndices.add(idx)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (!selectedResultIndices.contains(idx)) selectedResultIndices.add(idx)
                                            } else {
                                                selectedResultIndices.remove(idx)
                                            }
                                        }
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            QuestionTypeBadge(type = q.questionType, useShortLabel = true)
                                            Text("${q.points} poin", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Text(
                                            text = q.prompt,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Kunci: ${q.formatAnswerKey()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun CountStepperRow(
    label: String,
    count: Int,
    onChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(
                onClick = { if (count > 0) onChange(count - 1) },
                modifier = Modifier.size(38.dp)
            ) { Text("-") }
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextButton(
                onClick = { if (count < 15) onChange(count + 1) },
                modifier = Modifier.size(38.dp)
            ) { Text("+") }
        }
    }
}

private fun readAttachmentFromUri(
    context: Context,
    uri: Uri,
    fallbackMime: String,
    onResult: (name: String?, bytes: ByteArray?, mime: String?, error: String?) -> Unit
) {
    try {
        val resolver = context.contentResolver
        var fileName = "dokumen_materi"
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIdx >= 0) {
                fileName = cursor.getString(nameIdx) ?: fileName
            }
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null || bytes.isEmpty()) {
            onResult(null, null, null, "File kosong atau tidak dapat dibaca.")
            return
        }
        if (bytes.size > 19 * 1024 * 1024) {
            onResult(null, null, null, "Ukuran file ${bytes.size / (1024 * 1024)} MB melebihi batas 19 MB.")
            return
        }
        val mime = resolver.getType(uri) ?: fallbackMime
        onResult(fileName, bytes, mime, null)
    } catch (e: Exception) {
        onResult(null, null, null, e.localizedMessage ?: "Gagal membaca file.")
    }
}
