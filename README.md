# 🍽️ ScanMakanan – Analisis Kalori Makanan dan Minuman

## 📖 Deskripsi Aplikasi

ScanMakanan merupakan aplikasi Android yang dirancang untuk membantu pengguna mengetahui estimasi kalori dari makanan atau minuman hanya dengan menggunakan foto. Aplikasi memanfaatkan teknologi Artificial Intelligence (AI) melalui Google Gemini API untuk menganalisis gambar dan memberikan informasi mengenai nama makanan/minuman serta estimasi jumlah kalorinya.

Aplikasi ini dikembangkan menggunakan Java pada platform Android Studio dengan penerapan Material Design untuk menghasilkan antarmuka yang sederhana, modern, dan mudah digunakan.

---

# 🎯 Tujuan Aplikasi

* Membantu pengguna mengetahui estimasi kalori makanan dan minuman.
* Memanfaatkan teknologi AI untuk analisis gambar secara otomatis.
* Memberikan pengalaman pengguna yang cepat dan mudah tanpa perlu memasukkan data secara manual.
* Mendukung gaya hidup sehat melalui informasi nutrisi yang lebih mudah diakses.

---

# ✨ Fitur Utama

### 1. Splash Screen

* Menampilkan identitas aplikasi saat pertama kali dijalankan.
* Menampilkan informasi lokasi pengguna berdasarkan GPS.
* Navigasi otomatis menuju halaman utama.

### 2. Ambil Foto Makanan/Minuman

* Menggunakan kamera perangkat secara langsung.
* Foto yang diambil akan dikirim untuk proses analisis.

### 3. Pilih Gambar dari Galeri

* Pengguna dapat memilih gambar makanan atau minuman yang sudah tersimpan.
* Mendukung berbagai format gambar.

### 4. Preview Gambar

* Menampilkan gambar yang dipilih sebelum dianalisis.
* Pengguna dapat mengganti gambar jika diperlukan.

### 5. Analisis Menggunakan AI

* Menggunakan Google Gemini API.
* Mengidentifikasi objek makanan atau minuman pada gambar.
* Menghasilkan informasi berupa:

  * Nama makanan/minuman
  * Estimasi jumlah kalori

### 6. Hasil Analisis

* Menampilkan hasil analisis secara terstruktur.
* Menampilkan status proses analisis.
* Menampilkan informasi hasil yang mudah dibaca.

---

# 🏗️ Arsitektur Aplikasi

Aplikasi terdiri dari beberapa Activity utama:

## SplashActivity

Berfungsi sebagai halaman pembuka aplikasi.

Tugas:

* Menampilkan logo aplikasi.
* Meminta izin lokasi.
* Mengambil lokasi pengguna.
* Mengarahkan pengguna ke halaman utama.

---

## MainActivity

Halaman utama aplikasi.

Tugas:

* Menampilkan menu pengambilan gambar.
* Membuka kamera.
* Membuka galeri.
* Mengirim gambar ke halaman analisis.

---

## AnalysisActivity

Halaman pratinjau gambar.

Tugas:

* Menampilkan gambar yang dipilih.
* Memberikan opsi mengganti gambar.
* Menjalankan proses analisis AI.

---

## AnalysisResultActivity

Halaman hasil analisis.

Tugas:

* Mengirim gambar ke Gemini API.
* Mengolah hasil respons AI.
* Menampilkan nama makanan/minuman.
* Menampilkan estimasi kalori.

---

<img width="1836" height="3264" alt="20260303_162426" src="https://github.com/user-attachments/assets/3d5e375f-8325-4da9-a6ca-1e20c8f5421f" />

# 🎨 Analisis UI/UX

## 1. Splash Screen

### UI

* Menggunakan tampilan minimalis.
* Menampilkan logo di tengah layar.
* Menampilkan nama aplikasi dengan tipografi tegas.
* Menampilkan informasi lokasi pengguna.

### UX

* Memberikan identitas aplikasi sejak awal.
* Menunjukkan personalisasi melalui lokasi pengguna.
* Waktu tampilan singkat sehingga tidak mengganggu pengguna.

---

<img width="1836" height="3264" alt="20260303_162534" src="https://github.com/user-attachments/assets/7fbaf9e7-8352-46d3-8aaf-d3ad29b7b811" />

## 2. Halaman Utama (Main Screen)

### UI

* Menggunakan AppBar di bagian atas.
* Terdapat indikator langkah (Step Indicator) yang menunjukkan proses penggunaan aplikasi.
* Tombol kamera dan galeri dibuat dalam bentuk card yang mudah dikenali.
* Dominasi warna putih dan ungu untuk menjaga keterbacaan.

### UX

* Pengguna langsung memahami alur penggunaan.
* Tersedia dua metode input:

  * Kamera
  * Galeri
* Mengurangi jumlah klik untuk memulai analisis.

---

<img width="1836" height="3264" alt="20260303_162659" src="https://github.com/user-attachments/assets/796bbf84-8871-4634-a113-cdeb8a429026" />

## 3. Halaman Preview Analisis

### UI

* Menampilkan gambar dalam ukuran besar.
* Menampilkan progress step pada tahap kedua.
* Tombol aksi utama:

  * Analisis AI
  * Ganti Foto
  * Tutup

### UX

* Memberikan kesempatan pengguna memverifikasi gambar sebelum dianalisis.
* Mengurangi risiko kesalahan analisis akibat gambar yang salah.

---

<img width="1836" height="3264" alt="20260303_162736" src="https://github.com/user-attachments/assets/e5d3957d-0527-47e6-8cf5-e66bd83fb88e" />

## 4. Halaman Hasil Analisis

### UI

* Menampilkan gambar yang dianalisis.
* Menampilkan ProgressBar saat AI bekerja.
* Menampilkan hasil analisis dalam format yang mudah dibaca.
* Menampilkan informasi:

  * Nama makanan/minuman
  * Estimasi kalori

### UX

* Memberikan feedback sistem secara real-time.
* Pengguna mengetahui bahwa proses sedang berjalan.
* Hasil disajikan secara sederhana dan cepat dipahami.

---

# 🔄 User Flow

1. Pengguna membuka aplikasi.
2. Splash Screen ditampilkan.
3. Pengguna masuk ke halaman utama.
4. Pengguna:

   * Mengambil foto, atau
   * Memilih gambar dari galeri.
5. Gambar ditampilkan pada halaman preview.
6. Pengguna menekan tombol "Analisis AI".
7. Sistem mengirim gambar ke Gemini API.
8. AI menganalisis gambar.
9. Hasil analisis ditampilkan.
10. Pengguna dapat menyimpan atau menutup hasil.

---

# 🛠️ Teknologi yang Digunakan

## Front-End

* Java
* Android SDK
* Material Design Components
* ConstraintLayout

## Back-End AI

* Google Gemini API

## Library Android

* Activity Result API
* FileProvider
* Fused Location Provider
* Material Toolbar

---

# 📱 Kebutuhan Sistem

### Minimum

* Android 8.0 (Oreo)
* RAM 2 GB
* Kamera perangkat
* Koneksi internet

### Rekomendasi

* Android 10 ke atas
* RAM 4 GB
* Koneksi internet stabil

---

# 🚀 Cara Menjalankan

1. Clone repository.
2. Buka menggunakan Android Studio.
3. Sync Gradle.
4. Tambahkan API Key Gemini.
5. Jalankan pada emulator atau perangkat Android.
6. Berikan izin kamera dan lokasi.
7. Mulai melakukan analisis makanan atau minuman.

---

# 📌 Pengembangan Selanjutnya

* Menambahkan database riwayat analisis.
* Menampilkan informasi nutrisi lebih lengkap.
* Menambahkan fitur rekomendasi pola makan sehat.
* Menambahkan grafik konsumsi kalori harian.
* Integrasi dengan aplikasi kesehatan lainnya.

---

# 👨‍💻 Developer

Felix Amon Sitinjak

Proyek Android AI untuk analisis kalori makanan dan minuman berbasis pengenalan gambar menggunakan Google Gemini API.
