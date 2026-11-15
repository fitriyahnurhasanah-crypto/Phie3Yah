package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.ExamRepository
import com.example.model.ExamDocument
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExamListViewModel(
    private val repository: ExamRepository,
    @Suppress("unused") private val currentUserId: String
) : ViewModel() {

    val examsState: StateFlow<UiState<List<ExamDocument>>> = repository.observeExams()
        .map<List<ExamDocument>, UiState<List<ExamDocument>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing exams", rootError)
            emit(UiState.Error(rootError.localizedMessage ?: "Gagal memuat daftar naskah soal."))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun clearMessage() {
        _actionMessage.value = null
    }

    fun createExam(
        title: String,
        schoolName: String,
        subject: String,
        gradeLevel: String,
        semester: String,
        academicYear: String,
        durationMinutes: Long,
        instructions: String,
        withStarterTemplate: Boolean = false,
        onCreated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            try {
                val examId = repository.createExam(
                    ExamDocument(
                        title = title,
                        schoolName = schoolName,
                        subject = subject,
                        gradeLevel = gradeLevel,
                        semester = semester,
                        academicYear = academicYear,
                        durationMinutes = durationMinutes,
                        instructions = instructions
                    )
                )
                if (withStarterTemplate) {
                    repository.addQuestionsBatch(examId, buildStarterQuestions(subject))
                }
                _isBusy.value = false
                onCreated(examId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _isBusy.value = false
                Log.e(TAG, "Failed to create exam", e)
                _actionMessage.value = e.localizedMessage ?: "Gagal membuat naskah ujian."
            }
        }
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExam(examId)
                _actionMessage.value = "Naskah ujian berhasil dihapus."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete exam", e)
                _actionMessage.value = e.localizedMessage ?: "Gagal menghapus naskah ujian."
            }
        }
    }

    private fun buildStarterQuestions(subject: String): List<QuestionItem> {
        return when {
            subject.contains("IPAS", ignoreCase = true) -> listOf(
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA.name,
                    prompt = "Indonesia terletak di pertemuan tiga lempeng besar dunia, yaitu Eurasia, Indo-Australia, dan ....",
                    options = listOf("Lempeng Afrika", "Lempeng Pasifik", "Lempeng Amerika", "Lempeng Antartika"),
                    correctIndices = listOf(1L),
                    explanation = "Indonesia berada di pertemuan Lempeng Eurasia, Indo-Australia, dan Pasifik.",
                    topicTag = "Bab 1: Di Mana Indonesia Berada?",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
                    prompt = "Manakah yang termasuk komponen abiotik dalam sebuah ekosistem?",
                    options = listOf("Cahaya matahari", "Jamur pengurai", "Air", "Udara", "Belalang"),
                    correctIndices = listOf(0L, 2L, 3L),
                    explanation = "Komponen abiotik adalah benda tak hidup seperti cahaya matahari, air, tanah, udara, dan suhu.",
                    topicTag = "Bab 3: Harmoni dalam Ekosistem",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.BENAR_SALAH.name,
                    prompt = "Tentukan apakah pernyataan mengenai siklus air berikut Benar atau Salah:",
                    statements = listOf(
                        "Evaporasi adalah proses penguapan air oleh panas matahari.",
                        "Presipitasi adalah proses meresapnya air ke dalam pori-pori tanah."
                    ),
                    statementAnswers = listOf(true, false),
                    explanation = "Air meresap ke tanah disebut infiltrasi, sedangkan presipitasi adalah turunnya hujan.",
                    topicTag = "Bab 4: Air Sumber Kehidupan",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.ISIAN.name,
                    prompt = "Kemampuan kelelawar menentukan letak benda melalui pantulan bunyi disebut ....",
                    shortAnswerKey = "ekolokasi",
                    explanation = "Kelelawar dan lumba-lumba memanfaatkan ekolokasi untuk berburu dan navigasi.",
                    topicTag = "Bab 8: Ramai karena Bunyi",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.ESAI.name,
                    prompt = "Jelaskan 3 cara menjaga kelestarian air bersih di lingkungan rumah!",
                    essayRubric = "Menyebutkan 3 tindakan tepat (misal: mematikan keran saat tidak dipakai, memperbaiki keran bocor, menampung air hujan untuk menyiram tanaman). Skor 5 jika lengkap dan jelas.",
                    explanation = "Penghematan air menjaga ketersediaan air tanah dan sumber air bersih.",
                    topicTag = "Bab 4: Air Sumber Kehidupan",
                    points = 5L
                )
            )
            subject.contains("Matematika", ignoreCase = true) -> listOf(
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA.name,
                    prompt = "KPK dari bilangan 12 dan 18 adalah ....",
                    options = listOf("6", "24", "36", "72"),
                    correctIndices = listOf(2L),
                    explanation = "12 = 2² × 3 dan 18 = 2 × 3², sehingga KPK = 2² × 3² = 36.",
                    topicTag = "Bab 2: KPK dan FPB",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
                    prompt = "Pilih dua pernyataan yang benar mengenai ciri-ciri bangun datar persegi:",
                    options = listOf(
                        "Memiliki 4 sisi sama panjang",
                        "Hanya memiliki sepasang sisi sejajar",
                        "Keempat sudutnya siku-siku (90°)",
                        "Diagonalnya tidak sama panjang"
                    ),
                    correctIndices = listOf(0L, 2L),
                    explanation = "Persegi memiliki 4 sisi sama panjang, 4 sudut 90°, dan diagonal sama panjang berpotongan tegak lurus.",
                    topicTag = "Bab 7: Ciri Bangun Datar",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.BENAR_SALAH.name,
                    prompt = "Periksalah kebenaran pernyataan tentang sudut berikut ini:",
                    statements = listOf(
                        "Besar sudut siku-siku tepat 90°.",
                        "Sudut yang besarnya 125° termasuk sudut lancip."
                    ),
                    statementAnswers = listOf(true, false),
                    explanation = "Sudut 125° berada di antara 90° dan 180°, sehingga termasuk sudut tumpul.",
                    topicTag = "Bab 6: Sudut",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.ISIAN.name,
                    prompt = "Luas sebuah persegi panjang dengan panjang 8 cm dan lebar 4 cm adalah .... cm².",
                    shortAnswerKey = "32",
                    explanation = "Luas persegi panjang = p × l = 8 × 4 = 32 cm².",
                    topicTag = "Bab 5: Luas Bangun Datar",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.ESAI.name,
                    prompt = "Ibu membeli 2/5 kg gula dan 1/2 kg tepung. Berapa kilogram total belanjaan Ibu? Tuliskan langkah penyelesaiannya!",
                    essayRubric = "Menyamakan penyebut (KPK 5 dan 2 = 10): 4/10 + 5/10 = 9/10 kg.",
                    explanation = "Penjumlahan pecahan berpenyebut berbeda dilakukan dengan menyamakan penyebut terlebih dahulu.",
                    topicTag = "Bab 3: Bilangan Pecahan",
                    points = 5L
                )
            )
            else -> listOf(
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA.name,
                    prompt = "Idiom 'banting tulang' dalam kalimat 'Ayah banting tulang demi keluarga' memiliki arti ....",
                    options = listOf("bekerja keras", "berpikir cepat", "berhati lembut", "menjadi terkenal"),
                    correctIndices = listOf(0L),
                    explanation = "Banting tulang adalah ungkapan/idiom yang berarti bekerja keras.",
                    topicTag = "Bab 4: Belajar Berwirausaha",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.PILIHAN_GANDA_KOMPLEKS.name,
                    prompt = "Pilih dua kalimat yang menggunakan huruf kapital dengan tepat:",
                    options = listOf(
                        "Kami siswa kelas lima berkunjung ke Danau Toba.",
                        "Ayah pergi ke jakarta pada hari senin.",
                        "R.A. Kartini lahir pada bulan April.",
                        "adik membeli dua kilogram apel."
                    ),
                    correctIndices = listOf(0L, 2L),
                    explanation = "Huruf kapital digunakan pada awal kalimat, nama orang, nama tempat geografis, dan nama bulan.",
                    topicTag = "Bab 5: Cinta Indonesia",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.BENAR_SALAH.name,
                    prompt = "Tentukan benar atau salah pernyataan kebahasaan berikut:",
                    statements = listOf(
                        "Antonim dari kata 'berani' adalah 'penakut'.",
                        "Majas metafora menggunakan kata pembanding seperti 'bagaikan' dan 'seperti'."
                    ),
                    statementAnswers = listOf(true, false),
                    explanation = "Majas metafora adalah perbandingan langsung tanpa kata pembanding seperti/bagaikan.",
                    topicTag = "Bab 1 & Bab 2",
                    points = 2L
                ),
                QuestionItem(
                    type = QuestionType.ISIAN.name,
                    prompt = "Kata dasar 'lukis' apabila diberi imbuhan me- akan menjadi kata berimbuhan ....",
                    shortAnswerKey = "melukis",
                    explanation = "Awalan me- + lukis membentuk kata kerja aktif 'melukis'.",
                    topicTag = "Bab 3: Ekspresi Diri",
                    points = 3L
                ),
                QuestionItem(
                    type = QuestionType.ESAI.name,
                    prompt = "Jelaskan perbedaan prinsip 3M (Reduce, Reuse, Recycle) beserta masing-masing 1 contoh penerapannya di sekolah!",
                    essayRubric = "Menjelaskan Reduce (mengurangi sampah sekali pakai, misal bawa botol minum), Reuse (menggunakan kembali barang layak, misal pakai kertas bekas sisi kosong), Recycle (mendaur ulang sampah menjadi kerajinan/kompos).",
                    explanation = "Prinsip 3M adalah langkah pengelolaan sampah ramah lingkungan.",
                    topicTag = "Bab 6: Sayangi Bumi",
                    points = 5L
                )
            )
        }
    }

    private companion object {
        const val TAG = "ExamListVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
