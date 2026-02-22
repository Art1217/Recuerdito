package com.reminderpay.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.domain.usecases.DeleteReminderUseCase
import com.reminderpay.app.domain.usecases.GetReminderByIdUseCase
import com.reminderpay.app.domain.usecases.MarkReminderCompletedUseCase
import com.reminderpay.app.notifications.ReminderScheduler
import com.reminderpay.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = true,
    val isCompleted: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the ReminderDetailScreen.
 * Reads the reminderId from [SavedStateHandle].
 */
@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReminderById: GetReminderByIdUseCase,
    private val markCompleted: MarkReminderCompletedUseCase,
    private val deleteReminder: DeleteReminderUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<Int>("reminderId")?.let { id ->
            loadReminder(id)
        }
    }

    private fun loadReminder(id: Int) {
        viewModelScope.launch {
            getReminderById(id)
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { reminder ->
                    _uiState.update { it.copy(reminder = reminder, isLoading = false) }
                }
        }
    }

    fun markAsCompleted() {
        val reminder = _uiState.value.reminder ?: return
        viewModelScope.launch {
            markCompleted(reminder.id)
            reminderScheduler.cancelReminder(reminder.id)
            _uiState.update { it.copy(isCompleted = true) }
        }
    }

    fun deleteReminder() {
        val reminder = _uiState.value.reminder ?: return
        viewModelScope.launch {
            deleteReminder.invoke(reminder)
            reminderScheduler.cancelReminder(reminder.id)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    /** Formatted time-remaining string, e.g. "Faltan 2 días" */
    fun timeRemainingLabel(): String {
        val reminder = _uiState.value.reminder ?: return ""
        val due = DateUtils.combineDateAndTime(reminder.date, reminder.time)
        val diff = due - System.currentTimeMillis()
        return DateUtils.formatTimeRemaining(diff)
    }
}
