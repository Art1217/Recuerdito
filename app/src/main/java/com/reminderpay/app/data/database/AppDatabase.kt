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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton [AppDatabase] instance.
         * Used by workers that cannot use Hilt injection (e.g. WidgetUpdateWorker).
         */
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
