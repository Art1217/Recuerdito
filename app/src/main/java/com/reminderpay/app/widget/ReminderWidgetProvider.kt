package com.reminderpay.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.reminderpay.app.R
import com.reminderpay.app.utils.Constants

/**
 * Home Screen Widget for ReminderPay.
 * Shows the next 3 upcoming reminders. Updates via WorkManager every 30 min.
 */
class ReminderWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Push a placeholder immediately so the widget never shows blank/error
        val placeholder = RemoteViews(context.packageName, R.layout.reminder_widget)
        placeholder.setTextViewText(R.id.widget_item1, "Cargando recordatorios…")
        placeholder.setTextViewText(R.id.widget_item2, "")
        placeholder.setTextViewText(R.id.widget_item3, "")
        appWidgetManager.updateAppWidget(appWidgetIds, placeholder)

        // Then trigger the real data fetch via WorkManager
        try {
            WidgetUpdateWorker.enqueue(context)
        } catch (e: Exception) {
            // WorkManager not yet initialized — placeholder stays until next update
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        try {
            WidgetUpdateWorker.scheduleRepeating(context)
        } catch (e: Exception) { /* ignore */ }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        try {
            androidx.work.WorkManager.getInstance(context)
                .cancelAllWorkByTag(Constants.WIDGET_UPDATE_WORKER_TAG)
        } catch (e: Exception) { /* ignore */ }
    }
}
