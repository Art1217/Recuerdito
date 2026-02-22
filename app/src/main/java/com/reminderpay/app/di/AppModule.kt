package com.reminderpay.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.reminderpay.app.data.database.AppDatabase
import com.reminderpay.app.data.database.dao.ReminderDao
import com.reminderpay.app.data.repository.ReminderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module that provides application-scoped dependencies:
 * - Room Database & DAO
 * - Repository
 * - WorkManager
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()

    @Provides
    @Singleton
    fun provideReminderRepository(dao: ReminderDao): ReminderRepository =
        ReminderRepository(dao)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
