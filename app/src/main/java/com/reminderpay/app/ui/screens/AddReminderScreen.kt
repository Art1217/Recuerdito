package com.reminderpay.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.data.model.ReminderCategory
import com.reminderpay.app.data.model.RepeatType
import com.reminderpay.app.ui.components.categoryColor
import com.reminderpay.app.ui.components.categoryIcon
import com.reminderpay.app.ui.viewmodel.AddEditReminderViewModel
import com.reminderpay.app.utils.DateUtils
import java.util.*

/**
 * Screen for adding a NEW reminder.
 * Delegates state to [AddEditReminderViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    onBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context  = LocalContext.current

    // Navigate back once saved
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo recordatorio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Title ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value       = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                label       = { Text("Título *") },
                isError     = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier    = Modifier.fillMaxWidth(),
                singleLine  = true
            )

            // ── Description ───────────────────────────────────────────────────
            OutlinedTextField(
                value          = uiState.description,
                onValueChange  = viewModel::onDescriptionChanged,
                label          = { Text("Descripción (opcional)") },
                leadingIcon    = { Icon(Icons.Default.Notes, null) },
                modifier       = Modifier.fillMaxWidth(),
                minLines       = 2,
                maxLines       = 4
            )

            // ── Date & Time row ───────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Date picker button
                OutlinedButton(
                    onClick  = {
                        val cal = Calendar.getInstance().apply { timeInMillis = uiState.date }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val picked = Calendar.getInstance().apply {
                                    set(y, m, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                viewModel.onDateChanged(picked)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(DateUtils.formatDate(uiState.date))
                }

                // Time picker button
                OutlinedButton(
                    onClick  = {
                        val cal = Calendar.getInstance().apply { timeInMillis = uiState.time }
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                val picked = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, h)
                                    set(Calendar.MINUTE, m)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                                viewModel.onTimeChanged(picked)
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccessTime, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(DateUtils.formatTime(uiState.time))
                }
            }

            // ── Category selector ─────────────────────────────────────────────
            Text("Categoría", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ReminderCategory.all) { cat ->
                    val selected = cat == uiState.category
                    FilterChip(
                        selected = selected,
                        onClick  = { viewModel.onCategoryChanged(cat) },
                        label    = { Text(cat) },
                        leadingIcon = {
                            Icon(
                                categoryIcon(cat), null,
                                Modifier.size(16.dp),
                                tint = if (selected) categoryColor(cat)
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    )
                }
            }

            // ── Repeat type selector ──────────────────────────────────────────
            Text("Repetición", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RepeatType.all) { type ->
                    FilterChip(
                        selected = type == uiState.repeatType,
                        onClick  = { viewModel.onRepeatTypeChanged(type) },
                        label    = { Text(type) }
                    )
                }
            }

            // ── Notify days before slider ──────────────────────────────────────
            Column {
                Text(
                    "Notificar ${uiState.notifyDaysBefore} día(s) antes",
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value         = uiState.notifyDaysBefore.toFloat(),
                    onValueChange = { viewModel.onNotifyDaysBeforeChanged(it.toInt()) },
                    valueRange    = 0f..14f,
                    steps         = 13
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick  = viewModel::saveReminder,
                enabled  = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color    = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar recordatorio", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
