package com.example

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

// ============================================================
// IslamicAlarmReceiver — يستقبل الأذان + أدعية كل 20 دقيقة
// ============================================================
class IslamicAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PERIODIC_DUA -> {
                Log.d(TAG, "Periodic Dua triggered")
                IslamicNotificationManager.showPeriodicDuaNotification(context)
                // إعادة جدولة الـ 20 دقيقة التالية (chain)
                schedulePeriodicDua(context)
            }
            else -> {
                // أذان صلاة
                val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "الصلاة"
                Log.d(TAG, "Adhan alarm for: $prayerName")
                IslamicNotificationManager.showAdhanNotification(context, prayerName)
                // شغل AdhanService
                try {
                    val serviceIntent = Intent(context, AdhanService::class.java).apply {
                        putExtra("PRAYER_NAME", prayerName)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start AdhanService", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "IslamicAlarmReceiver"
        const val ACTION_PERIODIC_DUA = "COM_EXAMPLE_PERIODIC_DUA"
        private const val DUA_REQUEST_CODE = 999
        private const val DUA_INTERVAL_MS = 20 * 60 * 1000L // 20 دقيقة

        // ---- جدولة أدعية كل 20 دقيقة ----
        fun schedulePeriodicDua(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, IslamicAlarmReceiver::class.java).apply {
                    action = ACTION_PERIODIC_DUA
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    DUA_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val triggerAt = System.currentTimeMillis() + DUA_INTERVAL_MS
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
                Log.d(TAG, "Dua scheduled in 20 min")
            } catch (e: Exception) {
                Log.e(TAG, "schedulePeriodicDua error", e)
            }
        }

        // ---- جدولة أذان صلاة واحدة ----
        fun schedulePrayerAlarm(context: Context, prayerName: String, timeStr: String) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val parts = timeStr.trim().split(":")
                if (parts.size != 2) return
                val hour = parts[0].toIntOrNull() ?: return
                val minute = parts[1].toIntOrNull() ?: return

                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val intent = Intent(context, IslamicAlarmReceiver::class.java).apply {
                    putExtra("PRAYER_NAME", prayerName)
                    action = "COM_EXAMPLE_ALARM_${prayerName.hashCode()}"
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    prayerName.hashCode(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
                Log.d(TAG, "Scheduled adhan: $prayerName at $timeStr")
            } catch (e: Exception) {
                Log.e(TAG, "schedulePrayerAlarm error", e)
            }
        }

        // ---- جدولة الأذان للصلوات الخمس دفعة واحدة ----
        fun scheduleAllDailyAlarms(context: Context, timings: PrayerCacheEntity) {
            schedulePrayerAlarm(context, "الفجر",   timings.Fajr)
            schedulePrayerAlarm(context, "الظهر",   timings.Dhuhr)
            schedulePrayerAlarm(context, "العصر",   timings.Asr)
            schedulePrayerAlarm(context, "المغرب",  timings.Maghrib)
            schedulePrayerAlarm(context, "العشاء",  timings.Isha)
        }
    }
}

// ============================================================
// BootReceiver — يشتغل بعد restart الهاتف
// يعيد جدولة كل الأذان + الأدعية تلقائياً
// ============================================================
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        Log.d("BootReceiver", "Device booted — rescheduling all alarms")

        // 1. إعادة جدولة أدعية كل 20 دقيقة
        IslamicAlarmReceiver.schedulePeriodicDua(context)

        // 2. إعادة جدولة أذان الصلوات من Room (آخر بيانات محفوظة)
        try {
            val db = AppDatabase.getDatabase(context)
            // نشغل في thread منفصل لأن BroadcastReceiver مش coroutine
            Thread {
                try {
                    val cached = db.prayerCacheDao().getLatestSync()
                    if (cached != null) {
                        IslamicAlarmReceiver.scheduleAllDailyAlarms(context, cached)
                        Log.d("BootReceiver", "Prayer alarms rescheduled from cached data")
                    } else {
                        Log.w("BootReceiver", "No cached prayer times — alarms will be set when app opens")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error reading cached prayers", e)
                }
            }.start()
        } catch (e: Exception) {
            Log.e("BootReceiver", "DB error on boot", e)
        }

        // 3. أظهر إشعار دعاء فوراً كترحيب بعد الريستارت
        IslamicNotificationManager.showPeriodicDuaNotification(context)
    }
}

// ============================================================
// AdhanService — يشغل ملف أذانك الخاص من assets/adhan.mp3
// ============================================================
class AdhanService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "الصلاة"

        if (intent?.action == ACTION_STOP_ADHAN) {
            Log.d(TAG, "Stop adhan received")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "Starting adhan for: $prayerName")

        // زر إيقاف الأذان
        val stopIntent = Intent(this, AdhanService::class.java).apply { action = ACTION_STOP_ADHAN }
        val stopPending = PendingIntent.getService(
            this, 99, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("أذان صلاة $prayerName")
            .setContentText("حي على الصلاة — حي على الفلاح")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف الأذان", stopPending)
            .setOngoing(true)
            .build()

        startForeground(2027, notification)

        // ---- تشغيل adhan.mp3 من assets ----
        try {
            val afd = assets.openFd("adhan.mp3")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = false // الأذان يُشغَّل مرة واحدة كاملة
                setOnCompletionListener { stopSelf() }
                prepare()
                start()
            }
            Log.d(TAG, "adhan.mp3 playing from assets ✅")
        } catch (e: Exception) {
            Log.e(TAG, "adhan.mp3 not found in assets — falling back to system alarm", e)
            // Fallback: صوت النظام إذا الملف ما كانش
            try {
                val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@AdhanService, uri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    isLooping = false
                    setOnCompletionListener { stopSelf() }
                    prepare()
                    start()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Fallback also failed", ex)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "خدمة الأذان", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "AdhanService"
        private const val CHANNEL_ID = "noor_al_islam_service_adhan"
        const val ACTION_STOP_ADHAN = "STOP_ADHAN"
    }
}

// ============================================================
// RuqyahService — يشغل الرقية offline من assets/ruqyah.mp3
// مع زر Play/Pause وزر Stop في الإشعار
// ============================================================
class RuqyahService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_RUQYAH -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_PAUSE_RUQYAH -> {
                mediaPlayer?.pause()
                updateNotification(isPlaying = false)
                return START_NOT_STICKY
            }
            ACTION_RESUME_RUQYAH -> {
                mediaPlayer?.start()
                updateNotification(isPlaying = true)
                return START_NOT_STICKY
            }
        }

        // بدء التشغيل
        startForeground(3001, buildNotification(isPlaying = true))

        try {
            val afd = assets.openFd("ruqyah.mp3")
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true // الرقية تتكرر تلقائياً
                prepare()
                start()
            }
            Log.d(TAG, "ruqyah.mp3 playing from assets ✅")
        } catch (e: Exception) {
            Log.e(TAG, "ruqyah.mp3 not found in assets", e)
            stopSelf()
        }

        return START_STICKY // يعيد التشغيل إذا Android قتله
    }

    private fun buildNotification(isPlaying: Boolean): android.app.Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, RuqyahService::class.java).apply { action = ACTION_STOP_RUQYAH },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val toggleIntent = PendingIntent.getService(
            this, 1,
            Intent(this, RuqyahService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE_RUQYAH else ACTION_RESUME_RUQYAH
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        val toggleLabel = if (isPlaying) "إيقاف مؤقت" else "استئناف"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("الرقية الشرعية")
            .setContentText(if (isPlaying) "جارٍ التشغيل — offline ✅" else "متوقف مؤقتاً")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(toggleIcon, toggleLabel, toggleIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "إيقاف", stopIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(isPlaying: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(3001, buildNotification(isPlaying))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "الرقية الشرعية", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "RuqyahService"
        private const val CHANNEL_ID = "noor_al_islam_ruqyah"
        const val ACTION_STOP_RUQYAH = "STOP_RUQYAH"
        const val ACTION_PAUSE_RUQYAH = "PAUSE_RUQYAH"
        const val ACTION_RESUME_RUQYAH = "RESUME_RUQYAH"
    }
}
