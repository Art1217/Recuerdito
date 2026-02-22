package com.reminderpay.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reminderpay.app.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that fires a system notification for a reminder.
 * Injected via Hilt's @HiltWorker.
 */
@HiltWorker
class ReminderNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id    = inputData.getInt(Constants.EXTRA_REMINDER_ID, 0)
        val title = inputData.getString(Constants.EXTRA_REMINDER_TITLE) ?: "ReminderPay"
        val body  = inputData.getString(Constants.EXTRA_REMINDER_BODY)  ?: "Tienes un recordatorio pendiente"

        NotificationHelper.showNotification(
            context        = applicationContext,
            notificationId = id,
            title          = title,
            body           = body
        )

        return Result.success()
    }
}
