package com.reminderpay.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.reminderpay.app.R
import com.reminderpay.app.data.repository.ReminderRepository
import com.reminderpay.app.utils.Constants
import com.reminderpay.app.utils.DateUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that refreshes RemoteViews for all active ReminderPay widgets.
 * Reads the next [Constants.WIDGET_MAX_ITEMS] reminders from Room and updates the widget.
 */
@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ReminderRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminders = repository.getUpcomingRemindersList()
            .take(Constants.WIDGET_MAX_ITEMS)

        val views = RemoteViews(applicationContext.packageName, R.layout.reminder_widget)

        if (reminders.isEmpty()) {
            // Show empty state
            views.setTextViewText(R.id.widget_item1_title, "No tienes recordatorios próximos")
            views.setTextViewText(R.id.widget_item1_date, "")
            views.setTextViewText(R.id.widget_item1_remaining, "")
            views.setViewVisibility(R.id.widget_item2_layout, android.view.View.GONE)
            views.setViewVisibility(R.id.widget_item3_layout, android.view.View.GONE)
        } else {
            reminders.forEachIndexed { index, reminder ->
                val due       = DateUtils.combineDateAndTime(reminder.date, reminder.time)
                val remaining = DateUtils.formatTimeRemaining(due - System.currentTimeMillis())
                val dateLabel = DateUtils.formatFull(due)

                when (index) {
                    0 -> {
                        views.setTextViewText(R.id.widget_item1_title, reminder.title)
                        views.setTextViewText(R.id.widget_item1_date, dateLabel)
                        views.setTextViewText(R.id.widget_item1_remaining, remaining)
                        views.setViewVisibility(R.id.widget_item1_layout, android.view.View.VISIBLE)
                    }
                    1 -> {
                        views.setTextViewText(R.id.widget_item2_title, reminder.title)
                        views.setTextViewText(R.id.widget_item2_date, dateLabel)
                        views.setTextViewText(R.id.widget_item2_remaining, remaining)
                        views.setViewVisibility(R.id.widget_item2_layout, android.view.View.VISIBLE)
                    }
                    2 -> {
                        views.setTextViewText(R.id.widget_item3_title, reminder.title)
                        views.setTextViewText(R.id.widget_item3_date, dateLabel)
                        views.setTextViewText(R.id.widget_item3_remaining, remaining)
                        views.setViewVisibility(R.id.widget_item3_layout, android.view.View.VISIBLE)
                    }
                }
            }
            // Hide slots for missing reminders
            if (reminders.size < 2) views.setViewVisibility(R.id.widget_item2_layout, android.view.View.GONE)
            if (reminders.size < 3) views.setViewVisibility(R.id.widget_item3_layout, android.view.View.GONE)
        }

        // Push updated views to all widget instances
        val manager    = AppWidgetManager.getInstance(applicationContext)
        val widgetIds  = manager.getAppWidgetIds(
            ComponentName(applicationContext, ReminderWidgetProvider::class.java)
        )
        manager.updateAppWidget(widgetIds, views)

        return Result.success()
    }
}
