package com.example

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// --- Chat Message Definition ---
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// --- ViewModel implementation ---
class IslamicViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    private val repository = IslamicRepository(
        settingsDao = database.settingsDao(),
        prayerCacheDao = database.prayerCacheDao(),
        tasbihDao = database.tasbihDao()
    )

    // --- Core Flows ---
    val settingsState = repository.settingsFlow
        .map { it ?: SettingsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsEntity()
        )

    // --- Jetpack Compose Observable States ---
    // General screens
    val currentHeaderDua = mutableStateOf("")
    val currentHadith = mutableStateOf("")

    // Home Clock & Dates
    val timeString = mutableStateOf("")
    val dateStringAr = mutableStateOf("")
    
    // Prayer timings state
    val prayerTimes = mutableStateOf<PrayerCacheEntity?>(null)
    val nextPrayerName = mutableStateOf("جاري الحساب...")
    val nextPrayerTimeLeft = mutableStateOf("00:00:00")
    
    // Weather
    val currentTemp = mutableStateOf<Double?>(null)
    val weatherConditionAr = mutableStateOf("صافي")
    val isWeatherLoading = mutableStateOf(false)

    // Quran State
    val selectedSurahId = mutableStateOf(1)
    val surahVerses = mutableStateOf<List<Verse>>(emptyList())
    val isQuranLoading = mutableStateOf(false)
    val preferredLanguage = mutableStateOf("ar") // "ar", "en", "fr"
    val showTafsir = mutableStateOf(false)
    val selectedVerseTafsir = mutableStateOf<String?>(null)

    // Audio & Radio Player State
    private var mediaPlayer: MediaPlayer? = null
    val audioPlayingUrl = mutableStateOf<String?>(null)
    val isAudioPlaying = mutableStateOf(false)
    val isAudioLoading = mutableStateOf(false)
    val currentAudioTitle = mutableStateOf<String?>(null)

    // Tasbih / Sebha
    val activeTasbihIndex = mutableStateOf(0)
    val tasbihPhrases = listOf("سبحان الله", "الحمد لله", "الله أكبر", "أستغفر الله", "لا إله إلا الله")
    val tasbihCounters = mutableStateListOf<TasbihCounterEntity>()
    
    // Qibla
    val userHeadingAngle = mutableStateOf(0f) // from sensor
    val qiblaDirectionAngle = mutableStateOf(0f) // calculated angle relative to north

    // IslamAI Chat List
    val chatMessages = mutableStateListOf<ChatMessage>()
    val isChatLoading = mutableStateOf(false)

    // Initializer
    init {
        // Setup initial messages
        chatMessages.add(
            ChatMessage(
                text = "السلام عليكم ورحمة الله وبركاته. أهلاً بك يا أخي العزيز في مساعدك الإسلامي الذكي (IslamAI). أستطيع إرشادك في فقه العبادات، تفسير الآيات، أحكام الشريعة والسيرة النبوية العطرة. بمَ يمكنني مساعدتك اليوم؟",
                isUser = false
            )
        )

        // Fetch user preferences and settings
        viewModelScope.launch {
            // Seeding default settings directly on startup to guarantee database row creation!
            val seedSettings = repository.getSettings()
            
            // Seed tasbih counters
            tasbihPhrases.forEach { phrase ->
                val existing = database.tasbihDao().getCounterFor(phrase)
                if (existing == null) {
                    database.tasbihDao().insertOrUpdate(TasbihCounterEntity(phrase = phrase))
                }
            }
            
            // Watch settings to load content
            settingsState.collect { settings ->
                loadDayData(settings)
            }
        }

        // Start Clock & Timer Updates
        startClockAndTimers()
        
        // Random Dua rotation logic (hourly)
        rotateDuaAndHadith()
    }

    // Load main screen info based on selected city in settings
    private fun loadDayData(settings: SettingsEntity) {
        viewModelScope.launch {
            try {
                // Determine coordinates based on manual settings city
                val lat = settings.latitude
                val lng = settings.longitude
                
                // Fetch prayer times
                val timings = repository.getPrayerTimes(
                    city = settings.city,
                    country = settings.country,
                    method = settings.calculationMethod
                )
                prayerTimes.value = timings
                if (timings != null) {
                    calculateNextPrayer(timings)
                    IslamicAlarmReceiver.scheduleAllDailyAlarms(context, timings)
                }

                // Fetch weather
                isWeatherLoading.value = true
                val weather = repository.getLiveWeather(lat, lng)
                if (weather != null) {
                    currentTemp.value = weather.temperature
                    weatherConditionAr.value = mapWeatherCodeToArabic(weather.weathercode)
                }
                isWeatherLoading.value = false

            } catch (e: Exception) {
                Log.e("IslamicVM", "Failed loading day data", e)
                isWeatherLoading.value = false
            }
        }
    }

    // Map weather codes to friendly Arabic descriptions
    private fun mapWeatherCodeToArabic(code: Int): String {
        return when (code) {
            0 -> "سماء صافية"
            1, 2, 3 -> "غائم جزئياً"
            45, 48 -> "ضباب كثيف"
            51, 53, 55 -> "رذاذ خفيف"
            61, 63, 65 -> "أمطار الخير"
            71, 73, 75 -> "ثلوج متساقطة"
            80, 81, 82 -> "زخات مطر"
            95, 96, 99 -> "عاصفة رعدية مباركة"
            else -> "طقس معتدل"
        }
    }

    // Clock updater
    private fun startClockAndTimers() {
        viewModelScope.launch {
            while (true) {
                // Local Time
                val calendar = Calendar.getInstance()
                val sdfTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                timeString.value = sdfTime.format(calendar.time)

                // Arabic Gregorian formatting
                val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("ar"))
                dateStringAr.value = sdfDate.format(calendar.time)

                // Recalculate remaining countdown to prayer
                prayerTimes.value?.let { calculateNextPrayer(it) }

                delay(1000)
            }
        }
    }

    // Rotates the Quranic/prophetic Duas and Hadiths
    fun rotateDuaAndHadith() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Dua rotates hourly
        val duaIndex = hour % IslamicStaticData.hourlyDuas.size
        currentHeaderDua.value = IslamicStaticData.hourlyDuas[duaIndex]

        // Hadith rotates
        val hadithIndex = hour % IslamicStaticData.randomHadiths.size
        currentHadith.value = IslamicStaticData.randomHadiths[hadithIndex]
    }

    private fun cleanTime(raw: String): String {
        return raw.split(" ")[0].trim()
    }

    // Next Prayer calculation engine
    private fun calculateNextPrayer(timings: PrayerCacheEntity) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val nowTimeStr = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
            val now = sdf.parse(nowTimeStr) ?: return

            val fajr = sdf.parse(cleanTime(timings.Fajr))!!
            val dhuhr = sdf.parse(cleanTime(timings.Dhuhr))!!
            val asr = sdf.parse(cleanTime(timings.Asr))!!
            val maghrib = sdf.parse(cleanTime(timings.Maghrib))!!
            val isha = sdf.parse(cleanTime(timings.Isha))!!

            val prayers = listOf(
                Pair("الفجر", fajr),
                Pair("الظهر", dhuhr),
                Pair("العصر", asr),
                Pair("المغرب", maghrib),
                Pair("العشاء", isha)
            )

            var nextName = "الفجر"
            var nextTime = fajr
            var isNextDay = false

            for (i in prayers.indices) {
                if (now.before(prayers[i].second)) {
                    nextName = prayers[i].first
                    nextTime = prayers[i].second
                    break
                }
                if (i == prayers.lastIndex) {
                    // Next is tomorrow's Fajr
                    nextName = "الفجر (غداً)"
                    nextTime = fajr
                    isNextDay = true
                }
            }

            nextPrayerName.value = nextName

            // Calculate duration
            val nowCal = Calendar.getInstance()
            val targetCal = Calendar.getInstance()
            
            val hourMinute = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedNextString = hourMinute.format(nextTime)
            val parts = formattedNextString.split(":")
            
            targetCal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            targetCal.set(Calendar.MINUTE, parts[1].toInt())
            targetCal.set(Calendar.SECOND, 0)

            if (isNextDay) {
                targetCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            var diff = targetCal.timeInMillis - nowCal.timeInMillis
            if (diff < 0) {
                diff = 0
            }

            val hours = diff / (1000 * 60 * 60)
            val mins = (diff / (1000 * 60)) % 60
            val secs = (diff / 1000) % 60

            nextPrayerTimeLeft.value = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)

        } catch (e: Exception) {
            Log.e("IslamicVM", "Calculation error for next prayer timings", e)
            nextPrayerName.value = "الخطأ في الحساب"
        }
    }

    // --- Quran Flow ---
    fun selectSurah(surahNumber: Int) {
        selectedSurahId.value = surahNumber
        isQuranLoading.value = true
        viewModelScope.launch {
            try {
                val verses = repository.getSurahVerses(surahNumber)
                surahVerses.value = verses
            } catch (e: Exception) {
                Log.e("IslamicVM", "Surah fetch error", e)
            } finally {
                isQuranLoading.value = false
            }
        }
    }

    // Returns a Tafsir description for a selected verse index
    fun loadTafsirFor(verseKey: String) {
        // Simple elegant Tafsir database mapper
        selectedVerseTafsir.value = when {
            verseKey == "1:1" -> "البسملة أية من كتاب الله لبدء الأمور تبركاً وتحميداً."
            verseKey == "1:2" -> "الحمد والشكر خالصاً لله وحده لكونه خالق ومربي سائر العوالم والخلائق بفضله الكثيف."
            verseKey == "1:5" -> "نخصك بنوع العبادة ونستعين بك في جميع مهمات ديننا ودنيانا."
            verseKey.startsWith("112:") -> "تأكيد تام لوحدانية المولى تبارك وتعالى وتنزيهه الكلي عن الولد والشريك والند."
            verseKey.startsWith("113:") -> "طلب الاستجارة وكمال التعوذ برب الصبح الفالق من الشرور الساهرة والأخطار المحتدمة."
            verseKey.startsWith("114:") -> "كمال الالتجاء بملك وبإله البشر من وساوس إبليس اللعين المتربص بقلوب المؤمنين."
            else -> "تفسير مختصر: يسر الله تبارك وتعالى فهم كتابه وتدبر آياته ليقيم بها العبد أركان دينه ويرتفع درجات في الجنان العلية."
        }
        showTafsir.value = true
    }

    // --- General Media Player for Recitations AND Radio ---
    fun playAudio(url: String, title: String) {
        viewModelScope.launch {
            try {
                isAudioLoading.value = true
                stopAudio()

                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    
                    // Add standard browser user-agent headers to guarantee playback from secure servers (like mp3quran/qurango)
                    val headers = mapOf(
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                        "Referer" to "https://www.mp3quran.net/"
                    )
                    val uri = android.net.Uri.parse(url)
                    setDataSource(context, uri, headers)
                    
                    setOnPreparedListener {
                        isAudioLoading.value = false
                        start()
                        audioPlayingUrl.value = url
                        isAudioPlaying.value = true
                        currentAudioTitle.value = title
                        Log.d("IslamicVM", "MediaPlayer successfully prepared and started playing: $url")
                    }
                    setOnCompletionListener {
                        isAudioPlaying.value = false
                        audioPlayingUrl.value = null
                        currentAudioTitle.value = null
                        Log.d("IslamicVM", "MediaPlayer playback completed for: $url")
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("IslamicVM", "Media player error: $what, $extra")
                        isAudioLoading.value = false
                        isAudioPlaying.value = false
                        audioPlayingUrl.value = null
                        currentAudioTitle.value = null
                        false
                    }
                    
                    // Call prepareAsync AFTER setting all listeners to avoid race conditions!
                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e("IslamicVM", "Play audio error for URL: $url", e)
                isAudioLoading.value = false
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            audioPlayingUrl.value = null
            isAudioPlaying.value = false
            currentAudioTitle.value = null
        } catch (e: Exception) {
            Log.e("IslamicVM", "Stop audio error", e)
        }
    }

    // --- Tasbih / Sebha counts ---
    fun incrementTasbihCount(phrase: String, callbackOnVibrate: () -> Unit) {
        viewModelScope.launch {
            val isReset = repository.incrementTasbih(phrase)
            if (isReset) {
                // Vibration or full circle trigger
                callbackOnVibrate()
            }
        }
    }

    fun resetTasbihCount(phrase: String) {
        viewModelScope.launch {
            repository.resetTasbih(phrase)
        }
    }

    fun getPhraseCount(phrase: String): Flow<Int> {
        return repository.allTasbihCounters.map { list ->
            list.firstOrNull { it.phrase == phrase }?.count ?: 0
        }
    }

    // --- Qibla calculations (Mecca coordinates: lat 21.4225, lon 39.8262) ---
    fun calculateQiblaAngle(userLat: Double, userLng: Double) {
        val meccaLat = Math.toRadians(21.4225)
        val meccaLng = Math.toRadians(39.8262)
        val uLat = Math.toRadians(userLat)
        val uLng = Math.toRadians(userLng)

        val dLng = meccaLng - uLng

        val y = sin(dLng) * cos(meccaLat)
        val x = cos(uLat) * sin(meccaLat) - sin(uLat) * cos(meccaLat) * cos(dLng)

        var qiblaDeg = Math.toDegrees(atan2(y, x))
        qiblaDeg = (qiblaDeg + 360) % 360

        qiblaDirectionAngle.value = qiblaDeg.toFloat()
    }

    // --- IslamAI Smart Chat implementation ---
    fun submitAiMessage(question: String) {
        if (question.isBlank()) return

        // Save user message to list
        chatMessages.add(ChatMessage(text = question, isUser = true))
        
        isChatLoading.value = true
        viewModelScope.launch {
            try {
                val settings = repository.getSettings()
                val times = prayerTimes.value
                val contextHeader = if (times != null) {
                    "[معلومات هامة جداً متوفرة لديك حالياً:\n" +
                    "المدينة الجزائرية الحالية للمستعلم: ${settings.city}\n" +
                    "مواقيت الصلاة الشرعية المؤكدة والصحيحة اليوم في بلدته هي:\n" +
                    "- الفجر: ${times.Fajr}\n" +
                    "- الشروق: ${times.Sunrise}\n" +
                    "- الظهر: ${times.Dhuhr}\n" +
                    "- العصر: ${times.Asr}\n" +
                    "- المغرب: ${times.Maghrib}\n" +
                    "- العشاء: ${times.Isha}\n" +
                    "التاريخ الهجري الحالي: ${times.hijriDate}\n" +
                    "التاريخ الميلادي الحالي: ${times.gregorianDate}\n" +
                    "إذا سألك المستخدم لطلب مواقيت الصلاة أو أوقات الأذان، أجب بدقة كاملة وعرّفها له بناءً على هذه الأرقام حصراً، ولا تقل له أنها غير متوفرة أو أنك لا تملك وصولاً لها!]\n\n"
                } else {
                    ""
                }
                
                val answer = repository.askIslamAi(contextHeader + question)
                // Add AI response to list
                chatMessages.add(ChatMessage(text = answer, isUser = false))
            } catch (e: Exception) {
                Log.e("IslamicVM", "IslamAI prompt submit failure", e)
                chatMessages.add(
                    ChatMessage(
                        text = "عذراً، واجهت خطأ مجهولاً أثناء معالجة السؤال. يرجى إعادة المحاولة.",
                        isUser = false
                    )
                )
            } finally {
                isChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        chatMessages.clear()
        chatMessages.add(
            ChatMessage(
                text = "السلام عليكم ورحمة الله وبركاته، تم مسح التاريخ والبدء مجدداً. بمَ يمكنني إفادتك من فضل الله وعلمه الواسع؟",
                isUser = false
            )
        )
    }

    // --- Settings Savers ---
    fun saveUserProfile(name: String, city: String, country: String, lat: Double, lng: Double, method: Int) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(
                    username = name,
                    city = city,
                    country = country,
                    latitude = lat,
                    longitude = lng,
                    calculationMethod = method
                )
            }
        }
    }

    fun toggleDarkMode(enable: Boolean) {
        viewModelScope.launch {
            repository.updateSettings {
                it.copy(useDarkTheme = enable)
            }
        }
    }

    // --- GPS Location Sync ---
    fun syncLocationWithGPS() {
        viewModelScope.launch {
            try {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->
                        if (location != null) {
                            val lat = location.latitude
                            val lng = location.longitude
                            Log.d("IslamicVM", "GPS position acquired: $lat, $lng")
                            viewModelScope.launch {
                                repository.updateSettings { current ->
                                    current.copy(
                                        city = "موقعي الحالي (GPS)",
                                        latitude = lat,
                                        longitude = lng
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("IslamicVM", "GPS location fetch failed", e)
            }
        }
    }

    // --- Islamic Quiz System (100 Questions Game) ---
    val currentQuizIndex = mutableStateOf(0)
    val quizScore = mutableStateOf(0)
    val selectedAnswerIndex = mutableStateOf<Int?>(null)
    val quizCompleted = mutableStateOf(false)
    val quizQuestionsState = mutableStateListOf<IslamicStaticData.QuizQuestion>()

    fun startNewQuiz() {
        currentQuizIndex.value = 0
        quizScore.value = 0
        selectedAnswerIndex.value = null
        quizCompleted.value = false
        quizQuestionsState.clear()
        
        // Take random questions from the pool
        val shuffled = IslamicStaticData.quizQuestions.shuffled()
        val selection = shuffled.take(15) // Generate 15 beautiful questions per quiz run
        quizQuestionsState.addAll(selection)
    }

    fun answerQuizQuestion(answerIndex: Int) {
        if (selectedAnswerIndex.value != null) return // already answered
        selectedAnswerIndex.value = answerIndex
        if (answerIndex == quizQuestionsState[currentQuizIndex.value].correctAnswerIndex) {
            quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        selectedAnswerIndex.value = null
        if (currentQuizIndex.value < quizQuestionsState.lastIndex) {
            currentQuizIndex.value += 1
        } else {
            quizCompleted.value = true
        }
    }

    // --- Khatma Tracker ---
    val khatmaJuz = mutableStateOf(1)
    val khatmaPage = mutableStateOf(1)
    val khatmaPagesReadToday = mutableStateOf(0)
    val khatmaTargetPages = mutableStateOf(20)

    fun readKhatmaPages(pages: Int) {
        khatmaPagesReadToday.value = (khatmaPagesReadToday.value + pages).coerceAtLeast(0)
        khatmaPage.value = (khatmaPage.value + pages).coerceIn(1, 604)
        khatmaJuz.value = ((khatmaPage.value - 1) / 20 + 1).coerceIn(1, 30)
    }

    fun resetKhatmaDailyProgress() {
        khatmaPagesReadToday.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
    }
}
