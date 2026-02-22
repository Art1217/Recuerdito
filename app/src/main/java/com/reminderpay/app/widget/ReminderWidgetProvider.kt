package com.reminderpay.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import com.reminderpay.app.R
import com.reminderpay.app.utils.Constants
import java.util.concurrent.TimeUnit

/**
 * Home Screen Widget provider for ReminderPay.
 *
 * Shows the next 3 upcoming reminders with title, date, and time-remaining.
 * Tapping the widget opens the app's HomeScreen via MainActivity.
 */
class ReminderWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Trigger an immediate widget update via WorkManager
        enqueueWidgetUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicWidgetUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(Constants.WIDGET_UPDATE_WORKER_TAG)
    }

    companion object {
        fun enqueueWidgetUpdate(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .addTag(Constants.WIDGET_UPDATE_WORKER_TAG)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "widget_one_time_update",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        private fun schedulePeriodicWidgetUpdate(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                repeatInterval = 30,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
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
