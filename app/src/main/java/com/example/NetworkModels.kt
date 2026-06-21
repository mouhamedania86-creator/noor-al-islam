package com.example

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Url

data class IslamAiRequest(
    val model: String = "gemini3.1pro",
    val question: String
)

data class IslamAiResponse(
    val success: Boolean,
    val answer: String?,
    val model: String?,
    val method: String?,
    val credit: String?
)

// --- IslamAI Gemini 3.1 Pro API ---
interface IslamApiService {
    @POST("api/api.php")
    suspend fun askIslamAi(
        @Body request: IslamAiRequest
    ): IslamAiResponse
}

// --- AlAdhan Prayer Times API ---
data class PrayerResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: PrayerTimings,
    val date: DateInfo
)

data class PrayerTimings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Sunset: String,
    val Maghrib: String,
    val Isha: String,
    val Imsak: String,
    val Midnight: String
)

data class DateInfo(
    val readable: String,
    val timestamp: String,
    val hijri: HijriDate,
    val gregorian: GregorianDate
)

data class HijriDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: HijriWeekday,
    val month: HijriMonth,
    val year: String
)

data class HijriWeekday(
    val en: String,
    val ar: String
)

data class HijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class GregorianDate(
    val date: String,
    val format: String,
    val day: String,
    val weekday: GregorianWeekday,
    val month: GregorianMonth,
    val year: String
)

data class GregorianWeekday(
    val en: String
)

data class GregorianMonth(
    val number: Int,
    val en: String
)

interface AlAdhanApiService {
    @GET("v1/timingsByCity")
    suspend fun getTimings(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int
    ): PrayerResponse
}

// --- Open-Meteo Weather API ---
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: CurrentWeather?
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val winddirection: Double,
    val weathercode: Int,
    val is_day: Int,
    val time: String
)

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse
}

// --- Quran.com API Models ---
data class SurahListResponse(
    val chapters: List<Chapter>
)

data class Chapter(
    val id: Int,
    val revelation_place: String,
    val revelation_order: Int,
    val bismillah_pre: Boolean,
    val name_simple: String,
    val name_complex: String,
    val name_arabic: String,
    val verses_count: Int,
    val pages: List<Int>,
    val translated_name: TranslatedName
)

data class TranslatedName(
    val language_name: String,
    val name: String
)

data class VerseResponse(
    val verses: List<Verse>
)

data class Verse(
    val id: Int,
    val verse_number: Int,
    val verse_key: String,
    val text_uthmani: String?,
    val text_indopak: String?,
    val translations: List<VerseTranslation>?
)

data class VerseTranslation(
    val id: Int,
    val resource_id: Int,
    val text: String
)

data class ChapterRecitationResponse(
    val audio_files: List<AudioFile>
)

data class AudioFile(
    val id: Int,
    val chapter_id: Int,
    val file_size: Double,
    val format: String,
    val audio_url: String
)

interface QuranApiService {
    // We can fetch from API or use local database. Having both is super safe!
    @GET("api/v4/chapters")
    suspend fun getChapters(): SurahListResponse

    @GET("api/v4/quran/verses/uthmani")
    suspend fun getUthmaniVerses(
        @Query("chapter_number") chapterNumber: Int
    ): UthmaniVersesResponse

    @GET("api/v4/verses/by_chapter/{chapter_number}")
    suspend fun getVersesWithTranslation(
        @Query("chapter_number") chapterNumber: Int,
        @Query("language") language: String = "ar",
        @Query("translations") translationId: String = "131" // 131 is usually clean English translation (e.g., Dr. Mustafa Khattab)
    ): VerseResponse
}

data class UthmaniVersesResponse(
    val verses: List<UthmaniVerse>
)

data class UthmaniVerse(
    val id: Int,
    val verse_key: String,
    val text_uthmani: String
)

// --- Azkar Model ---
data class AzkarCategoryResponse(
    val title: String,
    val content: List<AzkarItem>
)

data class AzkarItem(
    val category: String,
    val count: String,
    val description: String,
    val reference: String,
    val zikr: String
)

interface AzkarApiService {
    @GET
    suspend fun getAzkar(@Url url: String): Map<String, List<AzkarItem>>
}
