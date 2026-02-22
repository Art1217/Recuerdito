package com.reminderpay.app.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.reminderpay.app.ui.viewmodel.AddEditReminderViewModel

/**
 * Edit Reminder screen — reuses [AddReminderScreen] layout.
 * The [AddEditReminderViewModel] reads `reminderId` from SavedStateHandle
 * and auto-populates the form for editing.
 */
@Composable
fun EditReminderScreen(
    onBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    // Reuse the exact same composable as AddReminderScreen
    // The ViewModel's isEditMode flag distinguishes add vs edit behavior.
    AddReminderScreen(onBack = onBack, viewModel = viewModel)
}
