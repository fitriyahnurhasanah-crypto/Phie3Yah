package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.ExamDocument
import com.example.model.QuestionType
import com.example.ui.theme.BadgeBsColor
import com.example.ui.theme.BadgeEsaiColor
import com.example.ui.theme.BadgeIsianColor
import com.example.ui.theme.BadgePgColor
import com.example.ui.theme.BadgePgkColor

fun QuestionType.badgeColor(): Color = when (this) {
    QuestionType.PILIHAN_GANDA -> BadgePgColor
    QuestionType.PILIHAN_GANDA_KOMPLEKS -> BadgePgkColor
    QuestionType.BENAR_SALAH -> BadgeBsColor
    QuestionType.ISIAN -> BadgeIsianColor
    QuestionType.ESAI -> BadgeEsaiColor
}

@Composable
fun QuestionTypeBadge(
    type: QuestionType,
    modifier: Modifier = Modifier,
    useShortLabel: Boolean = false
) {
    val accent = type.badgeColor()
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = accent.copy(alpha = 0.14f),
        modifier = modifier
    ) {
        Text(
            text = if (useShortLabel) type.shortLabel else type.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = accent,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamMetadataDialog(
    initialExam: ExamDocument?,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        schoolName: String,
        subject: String,
        gradeLevel: String,
        semester: String,
        academicYear: String,
        durationMinutes: Long,
        instructions: String,
        withStarterTemplate: Boolean
    ) -> Unit
) {
    val isEditing = initialExam != null
    var title by remember { mutableStateOf(initialExam?.title ?: "Penilaian Akhir Semester (PAS)") }
    var schoolName by remember { mutableStateOf(initialExam?.schoolName ?: "SD Negeri Nusantara") }
    var subject by remember { mutableStateOf(initialExam?.subject ?: "IPAS") }
    var gradeLevel by remember { mutableStateOf(initialExam?.gradeLevel ?: "Kelas 5 SD") }
    var semester by remember { mutableStateOf(initialExam?.semester ?: "Semester 1 (Ganjil)") }
    var academicYear by remember { mutableStateOf(initialExam?.academicYear ?: "2026/2027") }
    var durationText by remember { mutableStateOf((initialExam?.durationMinutes ?: 90L).toString()) }
    var instructions by remember {
        mutableStateOf(
            initialExam?.instructions
                ?: "Berdoalah sebelum mengerjakan. Tulislah nama dan nomor absen dengan jelas. Periksa kembali jawaban sebelum dikumpulkan."
        )
    }
    var withStarterTemplate by remember { mutableStateOf(!isEditing) }
    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val subjectSuggestions = listOf(
        "Bahasa Indonesia",
        "IPAS",
        "Matematika",
        "Pendidikan Pancasila",
        "Bahasa Inggris",
        "Seni Budaya"
    )

    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = {
            Text(
                text = if (isEditing) "Edit Identitas Naskah" else "Buat Naskah UAS Baru",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Ujian *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_title_input")
                )

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("Nama Sekolah / Instansi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Mata Pelajaran *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjectSuggestions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    subject = option
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Kelas") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Tahun Ajaran") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Waktu (menit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Petunjuk Umum Ujian") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isEditing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = withStarterTemplate,
                            onCheckedChange = { withStarterTemplate = it }
                        )
                        Column {
                            Text(
                                text = "Sertakan 5 contoh soal awal (semua tipe)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "PG, PG Kompleks, Benar/Salah, Isian, dan Esai siap sunting.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isBusy,
                onClick = {
                    if (title.isBlank() || subject.isBlank()) {
                        errorText = "Judul ujian dan mata pelajaran wajib diisi."
                        return@Button
                    }
                    val mins = durationText.toLongOrNull()?.coerceIn(5L, 600L) ?: 90L
                    onConfirm(
                        title.trim(),
                        schoolName.trim(),
                        subject.trim(),
                        gradeLevel.trim(),
                        semester.trim(),
                        academicYear.trim(),
                        mins,
                        instructions.trim(),
                        withStarterTemplate
                    )
                },
                modifier = Modifier.testTag("save_exam_button")
            ) {
                Text(if (isBusy) "Menyimpan..." else if (isEditing) "Simpan" else "Buat Naskah")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isBusy,
                onClick = onDismiss
            ) {
                Text("Batal")
            }
        }
    )
}
