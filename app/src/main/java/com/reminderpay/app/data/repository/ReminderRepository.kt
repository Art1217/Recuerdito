package com.reminderpay.app.data.repository

import com.reminderpay.app.data.database.dao.ReminderDao
import com.reminderpay.app.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for Reminder data.
 * The repository abstracts the DAO from the domain/UI layers.
 */
@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {

    // ─── Reactive streams ─────────────────────────────────────────────────────

    /** Flow of all active reminders ordered by date */
    val activeReminders: Flow<List<Reminder>> = reminderDao.getActiveReminders()

    /** Flow of completed reminders */
    val completedReminders: Flow<List<Reminder>> = reminderDao.getCompletedReminders()

    /** Flow of active reminder count */
    val activeCount: Flow<Int> = reminderDao.getActiveCount()

    // ─── CRUD operations ──────────────────────────────────────────────────────

    /** Insert a new reminder, returns the generated row id */
    suspend fun addReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder)

    /** Overwrite an existing reminder */
    suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder)

    /** Delete a reminder permanently */
    suspend fun deleteReminder(reminder: Reminder) =
        reminderDao.deleteReminder(reminder)

    suspend fun deleteReminderById(id: Int) =
        reminderDao.deleteReminderById(id)

    /** Get a single reminder as a reactive stream */
    fun getReminderById(id: Int): Flow<Reminder?> =
        reminderDao.getReminderById(id)

    /** Mark a reminder as completed */
    suspend fun markAsCompleted(id: Int) =
        reminderDao.markAsCompleted(id)

    /** Get urgent reminders (within 24 hours) */
    fun getUrgentReminders(now: Long, in24h: Long): Flow<List<Reminder>> =
        reminderDao.getUrgentReminders(now, in24h)

    /** Non-suspended snapshot for widget updates */
    suspend fun getUpcomingRemindersList(): List<Reminder> =
        reminderDao.getUpcomingRemindersList()
}
