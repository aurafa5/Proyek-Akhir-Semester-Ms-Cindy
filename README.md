# StudySpace - Smart Study Tracker

<div align="center">
  <h3>Aplikasi Android Pelacak Belajar Cerdas</h3>
  <p>Berbasis Jetpack Compose · Material Design 3 · MVVM · Room Database · Sensor Framework</p>
</div>

---

## Deskripsi Aplikasi

**StudySpace** adalah aplikasi Android untuk membantu siswa dan mahasiswa dalam merekam, memantau, dan menganalisis aktivitas belajar mereka secara lebih cerdas. Aplikasi ini menggabungkan timer belajar berbasis Pomodoro, monitoring sensor lingkungan secara real-time, dan analitik statistik yang dihitung langsung dari database lokal.

---

## Fitur Utama

### 1. Home – Dashboard
- Greeting berdasarkan waktu (pagi / siang / sore / malam)
- Total waktu belajar hari ini
- Jumlah sesi belajar hari ini
- Daily study goal dengan progress indicator
- 5 sesi belajar terbaru
- Quick action untuk memulai sesi belajar

### 2. Focus – Study Timer
- Circular progress timer dengan animasi halus
- Tiga mode: **Focus**, **Short Break**, **Long Break**
- Pilihan durasi preset: 25, 30, 45, 60 menit
- Durasi kustom (1–180 menit)
- Input nama mata pelajaran dan catatan belajar
- Tombol Start, Pause/Resume, Reset
- Penyimpanan otomatis sesi ke database saat timer selesai
- Dialog ringkasan sesi (durasi aktual, cahaya rata-rata, jumlah gerakan)
- Timer berbasis timestamp — akurat, tidak terpengaruh recomposition

### 3. Environment – Sensor Monitoring
- **Light Sensor (TYPE_LIGHT)**: membaca intensitas cahaya real-time dalam lux
  - Kategori: Sangat Gelap / Gelap / Redup / Cukup / Terang / Sangat Terang
  - Rekomendasi pencahayaan untuk belajar
  - Grafik perubahan intensitas cahaya
- **Accelerometer (TYPE_ACCELEROMETER)**: membaca nilai X, Y, Z
  - Low-pass filter untuk meredam noise
  - Threshold berbasis magnitude untuk deteksi gerakan
  - Counter jumlah gerakan selama sesi
  - Indikator Bergerak / Stabil
- Toggle aktifkan/nonaktifkan monitoring
- Pesan informatif jika sensor tidak tersedia di perangkat

### 4. History – Riwayat Belajar
- Daftar semua sesi belajar (LazyColumn)
- Filter berdasarkan mata pelajaran
- Detail sesi (durasi, mode, sensor data)
- Edit catatan sesi
- Hapus sesi dengan konfirmasi
- Empty state yang informatif

### 5. Statistics – Analitik Belajar
- Total waktu belajar keseluruhan
- Total jumlah sesi
- Rata-rata durasi sesi
- Mata pelajaran paling sering dipelajari
- Grafik batang durasi belajar harian (7 hari terakhir)
- Semua data dihitung langsung dari SQLite

---

## Teknologi yang Digunakan

| Komponen | Teknologi |
|---|---|
| Bahasa | Kotlin |
| UI Framework | Jetpack Compose |
| Design System | Material Design 3 |
| Arsitektur | MVVM (Model–View–ViewModel) |
| Database | SQLite via Room Database |
| Reactive Data | Kotlin Flow + Coroutines |
| Navigasi | Navigation Compose |
| Sensor | Android Sensor Framework |
| Build System | Gradle Kotlin DSL |
| Min SDK | 24 (Android 7.0) |
| Compile SDK | 34 (Android 14) |

---

## Dependency Utama

```toml
# Version Catalog (gradle/libs.versions.toml)
agp = "8.5.2"
kotlin = "2.0.21"
composeBom = "2024.10.01"
navigationCompose = "2.8.3"
room = "2.6.1"
ksp = "2.0.21-1.0.27"
lifecycleRuntimeKtx = "2.8.6"
coroutines = "1.9.0"
```

---

## Arsitektur MVVM

```
┌─────────────────────────────┐
│         UI Layer            │
│  (Composable Screens)       │
│  - HomeScreen               │
│  - FocusScreen              │
│  - EnvironmentScreen        │
│  - HistoryScreen            │
│  - StatisticsScreen         │
└──────────┬──────────────────┘
           │ observe StateFlow
┌──────────▼──────────────────┐
│      ViewModel Layer        │
│  - HomeViewModel            │
│  - FocusViewModel           │
│  - EnvironmentViewModel     │
│  - HistoryViewModel         │
│  - StatisticsViewModel      │
└──────────┬──────────────────┘
           │ call suspend fun / Flow
┌──────────▼──────────────────┐
│     Repository Layer        │
│  - StudyRepository          │
│  - SensorRepository         │
└──────┬───────────┬──────────┘
       │           │
┌──────▼──┐  ┌────▼──────────┐
│  Room   │  │ SensorManager │
│  (DAOs) │  │    Helper     │
└─────────┘  └───────────────┘
```

**Prinsip MVVM:**
- **UI Layer** hanya mengamati StateFlow dari ViewModel — tidak ada business logic di Composable
- **ViewModel** menyimpan UI state, mengorkestrasi data dari Repository
- **Repository** menjadi single source of truth — abstraksi database dan sensor
- **Data Layer** berisi Entity, DAO, dan Database Room

---

## Database – SQLite dengan Room

### Tabel StudySession
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | Int (PK) | Auto generate |
| subject | String | Nama mata pelajaran |
| startTime | Long | Unix timestamp mulai |
| endTime | Long | Unix timestamp selesai |
| duration | Int | Durasi aktual (detik) |
| notes | String | Catatan belajar |
| averageLight | Float | Rata-rata intensitas cahaya |
| minLight | Float | Intensitas cahaya minimum |
| maxLight | Float | Intensitas cahaya maksimum |
| movementCount | Int | Jumlah gerakan perangkat |
| mode | String | Mode sesi (focus/short_break/long_break) |

### Tabel StudySubject
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | Int (PK) | Auto generate |
| subjectName | String | Nama mata pelajaran |
| color | String | Warna hex representasi |
| createdAt | Long | Waktu dibuat |

### Tabel SensorLog
| Kolom | Tipe | Keterangan |
|---|---|---|
| id | Int (PK) | Auto generate |
| sessionId | Int (FK) | Relasi ke StudySession |
| lightIntensity | Float | Intensitas cahaya dalam lux |
| recordedAt | Long | Waktu pencatatan |

**Foreign Key:** `SensorLog.sessionId → StudySession.id` (CASCADE DELETE)

---

## Sensor yang Digunakan

### 1. `Sensor.TYPE_LIGHT` – Light Sensor
- Membaca intensitas cahaya aktual (lux) dari hardware sensor
- Kategori pencahayaan berdasarkan threshold (10, 50, 200, 500, 1000 lux)
- Rekomendasi kondisi belajar
- History pembacaan untuk grafik real-time
- Merekam min/max/rata-rata selama sesi untuk disimpan ke database

### 2. `Sensor.TYPE_ACCELEROMETER` – Accelerometer
- Membaca nilai X, Y, Z dari sensor hardware
- **Low-pass filter** (alpha = 0.8) untuk meredam noise
- **Threshold magnitude** (1.5 m/s²) untuk menghindari false positive
- Counter gerakan yang terus bertambah selama sesi aktif
- Indikator visual Bergerak / Stabil

**Catatan Sensor:**
> Data accelerometer hanya menunjukkan aktivitas gerakan perangkat, bukan ukuran konsentrasi manusia.
> Jika perangkat tidak memiliki sensor tertentu, aplikasi menampilkan pesan "sensor tidak tersedia" — tidak menggunakan data palsu.

### Sensor Lifecycle
- Register hanya saat dibutuhkan (`startMonitoring()`)
- Unregister saat tidak digunakan (`stopMonitoring()`)
- Implementasi `DefaultLifecycleObserver` untuk lifecycle-aware
- Resume sensor setelah Activity pause/resume
- Cleanup di `onDestroy()`

---

## Struktur Folder

```
com.example.studyspace/
├── data/
│   ├── database/
│   │   ├── StudyDatabase.kt      # Room Database (Singleton)
│   │   ├── StudyDao.kt           # Data Access Object
│   │   ├── StudySession.kt       # Entity sesi belajar
│   │   ├── StudySubject.kt       # Entity mata pelajaran
│   │   └── SensorLog.kt          # Entity log sensor
│   └── repository/
│       ├── StudyRepository.kt    # Repository utama
│       └── SensorRepository.kt  # Repository sensor
├── sensor/
│   └── SensorManagerHelper.kt   # Sensor lifecycle-aware manager
├── viewmodel/
│   ├── HomeViewModel.kt
│   ├── FocusViewModel.kt
│   ├── EnvironmentViewModel.kt
│   ├── HistoryViewModel.kt
│   └── StatisticsViewModel.kt
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt      # Route definitions & nav items
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── FocusScreen.kt
│   │   ├── EnvironmentScreen.kt
│   │   ├── HistoryScreen.kt
│   │   └── StatisticsScreen.kt
│   ├── components/
│   │   ├── StudyCard.kt          # Reusable card components
│   │   ├── TimerComponent.kt     # Circular timer
│   │   ├── SensorCard.kt         # Sensor display cards
│   │   ├── StatisticsChart.kt    # Custom bar chart
│   │   └── BottomNavigation.kt   # Bottom nav bar
│   └── theme/
│       ├── Color.kt              # Dark premium palette
│       ├── Theme.kt              # Material3 theme
│       └── Type.kt               # Typography
├── MainActivity.kt               # Entry point
└── StudySpaceApplication.kt      # Application class
```

---

## Screenshot Aplikasi

> **Catatan:** Screenshot diambil dari emulator Android Studio.

Folder `screenshots/` tersedia untuk menyimpan hasil screenshot dari setiap halaman:

```
screenshots/
├── 01_home_dashboard.png
├── 02_focus_timer_idle.png
├── 03_focus_timer_running.png
├── 04_focus_session_complete.png
├── 05_environment_monitoring.png
├── 06_history_list.png
├── 07_history_detail.png
├── 08_statistics.png
```

**Cara mengambil screenshot dari Android Studio:**
1. Jalankan emulator Android
2. Jalankan aplikasi
3. Di panel **Emulator**, klik ikon kamera (📷)
4. Screenshot tersimpan otomatis ke folder yang ditunjuk

---

## Cara Menjalankan Aplikasi

### Prasyarat
- **Android Studio** Hedgehog (2023.1.1) atau lebih baru
- **JDK 17** atau lebih baru
- **Android SDK** dengan API Level 24+
- **Emulator** Android API 24–34, atau perangkat fisik

### Langkah-langkah

1. **Buka project di Android Studio:**
   ```
   File → Open → Pilih folder StudySpace
   ```

2. **Tunggu Gradle Sync selesai**
   - Android Studio akan otomatis mengunduh dependency
   - Pastikan koneksi internet aktif pada sync pertama

3. **Build project:**
   ```
   Build → Make Project (Ctrl+F9)
   ```

4. **Jalankan aplikasi:**
   ```
   Run → Run 'app' (Shift+F10)
   ```
   Pilih emulator atau perangkat fisik yang sudah terhubung.

### Catatan Penting
- Gunakan **perangkat fisik** untuk pengujian sensor terbaik — emulator umumnya tidak mendukung sensor cahaya hardware nyata
- Emulator mendukung accelerometer dengan simulasi
- Semua data tersimpan secara **lokal** — tidak membutuhkan koneksi internet

---

## Pengujian

### Checklist Pengujian

| Fitur | Status |
|---|---|
| Navigasi antar 5 halaman | ✅ |
| Timer mulai / jeda / lanjut / reset | ✅ |
| Timer berbasis timestamp (tidak tereset saat pindah halaman) | ✅ |
| Validasi form sebelum mulai sesi | ✅ |
| Simpan sesi ke database saat timer selesai | ✅ |
| Tampilkan ringkasan sesi | ✅ |
| Sensor cahaya real-time | ✅ (perangkat fisik) |
| Sensor accelerometer real-time | ✅ |
| Filter riwayat berdasarkan mata pelajaran | ✅ |
| Edit catatan sesi | ✅ |
| Hapus sesi dengan konfirmasi | ✅ |
| Statistik mingguan dari database | ✅ |
| Empty state di semua halaman | ✅ |
| Data tetap ada setelah app ditutup | ✅ |
| Dark theme konsisten | ✅ |

### Keterbatasan Emulator
- Sensor cahaya (`TYPE_LIGHT`) umumnya tidak tersedia di emulator Android
- Aplikasi akan menampilkan pesan "sensor tidak tersedia" — ini perilaku yang benar
- Gunakan perangkat fisik untuk menguji fitur sensor cahaya secara lengkap

---

## Langkah Upload ke GitHub

```bash
# 1. Inisialisasi git di folder project
cd C:\Users\user\.gemini\antigravity\scratch\StudySpace
git init

# 2. Tambahkan .gitignore untuk Android
# (download dari https://github.com/github/gitignore/blob/main/Android.gitignore)

# 3. Stage semua file
git add .

# 4. Commit pertama
git commit -m "feat: Initial commit StudySpace Android App"

# 5. Buat repository di GitHub (via browser github.com)

# 6. Tambahkan remote
git remote add origin https://github.com/USERNAME/StudySpace.git

# 7. Push
git push -u origin main
```

---

## Informasi Developer

- **Nama Aplikasi:** StudySpace – Smart Study Tracker
- **Package:** `com.example.studyspace`
- **Versi:** 1.0
- **Target:** Siswa SMK/SMA dan Mahasiswa
- **Platform:** Android 7.0 (API 24) ke atas

---

*Dibangun dengan Kotlin + Jetpack Compose sebagai project demonstrasi MVVM Architecture dengan Android Sensor Framework.*
