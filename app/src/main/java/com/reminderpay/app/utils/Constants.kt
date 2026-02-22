package com.reminderpay.app.utils

/**
 * App-wide constants for notification channels, WorkManager tags, and extras.
 */
object Constants {

    // ─── Notification ─────────────────────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID   = "reminder_pay_channel_v2"
    const val NOTIFICATION_CHANNEL_NAME = "ReminderPay Alerts"

    // ─── WorkManager ──────────────────────────────────────────────────────────
    const val REMINDER_WORKER_TAG         = "reminder_worker"
    const val WIDGET_UPDATE_WORKER_TAG    = "widget_update_worker"
    const val BOOT_RESCHEDULE_WORKER_TAG  = "boot_reschedule_worker"

    // ─── Intent Extras ────────────────────────────────────────────────────────
    const val EXTRA_REMINDER_ID    = "reminder_id"
    const val EXTRA_REMINDER_TITLE = "reminder_title"
    const val EXTRA_REMINDER_BODY  = "reminder_body"

    // ─── Widget ───────────────────────────────────────────────────────────────
    const val WIDGET_MAX_ITEMS = 3
}
