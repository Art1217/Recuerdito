package com.reminderpay.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reminder entity stored in the local Room database.
 * Represents a single reminder (payment, event, task, etc.)
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Short title for the reminder */
    val title: String,

    /** Optional longer description */
    val description: String = "",

    /** Date stored as epoch milliseconds (yyyy-MM-dd at midnight) */
    val date: Long,

    /** Time stored as epoch milliseconds (hour+minute component) */
    val time: Long,

    /**
     * Category: Payment, Personal, Work, Study, Other
     */
    val category: String = ReminderCategory.OTHER,

    /**
     * Type label (e.g., "Bill", "Event", "Task", "Birthday")
     */
    val type: String = "Task",

    /**
     * Repetition: None, Monthly, Weekly, Yearly
     */
    val repeatType: String = RepeatType.NONE,

    /** How many days before the due date to send the early notification */
    val notifyDaysBefore: Int = 0,

    /**
     * Status: Active, Completed, Cancelled
     */
    val status: String = ReminderStatus.ACTIVE,

    /** Timestamp when this reminder was created */
    val createdAt: Long = System.currentTimeMillis()
)

/** Available reminder categories */
object ReminderCategory {
    const val PAYMENT = "Payment"
    const val PERSONAL = "Personal"
    const val WORK = "Work"
    const val STUDY = "Study"
    const val OTHER = "Other"

    val all = listOf(PAYMENT, PERSONAL, WORK, STUDY, OTHER)
}

/** Repeat schedule options */
object RepeatType {
    const val NONE = "None"
    const val WEEKLY = "Weekly"
    const val MONTHLY = "Monthly"
    const val YEARLY = "Yearly"

    val all = listOf(NONE, WEEKLY, MONTHLY, YEARLY)
}

/** Reminder status values */
object ReminderStatus {
    const val ACTIVE = "Active"
    const val COMPLETED = "Completed"
    const val CANCELLED = "Cancelled"
}

/**
 * Priority classification computed at runtime based on time until due.
 */
enum class ReminderPriority {
    URGENT,   // < 24 hours
    SOON,     // < 3 days
    UPCOMING  // > 3 days
}
