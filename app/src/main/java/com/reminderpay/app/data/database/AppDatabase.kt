package com.reminderpay.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.reminderpay.app.data.database.dao.ReminderDao
import com.reminderpay.app.data.model.Reminder

/**
 * Main Room database for ReminderPay.
 * Uses a singleton pattern managed by Hilt.
 */
@Database(
    entities = [Reminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "reminder_pay_db"
    }
}
