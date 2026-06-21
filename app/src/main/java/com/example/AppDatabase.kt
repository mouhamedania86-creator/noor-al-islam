package com.example

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Settings Entity ---
@Entity(tableName = "user_settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "عاشق المصطفى",
    val city: String = "الجزائر",
    val country: String = "DZ",
    val latitude: Double = 36.75,
    val longitude: Double = 3.04,
    val calculationMethod: Int = 3, // 3 - Algerian local / standard
    val preferredReciterId: Int = 1, // 1 - Abdul Basit, 2 - Al-Sudais, 3 - El-Minshawi
    val notificationsEnabled: Boolean = true,
    val useDarkTheme: Boolean = false
)

// --- Prayer Times Cache Entity ---
@Entity(tableName = "prayer_cache")
data class PrayerCacheEntity(
    @PrimaryKey val dateKey: String, // format "YYYY-MM-DD" or equivalent
    val city: String,
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String,
    val hijriDate: String,
    val gregorianDate: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// --- Custom Tasbih Counters ---
@Entity(tableName = "tasbih_counters")
data class TasbihCounterEntity(
    @PrimaryKey val phrase: String,
    val count: Int = 0,
    val maxLimit: Int = 33
)

// --- DAOs ---
@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<SettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SettingsEntity)
}

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM prayer_cache WHERE city = :city LIMIT 1")
    suspend fun getCachedTimings(city: String): PrayerCacheEntity?

    // نسخة synchronous تُستخدم في BootReceiver (خارج coroutine)
    @Query("SELECT * FROM prayer_cache LIMIT 1")
    fun getLatestSync(): PrayerCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheTimings(cacheEntry: PrayerCacheEntity)

    @Query("DELETE FROM prayer_cache")
    suspend fun clearCache()
}

@Dao
interface TasbihDao {
    @Query("SELECT * FROM tasbih_counters")
    fun getAllCountersFlow(): Flow<List<TasbihCounterEntity>>

    @Query("SELECT * FROM tasbih_counters WHERE phrase = :phrase LIMIT 1")
    suspend fun getCounterFor(phrase: String): TasbihCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(counter: TasbihCounterEntity)
}

// --- AppDatabase class ---
@Database(
    entities = [SettingsEntity::class, PrayerCacheEntity::class, TasbihCounterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao
    abstract fun prayerCacheDao(): PrayerCacheDao
    abstract fun tasbihDao(): TasbihDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noor_al_islam_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
