package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object IslamicNotificationManager {

    private const val GENERAL_CHANNEL_ID = "noor_al_islam_reminders"
    private const val ADHAN_CHANNEL_ID = "noor_al_islam_adhan"
    private const val GENERAL_NOTIFICATION_ID = 2004
    private const val ADHAN_NOTIFICATION_ID = 2026

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val generalChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                "نور الإسلام - أذكار وتذكيرات غيابية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "قناة إرسال الأذكار والأدعية العشوائية كل 20 دقيقة"
            }

            val adhanChannel = NotificationChannel(
                ADHAN_CHANNEL_ID,
                "نور الإسلام - إشعارات الأذان ودخول الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيه غيابي رائع عند دخول وقت الصلاة الشرعي"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(generalChannel)
            manager.createNotificationChannel(adhanChannel)
        }
    }

    fun showPeriodicDuaNotification(context: Context) {
        try {
            // Pick random Dua and Hadith from Static Data
            val randomDua = IslamicStaticData.hourlyDuas.random()
            val randomHadith = IslamicStaticData.randomHadiths.random()

            val title = "ذكر الله ساعة: تذكير بالصلاة والسلام"
            val body = "$randomDua\n\nحديث شريف: $randomHadith"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(randomDua)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(GENERAL_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAdhanNotification(context: Context, prayerName: String) {
        try {
            val title = "حان الآن موعد أذان صلاة $prayerName"
            val body = "أقم صلاتك قبل مماتك. حي على الصلاة، حي على الفلاح."

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, ADHAN_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(ADHAN_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
