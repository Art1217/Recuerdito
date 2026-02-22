package com.reminderpay.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.reminderpay.app.MainActivity
import com.reminderpay.app.utils.Constants

/**
 * Helper that creates the notification channel and posts system notifications.
 * Configured with sound, vibration, and heads-up (peek) display.
 */
object NotificationHelper {

    /**
     * Create the notification channel with HIGH importance, default ringtone and vibration.
     * Must be called before posting on API 26+.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // IMPORTANCE_HIGH = heads-up display
            ).apply {
                description       = "Alertas de recordatorios de ReminderPay"
                enableVibration(true)
                vibrationPattern  = longArrayOf(0L, 300L, 200L, 300L)
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor        = android.graphics.Color.parseColor("#6750A4")
            }

            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Post a high-priority heads-up notification.
     * Tapping it opens [MainActivity].
     *
     * On Android 13+ (API 33) the POST_NOTIFICATIONS permission must be granted;
     * if it isn't, the notification is silently skipped.
     */
    fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String
    ) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Intent that opens the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Full-screen intent so it pops up even when the screen is off
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 10000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // alarm clock icon
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            // HIGH priority triggers heads-up / peek on all API levels
            .setPriority(NotificationCompat.PRIORITY_MAX)
            // Sound + vibration for pre-Oreo or channels that aren't properly set
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0L, 300L, 200L, 300L))
            // Heads-up / lock-screen pop-up
            .setFullScreenIntent(fullScreenPendingIntent, /* highPriority = */ true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted on Android 13+ – silently skip
        }
    }
}
