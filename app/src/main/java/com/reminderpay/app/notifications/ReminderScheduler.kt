package com.reminderpay.app.notifications

import android.content.Context
import androidx.work.*
import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.utils.Constants
import com.reminderpay.app.utils.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels WorkManager jobs for reminders.
 *
 * Two notifications per reminder:
 *   1. Early alert: [notifyDaysBefore] days before due date
 *   2. Same-day alert: at the exact due time
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule notifications for a given reminder.
     * Uses unique work names keyed by reminder ID to avoid duplicates.
     */
    fun scheduleReminder(reminder: Reminder) {
        val dueMillis = DateUtils.combineDateAndTime(reminder.date, reminder.time)
        val now       = System.currentTimeMillis()

        // ── Same-day / on-time notification ───────────────────────────────────
        val delayMs = dueMillis - now
        if (delayMs > 0) {
            enqueueReminderWork(
                uniqueTag   = "reminder_${reminder.id}_ontime",
                reminderId  = reminder.id,
                title       = reminder.title,
                body        = "¡Es hoy! ${reminder.title}",
                delayMs     = delayMs
            )
        }

        // ── Early notification (days before) ──────────────────────────────────
        if (reminder.notifyDaysBefore > 0) {
            val earlyDelayMs = delayMs - (reminder.notifyDaysBefore * DateUtils.HOURS_24_MS)
            if (earlyDelayMs > 0) {
                enqueueReminderWork(
                    uniqueTag  = "reminder_${reminder.id}_early",
                    reminderId = reminder.id,
                    title      = "Recordatorio próximo",
                    body       = "Faltan ${reminder.notifyDaysBefore} día(s) para: ${reminder.title}",
                    delayMs    = earlyDelayMs
                )
            }
        }
    }

    /** Cancel all pending notifications for a reminder */
    fun cancelReminder(reminderId: Int) {
        workManager.cancelUniqueWork("reminder_${reminderId}_ontime")
        workManager.cancelUniqueWork("reminder_${reminderId}_early")
    }

    private fun enqueueReminderWork(
        uniqueTag: String,
        reminderId: Int,
        title: String,
        body: String,
        delayMs: Long
    ) {
        val data = workDataOf(
            Constants.EXTRA_REMINDER_ID    to reminderId,
            Constants.EXTRA_REMINDER_TITLE to title,
            Constants.EXTRA_REMINDER_BODY  to body
        )

        val request = OneTimeWorkRequestBuilder<ReminderNotificationWorker>()
            .setInputData(data)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(Constants.REMINDER_WORKER_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false) // Always fire even on low battery
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            uniqueTag,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
