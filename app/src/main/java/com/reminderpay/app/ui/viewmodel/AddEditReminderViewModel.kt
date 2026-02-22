package com.reminderpay.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reminderpay.app.data.model.*
import com.reminderpay.app.domain.usecases.*
import com.reminderpay.app.notifications.ReminderScheduler
import com.reminderpay.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI state for the Add/Edit reminder form */
data class AddEditUiState(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val time: Long = System.currentTimeMillis(),
    val category: String = ReminderCategory.OTRO,
    val type: String = "Task",
    val repeatType: String = RepeatType.NONE,
    val notifyDaysBefore: Int = 1,

    val titleError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false
)

/**
 * Shared ViewModel for AddReminderScreen and EditReminderScreen.
 * Uses [SavedStateHandle] to receive the optional reminderId navigation argument.
 */
@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addReminderUseCase: AddReminderUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val getReminderById: GetReminderByIdUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    init {
        // If editing, load existing reminder
        savedStateHandle.get<Int>("reminderId")?.let { id ->
            if (id != 0) loadReminder(id)
        }
    }

    private fun loadReminder(id: Int) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getReminderById(id).filterNotNull().first().let { reminder ->
                _uiState.update {
                    it.copy(
                        id = reminder.id,
                        title = reminder.title,
                        description = reminder.description,
                        date = reminder.date,
                        time = reminder.time,
                        category = reminder.category,
                        type = reminder.type,
                        repeatType = reminder.repeatType,
                        notifyDaysBefore = reminder.notifyDaysBefore,
                        isEditMode = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    // ─── Field update helpers ─────────────────────────────────────────────────

    fun onTitleChanged(value: String) =
        _uiState.update { it.copy(title = value, titleError = null) }

    fun onDescriptionChanged(value: String) =
        _uiState.update { it.copy(description = value) }

    fun onDateChanged(value: Long) =
        _uiState.update { it.copy(date = value) }

    fun onTimeChanged(value: Long) =
        _uiState.update { it.copy(time = value) }

    fun onCategoryChanged(value: String) =
        _uiState.update { it.copy(category = value) }

    fun onTypeChanged(value: String) =
        _uiState.update { it.copy(type = value) }

    fun onRepeatTypeChanged(value: String) =
        _uiState.update { it.copy(repeatType = value) }

    fun onNotifyDaysBeforeChanged(value: Int) =
        _uiState.update { it.copy(notifyDaysBefore = value) }

    // ─── Save ─────────────────────────────────────────────────────────────────

    fun saveReminder() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val reminder = Reminder(
                id = state.id,
                title = state.title.trim(),
                description = state.description.trim(),
                date = state.date,
                time = state.time,
                category = state.category,
                type = state.type,
                repeatType = state.repeatType,
                notifyDaysBefore = state.notifyDaysBefore,
                status = ReminderStatus.ACTIVE,
                createdAt = if (state.isEditMode) System.currentTimeMillis() else System.currentTimeMillis()
            )

            if (state.isEditMode) {
                updateReminderUseCase(reminder)
                reminderScheduler.cancelReminder(reminder.id)
                reminderScheduler.scheduleReminder(reminder)
            } else {
                val newId = addReminderUseCase(reminder)
                reminderScheduler.scheduleReminder(reminder.copy(id = newId.toInt()))
            }

            _uiState.update { it.copy(isLoading = false, isSaved = true) }
        }
    }
}
