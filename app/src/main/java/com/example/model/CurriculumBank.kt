package com.example.model

data class CurriculumChapter(
    val id: String,
    val subject: String,
    val chapterLabel: String,
    val title: String,
    val keyPoints: List<String>,
    val summaryPromptContext: String
)

object CurriculumBank {
    val chapters: List<CurriculumChapter> = listOf(
        // === BAHASA INDONESIA KELAS 5 ===
        CurriculumChapter(
            id = "bindo_bab1",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 1",
            title = "Aku yang Unik",
            keyPoints = listOf("Kata Sifat", "Menggunakan Kamus", "Puisi Akrostik", "Sinonim & Antonim", "Teks Naratif"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 1: Aku yang Unik.
                1. Kata Sifat: kata yang menerangkan sifat/keadaan/ciri seseorang, benda, atau tempat (contoh: rajin, cerah, manis, unik, ramah, giat, tinggi, wangi, berani).
                2. Menggunakan Kamus: daftar kata dan artinya; langkah: tentukan kata, cari abjad pertama, temukan kata, baca artinya. Contoh: unik = berbeda dari yang lain; khas; istimewa.
                3. Puisi Akrostik: puisi yang huruf awal setiap barisnya membentuk sebuah kata (contoh huruf U-N-I-K).
                4. Sinonim (persamaan kata): unik=istimewa, rajin=giat, ceria=gembira, besar=luas, cantik=indah. Antonim (lawan kata): maju><mundur, senang><sedih, bersih><kotor, tinggi><rendah, berani><penakut.
                5. Teks Naratif: menceritakan peristiwa/pengalaman secara runtut dengan tokoh, latar, alur, bertujuan menghibur/memberi pengalaman.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "bindo_bab2",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 2",
            title = "Buku Jendela Dunia",
            keyPoints = listOf("Teks Biografi", "Teks Deskripsi", "Majas Metafora", "Imbuhan pe-", "Kalimat Perintah & Larangan"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 2: Jendela Dunia.
                1. Teks Biografi (contoh R.A. Kartini: lahir 21 April 1879 di Jepara, wafat 17 September 1904 di Rembang, karya 'Habis Gelap Terbitlah Terang'): menceritakan riwayat hidup seseorang secara runtut, berisi fakta, ditulis orang lain.
                2. Teks Deskripsi: menggambarkan objek/tempat/suasana secara rinci melibatkan pancaindra dan kata sifat.
                3. Majas Metafora: perbandingan langsung tanpa kata pembanding (seperti/bagaikan). Contoh: 'Engkau adalah bintang di hidupku', 'Ia harimau lapangan', 'Waktu adalah emas'.
                4. Imbuhan pe-: membentuk kata benda (pe- + vokal -> pealat; pem- + b,p,m -> pembuat/pemimpin; pen- + c,d,t,j -> pencipta/penulis; peng- + k,g,h -> pengajar/penghubung; peny- + s,y -> penyanyi/penyebab; pel- + l -> pelajar).
                5. Kalimat Perintah (diakhiri tanda !, pakai kata ayo/mari/tolong/silakan) & Kalimat Larangan (pakai kata jangan/dilarang/tidak boleh).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "bindo_bab3",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 3",
            title = "Ekspresi Diri Melalui Hobi",
            keyPoints = listOf("Mengenal Hobi", "Imbuhan me-", "Teks Prosedur", "Infografik"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 3: Ekspresi Diri melalui Hobi.
                1. Hobi: kegiatan yang disukai di waktu luang untuk kebahagiaan, bakat, kreativitas, mengurangi stres.
                2. Imbuhan me-: awalan membentuk kata kerja aktif (menuang dari tuang, mengaduk dari aduk, mencampur dari campur, melukis dari lukis).
                3. Teks Prosedur: langkah-langkah berurutan melakukan/membuat sesuatu (menggunakan kata kerja imperatif dan kata penghubung urutan: pertama, lalu, kemudian, selanjutnya, terakhir).
                4. Infografik: penyajian informasi memadukan judul menarik, teks singkat padat, gambar/ikon, warna, dan data/fakta.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "bindo_bab4",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 4",
            title = "Belajar Berwirausaha",
            keyPoints = listOf("Ide Pokok", "Wawancara", "Kata Tanya", "Idiom"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 4: Belajar Berwirausaha.
                1. Ide Pokok: gagasan utama paragraf yang menjadi inti pembahasan dan dasar pengembangan kalimat penjelas.
                2. Wawancara: tanya jawab dengan narasumber; unsurnya: pewawancara, narasumber, pertanyaan, jawaban, tujuan.
                3. Kata Tanya: apa (benda/hal), siapa (orang), di mana (tempat), kapan (waktu), mengapa (alasan), bagaimana (cara).
                4. Idiom: ungkapan bermakna kiasan: bagai pinang dibelah dua (sangat cocok), ambil hati (berusaha menyenangkan), naik daun (menjadi terkenal/sukses), banting tulang (bekerja keras), putar otak (berpikir keras mencari ide).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "bindo_bab5",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 5",
            title = "Cinta Indonesia",
            keyPoints = listOf("Menulis Angka & Bilangan", "Pesan Bacaan", "Sebab Akibat", "Huruf Kapital", "Perintah & Larangan"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 5: Cinta Indonesia.
                1. Menulis Angka & Bilangan: jam pakai tanda titik (07.30.45); bilangan 1-2 kata ditulis huruf ('dua kilogram'), bilangan berkoma ditulis angka ('3,5 meter'); tahun ditulis angka utuh (1980).
                2. Menemukan Pesan Bacaan: baca saksama, cari gagasan utama & kalimat nasihat/ajakan/kesimpulan.
                3. Hubungan Sebab Akibat: penghubung sebab (karena, sebab, oleh karena itu), penghubung akibat (sehingga, maka, akibatnya, jadi).
                4. Huruf Kapital: awal kalimat, nama orang, nama tempat/geografis, nama hari/bulan/hari besar, kata ganti Anda/Kami/Kita/Saya.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "bindo_bab6",
            subject = "Bahasa Indonesia",
            chapterLabel = "Bab 6",
            title = "Sayangi Bumi",
            keyPoints = listOf("Sumber Informasi", "Prinsip 3M", "Imbuhan pe-an", "Diagram Sebab-Akibat"),
            summaryPromptContext = """
                Bahasa Indonesia Kelas 5 Bab 6: Sayangi Bumi.
                1. Sumber Informasi: blog internet, majalah cetak, infografik, media massa (TV, radio, koran, berita online terpercaya).
                2. Pengelolaan Sampah Prinsip 3M: Mengurangi/Reduce (kurangi sekali pakai), Menggunakan Kembali/Reuse (pakai ulang/kreatif), Mendaur Ulang/Recycle (pilah dan daur ulang).
                3. Imbuhan 'pe-an': membentuk kata benda tempat atau kumpulan: gunung -> pegunungan (kumpulan gunung), desa -> pedesaan (wilayah pertanian), labuh -> pelabuhan (tempat singgah kapal).
                4. Diagram Sebab-Akibat Lingkungan: buang sampah -> lingkungan kotor & bau; tebang liar -> banjir & tanah longsor; plastik berlebih -> ekosistem laut terganggu; polusi udara -> gangguan kesehatan.
            """.trimIndent()
        ),

        // === IPAS KELAS 5 ===
        CurriculumChapter(
            id = "ipas_bab1",
            subject = "IPAS",
            chapterLabel = "Bab 1",
            title = "Di Mana Indonesia Berada?",
            keyPoints = listOf("Letak Geografis & Astronomis", "Negara Kepulauan & Lempeng", "Jalur Perdagangan", "Maritim & Agraris"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 1: Di Mana Indonesia Berada?
                1. Letak Indonesia: 6° LU – 11° LS dan 95° BT – 141° BT; di antara dua benua (Asia dan Australia) dan dua samudra (Hindia dan Pasifik).
                2. Negara Kepulauan: >17.000 pulau (Sumatra, Jawa, Kalimantan, Sulawesi, Papua). Berada di pertemuan 3 lempeng: Eurasia, Indo-Australia, Pasifik -> sering gempa & banyak gunung api.
                3. Jalur Perdagangan Dunia: Selat Malaka (menghubungkan Samudra Hindia & Laut Cina Selatan) dan Selat Sunda (Samudra Hindia & Laut Jawa).
                4. Negara Maritim (luas laut besar: nelayan, transportasi laut, pariwisata bahari) & Negara Agraris (tanah subur tropis: pangan padi/jagung, perkebunan sawit/karet/kopi/kakao, buah tropis).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab2",
            subject = "IPAS",
            chapterLabel = "Bab 2",
            title = "Majulah Daerahku!",
            keyPoints = listOf("Produksi, Distribusi, Konsumsi", "Pelaku Ekonomi", "Faktor Perekonomian", "Konsumen Bijak"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 2: Majulah Daerahku!
                1. Proses Kegiatan Ekonomi: Produksi (menghasilkan barang/jasa), Distribusi (menyalurkan dari produsen ke konsumen), Konsumsi (menggunakan barang/jasa).
                2. Pelaku Ekonomi: Produsen, Distributor, Konsumen, dan Pemerintah (mengatur, membuat kebijakan, layanan publik).
                3. Faktor Pendukung Ekonomi Daerah: Sumber Daya Alam, Sumber Daya Manusia, Modal (uang, alat, mesin, teknologi), Infrastruktur (jalan, jembatan, pelabuhan, pasar, listrik), Kebijakan Pemerintah, Kerja Sama Masyarakat (gotong royong).
                4. Pelaku Ekonomi Bijak: penuhi kebutuhan bukan keinginan, belanja sesuai kemampuan, pilih barang berkualitas, tidak boros SDA, dukung produk lokal.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab3",
            subject = "IPAS",
            chapterLabel = "Bab 3",
            title = "Harmoni dalam Ekosistem",
            keyPoints = listOf("Biotik & Abiotik", "Rantai Makanan", "Jaring-jaring Makanan", "Peran Manusia"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 3: Harmoni dalam Ekosistem.
                1. Komponen Ekosistem: Biotik (makhluk hidup: produsen/tumbuhan, konsumen/hewan, pengurai/dekomposer) & Abiotik (benda tak hidup: cahaya matahari untuk fotosintesis, air, tanah, udara, suhu).
                2. Ekosistem Khas: Hutan Hujan Tropis (orangutan, curah hujan tinggi), Laut (terumbu karang, plankton, ikan), Padang Rumput (rusa, kelinci, zebra).
                3. Rantai Makanan: urutan makan dan dimakan (Rumput/Produsen -> Belalang/Konsumen I -> Katak/Konsumen II -> Ular/Konsumen III -> Elang/Konsumen IV). Jaring-jaring makanan: gabungan beberapa rantai makanan yang menjaga keseimbangan ekosistem.
                4. Peran Manusia: Positif (reboisasi, buang sampah pada tempatnya, hemat air, lindungi satwa) vs Negatif (pencemaran, penebangan liar, perburuan liar).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab4",
            subject = "IPAS",
            chapterLabel = "Bab 4",
            title = "Air Sumber Kehidupan",
            keyPoints = listOf("Arah Aliran Air", "Siklus Air (Evaporasi-Infiltrasi)", "Perjalanan Air Bersih", "Hemat Air"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 4: Air Sumber Kehidupan.
                1. Arah aliran air: mengalir dari tempat tinggi ke tempat rendah karena gaya gravitasi (run off ke sungai/danau/laut atau infiltrasi ke tanah).
                2. Siklus Air: (1) Evaporasi (penguapan air laut/danau/sungai oleh matahari), (2) Kondensasi (uap air mendingin menjadi awan), (3) Transpirasi (tumbuhan melepaskan uap air lewat daun), (4) Presipitasi (hujan/salju/es), (5) Infiltrasi (air meresap ke pori tanah menjadi air tanah).
                3. Sumber Air Bersih: air sungai, air tanah/sumur, mata air, air hujan. Diolah di instalasi pengolahan air -> reservoir/tandon -> pipa distribusi -> rumah.
                4. Menghemat Air: matikan keran saat sikat gigi, tampung air hujan, perbaiki keran bocor.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab5",
            subject = "IPAS",
            chapterLabel = "Bab 5",
            title = "Daerahku yang Bersejarah",
            keyPoints = listOf("Periodisasi Sejarah", "Warisan Budaya Benda & Tak Benda", "Akulturasi", "Kerajaan Majapahit"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 5: Daerahku yang Bersejarah.
                1. Periodisasi Sejarah Indonesia: Masa Praaksara (belum mengenal tulisan), Masa Hindu-Buddha (candi), Masa Islam (kerajaan & perdagangan), Masa Kolonial (penjajahan bangsa Eropa), Masa Pergerakan Kemerdekaan (merdeka 1945).
                2. Warisan Budaya Benda (fisik: Candi Borobudur, Keraton Yogyakarta, rumah adat, keris, tenun) & Warisan Budaya Tak Benda (tari tradisional, upacara adat, cerita rakyat, lagu daerah, bahasa daerah).
                3. Akulturasi Budaya: perpaduan dua budaya tanpa menghilangkan ciri asli (Masjid Menara Kudus: Hindu-Buddha + Islam; Batik pesisir: lokal + Tiongkok).
                4. Kerajaan Majapahit: berdiri abad ke-13 di Jawa Timur, dipimpin Raden Wijaya, Gajah Mada, Hayam Wuruk; semboyan Bhinneka Tunggal Ika (berbeda-beda tetapi tetap satu).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab6",
            subject = "IPAS",
            chapterLabel = "Bab 6",
            title = "Perubahan pada Diriku",
            keyPoints = listOf("Masa Pubertas", "Perubahan Fisik & Emosi", "Pola Hidup Sehat", "Privasi Tubuh"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 6: Perubahan pada Diriku.
                1. Masa Puber: peralihan anak-anak menuju remaja (perubahan fisik, emosi lebih sensitif/mandiri, cara berpikir kritis). Laki-laki: suara lebih berat, tumbuh jakun, mimpi basah, otot berkembang. Perempuan: payudara berkembang, menstruasi, pinggul melebar.
                2. Menjaga Kesehatan: makan bergizi seimbang, cukup air putih, tidur 8-10 jam, kebersihan tubuh, olahraga minimal 30 menit/hari.
                3. Privasi Tubuh: area tertutup pakaian dalam adalah privasi; tegas katakan 'TIDAK' terhadap sentuhan tidak nyaman, rahasia yang mengancam, pemotretan bagian pribadi, atau bujukan hadiah; segera lapor ke orang tua/guru.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab7",
            subject = "IPAS",
            chapterLabel = "Bab 7",
            title = "Bermain dengan Cahaya",
            keyPoints = listOf("Sifat Cahaya", "Benda Bening/Buram/Gelap", "Pemanfaatan Cahaya", "Bayangan & Pelangi"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 7: Bermain dengan Cahaya.
                1. Sifat Cahaya: merambat lurus, dapat memantul, dapat menembus benda bening, dapat diuraikan (dispersi cahaya putih oleh prisma/titik air menjadi merah-jingga-kuning-hijau-biru-nila-ungu).
                2. Jenis Benda terhadap Cahaya: Bening (kaca bening, air jernih), Buram (kaca buram, kertas minyak), Gelap/Opaque (kayu, batu, besi, karton tebal -> membentuk bayangan).
                3. Ukuran Bayangan: sumber cahaya dekat benda -> bayangan besar; sumber cahaya jauh -> bayangan kecil.
                4. Pemanfaatan: cermin, kaca pembesar, jendela, kacamata hitam, panel surya, lampu lalu lintas.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "ipas_bab8",
            subject = "IPAS",
            chapterLabel = "Bab 8",
            title = "Ramai karena Bunyi",
            keyPoints = listOf("Sumber & Intensitas Bunyi", "Perambatan Bunyi", "Gaung & Gema", "Ekolokasi & Peredam"),
            summaryPromptContext = """
                IPAS Kelas 5 Bab 8: Ramai karena Bunyi.
                1. Bunyi berasal dari benda yang bergetar (drum dipukul, senar dipetik, speaker). Intensitas bunyi dipengaruhi kuat-lemah getaran, jarak pendengar, dan penghalang.
                2. Perambatan Bunyi: butuh zat perantara (tidak bisa di ruang hampa/vakum). Paling cepat merambat melalui zat padat (rel kereta, kaleng benang), lalu cairan (komunikasi lumba-lumba), kemudian udara.
                3. Pemantulan Bunyi: Gaung (bunyi pantul terdengar samar hampir bersamaan dengan bunyi asli di ruang kosong) & Gema (terdengar jelas setelah bunyi asli di tebing/gua).
                4. Ekolokasi: kemampuan kelelawar dan lumba-lumba menentukan letak benda lewat pantulan bunyi. Peredam bunyi: bahan lunak dan tebal (busa, karpet, wol) di studio/bioskop.
            """.trimIndent()
        ),

        // === MATEMATIKA KELAS 5 ===
        CurriculumChapter(
            id = "mtk_bab1",
            subject = "Matematika",
            chapterLabel = "Bab 1",
            title = "Bilangan Cacah sampai 100.000",
            keyPoints = listOf("Nilai Tempat", "Membandingkan & Mengurutkan", "Komposisi & Dekomposisi", "Operasi Hitung"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 1: Bilangan Cacah sampai 100.000.
                1. Nilai tempat: Puluh Ribu, Ribu, Ratus, Puluh, Satuan (contoh: 52.740 = lima puluh dua ribu tujuh ratus empat puluh; 87.605 = delapan puluh tujuh ribu enam ratus lima).
                2. Membandingkan dari nilai tempat terbesar ke terkecil (32.450 > 25.890) dan mengurutkan naik/turun.
                3. Komposisi (40.000 + 6.000 + 300 + 20 + 5 = 46.325) & Dekomposisi (78.942 = 70.000 + 8.000 + 900 + 40 + 2).
                4. Operasi Hitung: Penjumlahan (24.567 + 13.428 = 37.995), Pengurangan (56.780 - 21.345 = 35.435), Perkalian bersusun (326 x 145 = 47.270), Pembagian porogapit (372 : 3 = 124).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab2",
            subject = "Matematika",
            chapterLabel = "Bab 2",
            title = "KPK dan FPB",
            keyPoints = listOf("Kelipatan & Kelipatan Persekutuan", "Faktor & Faktor Persekutuan", "Faktorisasi Prima", "KPK & FPB"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 2: KPK dan FPB.
                1. Kelipatan: hasil kali dengan bilangan cacah (kelipatan 4: 4, 8, 12, 16...; kelipatan 6: 6, 12, 18, 24...). KPK dari 4 dan 6 = 12.
                2. Faktor: bilangan yang membagi habis (faktor 12: 1, 2, 3, 4, 6, 12; faktor 18: 1, 2, 3, 6, 9, 18). FPB dari 12 dan 18 = 6.
                3. Faktorisasi Prima: 12 = 2² × 3, 18 = 2 × 3².
                   - KPK ambil semua faktor prima dengan pangkat tertinggi: 2² × 3² = 4 × 9 = 36.
                   - FPB ambil faktor prima yang sama saja dengan pangkat terkecil: 2 × 3 = 6.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab3",
            subject = "Matematika",
            chapterLabel = "Bab 3",
            title = "Bilangan Pecahan",
            keyPoints = listOf("Membandingkan Pecahan", "Mengurutkan Pecahan", "Penjumlahan Pecahan", "Pengurangan Pecahan"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 3: Bilangan Pecahan (a/b dengan b ≠ 0).
                1. Membandingkan & Mengurutkan: penyebut sama langsung bandingkan pembilang (3/7 < 5/7); penyebut berbeda samakan dengan KPK (2/3 = 8/12 < 3/4 = 9/12). Contoh urutan: 3/10 < 2/5 < 1/2 < 7/10.
                2. Penjumlahan: samakan penyebut dulu (2/7 + 3/7 = 5/7; 1/3 + 2/5 = 5/15 + 6/15 = 11/15).
                3. Pengurangan: samakan penyebut dulu (5/8 - 2/8 = 3/8; 3/4 - 1/6 = 9/12 - 2/12 = 7/12).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab4",
            subject = "Matematika",
            chapterLabel = "Bab 4",
            title = "Keliling Bangun Datar",
            keyPoints = listOf("Keliling Segitiga", "Keliling Segiempat", "Keliling Segi Banyak Beraturan", "Bangun Gabungan"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 4: Keliling Bangun Datar (jumlah panjang seluruh sisi terluar).
                1. Segitiga: K = a + b + c.
                2. Segiempat: Persegi K = 4 × s; Persegi Panjang K = 2 × (p + l); Jajar Genjang K = 2 × (a + b); Trapesium K = a + b + c + d; Layang-layang K = 2 × (a + b).
                3. Segi banyak beraturan: Segi lima K = 5 × s; Segi enam K = 6 × s.
                4. Keliling Bangun Gabungan: hitung hanya sisi-sisi terluar (batas luar) bangun tersebut.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab5",
            subject = "Matematika",
            chapterLabel = "Bab 5",
            title = "Luas Daerah Bangun Datar",
            keyPoints = listOf("Konsep Satuan Luas", "Rumus Luas Bangun Datar", "Luas Bangun Gabungan", "Hubungan Keliling & Luas"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 5: Luas Daerah Bangun Datar (cm², m², km²).
                1. Rumus Luas: Persegi L = s × s; Persegi Panjang L = p × l; Segitiga L = 1/2 × a × t; Jajar Genjang L = a × t; Trapesium L = 1/2 × (a + b) × t; Layang-layang L = 1/2 × d1 × d2.
                2. Luas Bangun Gabungan: uraikan menjadi beberapa persegi panjang/segitiga sederhana lalu jumlahkan luasnya.
                3. Hubungan Keliling & Luas: bangun dengan keliling sama belum tentu memiliki luas yang sama (misal persegi s=4 cm -> K=16 cm, L=16 cm² vs persegi panjang 6×2 cm -> K=16 cm, L=12 cm²).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab6",
            subject = "Matematika",
            chapterLabel = "Bab 6",
            title = "Sudut",
            keyPoints = listOf("Pengertian & Bagian Sudut", "Jenis-jenis Sudut", "Mengukur Sudut (Busur Derajat)", "Melukis Sudut"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 6: Sudut (daerah yang dibentuk dua ruas garis yang bertemu pada satu titik pangkal).
                1. Jenis Sudut: Lancip (0° < x < 90°), Siku-siku (x = 90°, contoh bingkai jendela/pintu/ubin), Tumpul (90° < x < 180°), Lurus (x = 180°), Refleks (180° < x < 360°).
                2. Sudut kongruen: dua sudut yang besarnya sama.
                3. Mengukur & Melukis Sudut dengan busur derajat: letakkan titik pusat busur di titik pangkal O, garis 0° berimpit dengan salah satu kaki sudut, baca/tandai skala kaki sudut lainnya.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab7",
            subject = "Matematika",
            chapterLabel = "Bab 7",
            title = "Membandingkan Ciri-ciri Bangun Datar",
            keyPoints = listOf("Klasifikasi Segitiga", "Sifat Segiempat", "Diagonal & Sudut Segiempat"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 7: Membandingkan Ciri-ciri Bangun Datar.
                1. Segitiga (3 sisi, 3 titik sudut, jumlah sudut selalu 180°): berdasarkan sisi (Sama Sisi sudut 60°, Sama Kaki, Sembarang); berdasarkan sudut (Lancip, Siku-siku, Tumpul).
                2. Segiempat (4 sisi, 4 titik sudut):
                   - Persegi: 4 sisi sama panjang, 4 sudut 90°, diagonal sama panjang & berpotongan tegak lurus.
                   - Persegi Panjang: sisi berhadapan sejajar & sama panjang, 4 sudut 90°, diagonal sama panjang saling membagi dua.
                   - Jajar Genjang: sisi berhadapan sejajar & sama panjang, sudut berdekatan jumlahnya 180°, diagonal saling membagi dua tidak sama panjang.
                   - Trapesium: sepasang sisi sejajar (alas dan atas), sudut berdekatan pada kaki yang sama berjumlah 180°.
                   - Belah Ketupat: 4 sisi sama panjang, sudut berhadapan sama besar, diagonal saling tegak lurus.
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab8",
            subject = "Matematika",
            chapterLabel = "Bab 8",
            title = "Data",
            keyPoints = listOf("Pengumpulan Data", "Membaca Piktogram", "Diagram Batang"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 8: Data.
                1. Mengumpulkan Data: melalui pengamatan, wawancara, atau angket (kuesioner); tentukan tujuan, sumber, cara, catat teliti.
                2. Piktogram: penyajian data dengan gambar/simbol. Perhatikan keterangan nilai setiap gambar (misal 1 gambar apel = 2 siswa, maka 4 gambar apel = 8 siswa).
                3. Diagram Batang: penyajian data menggunakan batang tegak/mendatar dengan sumbu kategori dan sumbu banyak data (skala harus sama dan dimulai dari 0).
            """.trimIndent()
        ),
        CurriculumChapter(
            id = "mtk_bab9",
            subject = "Matematika",
            chapterLabel = "Bab 9",
            title = "Bilangan Cacah sampai 1.000.000",
            keyPoints = listOf("Nilai Tempat Ratus Ribu", "Membaca & Menulis Bilangan", "Membandingkan & Mengurutkan", "Komposisi & Dekomposisi"),
            summaryPromptContext = """
                Matematika Kelas 5 Bab 9: Bilangan Cacah sampai 1.000.000.
                1. Nilai tempat: Ratus Ribu, Puluh Ribu, Ribu, Ratus, Puluh, Satuan. Contoh: 485.672 = 400.000 + 80.000 + 5.000 + 600 + 70 + 2 (empat ratus delapan puluh lima ribu enam ratus tujuh puluh dua).
                2. Tanda titik (.) sebagai pemisah ribuan; contoh 1.000.000 dibaca satu juta.
                3. Membandingkan & Mengurutkan dari nilai tempat tertinggi (582.316 > 578.923; 415.207 < 415.297).
                4. Komposisi & Dekomposisi: 648.105 = (6 × 100.000) + (4 × 10.000) + (8 × 1.000) + (1 × 100) + (5 × 1).
            """.trimIndent()
        )
    )

    val subjects: List<String> = chapters.map { it.subject }.distinct()
}
