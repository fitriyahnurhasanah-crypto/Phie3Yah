package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.QuestionItem
import com.example.model.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorSheet(
    initialQuestion: QuestionItem?,
    defaultOrderIndex: Long,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (QuestionItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember {
        mutableStateOf(initialQuestion?.questionType ?: QuestionType.PILIHAN_GANDA)
    }
    var prompt by remember { mutableStateOf(initialQuestion?.prompt ?: "") }
    var topicTag by remember { mutableStateOf(initialQuestion?.topicTag ?: "") }
    var explanation by remember { mutableStateOf(initialQuestion?.explanation ?: "") }
    var points by remember {
        mutableLongStateOf(initialQuestion?.points ?: selectedType.defaultPoints)
    }

    val options = remember {
        mutableStateListOf<String>().apply {
            val initial = initialQuestion?.options.orEmpty()
            if (initial.isNotEmpty()) addAll(initial)
            else addAll(listOf("", "", "", ""))
        }
    }
    val correctIndices = remember {
        mutableStateListOf<Long>().apply {
            val initial = initialQuestion?.correctIndices.orEmpty()
            if (initial.isNotEmpty()) addAll(initial) else add(0L)
        }
    }

    val statements = remember {
        mutableStateListOf<String>().apply {
            val initial = initialQuestion?.statements.orEmpty()
            if (initial.isNotEmpty()) addAll(initial) else addAll(listOf("", ""))
        }
    }
    val statementAnswers = remember {
        mutableStateListOf<Boolean>().apply {
            val initial = initialQuestion?.statementAnswers.orEmpty()
            if (initial.isNotEmpty()) addAll(initial) else addAll(listOf(true, false))
        }
    }

    var shortAnswerKey by remember { mutableStateOf(initialQuestion?.shortAnswerKey ?: "") }
    var essayRubric by remember { mutableStateOf(initialQuestion?.essayRubric ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val letters = listOf("A", "B", "C", "D", "E", "F")

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (initialQuestion == null) "Tambah Soal Baru" else "Sunting Soal",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { if (!isSaving) onDismiss() }) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup editor soal")
                }
            }

            Text(
                text = "Pilih Jenis Soal",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                QuestionType.entries.forEach { qType ->
                    FilterChip(
                        selected = selectedType == qType,
                        onClick = {
                            selectedType = qType
                            if (initialQuestion == null) {
                                points = qType.defaultPoints
                            }
                            validationError = null
                        },
                        label = { Text(qType.displayName) },
                        modifier = Modifier.testTag("type_chip_${qType.name.lowercase()}")
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                label = {
                    Text(
                        when (selectedType) {
                            QuestionType.BENAR_SALAH -> "Instruksi / Pengantar Soal *"
                            QuestionType.ISIAN -> "Kalimat Soal (gunakan .... untuk bagian rumpang) *"
                            else -> "Pertanyaan / Pokok Soal *"
                        }
                    )
                },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_prompt_input")
            )

            HorizontalDivider()

            when (selectedType) {
                QuestionType.PILIHAN_GANDA, QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                    val isComplex = selectedType == QuestionType.PILIHAN_GANDA_KOMPLEKS
                    Text(
                        text = if (isComplex) {
                            "Opsi Jawaban & Centang Lebih dari 1 Kunci Benar:"
                        } else {
                            "Opsi Jawaban & Pilih 1 Kunci Benar:"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )

                    options.forEachIndexed { idx, value ->
                        val isChecked = correctIndices.contains(idx.toLong())
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isComplex) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!correctIndices.contains(idx.toLong())) {
                                                correctIndices.add(idx.toLong())
                                            }
                                        } else {
                                            correctIndices.remove(idx.toLong())
                                        }
                                    }
                                )
                            } else {
                                RadioButton(
                                    selected = isChecked,
                                    onClick = {
                                        correctIndices.clear()
                                        correctIndices.add(idx.toLong())
                                    }
                                )
                            }
                            OutlinedTextField(
                                value = value,
                                onValueChange = { options[idx] = it },
                                label = { Text("Pilihan ${letters.getOrElse(idx) { "${idx + 1}" }}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (options.size > 2) {
                                IconButton(
                                    onClick = {
                                        options.removeAt(idx)
                                        val updated = correctIndices
                                            .filter { it != idx.toLong() }
                                            .map { if (it > idx) it - 1 else it }
                                        correctIndices.clear()
                                        correctIndices.addAll(updated)
                                        if (!isComplex && correctIndices.isEmpty() && options.isNotEmpty()) {
                                            correctIndices.add(0L)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus opsi")
                                }
                            }
                        }
                    }

                    if (options.size < 5) {
                        TextButton(
                            onClick = { options.add("") }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Opsi (${letters.getOrElse(options.size) { "" }})")
                        }
                    }
                }

                QuestionType.BENAR_SALAH -> {
                    Text(
                        text = "Daftar Pernyataan & Kunci (Benar / Salah):",
                        style = MaterialTheme.typography.labelLarge
                    )

                    statements.forEachIndexed { idx, st ->
                        val isTrue = statementAnswers.getOrElse(idx) { true }
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Pernyataan ${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (statements.size > 1) {
                                        IconButton(
                                            onClick = {
                                                statements.removeAt(idx)
                                                if (idx < statementAnswers.size) statementAnswers.removeAt(idx)
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Hapus pernyataan",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = st,
                                    onValueChange = { statements[idx] = it },
                                    placeholder = { Text("Tulis pernyataan yang akan dinilai benar atau salah...") },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                    SegmentedButton(
                                        selected = isTrue,
                                        onClick = { statementAnswers[idx] = true },
                                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                    ) {
                                        Text("Benar")
                                    }
                                    SegmentedButton(
                                        selected = !isTrue,
                                        onClick = { statementAnswers[idx] = false },
                                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                    ) {
                                        Text("Salah")
                                    }
                                }
                            }
                        }
                    }

                    if (statements.size < 6) {
                        OutlinedButton(
                            onClick = {
                                statements.add("")
                                statementAnswers.add(true)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tambah Pernyataan")
                        }
                    }
                }

                QuestionType.ISIAN -> {
                    OutlinedTextField(
                        value = shortAnswerKey,
                        onValueChange = { shortAnswerKey = it },
                        label = { Text("Kunci Jawaban Singkat *") },
                        placeholder = { Text("Contoh: fotosintesis / 36 / Bhinneka Tunggal Ika") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                QuestionType.ESAI -> {
                    OutlinedTextField(
                        value = essayRubric,
                        onValueChange = { essayRubric = it },
                        label = { Text("Kunci Jawaban & Pedoman Penskoran *") },
                        placeholder = { Text("Tuliskan poin-poin jawaban yang diharapkan beserta bobot penilaiannya...") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider()

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = topicTag,
                    onValueChange = { topicTag = it },
                    label = { Text("Tag Bab / Materi") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        TextButton(
                            onClick = { if (points > 1) points-- },
                            modifier = Modifier.size(38.dp)
                        ) { Text("-") }
                        Text(
                            text = "$points poin",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        TextButton(
                            onClick = { if (points < 50) points++ },
                            modifier = Modifier.size(38.dp)
                        ) { Text("+") }
                    }
                }
            }

            OutlinedTextField(
                value = explanation,
                onValueChange = { explanation = it },
                label = { Text("Pembahasan Kunci Jawaban (Opsional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            if (!validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Button(
                enabled = !isSaving,
                onClick = {
                    val cleanPrompt = prompt.trim()
                    if (cleanPrompt.isBlank()) {
                        validationError = "Pertanyaan tidak boleh kosong."
                        return@Button
                    }

                    val cleanOptions = options.map { it.trim() }.filter { it.isNotBlank() }
                    val cleanStatements = statements.map { it.trim() }.filter { it.isNotBlank() }

                    when (selectedType) {
                        QuestionType.PILIHAN_GANDA -> {
                            if (cleanOptions.size < 2) {
                                validationError = "Pilihan Ganda minimal memerlukan 2 opsi jawaban."
                                return@Button
                            }
                            val firstValid = correctIndices.firstOrNull { it in 0L until cleanOptions.size.toLong() }
                            if (firstValid == null) {
                                validationError = "Pilih 1 kunci jawaban yang benar."
                                return@Button
                            }
                        }
                        QuestionType.PILIHAN_GANDA_KOMPLEKS -> {
                            if (cleanOptions.size < 3) {
                                validationError = "PG Kompleks minimal memerlukan 3 opsi jawaban."
                                return@Button
                            }
                            val validKeys = correctIndices.filter { it in 0L until cleanOptions.size.toLong() }.distinct()
                            if (validKeys.size < 2) {
                                validationError = "PG Kompleks wajib memiliki minimal 2 jawaban benar."
                                return@Button
                            }
                        }
                        QuestionType.BENAR_SALAH -> {
                            if (cleanStatements.isEmpty()) {
                                validationError = "Isi minimal 1 pernyataan Benar/Salah."
                                return@Button
                            }
                        }
                        QuestionType.ISIAN -> {
                            if (shortAnswerKey.isBlank()) {
                                validationError = "Kunci jawaban isian singkat wajib diisi."
                                return@Button
                            }
                        }
                        QuestionType.ESAI -> {
                            if (essayRubric.isBlank()) {
                                validationError = "Kunci jawaban/pedoman penskoran esai wajib diisi."
                                return@Button
                            }
                        }
                    }

                    val finalCorrectIndices = when (selectedType) {
                        QuestionType.PILIHAN_GANDA -> listOf(
                            correctIndices.first { it in 0L until cleanOptions.size.toLong() }
                        )
                        QuestionType.PILIHAN_GANDA_KOMPLEKS ->
                            correctIndices.filter { it in 0L until cleanOptions.size.toLong() }.distinct().sorted()
                        else -> emptyList()
                    }

                    val finalStatementAnswers = if (selectedType == QuestionType.BENAR_SALAH) {
                        List(cleanStatements.size) { idx -> statementAnswers.getOrElse(idx) { true } }
                    } else emptyList()

                    val item = (initialQuestion ?: QuestionItem(orderIndex = defaultOrderIndex)).copy(
                        type = selectedType.name,
                        prompt = cleanPrompt,
                        options = if (selectedType == QuestionType.PILIHAN_GANDA || selectedType == QuestionType.PILIHAN_GANDA_KOMPLEKS) {
                            cleanOptions
                        } else emptyList(),
                        correctIndices = finalCorrectIndices,
                        statements = if (selectedType == QuestionType.BENAR_SALAH) cleanStatements else emptyList(),
                        statementAnswers = finalStatementAnswers,
                        shortAnswerKey = if (selectedType == QuestionType.ISIAN) shortAnswerKey.trim() else "",
                        essayRubric = if (selectedType == QuestionType.ESAI) essayRubric.trim() else "",
                        explanation = explanation.trim(),
                        topicTag = topicTag.trim(),
                        points = points
                    )
                    onSave(item)
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_question_button")
            ) {
                Text(if (isSaving) "Menyimpan..." else "Simpan Soal ke Naskah")
            }
        }
    }
}
