package com.reminderpay.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.ui.components.categoryColor
import com.reminderpay.app.ui.components.categoryIcon
import com.reminderpay.app.ui.viewmodel.ReminderDetailViewModel
import com.reminderpay.app.utils.DateUtils

private val PageBg  = Color(0xFFF1F5F9)
private val Emerald = Color(0xFF059669)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    viewModel: ReminderDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted, uiState.isDeleted) {
        if (uiState.isCompleted || uiState.isDeleted) onBack()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detalle",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color(0xFF475569))
                    }
                },
                actions = {
                    uiState.reminder?.let { rem ->
                        IconButton(onClick = { onEdit(rem.id) }) {
                            Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF475569))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.DeleteOutline, "Eliminar", tint = Color(0xFFEF4444))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->

        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Color(0xFF6750A4)) }

            uiState.reminder == null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Recordatorio no encontrado", color = Color(0xFF64748B)) }

            else -> {
                val reminder = uiState.reminder!!
                val due      = DateUtils.combineDateAndTime(reminder.date, reminder.time)
                val diff     = due - System.currentTimeMillis()

                // Banner color logic
                val bannerColor = when {
                    diff < 0                        -> Color(0xFFEF4444)
                    diff <= DateUtils.HOURS_1_MS    -> Color(0xFFF97316)
                    diff <= DateUtils.HOURS_24_MS   -> Color(0xFFF97316)
                    diff <= DateUtils.DAYS_3_MS     -> Color(0xFFF59E0B)
                    else                            -> Color(0xFF10B981)
                }
                val bannerLabel = when {
                    diff < 0                        -> "VENCIDO"
                    diff <= DateUtils.HOURS_1_MS    -> "¡MENOS DE 1 HORA!"
                    diff <= DateUtils.HOURS_24_MS   -> "URGENTE"
                    diff <= DateUtils.DAYS_3_MS     -> "PRONTO"
                    else                            -> "PRÓXIMO"
                }
                val bannerIcon = when {
                    diff < 0  -> Icons.Default.ErrorOutline
                    diff <= DateUtils.HOURS_24_MS -> Icons.Default.Warning
                    else      -> Icons.Default.Timer
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // ── Scrollable content ─────────────────────────────────────
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // ── Status banner ──────────────────────────────────────
                        Surface(
                            shape    = RoundedCornerShape(16.dp),
                            color    = bannerColor.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier          = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bannerColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        bannerIcon, null,
                                        tint     = bannerColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text       = bannerLabel,
                                        fontSize   = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color      = bannerColor
                                    )
                                    Text(
                                        text     = DateUtils.formatFull(due),
                                        fontSize = 12.sp,
                                        color    = bannerColor.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        // ── Main content card ──────────────────────────────────
                        Surface(
                            shape    = RoundedCornerShape(24.dp),
                            color    = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {

                                // Category pill
                                val catColor = categoryColor(reminder.category)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = catColor.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            categoryIcon(reminder.category), null,
                                            tint     = catColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            reminder.category,
                                            fontSize   = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color      = catColor
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Title
                                Text(
                                    text       = reminder.title,
                                    fontSize   = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = Color(0xFF0F172A)
                                )

                                // Description
                                if (reminder.description.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text     = reminder.description,
                                        fontSize = 14.sp,
                                        color    = Color(0xFF64748B)
                                    )
                                }

                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(Modifier.height(16.dp))

                                // Metadata rows
                                MetaRow(Icons.Default.Category,           "Categoría",       reminder.category,    Color(0xFF6750A4))
                                MetaRow(Icons.Default.Repeat,             "Repetición",      reminder.repeatType,  Color(0xFF0EA5E9))
                                MetaRow(Icons.Default.NotificationsActive,"Aviso anticipado","${reminder.notifyDaysBefore} día(s) antes", Color(0xFFF59E0B))
                                MetaRow(Icons.Default.Tag,                "Tipo",            reminder.type,        Color(0xFF10B981))
                                MetaRow(Icons.Default.AddCircleOutline,   "Creado",          DateUtils.formatFull(reminder.createdAt), Color(0xFF94A3B8))
                            }
                        }
                    }

                    // ── Sticky Complete button ─────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        color    = PageBg.copy(alpha = 0.96f)
                    ) {
                        Button(
                            onClick  = viewModel::markAsCompleted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .height(54.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Marcar como completado",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Delete dialog ──────────────────────────────────────────────────────
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon    = { Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFEF4444)) },
                title   = { Text("Eliminar recordatorio", fontWeight = FontWeight.Bold) },
                text    = { Text("¿Seguro que quieres eliminar este recordatorio? Esta acción no se puede deshacer.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReminder()
                    }) { Text("Eliminar", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun MetaRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with soft background
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text     = label,
            fontSize = 13.sp,
            color    = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
        Text(
            text       = value,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF0F172A)
        )
    }
}
