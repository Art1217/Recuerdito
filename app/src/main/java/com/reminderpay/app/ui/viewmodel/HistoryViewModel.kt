package com.reminderpay.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderpay.app.data.model.Reminder
import com.reminderpay.app.domain.usecases.DeleteReminderUseCase
import com.reminderpay.app.domain.usecases.GetCompletedRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val completedReminders: List<Reminder> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * ViewModel for HistoryScreen – shows completed reminders.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getCompletedReminders: GetCompletedRemindersUseCase,
    private val deleteReminder: DeleteReminderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getCompletedReminders()
                .catch { /* silently ignore stream errors */ }
                .collect { list ->
                    _uiState.update { it.copy(completedReminders = list, isLoading = false) }
                }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            deleteReminder.invoke(reminder)
        }
    }
}
