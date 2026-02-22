package com.reminderpay.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.reminderpay.app.R
import com.reminderpay.app.data.database.AppDatabase
import com.reminderpay.app.utils.Constants
import com.reminderpay.app.utils.DateUtils
import java.util.concurrent.TimeUnit

/**
 * Plain CoroutineWorker that reads Room directly (no Hilt dependency)
 * and pushes updated RemoteViews to all active ReminderPay widget instances.
 */
class WidgetUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db        = AppDatabase.getDatabase(applicationContext)
            val reminders = db.reminderDao().getUpcomingRemindersList().take(Constants.WIDGET_MAX_ITEMS)

            val views = RemoteViews(applicationContext.packageName, R.layout.reminder_widget)

            if (reminders.isEmpty()) {
                views.setTextViewText(R.id.widget_item1, "Sin recordatorios próximos")
                views.setTextViewText(R.id.widget_item2, "")
                views.setTextViewText(R.id.widget_item3, "")
            } else {
                reminders.getOrNull(0)?.let { r ->
                    val due = DateUtils.combineDateAndTime(r.date, r.time)
                    val rem = DateUtils.formatTimeRemaining(due - System.currentTimeMillis())
                    views.setTextViewText(R.id.widget_item1, "• ${r.title}  $rem")
                }
                reminders.getOrNull(1)?.let { r ->
                    val due = DateUtils.combineDateAndTime(r.date, r.time)
                    val rem = DateUtils.formatTimeRemaining(due - System.currentTimeMillis())
                    views.setTextViewText(R.id.widget_item2, "• ${r.title}  $rem")
                } ?: views.setTextViewText(R.id.widget_item2, "")

                reminders.getOrNull(2)?.let { r ->
                    val due = DateUtils.combineDateAndTime(r.date, r.time)
                    val rem = DateUtils.formatTimeRemaining(due - System.currentTimeMillis())
                    views.setTextViewText(R.id.widget_item3, "• ${r.title}  $rem")
                } ?: views.setTextViewText(R.id.widget_item3, "")
            }

            val manager   = AppWidgetManager.getInstance(applicationContext)
            val widgetIds = manager.getAppWidgetIds(
                ComponentName(applicationContext, ReminderWidgetProvider::class.java)
            )
            if (widgetIds.isNotEmpty()) {
                manager.updateAppWidget(widgetIds, views)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .addTag(Constants.WIDGET_UPDATE_WORKER_TAG)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "widget_one_time_update",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun scheduleRepeating(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
                .addTag(Constants.WIDGET_UPDATE_WORKER_TAG)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WIDGET_UPDATE_WORKER_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}
