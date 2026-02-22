package com.reminderpay.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.ui.components.*
import com.reminderpay.app.ui.viewmodel.HomeViewModel

/**
 * Home screen: shows Urgent, Soon, and Upcoming reminder sections
 * with a FAB to add reminders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddReminder: () -> Unit,
    onReminderClick: (Int) -> Unit,
    onHistoryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "ReminderPay",
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text  = "Tus recordatorios",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddReminder,
                icon    = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                text    = { Text("Nuevo") }
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val allEmpty = uiState.allActive.isEmpty()

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (allEmpty) {
                item {
                    Spacer(Modifier.height(64.dp))
                    EmptyStateView(
                        icon     = {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        message  = "Sin recordatorios activos",
                        subtitle = "Toca el botón + para agregar uno"
                    )
                }
            } else {
                // ── Urgent ──────────────────────────────────────────────
                if (uiState.urgentReminders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🔴 Urgente",
                            count = uiState.urgentReminders.size
                        )
                    }
                    items(
                        items = uiState.urgentReminders,
                        key   = { it.id }
                    ) { reminder ->
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn() + slideInVertically()
                        ) {
                            ReminderCard(
                                reminder   = reminder,
                                onClick    = { onReminderClick(reminder.id) },
                                onComplete = { viewModel.markAsCompleted(reminder) }
                            )
                        }
                    }
                }

                // ── Soon ─────────────────────────────────────────────────
                if (uiState.soonReminders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🟠 Pronto",
                            count = uiState.soonReminders.size
                        )
                    }
                    items(uiState.soonReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder   = reminder,
                            onClick    = { onReminderClick(reminder.id) },
                            onComplete = { viewModel.markAsCompleted(reminder) }
                        )
                    }
                }

                // ── Upcoming ─────────────────────────────────────────────
                if (uiState.upcomingReminders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "🔵 Próximamente",
                            count = uiState.upcomingReminders.size
                        )
                    }
                    items(uiState.upcomingReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder   = reminder,
                            onClick    = { onReminderClick(reminder.id) },
                            onComplete = { viewModel.markAsCompleted(reminder) }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) } // FAB clearance
            }
        }
    }
}
