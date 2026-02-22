package com.reminderpay.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.data.model.ReminderPriority
import com.reminderpay.app.domain.usecases.*
import com.reminderpay.app.notifications.ReminderScheduler
import com.reminderpay.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the home screen */
data class HomeUiState(
    val urgentReminders: List<Reminder> = emptyList(),
    val soonReminders: List<Reminder> = emptyList(),
    val upcomingReminders: List<Reminder> = emptyList(),
    val allActive: List<Reminder> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for [HomeScreen].
 * Classifies reminders into Urgent / Soon / Upcoming buckets.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getActiveReminders: GetActiveRemindersUseCase,
    private val markReminderCompleted: MarkReminderCompletedUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            getActiveReminders()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { reminders ->
                    val now = System.currentTimeMillis()
                    val urgent = reminders.filter { getPriority(it, now) == ReminderPriority.URGENT }
                    val soon = reminders.filter { getPriority(it, now) == ReminderPriority.SOON }
                    val upcoming = reminders.filter { getPriority(it, now) == ReminderPriority.UPCOMING }
                    _uiState.update {
                        it.copy(
                            urgentReminders = urgent,
                            soonReminders = soon,
                            upcomingReminders = upcoming,
                            allActive = reminders,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /** Mark a reminder as done and cancel its scheduled notification */
    fun markAsCompleted(reminder: Reminder) {
        viewModelScope.launch {
            markReminderCompleted(reminder.id)
            reminderScheduler.cancelReminder(reminder.id)
        }
    }

    /** Permanently remove a reminder */
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            deleteReminder.invoke(reminder)
            reminderScheduler.cancelReminder(reminder.id)
        }
    }

    /** Compute priority based on milliseconds until due datetime */
    private fun getPriority(reminder: Reminder, now: Long): ReminderPriority {
        val dueMills = DateUtils.combineDateAndTime(reminder.date, reminder.time)
        val diff = dueMills - now
        return when {
            diff <= DateUtils.HOURS_24_MS -> ReminderPriority.URGENT
            diff <= DateUtils.DAYS_3_MS   -> ReminderPriority.SOON
            else                          -> ReminderPriority.UPCOMING
        }
    }
}
