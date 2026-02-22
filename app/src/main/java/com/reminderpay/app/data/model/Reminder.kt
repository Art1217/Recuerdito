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
    val category: String = ReminderCategory.OTRO,

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

/** Available reminder categories (in Spanish) */
object ReminderCategory {
    // ── Pagos específicos ──────────────────────────────────────────────────────
    const val PAGO_LUZ         = "Pago Luz"
    const val PAGO_AGUA        = "Pago Agua"
    const val PAGO_INTERNET    = "Pago Internet"
    const val PAGO_GAS         = "Pago Gas"
    const val PAGO_UNIVERSIDAD = "Pago Universidad"
    // ── Otras categorías ──────────────────────────────────────────────────────
    const val PERSONAL         = "Personal"
    const val TRABAJO          = "Trabajo"
    const val ESTUDIO          = "Estudio"
    const val OTRO             = "Otro"

    val all = listOf(
        PAGO_LUZ, PAGO_AGUA, PAGO_INTERNET, PAGO_GAS, PAGO_UNIVERSIDAD,
        PERSONAL, TRABAJO, ESTUDIO, OTRO
    )

    /** Returns true if this category is a payment type */
    fun isPayment(category: String) = category.startsWith("Pago")
}

/** Repeat schedule options */
object RepeatType {
    const val NONE     = "Sin repetición"
    const val WEEKLY   = "Semanal"
    const val MONTHLY  = "Mensual"
    const val YEARLY   = "Anual"

    val all = listOf(NONE, WEEKLY, MONTHLY, YEARLY)
}

/** Reminder status values */
object ReminderStatus {
    const val ACTIVE    = "Active"
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
