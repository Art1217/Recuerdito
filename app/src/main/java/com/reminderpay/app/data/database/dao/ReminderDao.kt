package com.reminderpay.app.data.database.dao

import androidx.room.*
import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.data.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the [Reminder] entity.
 * All queries return [Flow] so the UI reacts automatically to DB changes.
 */
@Dao
interface ReminderDao {

    // ─── Insert / Update / Delete ─────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    // ─── Queries ──────────────────────────────────────────────────────────────

    /** All active reminders ordered by date ascending */
    @Query(
        """SELECT * FROM reminders 
           WHERE status = :status 
           ORDER BY date ASC, time ASC"""
    )
    fun getActiveReminders(status: String = ReminderStatus.ACTIVE): Flow<List<Reminder>>

    /** Completed / historical reminders ordered by date descending */
    @Query(
        """SELECT * FROM reminders 
           WHERE status = :status 
           ORDER BY date DESC"""
    )
    fun getCompletedReminders(status: String = ReminderStatus.COMPLETED): Flow<List<Reminder>>

    /** All reminders (for widget updates) */
    @Query("SELECT * FROM reminders WHERE status = 'Active' ORDER BY date ASC, time ASC")
    suspend fun getUpcomingRemindersList(): List<Reminder>

    /** Single reminder by id */
    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Int): Flow<Reminder?>

    /** Urgent reminders: due within the next 24 hours */
    @Query(
        """SELECT * FROM reminders 
           WHERE status = 'Active' 
             AND (date + time) BETWEEN :now AND :in24h 
           ORDER BY date ASC"""
    )
    fun getUrgentReminders(now: Long, in24h: Long): Flow<List<Reminder>>

    /** Reminders by category */
    @Query(
        """SELECT * FROM reminders 
           WHERE status = 'Active' AND category = :category 
           ORDER BY date ASC"""
    )
    fun getRemindersByCategory(category: String): Flow<List<Reminder>>

    /** Mark a reminder as completed */
    @Query("UPDATE reminders SET status = 'Completed' WHERE id = :id")
    suspend fun markAsCompleted(id: Int)

    /** Count of active reminders (for widget badge) */
    @Query("SELECT COUNT(*) FROM reminders WHERE status = 'Active'")
    fun getActiveCount(): Flow<Int>
}
