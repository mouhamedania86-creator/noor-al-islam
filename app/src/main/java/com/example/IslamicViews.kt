@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Vibrator
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 3x3 Section Grid Button Model ---
data class GridItem(val labelAr: String, val icon: ImageVector, val targetRoute: String)

val navGridItems = listOf(
    GridItem("القرآن الكريم", Icons.Default.MenuBook, "quran"),
    GridItem("الأدعية والأذكار", Icons.Default.Favorite, "azkar"),
    GridItem("مواقيت الصلاة", Icons.Default.AccessTime, "prayers"),
    GridItem("القبلة الشريفة", Icons.Default.CompassCalibration, "qibla"),
    GridItem("التصحيح والسبحة", Icons.Default.Sync, "sebha"),
    GridItem("التقويم الهجري", Icons.Default.CalendarMonth, "calendar"),
    GridItem("تجويد القرآن", Icons.Default.School, "tajweed"),
    GridItem("فقه الصلاة", Icons.Default.MenuBook, "fiqh"),
    GridItem("الوعظ والخطب", Icons.Default.Campaign, "sermons"),
    GridItem("الحج والعمرة", Icons.Default.Map, "hajj"),
    GridItem("الختمة اليومية", Icons.Default.TrackChanges, "khatma"),
    GridItem("الرقية الشرعية", Icons.Default.OfflineBolt, "ruqyah"),
    GridItem("المسابقة الإسلامية", Icons.Default.Quiz, "quiz"),
    GridItem("أسماء الله الحُسنى", Icons.Default.Star, "names"),
    GridItem("راديو القرآن", Icons.Default.Radio, "radio"),
    GridItem("الإعدادات", Icons.Default.Settings, "settings")
)

@Composable
fun HomeView(
    viewModel: IslamicViewModel,
    settings: SettingsEntity,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // States from VM
    val time by viewModel.timeString
    val dateAr by viewModel.dateStringAr
    val randomDua by viewModel.currentHeaderDua
    val prayers by viewModel.prayerTimes
    val nextPrayName by viewModel.nextPrayerName
    val nextPrayTimeLeft by viewModel.nextPrayerTimeLeft
    val temp by viewModel.currentTemp
    val condition by viewModel.weatherConditionAr

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D4A30), Color(0xFF061A11))
                )
            ),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // --- 1. Top Status/Weather Bar ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = prayers?.hijriDate ?: "الخميس، ١٥ رمضان",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = dateAr,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Weather Badge Pill
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (viewModel.isWeatherLoading.value) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 1.5.dp)
                        } else {
                            Text(
                                text = if (temp != null) "${temp?.toInt()}°" else "٢٤°",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 12.sp
                            )
                            Text(
                                text = settings.city,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                lineHeight = 10.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFFFACC15).copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFFACC15), CircleShape)
                        )
                    }
                }
            }
        }

        // --- 2. User Greeting & Main Clock ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "السلام عليكم، ${settings.username}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = time,
                    color = Color(0xFFC9A84C),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "صلاة $nextPrayName بعد $nextPrayTimeLeft",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // --- 3. Prayer Times Card ---
        item {
            prayers?.let { p ->
                val list = listOf(
                    Pair("الفجر", p.Fajr),
                    Pair("الظهر", p.Dhuhr),
                    Pair("العصر", p.Asr),
                    Pair("المغرب", p.Maghrib),
                    Pair("العشاء", p.Isha)
                )
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header block labels row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            list.forEach { (name, _) ->
                                val isNext = nextPrayName.contains(name)
                                Text(
                                    text = name,
                                    color = if (isNext) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Times block content row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            list.forEachIndexed { index, (name, timeVal) ->
                                val isNext = nextPrayName.contains(name)
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isNext) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFC9A84C).copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                                                .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = timeVal,
                                                color = Color(0xFFC9A84C),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = timeVal,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                                if (index < list.size - 1) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(16.dp)
                                            .background(Color.White.copy(alpha = 0.10f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Random Dua Card with Left Gold Accent ---
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clickable { viewModel.rotateDuaAndHadith() }
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0A3A26), Color(0xFF0D4A30))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFC9A84C))
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Dua star",
                            tint = Color(0xFFC9A84C).copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "دعاء اللحظة (اضغط للتغيير)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = randomDua.ifBlank { "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي" },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- 5. 3x3 Grid Navigation ---
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "بوابة نور الإسلام الشاملة",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC9A84C),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val chunks = navGridItems.chunked(3)
                chunks.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .clickable { onNavigate(item.targetRoute) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.05f)
                                ),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(0xFFC9A84C).copy(alpha = 0.10f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.labelAr,
                                            tint = Color(0xFFC9A84C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.labelAr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 6. Bottom AI Button Bar ---
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .clickable { onNavigate("chat") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC9A84C)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Spark Diamond rotate-45 asset
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.20f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .rotate(45f)
                                    .background(Color.White)
                            )
                        }
                        
                        Text(
                            text = "اسأل IslamAI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D4A30)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ask IslamAI",
                        tint = Color(0xFF0D4A30).copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- QURAN CREEEM VIEW ---
@Composable
fun QuranView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    val verses by viewModel.surahVerses
    val isPlaying by viewModel.isAudioPlaying
    val isLoading by viewModel.isAudioLoading
    val activeUrl by viewModel.audioPlayingUrl
    val currentTitle by viewModel.currentAudioTitle
    
    val currentSurahId by viewModel.selectedSurahId
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showSurahList by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { 
                    Text(
                        text = if (showSurahList) "فهرس القرآن الكريم (١١٤ سورة)" else "سورة ${IslamicStaticData.surahs.find { it.number == currentSurahId }?.nameAr ?: ""}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSurahList) {
                            onBack()
                        } else {
                            viewModel.stopAudio()
                            showSurahList = true
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showSurahList) {
                // List of 114 Surahs
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(IslamicStaticData.surahs) { surah ->
                        Card(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectSurah(surah.number)
                                    showSurahList = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Number Circle
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0xFF0D4A30), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${surah.number}", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = surah.nameAr, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                        Text(text = "${surah.nameEn} • ${surah.englishMeaning}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = surah.typeAr, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "${surah.verseCount} آية", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                // View Surah Verses
                Column(modifier = Modifier.fillMaxSize()) {
                    // Settings Bar (Language Translator / Reciter select)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF10442D))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Translation select
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "الترجمة: ", fontSize = 11.sp, color = Color.White)
                            val langs = listOf("ar" to "عربي", "en" to "EN", "fr" to "FR")
                            langs.forEach { (code, label) ->
                                TextButton(
                                    onClick = { viewModel.preferredLanguage.value = code },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (viewModel.preferredLanguage.value == code) FontWeight.Bold else FontWeight.Normal,
                                        color = if (viewModel.preferredLanguage.value == code) Color(0xFFC9A84C) else Color.White
                                    )
                                }
                            }
                        }

                        // Play Surah MP3 Recitation audio
                        IconButton(onClick = {
                            if (isPlaying) {
                                viewModel.stopAudio()
                            } else {
                                // Standard Quran.com MP3 recitation URLs from beautiful hosts
                                val formatSurahNum = String.format("%03d", currentSurahId)
                                val playUrl = "https://download.quranicaudio.com/quran/abdul_basit_murattal/$formatSurahNum.mp3"
                                val nameOfSurah = IslamicStaticData.surahs.find { it.number == currentSurahId }?.nameAr ?: ""
                                viewModel.playAudio(playUrl, "سورة $nameOfSurah - بصوت الشيخ عبد الباسط عبد الصمد")
                            }
                        }) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFC9A84C), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = if (isPlaying && activeUrl?.contains(String.format("%03d", currentSurahId)) == true) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Play Surah audio",
                                    tint = Color(0xFFC9A84C),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    if (viewModel.isQuranLoading.value) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF0D4A30))
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Show Bismillah on all surahs except At-Tawbah (Surah 9)
                            if (currentSurahId != 9) {
                                item {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color(0xFF0D4A30),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            items(verses) { verse ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Arabic text with complete diacritics
                                        Text(
                                            text = "${verse.text_uthmani ?: ""} ﴿${verse.verse_number}﴾",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            lineHeight = 34.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        
                                        // Selected translation
                                        val curLang = viewModel.preferredLanguage.value
                                        if (curLang != "ar") {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val translationText = when (curLang) {
                                                "en" -> verse.translations?.firstOrNull { it.resource_id == 131 }?.text ?: "No English translation available"
                                                "fr" -> verse.translations?.firstOrNull { it.resource_id == 136 }?.text ?: "Pas de traduction disponible"
                                                else -> ""
                                            }
                                            Text(
                                                text = translationText.replace(Regex("<[^>]*>"), ""),
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Left,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Actions: Listen / Tafsir
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { viewModel.loadTafsirFor(verse.verse_key) }) {
                                                Text(text = "عرض التفسير", fontSize = 11.sp, color = Color(0xFF156E43), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Player Banner (if any recitation is loaded or playing)
            if (isPlaying || isLoading) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF072417)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Active Music Icon",
                                tint = Color(0xFFC9A84C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentTitle ?: "جاري تشغيل التلاوة...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(text = "صوت نقي بصيغة MP3 مباشر", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { viewModel.stopAudio() }) {
                            Icon(imageVector = Icons.Default.StopCircle, contentDescription = "Stop Recruitment", tint = Color.Red, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
        }
    }

    // Tafsir Dialog
    if (viewModel.showTafsir.value) {
        AlertDialog(
            onDismissRequest = { viewModel.showTafsir.value = false },
            title = { Text(text = "التفسير الميسر للآية", fontWeight = FontWeight.Bold, color = Color(0xFF0D4A30)) },
            text = { Text(text = viewModel.selectedVerseTafsir.value ?: "", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { viewModel.showTafsir.value = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30))
                ) {
                    Text(text = "حسناً", color = Color.White)
                }
            }
        )
    }
}

// --- PRAYER TIMES SCHEDULE SCREEN ---
@Composable
fun PrayersView(viewModel: IslamicViewModel, settings: SettingsEntity, onBack: () -> Unit) {
    val prayers by viewModel.prayerTimes
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "مواقيت الصلاة الشرعية", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Timetable", tint = Color(0xFFC9A84C), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "أوقات الصلاة في مدينة ${settings.city}", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "طريقة الحساب: رابطة العالم الإسلامي / الجزائر", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }

            prayers?.let { p ->
                val list = listOf(
                    Pair("الإمساك", p.Sunrise), // Standard reference Sunrise as proxy
                    Pair("الفجر", p.Fajr),
                    Pair("الشروق", p.Sunrise),
                    Pair("الظهر", p.Dhuhr),
                    Pair("العصر", p.Asr),
                    Pair("المغرب", p.Maghrib),
                    Pair("العشاء", p.Isha)
                )
                items(list) { (name, timeVal) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(34.dp).background(Color(0xFF0D4A30).copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = name, tint = Color(0xFF0D4A30), modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text(text = timeVal, color = Color(0xFF0D4A30), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        IslamicNotificationManager.showAdhanNotification(context, "الفجر")
                        // Trigger immediate alert to show user the notification is alive and well!
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "اختبار منبه الصلاة (الأذان)", color = Color.White)
                }
            }
        }
    }
}

// --- AZKAR & DUAS VIEW ---
@Composable
fun AzkarView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    var curCategory by remember { mutableStateOf("أذكار الصباح") }
    val categories = listOf("أذكار الصباح", "أذكار المساء", "أدعية الأنبياء", "أدعية الصلاة")
    
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "الأذكار والأدعية اليومية", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Horizontal Scroller Category Selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color(0xFF093120))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isActive = curCategory == cat
                    Card(
                        modifier = Modifier
                            .clickable { curCategory = cat }
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) Color(0xFFC9A84C) else Color.Transparent
                        ),
                        border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = if (isActive) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // List of respective azkar with Click-Vibrate-Count loop
            val azkarList = when (curCategory) {
                "أذكار الصباح" -> listOf(
                    Triple("أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.", "3", "فضل: من قالها كفي هم يومه وحفظه المولى."),
                    Triple("يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ أَصْلِحْ لِي شَأْنِي كُلَّهُ.", "3", "فضل: صلاح الشأن والنفس من الله."),
                    Triple("اللَّهُمَّ عافِنِي فِي بَدَنِي، اللَّهُمَّ عافِنِي فِي سَمْعِي، اللَّهُمَّ عافِنِي فِي بَصَرِي.", "3", "طلب العافية والوقاية البدنية.")
                )
                "أذكار المساء" -> listOf(
                    Triple("أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ.", "3", "أداء شكر اليوم في المساء."),
                    Triple("أعوذ بكلمات الله التامات من شر ما خلق.", "3", "فضل: لا يضره شيء في ليلته تلك."),
                    Triple("اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ.", "1", "حفظ وتفويض الشؤون للخالق.")
                )
                "أدعية الأنبياء" -> IslamicStaticData.offlineDuas["أدعية الأنبياء"]!!.map { Triple(it.prayerText, "١", it.benefit) }
                "أدعية الصلاة" -> IslamicStaticData.offlineDuas["أدعية الصلاة"]!!.map { Triple(it.prayerText, "١", it.benefit) }
                else -> emptyList()
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                items(azkarList) { (text, targetRepeat, benefit) ->
                    var countState = remember { mutableStateOf(0) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                if (countState.value < targetRepeat.toInt()) {
                                    countState.value++
                                    vibrator.vibrate(40) // Click vibration
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = text,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Right
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = benefit, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Progress Circle
                                Button(
                                    onClick = { countState.value = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "إعادة ضبط", color = Color.Red, fontSize = 10.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "التكرار: ", fontSize = 12.sp, color = Color.Gray)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(if (countState.value >= targetRepeat.toInt()) Color(0xFFC9A84C) else Color(0xFF0D4A30), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "${countState.value}/${targetRepeat}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- LIVE RADIO SCREEN VIEW ---
@Composable
fun AudioEqualizerAnimation(isPlaying: Boolean) {
    if (!isPlaying) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.height(18.dp)
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(3.dp)
                        .background(Color(0xFFC9A84C), RoundedCornerShape(1.dp))
                )
            }
        }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.height(18.dp)
        ) {
            val heights = listOf(0.2f, 0.7f, 0.4f, 0.9f, 0.3f)
            val durations = listOf(500, 750, 600, 850, 550)
            
            heights.forEachIndexed { index, baseHeight ->
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = baseHeight,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durations[index], easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$index"
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight(scale)
                        .background(Color(0xFFC9A84C), RoundedCornerShape(1.dp))
                )
            }
        }
    }
}

@Composable
fun RadioView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    val isPlaying by viewModel.isAudioPlaying
    val isLoading by viewModel.isAudioLoading
    val activeUrl by viewModel.audioPlayingUrl
    val currentTitle by viewModel.currentAudioTitle

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val pulseScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseSize"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "الراديو الإسلامي المباشر", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFF051D12)) // Immersive deep emerald dark background
        ) {
            // Immersive physical player deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F3A22), Color(0xFF072013))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.3f)), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Outer rotating/pulsing dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFC9A84C).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFF051D12), CircleShape)
                                .border(BorderStroke(2.dp, Color(0xFFC9A84C)), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radio,
                                contentDescription = "Radio signal",
                                tint = if (isPlaying) Color(0xFFE9C46A) else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPlaying) "يبث الآن بجودة عالية" else if (isLoading) "جاري الاتصال بقناة البث وتجهيز الصوت..." else "الراديو متوقف",
                        color = if (isPlaying) Color(0xFFE9C46A) else Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentTitle ?: "اختر محطة للبث المباشر MP3 عالي الدقة أدناه",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Visualizer Equalizer
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioEqualizerAnimation(isPlaying = isPlaying)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Player buttons row
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFFC9A84C),
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                        } else {
                            if (isPlaying) {
                                // STOP broadcast button
                                FloatingActionButton(
                                    onClick = { viewModel.stopAudio() },
                                    containerColor = Color(0xFFD62828),
                                    contentColor = Color.White,
                                    shape = CircleShape,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Stop stream",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            } else {
                                // PLAY active station if any, or play the first station of list (Ruqyah) as default!
                                val defaultStation = IslamicStaticData.radioStations.first()
                                FloatingActionButton(
                                    onClick = { 
                                        viewModel.playAudio(
                                            activeUrl ?: defaultStation.url, 
                                            currentTitle ?: defaultStation.name
                                        ) 
                                    },
                                    containerColor = Color(0xFFC9A84C),
                                    contentColor = Color.Black,
                                    shape = CircleShape,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play stream",
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "قنوات البث المباشر المتاحة:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE9C46A),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Stations list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                items(IslamicStaticData.radioStations) { station ->
                    val isCurrent = activeUrl == station.url
                    val isCurrentAndPlaying = isCurrent && isPlaying
                    
                    val borderGradient = if (isCurrent) {
                        BorderStroke(1.5.dp, Color(0xFFC9A84C))
                    } else {
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                viewModel.playAudio(station.url, station.name)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFF134E39) else Color(0xFF0B2B1B)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = borderGradient
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left accent indicator, matching category colors
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(50.dp)
                                    .background(
                                        color = when (station.category) {
                                            "رقية شرعية" -> Color(0xFFE76F51) // Warm warning/protection color
                                            "الجزائر", "ورشفة جزائرية" -> Color(0xFF2A9D8F) // Beautiful Algerian Teal
                                            "مصر" -> Color(0xFF3A86C8) // Nile Blue
                                            else -> Color(0xFFC9A84C) // Elegant Golden
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = station.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isCurrent) Color(0xFFE9C46A) else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Custom visual category badge
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = Color.White.copy(alpha = 0.08f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = station.category, 
                                            fontSize = 9.sp, 
                                            color = Color(0xFFE9C46A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = station.info, 
                                    fontSize = 11.sp, 
                                    color = Color.White.copy(alpha = 0.6f),
                                    lineHeight = 15.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { 
                                    if (isCurrentAndPlaying) {
                                        viewModel.stopAudio()
                                    } else {
                                        viewModel.playAudio(station.url, station.name)
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (isCurrent) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.05f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isCurrentAndPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = if (isCurrentAndPlaying) "Stop" else "Play",
                                    tint = if (isCurrent) Color.Black else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ELECTRONIC SEBHA SCREEN ---
@Composable
fun SebhaView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    val phrases = viewModel.tasbihPhrases
    val activeIdx by viewModel.activeTasbihIndex
    val currentPhrase = phrases[activeIdx]
    
    // Flow collector
    val counterFlow = remember(currentPhrase) { viewModel.getPhraseCount(currentPhrase) }
    val count by counterFlow.collectAsState(initial = 0)

    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Scale animation state for rosary shake
    val rosaryScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "السبحة الإلكترونية الذاتية", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                // Phrase selectors tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    phrases.forEachIndexed { idx, pr ->
                        val isSelected = activeIdx == idx
                        Card(
                            modifier = Modifier.clickable { viewModel.activeTasbihIndex.value = idx },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFC9A84C) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Text(
                                text = pr,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                // Active Phrase banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30))
                ) {
                    Text(
                        text = currentPhrase,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Central Ring Clicker button (concentric circles)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(rosaryScale.value)
                    .background(Color(0xFF0D4A30).copy(alpha = 0.08f), CircleShape)
                    .border(3.dp, Color(0xFFC9A84C), CircleShape)
                    .clickable {
                        coroutineScope.launch {
                            // Rosary ripple impact visual effect
                            rosaryScale.animateTo(0.9f, spring(stiffness = Spring.StiffnessHigh))
                            rosaryScale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                        }

                        viewModel.incrementTasbihCount(currentPhrase) {
                            // Vibrates when completes 33 repetitions
                            vibrator.vibrate(200)
                        }
                        vibrator.vibrate(35) // normal click vibration
                    },
                contentAlignment = Alignment.Center
            ) {
                // Concentric interior dial
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(Color(0xFF0D4A30), CircleShape)
                        .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "اضغط للتسبيح", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$count",
                            color = Color(0xFFC9A84C),
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "دورة الـ ٣٣", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            // Lower Action Tray (Reset, configure)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { viewModel.resetTasbihCount(currentPhrase) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "إعادة تصفير العداد", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 99 NAMES OF ALLAH SCREEN ---
@Composable
fun NamesView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    var selectedName by remember { mutableStateOf<IslamicStaticData.NameOfAllah?>(null) }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "أسماء الله الحسنى ومعانيها", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                items(IslamicStaticData.namesOfAllah) { info ->
                    Card(
                        modifier = Modifier
                            .padding(6.dp)
                            .aspectRatio(1f)
                            .clickable { selectedName = info },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = info.nameAr,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0D4A30),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = info.nameEn,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Details Modal
    selectedName?.let { n ->
        AlertDialog(
            onDismissRequest = { selectedName = null },
            title = {
                Text(
                    text = n.nameAr + " - " + n.nameEn,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D4A30),
                    fontSize = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "المعنى باللغة العربية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Text(text = n.meaningAr, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text(text = "Meaning in English:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    Text(text = n.meaningEn, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedName = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30))
                ) {
                    Text(text = "سبحان الله العظيم", color = Color.White)
                }
            }
        )
    }
}

// --- QIBLA DIRECTION COMPASS VIEW ---
@Composable
fun QiblaView(viewModel: IslamicViewModel, settings: SettingsEntity, onBack: () -> Unit) {
    val context = LocalContext.current
    val qAngle by viewModel.qiblaDirectionAngle
    val sensorHeading by viewModel.userHeadingAngle

    // Calculate Mecca azimuth once
    LaunchedEffect(Unit) {
        viewModel.calculateQiblaAngle(settings.latitude, settings.longitude)
    }

    // Compass sensor listening hook
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) }
    
    val listener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // gets the azimuth angle
                    viewModel.userHeadingAngle.value = it.values[0]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Smooth compass pointer rotation
    val compassRotation = (360f - sensorHeading) 
    val meccaRotationOffset = (compassRotation + qAngle) % 360f

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "تحديد اتجاه القبلة شرياناً", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "مكة المكرمة باتجاه ${String.format("%.1f", qAngle)}° شمالاً",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "موقعك الحالي: ${settings.city}. وجّه ظهر الهاتف نحو القبلة برفق.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Beautiful Compass Visual Dial
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color(0xFF0D4A30).copy(alpha = 0.05f), CircleShape)
                    .border(4.dp, Color(0xFFC9A84C), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // North symbol indicator rose (rotated by compass azimuth)
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .rotate(compassRotation),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "شمال (N)",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopCenter).padding(8.dp)
                    )
                    Text(
                        text = "جنوب (S)",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
                    )
                }

                // High-Contrast Golden Mecca Needle (point directly to Kaaba angle!)
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .rotate(meccaRotationOffset),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Mecca arrow direction",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.TopCenter)
                    )
                    
                    // Center dial
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0D4A30), CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Mecca Kaaba Center icon", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30))
            ) {
                Text(
                    text = "تقع الكعبة المشرفة في مكة المكرمة بالاتجاه المبين بالسهم الذهبي التام.",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(14.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --- ISLAMAI AI CHAT SCREEN CHIEF VIEW ---
@Composable
fun ChatView(viewModel: IslamicViewModel, onBack: () -> Unit) {
    val messages = viewModel.chatMessages
    val isLoading by viewModel.isChatLoading
    var userInput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "المساعد الذكي IslamAI", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat history list bubble
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                reverseLayout = false
            ) {
                items(messages) { msg ->
                    val bubbleBg = if (msg.isUser) Color(0xFFE4EDE9) else Color(0xFF0D4A30).copy(alpha = 0.08f)
                    val alignAlign = if (msg.isUser) Alignment.End else Alignment.Start
                    val borderSide = if (msg.isUser) null else BorderStroke(1.dp, Color(0xFF0D4A30).copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = alignAlign
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = bubbleBg),
                            border = borderSide,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                bottomEnd = if (msg.isUser) 4.dp else 16.dp
                            )
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = if (msg.isUser) Color.Black else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }
                
                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "IslamAI يفكر الآن ويرتب الأدلة الإسلامية...", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Divider separator
            HorizontalDivider()

            // Input Form Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text(text = "اسأل عن الفقه التفسير السيرة...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            viewModel.submitAiMessage(userInput)
                            userInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF0D4A30), CircleShape)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send prompt", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// --- SETTINGS PREFERENCES VIEW ---
@Composable
fun SettingsView(viewModel: IslamicViewModel, settings: SettingsEntity, onBack: () -> Unit) {
    var editName by remember { mutableStateOf(settings.username) }
    var selectedCityIdx by remember { mutableStateOf(0) }
    
    // Predetermined preset cities
    data class CityPreset(val nameAr: String, val nameEn: String, val country: String, val lat: Double, val lng: Double)
    val presets = listOf(
        CityPreset("الجزائر", "Algiers", "DZ", 36.75, 3.04),
        CityPreset("مكة المكرمة", "Mecca", "SA", 21.42, 39.82),
        CityPreset("المدينة المنورة", "Medina", "SA", 24.46, 39.61),
        CityPreset("القاهرة", "Cairo", "EG", 30.04, 31.23),
        CityPreset("الرباط", "Rabat", "MA", 34.02, -6.83)
    )

    LaunchedEffect(settings) {
        val presetIdx = presets.indexOfFirst { it.nameAr == settings.city }
        if (presetIdx != -1) {
            selectedCityIdx = presetIdx
        }
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(text = "الإعدادات والملف الشخصي", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D4A30))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: User Profile Setup Card
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "تهيئة الملف الشخصي للترحيب", fontWeight = FontWeight.Bold, color = Color(0xFF0D4A30))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(text = "اسم المستخدم") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Section 2: City Selectionpreset
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "اختر المدينة لحساب مواقيت الصلاة والطقس", fontWeight = FontWeight.Bold, color = Color(0xFF0D4A30))
                    Spacer(modifier = Modifier.height(10.dp))
                    presets.forEachIndexed { idx, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCityIdx = idx }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCityIdx == idx,
                                onClick = { selectedCityIdx = idx }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = p.nameAr + " (${p.nameEn})", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Save Settings Button
            Button(
                onClick = {
                    val p = presets[selectedCityIdx]
                    viewModel.saveUserProfile(
                        name = editName,
                        city = p.nameAr,
                        country = p.country,
                        lat = p.lat,
                        lng = p.lng,
                        method = 3 // default Algerian MWL method
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "حفظ التغييرات الآن", color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Section 3: Developer Info credit card - Developed by FaresCodeX as requested
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFC9A84C))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "About Developer", tint = Color(0xFFC9A84C), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "مطور التطبيق ونسب العمل", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF0D4A30))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تصميم وإنجاز المطور القدير: FaresCodeX",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تم برمجية هذا الصرح الإسلامي الشامل ابتغاء وجه الله تعالى وتحقيقاً لكافة رغبة وراحة الأمة الإسلامية جمعاء.",
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. SPLASH VIEW (شاشة ترحيبية تفاعلية)
// -------------------------------------------------------------
@Composable
fun SplashView(
    viewModel: IslamicViewModel,
    onLoadingFinished: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var currentDuaIndex by remember { mutableStateOf(0) }
    
    val splashDuas = listOf(
        "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُورًا، وَفِي بَصَرِي نُورًا",
        "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
        "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
        "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي"
    )

    // Animated splash background
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        // Rotate loading supplications
        launch {
            while (progress < 1.0f) {
                delay(1200)
                currentDuaIndex = (currentDuaIndex + 1) % splashDuas.size
            }
        }
        // Increment progress simulating robust initializing of GPS / database
        launch {
            while (progress < 1.0f) {
                delay(30)
                progress += 0.01f
            }
            delay(400)
            onLoadingFinished()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D4A30), Color(0xFF051D14))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Crescent and Star Symbol
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Brightness3,
                    contentDescription = "Crescent symbol",
                    tint = Color(0xFFC9A84C),
                    modifier = Modifier.size(100.dp).rotate(-45f)
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star symbol",
                    tint = Color(0xFFC9A84C),
                    modifier = Modifier.size(24.dp).padding(top = 10.dp, end = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "نور الإسلام",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Noor Al-Islam",
                fontSize = 14.sp,
                color = Color(0xFFC9A84C),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Supplication Card fading in and out
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Star icon",
                        tint = Color(0xFFC9A84C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = splashDuas[currentDuaIndex],
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = Color(0xFFC9A84C),
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "جاري تهيئة مواقيت الصلاة والأذكار... ${(progress * 100).toInt()}%",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// -------------------------------------------------------------
// 2. HIJRI CALENDAR VIEW (التقويم الهجري والأحداث الإسلامية)
// -------------------------------------------------------------
@Composable
fun HijriCalendarView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    val hijriMonths = listOf(
        Pair("مُحَرَّم", "الشهور الحُرم - بداية السنة الهجرية"),
        Pair("صَفَر", "ثاني شهور السنة الهجرية"),
        Pair("رَبِيع الأَوَّل", "شهد مولد الرسول ﷺ"),
        Pair("رَبِيع الآخِر", "من الشهور المباركة"),
        Pair("جُمَادَى الأُولَى", "من شهور فصل الشتاء قديماً"),
        Pair("جُمَادَى الآخِرَة", "من الشهور العربية الجليلة"),
        Pair("رَجَب", "من الأشهر الحُرُم والأسرى والمعراج"),
        Pair("شَعْبَان", "ترفع فيه الأعمال إلى الله"),
        Pair("رَمَضَان", "شَهْرُ الصِّيَامِ وَالْقُرْآن"),
        Pair("شَوَّال", "فيه عيد الفطر المبارك"),
        Pair("ذُو الْقَعْدَة", "من الأشهر الحُرُم الحبيبة"),
        Pair("ذُو الْحِجَّة", "فيه مناسك الحج وعيد الأضحى")
    )

    val events = listOf(
        Triple("رأس السنة الهجرية", "1 محرم", "بداية التقويم الإسلامي الحنيف"),
        Triple("عاشوراء", "10 محرم", "صيام شكر لنجاة سيدنا موسى عليه السلام"),
        Triple("المولد النبوي الشريف", "12 ربيع الأول", "مولد المصطفى والهدى محمد ﷺ"),
        Triple("الإسراء والمعراج", "27 رجب", "معجزة الإسراء بمحمد ﷺ إلى السماوات العلى"),
        Triple("بداية شهر رمضان", "1 رمضان", "بدء صيام المسلمين ونزول القرآن الكريم"),
        Triple("عيد الفطر المبارك", "1 شوال", "جائزة الصائمين بعد تمام الفرض"),
        Triple("يوم عرفة", "9 ذو الحجة", "أعظم أيام الدنيا والوقوف بجبل الرحمة"),
        Triple("عيد الأضحى المبارك", "10 ذو الحجة", "النحر وإحياء سنة الخليل إبراهيم عليه السلام")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        // Dynamic Gold header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("التقويم الإسلامي والمناسبات", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = Color(0xFFC9A84C))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Countdowns Section
            item {
                Text("العد التنازلي للمناسبات الكبرى", color = Color(0xFFC9A84C), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المناسبة القادمة", color = Color.LightGray, fontSize = 12.sp)
                            Text("الأيام المتبقية (تقريبي)", color = Color(0xFFC9A84C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Color.White.copy(alpha = 0.10f))
                        
                        val upcoming = listOf(
                            Pair("شهر رمضان المبارك", 260),
                            Pair("عيد الفطر السعيد", 290),
                            Pair("يوم عرفة الميمون", 350)
                        )
                        upcoming.forEach { (name, days) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFC9A84C), CircleShape))
                                    Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("$days يوم", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // Months of Hijri Year grid list
            item {
                Text("شهور السنة الهجرية", color = Color(0xFFC9A84C), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(hijriMonths.chunked(2)) { pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { (name, desc) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(name, color = Color(0xFFC9A84C), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            // Events List Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("الأحداث والمناسبات في الإسلام", color = Color(0xFFC9A84C), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(events) { (title, date, desc) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFF0D4A30).copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFC9A84C).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(date, color = Color(0xFFC9A84C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TAJWEED VIEW (تعليم التجويد من الصفر إلى الاحتراف)
// -------------------------------------------------------------
@Composable
fun TajweedView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) } // 1: Noon, 2: Meem, 3: Madd, 4: Recitation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("موسوعة فقه علوم التجويد", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.School, contentDescription = "Tajweed", tint = Color(0xFFC9A84C))
            }
        }

        // Horizontal tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30).copy(alpha = 0.5f))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(
                Triple(1, "النون الساكنة", Icons.Default.MenuBook),
                Triple(2, "الميم الساكنة", Icons.Default.Favorite),
                Triple(3, "أحكام المدود", Icons.Default.School),
                Triple(4, "التفخيم", Icons.Default.ArrowUpward)
            ).forEach { (id, label, icon) ->
                val isSel = selectedTab == id
                Column(
                    modifier = Modifier
                        .clickable { selectedTab = id }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSel) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSel) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (selectedTab == 1) {
                item {
                    Text("أحكام النون الساكنة والتنوين", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("للنون الساكنة والتنوين أربعة أحكام تختلف بحسب الحرف الذي بعدهما:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                val rules = listOf(
                    Triple("1. الإظهار الحلقي", "نطق النون بوضوح من غير غنّة عند ملاقاة أحد حروف الحلق الستة:", "حروفه: ( أ، هـ، ع، غ، ح، خ ) - مثال: (مَنْ آمَنَ، يَنْأَوْنَ)"),
                    Triple("2. الإدغام يغنة وبغير غنة", "دمج النون الساكنة بالحرف التالي بحيث يصيران حرفاً مشدداً واحد:", "حروفه: ( ي، ر، م، ل، و، ن ) مجموعة في 'يرملون'.\nبإدغام يغنة (ينمو) - مثال: (مَن يَقُول)\nبغير غنة (ل، ر) - مثال: (مِّن رَّبِّهم)"),
                    Triple("3. الإقلاب", "قلب النون الساكنة أو التنوين ميماً مخفاة مع بقاء الغنّة إذا خلفهما حرف الباء:", "حروفه: ( الباء فقط - ب ) - مثال: (مِن بَعْدِ، سَمِيعٌ بَصِيرٌ) ونضع ميم صغيرة فوق الكلمة"),
                    Triple("4. الإخفاء الحقيقي", "نطق الحرف بحالة بين الإظهار والإدغام عار عن التشديد مع الغنة:", "حروفه: بقية حروف الهجاء الـ15 مجموعة في أوائل كلمات البيت: 'صف ذا ثنا كم جاد شخص قد سما..'")
                )
                items(rules) { (title, desc, examples) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D4A30).copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(examples, color = Color(0xFFC9A84C), fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                item {
                    Text("أحكام الميم الساكنة", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("للميم الساكنة ثلاثة أحكام رئيسية عند التقائها بحروف الهجاء:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                val meemRules = listOf(
                    Triple("1. الإخفاء الشفوي", "إخفاء الميم الساكنة مع بقاء الغنة إذا التقت بحرف الباء ككلمة واحدة أو كلمتين:", "مثال: (تَرْمِيهِم بِحِجَارَةٍ ، يعتصم بالله)"),
                    Triple("2. إدغام المثلين الصغير", "إدغام الميم الساكنة بميم متحركة تليها وتلفظ ميم واحدة مشددة غنة:", "مثال: (أَم مَّن ، لَهُم مَّا يَشَاءُونَ)"),
                    Triple("3. الإظهار الشفوي", "نطق الميم مظهراً واضحاً من غير غنة عند التقائها بباقي الحروف:", "مثال: (أَلَمْ تَرَ ، تَمْسُونَ) وتتميز بالبطش الزائد عند حرفي الواو والفاء لقرب المخرّج")
                )
                items(meemRules) { (title, desc, examples) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D4A30).copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(examples, color = Color(0xFFC9A84C), fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            } else if (selectedTab == 3) {
                item {
                    Text("أحكام المدود (إطالة الحرف)", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("حروف المد ثلاثة وهي الألف الساكنة المفتوح ما قبلها، والواو المضموم ما قبلها، والياء المكسور ما قبلها:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                val mads = listOf(
                    Triple("1. المد الأصلي (الطبيعي)", "هو الذي لا تتوقف كتابته وصوته على سبب من همز أو سكون ويمد بمقدار حركتين:", "مثال: (قولوا، نوحيها)"),
                    Triple("2. المد الفرعي بسبب الهمز", "مد يطال بوجود همزة بعده، وينقسم لمد متصل واجب (٤-٥ حركات) ومنفصل جائز:", "متصل مثال: (جَاءَ، لِقَاءَ)\nمنفصل مثال: (بِمَا أُنزِلَ)"),
                    Triple("3. المد الفرعي بسبب السكون", "مد سببه وقوع سكون عارض أو سكون لازم بعد حرف المد:", "عارض للسكون (٢ أو ٤ أو ٦ حركات) مثال: (العالمين، نستعين)\nلازم كعلم مشدد (٦ حركات) مثال: (الْضَّالِّينَ)")
                )
                items(mads) { (title, desc, examples) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D4A30).copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(examples, color = Color(0xFFC9A84C), fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            } else if (selectedTab == 4) {
                item {
                    Text("أحكام التفخيم والترقيق", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("تسمين الحرف أو تنحيف صوته لتزيين القراءة وصونها من اللحن:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                val levels = listOf(
                    Triple("الحروف المفخمة دائماً", "سبعة أحرف يجمعها مسمى 'خص ضغط قظ' وتأتي في مراتب التفخيم القوية:", "مثال: (خَالِدِينَ ، الطَّامَّة ، الصَّابِرِينَ)"),
                    Triple("الحروف المرققة دائماً", "بقية أحرف اللغة العربية ما عدا الأحرف الدائرة بين التفخيم والترقيق:", "مثال: (ب، ت، ث، ج، ح، د) وغيرها تنطق خفيفة مرققة"),
                    Triple("الحروف الدائرة بين التفخيم والترقيق", "تارة تفخم وتارة ترقق بحسب موضعها وقواعدها المحددة:", "ألف المد (تتبع ما قبلها تفخيماً وترقيقاً)\nلام لفظ الجلالة (تفخم بعد الفتح والضم وترقق بعد الكسر)\nحرف الراء (له أحكام تفخيم مفصلة)")
                )
                items(levels) { (title, desc, examples) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(desc, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0D4A30).copy(alpha = 0.2f))
                                    .padding(8.dp)
                            ) {
                                Text(examples, color = Color(0xFFC9A84C), fontSize = 12.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. FIQH VIEW (فقه الصلاة وتعليم السنن والأركان)
// -------------------------------------------------------------
@Composable
fun FiqhView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    var selectedCat by remember { mutableStateOf("pillars") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("فقه الصلاة والأحكام الفقهية", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.MenuBook, contentDescription = "Fiqh", tint = Color(0xFFC9A84C))
            }
        }

        // Segmented selector list selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30).copy(alpha = 0.3f))
                .padding(8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("pillars", "أركان الصلاة"),
                Pair("conditions", "شروط صحة الصلاة"),
                Pair("sunnahs", "سنن الصلاة المأثورة"),
                Pair("disliked", "مكروهات الصلاة"),
                Pair("jamaa", "صلاة الجماعة والإمامة")
            ).forEach { (code, label) ->
                val isSel = selectedCat == code
                Box(
                    modifier = Modifier
                        .clickable { selectedCat = code }
                        .background(
                            color = if (isSel) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSel) Color(0xFF051D14) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            when (selectedCat) {
                "pillars" -> {
                    item {
                        Text("أركان الصلاة الأربعة عشر (لا تسقط سهواً ولا عمداً)", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    val pillarsList = listOf(
                        "1. القيام في الفرض مع القدرة عليه.",
                        "2. تكبيرة الإحرام لبدء الصلاة (قول: الله أكبر).",
                        "3. قراءة الفاتحة في كل ركعة للأمام والمنفرد.",
                        "4. الركوع وتطمين الظهر مستوياً.",
                        "5. الرفع من الركوع والاعتدال قائماً.",
                        "6. السجود على الأعضاء السبعة (الجبهة مع الأنف، الكفين، الركبتين، أطراف القدمين).",
                        "7. الرفع من السجود والاعتدال جالساً.",
                        "8. الجلوس بين السجدتين وهو ركن السكينة والطمأنينة.",
                        "9. الطمأنينة الكاملة في جميع الأفعال المذكورة.",
                        "10. التشهد الأخير والجلوس له.",
                        "11. الصلاة على النبي مأثورة في التشهد الأخير.",
                        "12. التسليمتان جليتان يميناً ويساراً.",
                        "13. الترتيب بين كافة الأركان السابقة كما تواتر."
                    )
                    items(pillarsList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(text = item, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
                "conditions" -> {
                    item {
                        Text("شروط الصلاة التسعة (مقدمات خارج الصلاة تسبقها)", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    val conditionsList = listOf(
                        "1. الإسلام: فلا يقبل الله عملاً من مرتد أو غير مسلم.",
                        "2. العقل: فلا تجب الصلاة ولا تصح من مجنون وغير واعي.",
                        "3. التمييز: بلوغ سن السابعة ليفهم الخطاب ويتقن الفعل.",
                        "4. رفع الحدث: بالوضوء الأصغر أو الغسل الأكبر إذا لزم.",
                        "5. إزالة النجاسة: من البدن، والثوب، والمكان الذي يصلي عليه.",
                        "6. ستر العورة: للرجل من السرة إلى الركبة، والمرأة كل بدنها ما عدا وجهها وكفيها.",
                        "7. دخول الوقت: فلا تجزئ صلاة قبل وقتها الشرعي.",
                        "8. استقبال القبلة: التوجه نحو الكعبة المشرفة بمكة المكرمة كشرط أساسي.",
                        "9. النية: محلها القلب ولا يشرع التلفظ بجهر لتجنّب اللبث."
                    )
                    items(conditionsList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(text = item, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
                "sunnahs" -> {
                    item {
                        Text("أبرز سنن الصلاة القولية والفعلية (تجبر الصلاة وتكملها)", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    val sunnahsList = listOf(
                        "دعاء الاستفتاح: بعد تكبيرة الإحرام (سبحانك اللهم وبحمدك وتبارك اسمك وتعالى جدك..).",
                        "رفع اليدين: حذو المنكبين أو فروع الأذنين عند تكبيرة الإحرام، وعند الركوع والرفع منه والقيام للركعة الثالثة.",
                        "وضع الكف الأيمن على الأيسر فوق الصدر أثناء القيام قبل الركوع.",
                        "قراءة سورة بعد الفاتحة في الركعتين الأولى والثانية من كل صلاة فرض.",
                        "الجهر بالقراءة في صلوات الفجر، المغرب، والعشاء، والإسرار في الظهر والعصر.",
                        "التورك والافتراش في وضعية الجلوس للتشهد والصلوات."
                    )
                    items(sunnahsList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(text = item, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
                "disliked" -> {
                    item {
                        Text("مكروهات الصلاة (أعمال تقلل من الثواب دون أن تبطلها)", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    val dislikedList = listOf(
                        "الالتفات بالوجه أو البصر لغير مبرر أو لزوم شرعي.",
                        "غمض العينين من دون خشوع أو حاجة.",
                        "تغطية الوجه مثل اللثام أو العبث بالملابس واللحية أو الساعة أثناء الوقوف.",
                        "افتراش الذراعين أثناء السجود (التشبه بافتراش السبع).",
                        "الصلاة بحضرة طعام يشتهيه، أو تمديد البدن تشاقياً بمدافعة الأخبثين (البول والبراز)."
                    )
                    items(dislikedList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(text = item, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
                "jamaa" -> {
                    item {
                        Text("أحكام صلاة الجماعة ومسؤوليات المأموم والإمام", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    val jamaaList = listOf(
                        "أن صلاة الجماعة تفضل صلاة الفرد بـ 27 درجة كما صح في الحديث النبوي.",
                        "متابعة المأموم للإمام فرض: فلا يسبقه في ركوع أو سجود ولا يتأخر تأخيراً يستوجب قطع المتابعة.",
                        "أولى الناس بالإمامة أقرؤهم لكتاب الله، فإن كانوا في القراءة سواء فأعلمهم بالسنة.",
                        "أقل الجماعة اثنان: إمام ومأموم يقف عن يمينه محاذياً له تماماً في المساواة."
                    )
                    items(jamaaList) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Text(text = item, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. SERMONS VIEW (المواعظ والدواوين والخطب المكتوبة وقصص الأنبياء)
// -------------------------------------------------------------
@Composable
fun SermonsView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    var selectedCat by remember { mutableStateOf("sermons") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("منبر خطب الجمعة وقصص الأنبياء", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.Campaign, contentDescription = "Sermons", tint = Color(0xFFC9A84C))
            }
        }

        // Category Switcher tabs
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0D4A30).copy(alpha = 0.5f)),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(
                Pair("sermons", "خطب الجمعة"),
                Pair("daily", "مواعظ مؤثرة"),
                Pair("props", "قصص الأنبياء")
            ).forEach { (code, label) ->
                val isSel = selectedCat == code
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSel) Color(0xFFC9A84C) else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier
                        .clickable { selectedCat = code }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (selectedCat == "sermons") {
                val sermonsList = listOf(
                    Pair(
                        "خطبة: فضل الصبر والإيمان والرضا بالقضاء والقدر المكتوب",
                        "الحمد لله العليّ القدير الباطن الظاهر الميسر لكل تيسير، والصلاة على البشير النذير.. أما بعد، فإن الصبر من عزم الأمور وشعبة متينة من مكارم وعلا الإيمان، فالصابر مستحق لمحبة ربه العليم، ومهما اشرأب البلاء وحاصر المرء، فإن فرج ربه قريب ونوره ساطع يملأ الدجى، فاتقوا الله صبراً ورضا وتوكلاً."
                    ),
                    Pair(
                        "خطبة: حسن الخلق ومسؤوليته العظمى في بناء حضارة الأمة الإسلامية",
                        "عباد الله، إن رسالة نبيكم المصطفى ﷺ تلخصت في حديثه الجامع الصريح: 'إنما بعثت لأتمم مكارم الأخلاق'. الخُلُق السامي هو مرآة الإيمان ومؤشر التقوى وقرب المنازل من مجالس النبيين في اليوم المعلوم. فلنحفظ الجوارح عن الأذى ونملأ مجتمعاتنا بالصدق، والعدل، والأمانة الكريمة."
                    )
                )
                items(sermonsList) { (title, content) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(content, color = Color.White, fontSize = 13.sp, lineHeight = 22.sp, textAlign = TextAlign.Right)
                        }
                    }
                }
            } else if (selectedCat == "daily") {
                val dailyList = listOf(
                    "ما من يوم ينشق فجره إلا وينادي منادٍ: يا ابن آدم، أنا خلق جديد، وعلى عملك شهيد، فتزود مني، فإني لا أعود إلى يوم القيامة.",
                    "إذا هممت بسيئة فلا تكتبها لعل الله يعفو، وإذا هممت بحسنة فاعملها فوراً فالحياة ساعات وخطوات معدودة تحت التراب.",
                    "الاستغفار يفتح الأقفال ويزيد الأرزاق ويريح الصدر ويكفّر الخطايا ويكشف الكرب والبلاء بإذن رب الأرض والسماوات."
                )
                items(dailyList) { text ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                        border = BorderStroke(1.dp, Color(0xFF0D4A30).copy(alpha = 0.5f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Dua icon", tint = Color(0xFFC9A84C), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (selectedCat == "props") {
                val storiesList = listOf(
                    Pair("سيدنا نوح عليه السلام - بناء السفينة والصبر العظيم", "دعا قومه 950 سنة ليلاً ونهارا وسراً وجهاراً وملاطفة وتوجيهاً فلم يؤمن معه إلا قليل، فأمره ربه تعالى ببناء سفينة النجاة بأرض جرداء وسط سخرية قومه حتى جاء أمر الله وفاضت الأرض طوفاناً نجى فيه المؤمنين الصابرين."),
                    Pair("سيدنا يوسف عليه السلام - البئر الضيق وعرش مصر العظيم", "ألقى به إخوته حسداً في الجب، فبيع بثمن بخس، ثم سجن ظلماً وصبر عفة وتقوى، حتى مكن الله له وجعله على خزائن مصر لإيمانه وعفوه عند مقدرة اللقاء بإخوته."),
                    Pair("سيدنا إبراهيم عليه السلام - النمرود والنيران التي أصبحت برداً وسلاماً", "حارب الشرك وحطم الأصنام وتحدى الطاغية المسمى نمرود، فألقوه بالنيران العظيمة فجاءه نداء السماء العظيم: 'يا نار كوني برداً وسلاماً على إبراهيم' ليخرج منها سالماً بإيمانه المخلص والعميق.")
                )
                items(storiesList) { (title, story) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(story, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 20.sp, textAlign = TextAlign.Right)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. HAJJ & UMRAH VIEW (دليل الحج والعمرة والخرائط التفاعلية للبلاد الحرام)
// -------------------------------------------------------------
@Composable
fun HajjView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    var stepIndex by remember { mutableStateOf(0) }
    
    val steps = listOf(
        Pair("1. الإحرام وتأدية ركن النية", "يبدأ الحاج غسله وتطهيره ثم يرتدي قطعتي ملابس الإحرام البيضاء في الميقات المخصص مع بقاء النية والتلبية والقول الدائم: 'لبيك اللهم لبيك'."),
        Pair("2. طواف القدوم الشريف بمكة", "يدخل المعتمر أو القارن لمكة الحبيبة ويطوف حول الكعبة المشرفة سبعة أشواط واثق الخطوة بدءاً من الحجر الأسود المعظم ويسن لمس الركن اليماني."),
        Pair("3. السعي الجليل بين الصفا والمروة", "يطوف سبع أشواط يقطعها بين جبل الصفا وجبل المروة جرياً وسيراً واحتساباً مقتدياً بسير السيدة هاجر عليها السلام."),
        Pair("4. يوم التروية في منى", "يأتي الحاج لمنى في الثامن من ذي الحجة فيبيت فيها ويصلي الصلوات الخمس وفق شريعة القصر والجمع بلا عجلة وتأهباً لليوم العظيم."),
        Pair("5. الوقوف الأكبر بعرفات", "في التاسع من ذي الحجة يقف الحاج بحدود جبل عرفة من زوال الشمس لغروبها ملازماً للذكر والدموع والضراعة والتوبة مستشعراً عظمة المشهد الأقدس."),
        Pair("6. مزدلفة وجمع الجمرات", "يفيض الحجاج لمزدلفة بعد الغروب فيصيبهم الخشوع ويصلون المغرب والعشاء جمعا ويجمعون حصوات رمي الجمرات مبيت التوكل والإيمان.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("مناسك الحج والعمرة والخرائط الشرعية", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.Map, contentDescription = "Hajj Map", tint = Color(0xFFC9A84C))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Supplication Section for rituals
            item {
                Text("أدعية المناسك المأثورة", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لا شَرِيكَ لَكَ.",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "دعاء الطواف المأثور: 'رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ'.",
                            color = Color(0xFFC9A84C),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Interactive Ritual steps walk
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مسيرة مناسك الحج خطوة بخطوة", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("المرحلة ${stepIndex + 1} من ${steps.size}", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = steps[stepIndex].first, color = Color(0xFFC9A84C), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = steps[stepIndex].second, color = Color.White, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Right)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { if (stepIndex > 0) stepIndex-- },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                enabled = stepIndex > 0
                            ) {
                                Text("السابق", color = Color.White)
                            }
                            Button(
                                onClick = { if (stepIndex < steps.size - 1) stepIndex++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A84C)),
                                enabled = stepIndex < steps.size - 1
                            ) {
                                Text("التالي", color = Color(0xFF051D14))
                            }
                        }
                    }
                }
            }

            // Map and Holy Sites Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("الموقع الجغرافي للمشاعر المقدسة بمكة", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Drawing schematic stylized layout map representation of Holy Sites
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF0D4A30).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFC9A84C).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("مسجد نمرة بعرفات ──> منى ──> مزدلفة ──> الحرم المكي", color = Color(0xFFC9A84C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("خريطة مرجعية تخطيطية موجه بالاتجاه الشرقي لمكة المكرمة", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "منى تقع شرق مكة على مسافة ٧ كم ومزدلفة تقع بين منى وعرفات وجبل الرحمة بعرفة يقع على مسافة ١٣ كم جنوباً، حيث يتلاحم المصلون لإرساء المناسك بفيض الإيمان وطيب الرحمة.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. KHATMA COMPLETION VIEW (متابعة ختم القرأن اليومي)
// -------------------------------------------------------------
@Composable
fun KhatmaView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    val juz by viewModel.khatmaJuz
    val page by viewModel.khatmaPage
    val readToday by viewModel.khatmaPagesReadToday
    val target by viewModel.khatmaTargetPages

    val percentage = (page.toFloat() / 604f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("منظم ومتابع ختم القرآن الكريم", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.TrackChanges, contentDescription = "Khatma", tint = Color(0xFFC9A84C))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Large circular progress percent status card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("معدل التقدم الإجمالي في الختمة", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = percentage,
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFFC9A84C),
                                strokeWidth = 10.dp,
                                trackColor = Color.White.copy(alpha = 0.10f)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(percentage * 100).toInt()}%", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                Text("إجمالي المنجز", color = Color(0xFFC9A84C), fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الصفحة الحالية", color = Color.LightGray, fontSize = 11.sp)
                                Text("$page / 604", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الجزء الحالي", color = Color.LightGray, fontSize = 11.sp)
                                Text("الجزء $juz", color = Color(0xFFC9A84C), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Daily tracking card action
            item {
                Text("تحديث الإنجاز اليومي", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ما تم قراءته اليوم", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("$readToday صفحة / $target صفحة", color = Color(0xFFC9A84C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = (readToday.toFloat() / target.toFloat()).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFC9A84C),
                            trackColor = Color.White.copy(alpha = 0.08f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.readKhatmaPages(5) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30))
                            ) {
                                Text("قرأت 5 صفحات", color = Color.White, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.readKhatmaPages(1) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D4A30))
                            ) {
                                Text("قرأت صفحة واحدة", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetKhatmaDailyProgress() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Text("تصفير عداد اليوم والبدء من جديد", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Daily tips motivation and encouragement
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC9A84C).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Goal", tint = Color(0xFFC9A84C))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "يا لها من بركة عظيمة! قراءة 20 صفحة يومياً (حوالي جزء واحد) يمنحك شرف ختم كتاب الله كاملاً في غضون 30 يوماً بكل سهولة ويسر ويملأ حياتك بالأنوار.",
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. RUQYAH SHARIA VIEW (الرقية الشرعية المكتوبة ورق المسموع)
// -------------------------------------------------------------
@Composable
fun RuqyahView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var playActive by remember { mutableStateOf(false) }
    var isPaused  by remember { mutableStateOf(false) }

    val ruqyahTexts = listOf(
        Pair("1. سورة الفاتحة الشافية المباركة:", "بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ (١) الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ (٢) الرَّحْمَنِ الرَّحِيمِ (٣) مَالِكِ يَوْمِ الدِّينِ (٤) إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ (٥) اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ (٦) صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ (٧) (تكرر ٧ مرات)"),
        Pair("2. آية الكرسي حامية الصدور والحافظة:", "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ (تكرر ٣ مرات)"),
        Pair("3. آيات الشفاء الست المذهلة للوجع والكرب:", "· ويشفِ صدوْر قَوْم مُّؤْمِنينَ (التوبة: ١٤)\n· وشفاء لِّما فى الصُّدُور (يونس: ٥٧)\n· يخرُج من بُطونها شَراب مُّخْتَلِف أَلْوَانه فيه شفاء للناس (النحل: ٦٩)\n· وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ (الإسراء: ٨٢)\n· وإذا مرضْتُ فهو يشفِينِ (الشعراء: ٨٠)\n· قلْ هو للذين آمنوا هُدًى وشفاء (فصلت: ٤٤)"),
        Pair("4. المعوذتان والإخلاص للحصن الأكمل والشفاء:", "· قُلْ هُوَ اللَّهُ أَحَدٌ... (تكرر ٣ مرات)\n· قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ... (تكرر ٣ مرات)\n· قُلْ أَعُوذُ بِرَبِّ النَّاسِ... (تكرر ٣ مرات)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("حصن الرقية الشرعية المكتوبة والمسموعة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.OfflineBolt, contentDescription = "Ruqyah", tint = Color(0xFFC9A84C))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Audio Ruqyah stream controller
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D4A30).copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, Color(0xFFC9A84C).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الرقية الشرعية الصوتية — تشغيل بدون انترنت ✅", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ملف الرقية محفوظ في التطبيق — يشتغل offline تماماً", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (playActive) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Playing",
                                    tint = Color(0xFFC9A84C),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // زر Pause/Resume
                                    Button(
                                        onClick = {
                                            if (isPaused) {
                                                context.startService(
                                                    Intent(context, RuqyahService::class.java).apply {
                                                        action = RuqyahService.ACTION_RESUME_RUQYAH
                                                    }
                                                )
                                                isPaused = false
                                            } else {
                                                context.startService(
                                                    Intent(context, RuqyahService::class.java).apply {
                                                        action = RuqyahService.ACTION_PAUSE_RUQYAH
                                                    }
                                                )
                                                isPaused = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A84C)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isPaused) "▶ استئناف الرقية" else "⏸ إيقاف مؤقت", color = Color(0xFF051D14), fontWeight = FontWeight.Bold)
                                    }
                                    // زر Stop كامل
                                    Button(
                                        onClick = {
                                            context.startService(
                                                Intent(context, RuqyahService::class.java).apply {
                                                    action = RuqyahService.ACTION_STOP_RUQYAH
                                                }
                                            )
                                            playActive = false
                                            isPaused = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("⏹ إيقاف الرقية كلياً", color = Color.White)
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Not Playing",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // زر تشغيل offline
                                    Button(
                                        onClick = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                context.startForegroundService(Intent(context, RuqyahService::class.java))
                                            } else {
                                                context.startService(Intent(context, RuqyahService::class.java))
                                            }
                                            playActive = true
                                            isPaused = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A84C)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("▶ تشغيل الرقية — بدون انترنت", color = Color(0xFF051D14), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Beautiful formatted written Ruqyah
            item {
                Text("نصوص الرقية الشرعية المكتوبة للتحصين", color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(ruqyahTexts) { (title, ayat) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(title, color = Color(0xFFC9A84C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ayat,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. ISLAMIC QUIZ VIEW (مسابقة 100 سؤال ديني تفاعلي)
// -------------------------------------------------------------
@Composable
fun IslamicQuizView(
    viewModel: IslamicViewModel,
    onBack: () -> Unit
) {
    val currentIndex by viewModel.currentQuizIndex
    val score by viewModel.quizScore
    val selectedAns by viewModel.selectedAnswerIndex
    val isCompleted by viewModel.quizCompleted
    val questions = viewModel.quizQuestionsState

    // Start with a randomized quiz if it is empty!
    LaunchedEffect(Unit) {
        if (questions.isEmpty()) {
            viewModel.startNewQuiz()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF051D14))
    ) {
        // Gold Header bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D4A30))
                .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("المسابقة والثقافة الدينية الشاملة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = Icons.Default.Quiz, contentDescription = "Quiz Logo", tint = Color(0xFFC9A84C))
            }
        }

        if (isCompleted) {
            // Summary celebration screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Success Star",
                    tint = Color(0xFFC9A84C),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "تبارك الرحمن! تم إكمال المسابقة بنجاح",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                val level = when {
                    score >= 12 -> "فقيه وعالم دين مخلص"
                    score >= 8 -> "قارئ ومثقف إسلامي رائع"
                    else -> "مبتدئ محب لدينه وسيرته"
                }
                
                Text(
                    text = "مجموع الإجابات الصحيحة: $score من ${questions.size}",
                    color = Color(0xFFC9A84C),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مستواك الإيماني الفقهي: $level",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.startNewQuiz() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A84C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ابدأ اختباراً جديداً بأسئلة عشوائية", color = Color(0xFF051D14), fontWeight = FontWeight.Bold)
                }
            }
        } else if (questions.isNotEmpty()) {
            val q = questions[currentIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                // Progress count indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("السؤال ${currentIndex + 1} من ${questions.size}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text("النقاط: $score", color = Color(0xFFC9A84C), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = (currentIndex.toFloat() / questions.size.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFC9A84C),
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFC9A84C).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(q.level, color = Color(0xFFC9A84C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = q.question,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Answer Options Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    q.choices.forEachIndexed { idx, option ->
                        val isSelected = selectedAns == idx
                        val isCorrect = q.correctAnswerIndex == idx
                        
                        val buttonColor = when {
                            selectedAns == null -> Color.White.copy(alpha = 0.05f) // Normal idle option state
                            isCorrect -> Color(0xFF10B981) // Green for correct answer!
                            isSelected -> Color(0xFFEF4444) // Red for selected wrong answer!
                            else -> Color.White.copy(alpha = 0.03f)
                        }

                        val borderOutlineColor = when {
                            selectedAns == null -> Color.White.copy(alpha = 0.10f)
                            isCorrect -> Color(0xFF10B981)
                            isSelected -> Color(0xFFEF4444)
                            else -> Color.White.copy(alpha = 0.03f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = selectedAns == null) {
                                    viewModel.answerQuizQuestion(idx)
                                },
                            colors = CardDefaults.cardColors(containerColor = buttonColor),
                            border = BorderStroke(1.dp, borderOutlineColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = option,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Next Action button
                if (selectedAns != null) {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9A84C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (currentIndex == questions.lastIndex) "عرض التقييم النهائي" else "السؤال التالي ──>",
                            color = Color(0xFF01140A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

