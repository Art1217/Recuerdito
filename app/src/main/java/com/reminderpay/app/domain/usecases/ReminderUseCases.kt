package com.reminderpay.app.domain.usecases

import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.data.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Retrieve all active reminders as a reactive stream */
class GetActiveRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.activeReminders
}

/** Retrieve all completed reminders (history) */
class GetCompletedRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(): Flow<List<Reminder>> = repository.completedReminders
}

/** Get a single reminder by ID */
class GetReminderByIdUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(id: Int): Flow<Reminder?> = repository.getReminderById(id)
}

/** Add a new reminder and return its generated ID */
class AddReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder): Long = repository.addReminder(reminder)
}

/** Update an existing reminder */
class UpdateReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) = repository.updateReminder(reminder)
}

/** Delete a reminder by object reference */
class DeleteReminderUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(reminder: Reminder) = repository.deleteReminder(reminder)
}

/** Mark a reminder as completed */
class MarkReminderCompletedUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    suspend operator fun invoke(id: Int) = repository.markAsCompleted(id)
}

/** Get urgent reminders within 24-hour window */
class GetUrgentRemindersUseCase @Inject constructor(
    private val repository: ReminderRepository
) {
    operator fun invoke(now: Long, in24h: Long): Flow<List<Reminder>> =
        repository.getUrgentReminders(now, in24h)
}
