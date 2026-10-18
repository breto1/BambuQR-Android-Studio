package com.example.bambuqr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// 1. PALET WARNA TEMA CERAH PANDAKODE (HIJAU BAMBU, PUTIH, HITAM PANDA, GOLD)
// ============================================================================
val BambooGreen = Color(0xFF10B981)       // Hijau Bambu Segar
val DeepBamboo = Color(0xFF047857)        // Hijau Daun Tua
val MintLightBg = Color(0xFFF4FBF7)       // Putih Sejuk (Background Utama)
val SoftMintBorder = Color(0xFFD1FAE5)    // Border Hijau Lembut
val RoyalGold = Color(0xFFF59E0B)         // Gold Cerah
val SoftGoldBg = Color(0xFFFEF3C7)        // Latar Aksen Gold Muda
val PandaBlack = Color(0xFF18181B)        // Hitam Panda
val PureWhite = Color(0xFFFFFFFF)         // Putih Bersih

private val PandaLightColorScheme = lightColorScheme(
    primary = BambooGreen,
    onPrimary = PureWhite,
    primaryContainer = SoftMintBorder,
    onPrimaryContainer = DeepBamboo,
    secondary = RoyalGold,
    onSecondary = PandaBlack,
    secondaryContainer = SoftGoldBg,
    onSecondaryContainer = PandaBlack,
    background = MintLightBg,
    onBackground = PandaBlack,
    surface = PureWhite,
    onSurface = PandaBlack
)

// ============================================================================
// 2. MODEL DATA & PILIHAN KATEGORI QR (9 JENIS LENGKAP)
// ============================================================================
enum class QrCategory(val title: String, val icon: ImageVector, val hint: String) {
    LINK("Link / Teks", Icons.Default.Language, "Website atau catatan teks"),
    WIFI("Wi-Fi", Icons.Default.Wifi, "Sambung Wi-Fi otomatis"),
    WHATSAPP("WhatsApp", Icons.Default.Chat, "Langsung buka chat WA"),
    CONTACT("Kontak vCard", Icons.Default.ContactPhone, "Simpan kartu nama ke HP"),
    EMAIL("Email", Icons.Default.Email, "Kirim email otomatis"),
    LOCATION("Lokasi Maps", Icons.Default.LocationOn, "Titik koordinat Google Maps"),
    SOCIAL("Sosmed / IG", Icons.Default.AlternateEmail, "Profil Instagram/TikTok/YT"),
    SMS("SMS Cepat", Icons.Default.Sms, "Kirim pesan SMS instan"),
    EVENT("Acara / Event", Icons.Default.Event, "Jadwal pengingat kalender")
}

// DIPERLUAS: Pilihan Tema Warna QR Code yang Jauh Lebih Menarik & Estetik
enum class QrStyleOption(val label: String, val fgColor: Color, val bgColor: Color, val borderGold: Boolean) {
    BAMBOO_GOLD("Bambu & Gold", DeepBamboo, PureWhite, true),
    CLASSIC_PANDA("Klasik Panda", PandaBlack, PureWhite, false),
    ROYAL_GOLD("Hitam & Emas", PandaBlack, Color(0xFFFFFBEB), true),
    EMERALD_MINT("Emerald Segar", Color(0xFF065F46), Color(0xFFECFDF5), false),
    GOLD_LUXURY("Emas Mewah", Color(0xFFB45309), PureWhite, true),
    OCEAN_NEON("Samudra Biru", Color(0xFF0284C7), Color(0xFFF0F9FF), false),
    SUNSET_RUBY("Ruby Merah", Color(0xFFBE123C), Color(0xFFFFF1F2), true),
    PURPLE_ROYAL("Ungu Kerajaan", Color(0xFF7C3AED), Color(0xFFF5F3FF), false)
}

// DIPERLUAS: Bentuk Titik QR Code yang Lebih Beragam
enum class QrDotShape(val label: String) {
    SQUARE("Kotak Klasik"),
    ROUNDED("Sudut Halus"),
    DOTS("Titik Bulat"),
    DIAMOND("Belah Ketupat")
}

data class QrHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val category: String,
    val content: String,
    val timestamp: String,
    val isFavorite: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = PandaLightColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BambuQrApp()
                }
            }
        }
    }
}

// ============================================================================
// 3. KOMPONEN UTAMA & NAVIGASI 4 TAB
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BambuQrApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val historyList = remember {
        mutableStateListOf<QrHistoryItem>().apply {
            addAll(loadHistoryFromPrefs(context))
        }
    }

    fun syncHistory() {
        saveHistoryToPrefs(context, historyList)
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(DeepBamboo, BambooGreen)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_panda),
                            contentDescription = "Logo BambuQR",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(PureWhite)
                                .border(2.dp, RoyalGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BambuQR",
                                    color = PureWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = RoyalGold,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ULTIMATE",
                                        color = PandaBlack,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Smart Panda QR & Barcode Studio",
                                color = Color(0xFFD1FAE5),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        color = PureWhite.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, RoyalGold)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${historyList.size} Item",
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.QrCode2, contentDescription = "Buat QR") },
                    label = { Text("Buat QR", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBamboo,
                        selectedTextColor = DeepBamboo,
                        indicatorColor = SoftMintBorder
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan & Riwayat") },
                    label = { Text("Scan & Data", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBamboo,
                        selectedTextColor = DeepBamboo,
                        indicatorColor = SoftMintBorder
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BuildCircle, contentDescription = "Alat Pintar") },
                    label = { Text("Alat Pintar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBamboo,
                        selectedTextColor = DeepBamboo,
                        indicatorColor = SoftMintBorder
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Verified, contentDescription = "PandaKode") },
                    label = { Text("Tentang", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBamboo,
                        selectedTextColor = DeepBamboo,
                        indicatorColor = SoftMintBorder
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CreateQrScreen(
                    onSaveHistory = { category, content ->
                        val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                        historyList.add(0, QrHistoryItem(category = category, content = content, timestamp = time))
                        syncHistory()
                    }
                )
                1 -> ScanAndHistoryScreen(
                    historyList = historyList,
                    onAddScanned = { content ->
                        val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                        historyList.add(0, QrHistoryItem(category = "Hasil Scan", content = content, timestamp = time))
                        syncHistory()
                    },
                    onToggleFavorite = { item ->
                        val index = historyList.indexOfFirst { it.id == item.id }
                        if (index != -1) {
                            historyList[index] = item.copy(isFavorite = !item.isFavorite)
                            syncHistory()
                        }
                    },
                    onDelete = { item ->
                        historyList.remove(item)
                        syncHistory()
                    },
                    onClearAll = {
                        historyList.clear()
                        syncHistory()
                    }
                )
                2 -> SmartToolsScreen(
                    onSaveHistory = { category, content ->
                        val time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
                        historyList.add(0, QrHistoryItem(category = category, content = content, timestamp = time))
                        syncHistory()
                    }
                )
                3 -> AboutPandaKodeScreen()
            }
        }
    }
}

// ============================================================================
// 4. LAYAR 1: PEMBUAT QR KUSTOM (DENGAN TANDA GESER ANIMATIF DI STUDIO DESAIN)
// ============================================================================
@Composable
fun CreateQrScreen(onSaveHistory: (String, String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val categoryScrollState = rememberLazyListState()
    val styleScrollState = rememberLazyListState() // State khusus untuk scroll palet warna

    var selectedCategory by remember { mutableStateOf(QrCategory.LINK) }
    var showAllCategoriesGrid by remember { mutableStateOf(false) }

    // State Input 9 Kategori
    var textInput by remember { mutableStateOf("https://pandakode.com") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPass by remember { mutableStateOf("") }
    var wifiType by remember { mutableStateOf("WPA") }
    var waPhone by remember { mutableStateOf("628") }
    var waMessage by remember { mutableStateOf("Halo PandaKode!") }
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactOrg by remember { mutableStateOf("PandaKode") }
    var emailTo by remember { mutableStateOf("info@pandakode.com") }
    var emailSubject by remember { mutableStateOf("Kerjasama Project") }
    var emailBody by remember { mutableStateOf("") }
    var mapsLat by remember { mutableStateOf("0.5709") }
    var mapsLng by remember { mutableStateOf("101.4255") }
    var socialPlatform by remember { mutableStateOf("Instagram") }
    var socialUsername by remember { mutableStateOf("pandakode") }
    var smsPhone by remember { mutableStateOf("0812") }
    var smsBody by remember { mutableStateOf("Info layanan PandaKode") }
    var eventTitle by remember { mutableStateOf("Meeting Project PandaKode") }
    var eventLocation by remember { mutableStateOf("Pekanbaru") }

    // State Kustomisasi Visual QR
    var selectedStyle by remember { mutableStateOf(QrStyleOption.BAMBOO_GOLD) }
    var selectedDotShape by remember { mutableStateOf(QrDotShape.ROUNDED) }
    var showPandaBadge by remember { mutableStateOf(true) }
    var customBottomLabel by remember { mutableStateOf("SCAN ME") }

    val logoBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_panda)
    }

    val finalQrPayload = remember(
        selectedCategory, textInput, wifiSsid, wifiPass, wifiType, waPhone, waMessage,
        contactName, contactPhone, contactOrg, emailTo, emailSubject, emailBody,
        mapsLat, mapsLng, socialPlatform, socialUsername, smsPhone, smsBody, eventTitle, eventLocation
    ) {
        when (selectedCategory) {
            QrCategory.LINK -> textInput.ifBlank { "https://pandakode.com" }
            QrCategory.WIFI -> {
                val ssid = wifiSsid.ifBlank { "PandaKode_WiFi" }
                if (wifiType == "nopass") "WIFI:S:$ssid;T:nopass;;"
                else "WIFI:S:$ssid;T:$wifiType;P:$wifiPass;;"
            }
            QrCategory.WHATSAPP -> {
                val cleanPhone = waPhone.replace("[^0-9]".toRegex(), "")
                "https://wa.me/$cleanPhone?text=${Uri.encode(waMessage)}"
            }
            QrCategory.CONTACT -> {
                val name = contactName.ifBlank { "PandaKode" }
                "BEGIN:VCARD\nVERSION:3.0\nFN:$name\nORG:$contactOrg\nTEL:$contactPhone\nEND:VCARD"
            }
            QrCategory.EMAIL -> "MATMSG:TO:$emailTo;SUB:$emailSubject;BODY:$emailBody;;"
            QrCategory.LOCATION -> "https://maps.google.com/?q=${mapsLat.ifBlank { "0.5709" }},${mapsLng.ifBlank { "101.4255" }}"
            QrCategory.SOCIAL -> {
                val cleanUser = socialUsername.replace("@", "").trim()
                when (socialPlatform) {
                    "Instagram" -> "https://instagram.com/$cleanUser"
                    "TikTok" -> "https://tiktok.com/@$cleanUser"
                    "YouTube" -> "https://youtube.com/@$cleanUser"
                    else -> "https://linkedin.com/in/$cleanUser"
                }
            }
            QrCategory.SMS -> "SMSTO:$smsPhone:$smsBody"
            QrCategory.EVENT -> "BEGIN:VEVENT\nSUMMARY:$eventTitle\nLOCATION:$eventLocation\nEND:VEVENT"
        }
    }

    val qrBitmap = remember(finalQrPayload, selectedStyle, selectedDotShape, showPandaBadge, customBottomLabel) {
        generateStyledQrBitmap(
            content = finalQrPayload,
            fgColor = selectedStyle.fgColor.toArgb(),
            bgColor = selectedStyle.bgColor.toArgb(),
            addGoldFrame = selectedStyle.borderGold,
            addPandaCenter = showPandaBadge,
            centerLogoBitmap = logoBitmap,
            dotShape = selectedDotShape,
            bottomLabel = customBottomLabel.trim()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER PILIH JENIS QR DENGAN INDIKATOR GESER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "1. Pilih Jenis QR (${QrCategory.entries.size} Kategori)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = DeepBamboo
                )
                Text(
                    text = "👈 Geser untuk melihat pilihan lainnya 👉",
                    fontSize = 11.sp,
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = SoftGoldBg,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, RoyalGold),
                modifier = Modifier.clickable { showAllCategoriesGrid = !showAllCategoriesGrid }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = if (showAllCategoriesGrid) Icons.Default.ViewCarousel else Icons.Default.GridView,
                        contentDescription = null,
                        tint = PandaBlack,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAllCategoriesGrid) "Mode Geser" else "Lihat Semua",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PandaBlack
                    )
                }
            }
        }

        if (showAllCategoriesGrid) {
            val chunkedCategories = QrCategory.entries.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedCategories.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { category ->
                            val isSelected = category == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BambooGreen else PureWhite,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) RoyalGold else SoftMintBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = category }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) RoyalGold else DeepBamboo,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = category.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) PureWhite else PandaBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.dp, BambooGreen),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            coroutineScope.launch {
                                val target = (categoryScrollState.firstVisibleItemIndex - 2).coerceAtLeast(0)
                                categoryScrollState.animateScrollToItem(target)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Geser Kiri", tint = DeepBamboo)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                LazyRow(
                    state = categoryScrollState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(QrCategory.entries) { index, category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = category
                                coroutineScope.launch {
                                    categoryScrollState.animateScrollToItem(index)
                                }
                            },
                            label = { Text(category.title, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(category.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BambooGreen,
                                selectedLabelColor = PureWhite,
                                selectedLeadingIconColor = RoyalGold,
                                containerColor = PureWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) RoyalGold else BambooGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = RoyalGold,
                    border = BorderStroke(1.dp, DeepBamboo),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            coroutineScope.launch {
                                val target = (categoryScrollState.firstVisibleItemIndex + 2)
                                    .coerceAtMost(QrCategory.entries.size - 1)
                                categoryScrollState.animateScrollToItem(target)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Geser Kanan", tint = PandaBlack)
                    }
                }
            }
        }

        // FORM INPUT DINAMIS
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, SoftMintBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(selectedCategory.icon, contentDescription = null, tint = DeepBamboo, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedCategory.title} — ${selectedCategory.hint}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBamboo
                    )
                }

                when (selectedCategory) {
                    QrCategory.LINK -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            label = { Text("Masukkan URL Website atau Teks") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = BambooGreen) },
                            trailingIcon = {
                                if (textInput.isNotEmpty()) {
                                    IconButton(onClick = { textInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.WIFI -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("Nama Wi-Fi (SSID)") },
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("WPA", "WEP", "nopass").forEach { sec ->
                                FilterChip(
                                    selected = wifiType == sec,
                                    onClick = { wifiType = sec },
                                    label = { Text(if (sec == "nopass") "Tanpa Sandi" else sec) }
                                )
                            }
                        }
                        if (wifiType != "nopass") {
                            OutlinedTextField(
                                value = wifiPass,
                                onValueChange = { wifiPass = it },
                                label = { Text("Password Wi-Fi") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = RoyalGold) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                    QrCategory.WHATSAPP -> {
                        OutlinedTextField(
                            value = waPhone,
                            onValueChange = { waPhone = it },
                            label = { Text("Nomor WhatsApp (Awali 628...)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = waMessage,
                            onValueChange = { waMessage = it },
                            label = { Text("Pesan Chat Otomatis") },
                            leadingIcon = { Icon(Icons.Default.Message, contentDescription = null, tint = RoyalGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.CONTACT -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Nama Lengkap") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Nomor HP / Telepon") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = RoyalGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contactOrg,
                            onValueChange = { contactOrg = it },
                            label = { Text("Perusahaan / Instansi") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.EMAIL -> {
                        OutlinedTextField(
                            value = emailTo,
                            onValueChange = { emailTo = it },
                            label = { Text("Alamat Email Tujuan") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = { emailSubject = it },
                            label = { Text("Subjek Email") },
                            leadingIcon = { Icon(Icons.Default.Subject, contentDescription = null, tint = RoyalGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = { emailBody = it },
                            label = { Text("Isi Pesan Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.LOCATION -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mapsLat,
                                onValueChange = { mapsLat = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = mapsLng,
                                onValueChange = { mapsLng = it },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                    QrCategory.SOCIAL -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Instagram", "TikTok", "YouTube", "LinkedIn").forEach { plat ->
                                FilterChip(
                                    selected = socialPlatform == plat,
                                    onClick = { socialPlatform = plat },
                                    label = { Text(plat, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = socialUsername,
                            onValueChange = { socialUsername = it },
                            label = { Text("Username $socialPlatform (tanpa @)") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.SMS -> {
                        OutlinedTextField(
                            value = smsPhone,
                            onValueChange = { smsPhone = it },
                            label = { Text("Nomor Tujuan SMS") },
                            leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = smsBody,
                            onValueChange = { smsBody = it },
                            label = { Text("Isi Pesan SMS") },
                            leadingIcon = { Icon(Icons.Default.Sms, contentDescription = null, tint = RoyalGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    QrCategory.EVENT -> {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Nama Acara / Agenda") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, tint = BambooGreen) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = eventLocation,
                            onValueChange = { eventLocation = it },
                            label = { Text("Lokasi Acara") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = RoyalGold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // ====================================================================
        // STUDIO KUSTOMISASI DESAIN QR (DENGAN TANDA PANAH GESER KIRI-KANAN)
        // ====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "2. Studio Desain & Aksen Gold",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = DeepBamboo
                )
                Text(
                    text = "👉 Geser menyamping untuk opsi warna & gaya",
                    fontSize = 11.sp,
                    color = RoyalGold,
                    fontWeight = FontWeight.Bold
                )
            }

            // Indikator panah interaktif untuk menggeser palet warna
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = CircleShape,
                    color = PureWhite,
                    border = BorderStroke(1.dp, BambooGreen),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            coroutineScope.launch {
                                val target = (styleScrollState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                styleScrollState.animateScrollToItem(target)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = DeepBamboo, modifier = Modifier.size(16.dp))
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = RoyalGold,
                    border = BorderStroke(1.dp, DeepBamboo),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            coroutineScope.launch {
                                val target = (styleScrollState.firstVisibleItemIndex + 1)
                                    .coerceAtMost(QrStyleOption.entries.size - 1)
                                styleScrollState.animateScrollToItem(target)
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PandaBlack, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Daftar Pilihan Tema Warna yang Diperkaya (Bisa Digeser)
        LazyRow(
            state = styleScrollState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(QrStyleOption.entries) { styleOption ->
                val isSelected = selectedStyle == styleOption
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) SoftGoldBg else PureWhite,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) RoyalGold else SoftMintBorder
                    ),
                    modifier = Modifier
                        .clickable { selectedStyle = styleOption }
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(styleOption.fgColor)
                                .border(1.dp, RoyalGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = styleOption.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = PandaBlack
                        )
                    }
                }
            }
        }

        // Pilihan Bentuk Titik (Dot Shape) yang Diperluas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QrDotShape.entries.forEach { shapeOption ->
                val isSelected = selectedDotShape == shapeOption
                OutlinedButton(
                    onClick = { selectedDotShape = shapeOption },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) SoftMintBorder else PureWhite
                    ),
                    border = BorderStroke(1.dp, if (isSelected) DeepBamboo else SoftMintBorder),
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 2.dp)
                ) {
                    Text(
                        text = shapeOption.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = DeepBamboo,
                        maxLines = 1
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SoftMintBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customBottomLabel,
                    onValueChange = { if (it.length <= 22) customBottomLabel = it },
                    label = { Text("Label Teks Bawah QR (Kosongkan jika tanpa teks)") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = RoyalGold) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_panda),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, RoyalGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tampilkan Logo Panda di Tengah QR", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Switch(
                        checked = showPandaBadge,
                        onCheckedChange = { showPandaBadge = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RoyalGold,
                            checkedTrackColor = DeepBamboo
                        )
                    )
                }
            }
        }

        // KARTU PREVIEW QR CODE
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(2.dp, RoyalGold),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = SoftGoldBg,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "✨ ${selectedCategory.title.uppercase()} • BAMBU QR ✨",
                        color = PandaBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                qrBitmap?.let { btm ->
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = "Generated QR Code",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = finalQrPayload.replace("\n", " • "),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            qrBitmap?.let {
                                onSaveHistory(selectedCategory.title, finalQrPayload)
                                saveQrToGallery(context, it, "BambuQR_${System.currentTimeMillis()}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = PandaBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unduh PNG", color = PandaBlack, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = {
                            qrBitmap?.let {
                                onSaveHistory(selectedCategory.title, finalQrPayload)
                                shareQrBitmap(context, it)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BambooGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan QR", color = PureWhite, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. LAYAR 2: SCANNER KAMERA, PENCARIAN RIWAYAT, FAVORIT & POPUP PREVIEW QR
// ============================================================================
@Composable
fun ScanAndHistoryScreen(
    historyList: List<QrHistoryItem>,
    onAddScanned: (String) -> Unit,
    onToggleFavorite: (QrHistoryItem) -> Unit,
    onDelete: (QrHistoryItem) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var selectedItemForPopup by remember { mutableStateOf<QrHistoryItem?>(null) }

    val logoBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_panda)
    }

    val filteredHistory = remember(historyList.size, historyList.toList(), searchQuery, filterFavoritesOnly) {
        historyList.filter { item ->
            val matchesSearch = item.content.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            val matchesFav = if (filterFavoritesOnly) item.isFavorite else true
            matchesSearch && matchesFav
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, BambooGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pemindai Kamera Cerdas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = PandaBlack
                    )
                    Text(
                        text = "Scan QR & Barcode apa saja, otomatis tersimpan ke riwayat.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val options = GmsBarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                            .enableAutoZoom()
                            .build()
                        GmsBarcodeScanning.getClient(context, options)
                            .startScan()
                            .addOnSuccessListener { barcode ->
                                val rawValue = barcode.rawValue ?: ""
                                if (rawValue.isNotEmpty()) {
                                    onAddScanned(rawValue)
                                    Toast.makeText(context, "Berhasil di-scan!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Batal/Gagal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = PandaBlack)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SCAN", color = PandaBlack, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari riwayat QR...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BambooGreen) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            FilterChip(
                selected = filterFavoritesOnly,
                onClick = { filterFavoritesOnly = !filterFavoritesOnly },
                label = { Text("Favorit") },
                leadingIcon = {
                    Icon(
                        imageVector = if (filterFavoritesOnly) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = RoyalGold
                    )
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Daftar Tersimpan (${filteredHistory.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DeepBamboo
            )
            if (historyList.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Hapus Semua", fontSize = 12.sp, color = Color.Red)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredHistory, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (item.isFavorite) RoyalGold else SoftMintBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedItemForPopup = item }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onToggleFavorite(item) }) {
                            Icon(
                                imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorit",
                                tint = RoyalGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = SoftMintBorder,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepBamboo,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = item.timestamp, fontSize = 11.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.content,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PandaBlack,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { copyToClipboard(context, item.content) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = BambooGreen)
                        }
                        IconButton(onClick = { onDelete(item) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }

    selectedItemForPopup?.let { item ->
        val popupBitmap = remember(item.content) {
            generateStyledQrBitmap(
                content = item.content,
                fgColor = DeepBamboo.toArgb(),
                bgColor = PureWhite.toArgb(),
                addGoldFrame = true,
                addPandaCenter = true,
                centerLogoBitmap = logoBitmap,
                dotShape = QrDotShape.ROUNDED,
                bottomLabel = item.category.uppercase()
            )
        }

        AlertDialog(
            onDismissRequest = { selectedItemForPopup = null },
            containerColor = PureWhite,
            title = {
                Text(text = "Detail: ${item.category}", fontWeight = FontWeight.ExtraBold, color = DeepBamboo)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    popupBitmap?.let { btm ->
                        Image(
                            bitmap = btm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.content,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = PandaBlack
                    )

                    if (item.content.startsWith("WIFI:", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = SoftGoldBg,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, RoyalGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = parseWifiQrString(item.content),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PandaBlack,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        popupBitmap?.let { shareQrBitmap(context, it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BambooGreen)
                ) {
                    Text("Bagikan QR", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForPopup = null }) {
                    Text("Tutup", color = DeepBamboo)
                }
            }
        )
    }
}

// ============================================================================
// 6. LAYAR 3: ALAT PINTAR (BARCODE KASIR/PRODUK 1D & BATCH QR GENERATOR)
// ============================================================================
@Composable
fun SmartToolsScreen(onSaveHistory: (String, String) -> Unit) {
    val context = LocalContext.current
    var barcodeInput by remember { mutableStateOf("PK-2026-001") }
    var batchInput by remember { mutableStateOf("Meja 01\nMeja 02\nMeja 03") }
    var generatedBatchCount by remember { mutableIntStateOf(0) }

    val logoBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.logo_panda)
    }

    val barcodeBitmap = remember(barcodeInput) {
        generateCode128Barcode(barcodeInput.ifBlank { "PANDAKODE" })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(2.dp, RoyalGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ViewWeek, contentDescription = null, tint = DeepBamboo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Pembuat Barcode Produk / Inventaris (Code-128)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = DeepBamboo
                        )
                        Text(
                            text = "Cocok untuk label barang, kode aset laboratorium, atau kasir toko.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text("Kode Barang / Nomor Seri") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                barcodeBitmap?.let { btm ->
                    Image(
                        bitmap = btm.asImageBitmap(),
                        contentDescription = "Barcode 1D",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            barcodeBitmap?.let {
                                onSaveHistory("Barcode 1D", barcodeInput)
                                saveQrToGallery(context, it, "Barcode_${barcodeInput}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalGold)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = PandaBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unduh Barcode", color = PandaBlack, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            barcodeBitmap?.let {
                                onSaveHistory("Barcode 1D", barcodeInput)
                                shareQrBitmap(context, it)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BambooGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, BambooGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LibraryAdd, contentDescription = null, tint = DeepBamboo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Batch QR Generator (Buat Massal Sekaligus)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = DeepBamboo
                        )
                        Text(
                            text = "Ketik 1 item per baris untuk langsung menyimpan semuanya ke Galeri & Riwayat.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                OutlinedTextField(
                    value = batchInput,
                    onValueChange = { batchInput = it },
                    label = { Text("Masukkan daftar (1 baris = 1 QR Code)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 6
                )

                Button(
                    onClick = {
                        val lines = batchInput.lines().map { it.trim() }.filter { it.isNotEmpty() }
                        var count = 0
                        lines.take(20).forEach { line ->
                            val btm = generateStyledQrBitmap(
                                content = line,
                                fgColor = DeepBamboo.toArgb(),
                                bgColor = PureWhite.toArgb(),
                                addGoldFrame = true,
                                addPandaCenter = true,
                                centerLogoBitmap = logoBitmap,
                                dotShape = QrDotShape.ROUNDED,
                                bottomLabel = line.take(18)
                            )
                            if (btm != null) {
                                saveQrToGallery(context, btm, "BatchQR_${count + 1}_${System.currentTimeMillis()}", showToast = false)
                                onSaveHistory("Batch QR", line)
                                count++
                            }
                        }
                        generatedBatchCount = count
                        Toast.makeText(
                            context,
                            "Berhasil membuat & mengunduh $count QR Code ke Galeri!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BambooGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = RoyalGold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GENERATE & SIMPAN SEMUA KE GALERI", color = PureWhite, fontWeight = FontWeight.ExtraBold)
                }

                if (generatedBatchCount > 0) {
                    Text(
                        text = "✅ $generatedBatchCount QR Code telah disimpan ke Galeri (Folder Pictures/BambuQR) & tab Riwayat.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DeepBamboo
                    )
                }
            }
        }
    }
}

// ============================================================================
// 7. LAYAR 4: TENTANG PANDAKODE
// ============================================================================
@Composable
fun AboutPandaKodeScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, RoyalGold),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_panda),
                    contentDescription = "Logo Resmi BambuQR",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(PureWhite)
                        .border(3.dp, RoyalGold, CircleShape)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "BambuQR Studio v1.1.0",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepBamboo
                )
                Text(
                    text = "Official Product by PandaKode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Solusi lengkap pembuatan QR Code 9 kategori, Barcode Inventaris Code-128, serta Batch Generator dengan pemrosesan 100% lokal yang cepat dan aman.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SoftMintBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = BambooGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keamanan & Privasi Pengguna", fontWeight = FontWeight.Bold, color = PandaBlack)
                }
                Text(
                    text = "• Semua kode QR & Barcode dibuat secara offline di perangkat Anda.\n• Kami tidak mengunggah password Wi-Fi atau kontak Anda ke server eksternal.",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Button(
                    onClick = {
                        val url = "https://pandakode.com/privacy-policy"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BambooGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Policy, contentDescription = null, tint = PureWhite)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kebijakan Privasi (Privacy Policy)", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// 8. ENGINE GENERATOR QR KUSTOM (DENGAN BENTUK DIAMOND / BELAH KETUPAT)
// ============================================================================
fun generateStyledQrBitmap(
    content: String,
    fgColor: Int,
    bgColor: Int,
    addGoldFrame: Boolean,
    addPandaCenter: Boolean,
    centerLogoBitmap: Bitmap? = null,
    dotShape: QrDotShape = QrDotShape.SQUARE,
    bottomLabel: String = ""
): Bitmap? {
    return try {
        val qrSize = 700
        val extraBottom = if (bottomLabel.isNotEmpty()) 90 else 0
        val totalHeight = qrSize + extraBottom

        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        val matrixWidth = matrix.width
        val padding = 48f
        val cellSize = (qrSize - (padding * 2)) / matrixWidth

        val bitmap = Bitmap.createBitmap(qrSize, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.style = Paint.Style.FILL
        paint.color = bgColor
        canvas.drawRoundRect(RectF(0f, 0f, qrSize.toFloat(), totalHeight.toFloat()), 36f, 36f, paint)

        paint.color = fgColor
        for (x in 0 until matrixWidth) {
            for (y in 0 until matrixWidth) {
                if (matrix[x, y]) {
                    val left = padding + x * cellSize
                    val top = padding + y * cellSize
                    val rect = RectF(left, top, left + cellSize, top + cellSize)
                    when (dotShape) {
                        QrDotShape.SQUARE -> canvas.drawRect(rect, paint)
                        QrDotShape.ROUNDED -> canvas.drawRoundRect(rect, cellSize * 0.35f, cellSize * 0.35f, paint)
                        QrDotShape.DOTS -> canvas.drawCircle(
                            left + cellSize / 2f,
                            top + cellSize / 2f,
                            (cellSize / 2f) * 0.92f,
                            paint
                        )
                        QrDotShape.DIAMOND -> {
                            val path = Path().apply {
                                moveTo(left + cellSize / 2f, top)
                                lineTo(left + cellSize, top + cellSize / 2f)
                                lineTo(left + cellSize / 2f, top + cellSize)
                                lineTo(left, top + cellSize / 2f)
                                close()
                            }
                            canvas.drawPath(path, paint)
                        }
                    }
                }
            }
        }

        if (addGoldFrame) {
            paint.style = Paint.Style.STROKE
            paint.color = android.graphics.Color.parseColor("#F59E0B")
            paint.strokeWidth = 18f
            val rect = RectF(9f, 9f, (qrSize - 9).toFloat(), (totalHeight - 9).toFloat())
            canvas.drawRoundRect(rect, 36f, 36f, paint)
        }

        if (addPandaCenter) {
            val center = qrSize / 2f
            val radius = qrSize * 0.115f

            paint.style = Paint.Style.FILL
            paint.color = android.graphics.Color.WHITE
            canvas.drawCircle(center, center, radius + 6f, paint)

            if (centerLogoBitmap != null) {
                val targetRect = RectF(center - radius, center - radius, center + radius, center + radius)
                val saveCount = canvas.save()
                val clipPath = Path().apply {
                    addCircle(center, center, radius, Path.Direction.CW)
                }
                canvas.clipPath(clipPath)
                canvas.drawBitmap(centerLogoBitmap, null, targetRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                canvas.restoreToCount(saveCount)
            }

            paint.style = Paint.Style.STROKE
            paint.color = android.graphics.Color.parseColor("#F59E0B")
            paint.strokeWidth = 6f
            canvas.drawCircle(center, center, radius + 3f, paint)
        }

        if (bottomLabel.isNotEmpty()) {
            val pillRect = RectF(60f, qrSize - 10f, qrSize - 60f, totalHeight - 24f)
            paint.style = Paint.Style.FILL
            paint.color = android.graphics.Color.parseColor("#F59E0B")
            canvas.drawRoundRect(pillRect, 30f, 30f, paint)

            paint.color = android.graphics.Color.parseColor("#18181B")
            paint.textSize = 34f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            val fm = paint.fontMetrics
            val centerY = pillRect.centerY() - (fm.descent + fm.ascent) / 2
            canvas.drawText(bottomLabel.uppercase(), qrSize / 2f, centerY, paint)
        }

        bitmap
    } catch (e: Exception) {
        null
    }
}

fun generateCode128Barcode(content: String): Bitmap? {
    return try {
        val width = 700
        val height = 240
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, width, height - 60)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val fgColor = android.graphics.Color.parseColor("#18181B")
        for (x in 0 until width) {
            for (y in 0 until (height - 60)) {
                if (bitMatrix[x, y]) {
                    bitmap.setPixel(x, y + 10, fgColor)
                }
            }
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            textSize = 30f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(content, width / 2f, (height - 14).toFloat(), paint)
        bitmap
    } catch (e: Exception) {
        null
    }
}

// ============================================================================
// 9. HELPER: SIMPAN KE GALERI HP, SHARE & PREFS
// ============================================================================
fun saveQrToGallery(context: Context, bitmap: Bitmap, fileName: String, showToast: Boolean = true) {
    try {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BambuQR")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            if (showToast) {
                Toast.makeText(context, "Tersimpan di Galeri (Pictures/BambuQR)!", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        if (showToast) {
            Toast.makeText(context, "Gagal menyimpan ke galeri: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareQrBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "bambu_qr_pandakode.png")
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_TEXT, "Dibuat dengan BambuQR by PandaKode 🐼✨")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Kode ke..."))
    } catch (e: Exception) {
        Toast.makeText(context, "Gagal membagikan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("QR Content", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Berhasil disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
}

fun parseWifiQrString(raw: String): String {
    val ssid = Regex("S:([^;]+);").find(raw)?.groupValues?.get(1) ?: "-"
    val pass = Regex("P:([^;]+);").find(raw)?.groupValues?.get(1) ?: "Tanpa Password"
    val type = Regex("T:([^;]+);").find(raw)?.groupValues?.get(1) ?: "WPA"
    return "📶 Detail Wi-Fi Terdeteksi:\n• Nama SSID: $ssid\n• Password: $pass\n• Keamanan: $type"
}

fun saveHistoryToPrefs(context: Context, list: List<QrHistoryItem>) {
    val prefs = context.getSharedPreferences("bambu_qr_prefs", Context.MODE_PRIVATE)
    val jsonArray = JSONArray()
    list.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("category", item.category)
            put("content", item.content)
            put("timestamp", item.timestamp)
            put("isFavorite", item.isFavorite)
        }
        jsonArray.put(obj)
    }
    prefs.edit().putString("history_json", jsonArray.toString()).apply()
}

fun loadHistoryFromPrefs(context: Context): List<QrHistoryItem> {
    val prefs = context.getSharedPreferences("bambu_qr_prefs", Context.MODE_PRIVATE)
    val raw = prefs.getString("history_json", null)
    if (raw.isNullOrEmpty()) {
        return listOf(
            QrHistoryItem(1, "Link / Teks", "https://pandakode.com", "Contoh Bawaan", true),
            QrHistoryItem(2, "Wi-Fi", "WIFI:S:PandaKode_Office;T:WPA;P:bambu2026;;", "Contoh Bawaan", false)
        )
    }
    return try {
        val array = JSONArray(raw)
        val result = mutableListOf<QrHistoryItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                QrHistoryItem(
                    id = obj.optLong("id", System.currentTimeMillis() + i),
                    category = obj.optString("category", "Teks"),
                    content = obj.optString("content", ""),
                    timestamp = obj.optString("timestamp", ""),
                    isFavorite = obj.optBoolean("isFavorite", false)
                )
            )
        }
        result
    } catch (e: Exception) {
        emptyList()
    }
}