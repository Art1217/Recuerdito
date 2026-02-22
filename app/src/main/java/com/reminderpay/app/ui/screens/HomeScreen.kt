package com.reminderpay.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.ui.components.*
import com.reminderpay.app.ui.viewmodel.HomeViewModel

// Slate-50 equivalent — subtle gray page background for depth
private val PageBackground = Color(0xFFF1F5F9)

/**
 * Home screen — shows Urgent, Soon, and Upcoming reminder sections.
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
        containerColor = PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text       = "ReminderPay",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color(0xFF0F172A)
                        )
                        Text(
                            text     = "Tus recordatorios",
                            fontSize = 12.sp,
                            color    = Color(0xFF94A3B8)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Historial",
                            tint = Color(0xFF475569)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = onAddReminder,
                icon           = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                text           = { Text("Nuevo", fontWeight = FontWeight.SemiBold) },
                containerColor = Color(0xFF6750A4),
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF6750A4)) }
            return@Scaffold
        }

        val allEmpty = uiState.allActive.isEmpty()

        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBackground),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (allEmpty) {
                item {
                    Spacer(Modifier.height(80.dp))
                    EmptyStateView(
                        icon     = {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(72.dp),
                                tint     = Color(0xFFCBD5E1)
                            )
                        },
                        message  = "Sin recordatorios activos",
                        subtitle = "Toca el botón + para agregar uno"
                    )
                }
            } else {
                // ── Urgente ──────────────────────────────────────────────
                if (uiState.urgentReminders.isNotEmpty()) {
                    item {
                        SectionHeader(title = "🔴  Urgente", count = uiState.urgentReminders.size)
                    }
                    items(uiState.urgentReminders, key = { it.id }) { reminder ->
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

                // ── Pronto ───────────────────────────────────────────────
                if (uiState.soonReminders.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionHeader(title = "🟠  Pronto", count = uiState.soonReminders.size)
                    }
                    items(uiState.soonReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder   = reminder,
                            onClick    = { onReminderClick(reminder.id) },
                            onComplete = { viewModel.markAsCompleted(reminder) }
                        )
                    }
                }

                // ── Próximamente ─────────────────────────────────────────
                if (uiState.upcomingReminders.isNotEmpty()) {
                    item { Spacer(Modifier.height(4.dp)) }
                    item {
                        SectionHeader(title = "🔵  Próximamente", count = uiState.upcomingReminders.size)
                    }
                    items(uiState.upcomingReminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder   = reminder,
                            onClick    = { onReminderClick(reminder.id) },
                            onComplete = { viewModel.markAsCompleted(reminder) }
                        )
                    }
                }

                item { Spacer(Modifier.height(88.dp)) } // clearance for FAB
            }
        }
    }
}
