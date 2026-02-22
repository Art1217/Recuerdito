package com.reminderpay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.ui.components.*
import com.reminderpay.app.ui.theme.CompletedGreen
import com.reminderpay.app.ui.viewmodel.ReminderDetailViewModel
import com.reminderpay.app.utils.DateUtils

/**
 * Detail screen for a single reminder: shows full info, time remaining,
 * and actions (complete / edit / delete).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-navigate on completion or deletion
    LaunchedEffect(uiState.isCompleted, uiState.isDeleted) {
        if (uiState.isCompleted || uiState.isDeleted) onBack()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    uiState.reminder?.let { rem ->
                        IconButton(onClick = { onEdit(rem.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.reminder == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Recordatorio no encontrado") }

            else -> {
                val reminder = uiState.reminder!!
                val due      = DateUtils.combineDateAndTime(reminder.date, reminder.time)
                val diff     = due - System.currentTimeMillis()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Priority / time remaining banner ──────────────────────
                    val priorityColor = when {
                        diff < 0                        -> Color(0xFFEF4444)
                        diff <= DateUtils.HOURS_24_MS   -> Color(0xFFF97316)
                        diff <= DateUtils.DAYS_3_MS     -> Color(0xFFF59E0B)
                        else                            -> Color(0xFF10B981)
                    }
                    val prioBg = priorityColor.copy(alpha = 0.12f)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = prioBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = priorityColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text       = viewModel.timeRemainingLabel(),
                                    style      = MaterialTheme.typography.headlineSmall,
                                    color      = priorityColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text  = DateUtils.formatFull(due),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = priorityColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // ── Info card ─────────────────────────────────────────────
                    Card(
                        shape  = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text       = reminder.title,
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (reminder.description.isNotBlank()) {
                                Text(
                                    text  = reminder.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider()

                            // Meta info rows
                            InfoRow(Icons.Default.Category, "Categoría", reminder.category)
                            InfoRow(Icons.Default.Repeat, "Repetición", reminder.repeatType)
                            InfoRow(
                                Icons.Default.NotificationsActive,
                                "Aviso anticipado",
                                "${reminder.notifyDaysBefore} día(s) antes"
                            )
                            InfoRow(Icons.Default.Tag, "Tipo", reminder.type)
                            InfoRow(
                                Icons.Default.AddCircleOutline,
                                "Creado",
                                DateUtils.formatFull(reminder.createdAt)
                            )
                        }
                    }

                    // ── Mark complete button ───────────────────────────────────
                    Button(
                        onClick  = viewModel::markAsCompleted,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = CompletedGreen
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Marcar como completado", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ── Delete confirmation dialog ─────────────────────────────────────────
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon    = { Icon(Icons.Default.DeleteForever, null) },
                title   = { Text("Eliminar recordatorio") },
                text    = { Text("¿Seguro que quieres eliminar este recordatorio? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReminder()
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

/** Small icon + label + value row for the detail card */
@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(
            text  = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
