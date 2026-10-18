<div align="center">

<!-- TARIK & LEPASKAN FILE logo_panda.png ANDA DI BARIS BAWAH INI -->
<img src="app/src/main/res/drawable/logo_panda.png" alt="Logo BambuQR PandaKode" width="130" />

# 🐼 BambuQR Studio — Smart QR & Barcode Maker
### **Official Native Android Product by PandaKode**

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-10B981?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Offline First](https://img.shields.io/badge/Privacy-100%25%20Offline%20%26%20Secure-F59E0B?style=for-the-badge&logo=shield&logoColor=black)
![API Level](https://img.shields.io/badge/Android-API%2024%2B%20(Nougat%20to%20Android%2016)-047857?style=for-the-badge&logo=android&logoColor=white)

**BambuQR Studio** adalah aplikasi Android Native modern dengan identitas visual **Emerald Bamboo & Royal Gold**. Dirancang untuk memenuhi kebutuhan personal, UMKM, hingga manajemen inventaris laboratorium dengan kemampuan membuat **9 jenis QR Code kustom**, **Pemindai Cerdas ML Kit**, **Barcode Produk 1D (Code-128)**, serta **Batch QR Generator**.

</div>

---

## 📸 Tampilan Aplikasi (App Screenshots)

> *Tampilan antarmuka cerah, bersih, dan modern dengan tema khas PandaKode (Hijau Bambu, Putih, Hitam Panda, dan Aksen Gold).*

| Buat QR & Studio Desain | 9 Kategori Lengkap | Scan & Bongkar Wi-Fi | Alat Pintar (Barcode & Batch) |
| :---: | :---: | :---: | :---: |
| *(Tarik & Lepas SS 1 di sini)* | *(Tarik & Lepas SS 2 di sini)* | *(Tarik & Lepas SS 3 di sini)* | *(Tarik & Lepas SS 4 di sini)* |

---

## ✨ Fitur Utama (Key Features)

### 1. 🌐 9 Kategori Smart QR Code Generator
Mendukung pembuatan kode QR otomatis tanpa perlu mengetik format teknis secara manual:
* **Link / URL & Teks Bebas:** Untuk website, portofolio, atau catatan instan.
* **Wi-Fi Otomatis (`WIFI:`):** Bagikan koneksi Wi-Fi kantor/kafe tanpa perlu mendikte *password* (mendukung WPA/WPA2, WEP, dan Tanpa Sandi).
* **Direct WhatsApp (`wa.me`):** Langsung membuka ruang *chat* WhatsApp ke nomor tujuan lengkap dengan teks pesan otomatis.
* **Kartu Nama Digital (`vCard 3.0`):** Simpan Nama, Nomor Telepon, dan Instansi langsung ke buku kontak HP dalam sekali *scan*.
* **Email Otomatis, Koordinat Google Maps, Profil Sosial Media (IG/TikTok/YT/LinkedIn), SMS Cepat,** dan **Agenda Kalender (`vEvent`)**.

### 2. 🎨 Studio Desain & Aksen Royal Gold
* **8 Palet Warna Eksklusif:** *Bambu & Gold, Klasik Panda, Hitam & Emas, Emerald Segar, Emas Mewah, Samudra Biru, Ruby Merah,* dan *Ungu Kerajaan*.
* **4 Variasi Bentuk Titik (*Dot Shapes*):** *Kotak Klasik, Sudut Halus (Rounded), Titik Bulat (Dots),* dan *Belah Ketupat (Diamond)*.
* **Custom Branding:** Penyisipan **Logo Panda Resmi** di tengah QR Code (dengan koreksi error tingkat tinggi `ErrorCorrectionLevel.H` sehingga tetap terbaca 100% akurat), bingkai emas (*Gold Frame*), dan **Pita Label Teks Kustom** di bagian bawah.

### 3. 🛠️ Alat Pintar Bisnis & Inventaris
* **Generator Barcode 1D (Code-128):** Membuat barcode garis standar industri untuk label produk toko, kasir, maupun penomoran aset inventaris laboratorium.
* **Batch QR Generator (Pembuatan Massal):** Membuat dan mengunduh puluhan QR Code sekaligus ke Galeri HP (`Pictures/BambuQR`) hanya dengan mengetik daftar item per baris.

### 4. 📷 Pemindai Kamera Instan & Wi-Fi Password Decoder
* Menggunakan **Google Play Services Code Scanner (ML Kit)** dengan *Auto-Zoom* cepat dan akurat.
* **Smart Wi-Fi Decoder:** Jika memindai atau membuka riwayat QR Wi-Fi, aplikasi otomatis mengurai dan menampilkan Nama SSID, Password, serta tipe keamanannya secara jelas.
* **Riwayat Permanen & Favorit ⭐:** Tersimpan aman di memori internal perangkat lengkap dengan fitur pencarian instan.

---

## 🏗️ Arsitektur & Tech Stack

| Komponen | Teknologi yang Digunakan |
| :--- | :--- |
| **Bahasa Pemrograman** | 100% Kotlin |
| **UI Toolkit** | Jetpack Compose (Material Design 3) — *Single-Activity Architecture* |
| **QR & Barcode Engine** | ZXing Core (`com.google.zxing:core:3.5.3`) + Custom Android 2D `Canvas` & `Path` Renderer |
| **Camera Scanner** | Google ML Kit Barcode UI (`play-services-code-scanner:16.1.0`) |
| **Penyimpanan Data** | Local JSON Serialization via `SharedPreferences` & Modern Android `MediaStore` API |
| **Keamanan & Privasi** | 100% *Client-Side Processing* (Tanpa pengiriman data sensitif ke server eksternal) |

---

## 🔒 Komitmen Privasi (Privacy & Data Safety)

Aplikasi ini dirancang dengan prinsip **Offline-First & Privacy-by-Design**:
1. Seluruh proses pembuatan dan pembacaan QR Code / Barcode berjalan **100% secara lokal di perangkat pengguna**.
2. Kata sandi Wi-Fi, nomor kontak, dan riwayat pemindaian **tidak pernah dikirim atau disimpan di server mana pun**.
3. Penyimpanan gambar ke galeri menggunakan `MediaStore` modern Android tanpa meminta izin akses pembacaan file pribadi pengguna.

---

## 👨‍💻 Pengembang (Author)

Dikembangkan oleh **Muhammad Saputra**  
🏢 **Founder of PandaKode** *(Software House — Android Apps, Web Systems, IoT Solutions, Desktop Apps & Games)*  
📍 Pekanbaru, Riau, Indonesia
