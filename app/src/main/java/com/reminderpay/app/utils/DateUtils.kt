package com.reminderpay.app.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility functions for date/time formatting and calculations.
 */
object DateUtils {

    const val HOURS_1_MS  = 1L  * 60 * 60 * 1000
    const val HOURS_24_MS = 24L * 60 * 60 * 1000
    const val DAYS_3_MS   = 3L  * 24 * 60 * 60 * 1000

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a",     Locale.getDefault())
    private val fullFormat  = SimpleDateFormat("dd MMM yyyy • h:mm a", Locale.getDefault())

    /** Format a timestamp to a human-readable date string */
    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    /** Format a timestamp to a human-readable time string */
    fun formatTime(millis: Long): String = timeFormat.format(Date(millis))

    /** Full date + time label */
    fun formatFull(millis: Long): String = fullFormat.format(Date(millis))

    /**
     * Combine separate date and time epoch values into a single due-date epoch.
     * Room stores date and time as epoch millis from separate pickers;
     * we combine them by extracting time-of-day from [time] and date from [date].
     */
    fun combineDateAndTime(dateMills: Long, timeMills: Long): Long {
        val dateCalendar = Calendar.getInstance().apply { timeInMillis = dateMills }
        val timeCalendar = Calendar.getInstance().apply { timeInMillis = timeMills }
        return Calendar.getInstance().apply {
            set(Calendar.YEAR,        dateCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH,       dateCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH,dateCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE,      timeCalendar.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Return a human-readable countdown string given a duration in millis.
     * Examples: "Faltan 2 días", "Faltan 3 horas", "¡Hoy!", "Vencido"
     */
    fun formatTimeRemaining(diffMillis: Long): String {
        if (diffMillis < 0) return "Vencido"
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days  = TimeUnit.MILLISECONDS.toDays(diffMillis)
        return when {
            hours < 1  -> "¡Menos de 1 hora!"
            hours < 24 -> "Faltan ${hours} hora${if (hours == 1L) "" else "s"}"
            days == 1L -> "Mañana"
            else       -> "Faltan ${days} días"
        }
    }

    /** Epoch millis for today at midnight */
    fun todayAtMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Calendar set to date from epoch + offset in days */
    fun epochPlusDays(epochMillis: Long, days: Int): Long =
        epochMillis + days * 24L * 60 * 60 * 1000
}
