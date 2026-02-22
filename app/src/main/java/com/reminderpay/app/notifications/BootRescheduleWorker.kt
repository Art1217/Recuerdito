package com.reminderpay.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reminderpay.app.data.repository.ReminderRepository
import com.reminderpay.app.data.model.ReminderStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Runs after device boot: re-schedules WorkManager notifications for all active reminders.
 */
@HiltWorker
class BootRescheduleWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ReminderRepository,
    private val scheduler: ReminderScheduler
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val activeReminders = repository.getUpcomingRemindersList()
        activeReminders.forEach { reminder ->
            scheduler.cancelReminder(reminder.id)
            scheduler.scheduleReminder(reminder)
        }
        return Result.success()
    }
}
