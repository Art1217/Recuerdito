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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.reminderpay.app.data.model.ReminderCategory
import com.reminderpay.app.data.model.RepeatType
import com.reminderpay.app.ui.components.categoryColor
import com.reminderpay.app.ui.components.categoryIcon
import com.reminderpay.app.ui.viewmodel.AddEditReminderViewModel
import com.reminderpay.app.utils.DateUtils
import java.util.*

private val PageBg   = Color(0xFFF1F5F9)
private val CardBg   = Color.White
private val SlateMid = Color(0xFF64748B)
private val Primary  = Color(0xFF6750A4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    onBack: () -> Unit,
    viewModel: AddEditReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context  = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Nuevo recordatorio",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF475569)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->

        // ── Outer Box: scroll on top, sticky button at bottom ──────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Scrollable form ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 88.dp), // 88dp for button clearance
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ── Title ──────────────────────────────────────────────────────
                FormField {
                    OutlinedTextField(
                        value         = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        label         = { Text("Título *") },
                        isError       = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { { Text(it) } },
                        leadingIcon   = {
                            Icon(Icons.Default.Title, null, tint = SlateMid)
                        },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        shape         = RoundedCornerShape(14.dp),
                        colors        = fieldColors()
                    )
                }

                // ── Description ────────────────────────────────────────────────
                FormField {
                    OutlinedTextField(
                        value         = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        label         = { Text("Descripción (opcional)") },
                        leadingIcon   = { Icon(Icons.Default.Notes, null, tint = SlateMid) },
                        modifier      = Modifier.fillMaxWidth(),
                        minLines      = 2,
                        maxLines      = 4,
                        shape         = RoundedCornerShape(14.dp),
                        colors        = fieldColors()
                    )
                }

                // ── Date & Time ────────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Date
                    PickerButton(
                        icon    = Icons.Default.CalendarToday,
                        label   = DateUtils.formatDate(uiState.date),
                        modifier = Modifier.weight(1f),
                        onClick  = {
                            val cal = Calendar.getInstance().apply { timeInMillis = uiState.date }
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val picked = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    viewModel.onDateChanged(picked)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    )
                    // Time
                    PickerButton(
                        icon    = Icons.Default.AccessTime,
                        label   = DateUtils.formatTime(uiState.time),
                        modifier = Modifier.weight(1f),
                        onClick  = {
                            val cal = Calendar.getInstance().apply { timeInMillis = uiState.time }
                            TimePickerDialog(
                                context,
                                { _, h, min ->
                                    val picked = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, min)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                    viewModel.onTimeChanged(picked)
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }
                    )
                }

                // ── Category ───────────────────────────────────────────────────
                SectionLabel("Categoría")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReminderCategory.all) { cat ->
                        val selected = cat == uiState.category
                        val catColor = categoryColor(cat)
                        Surface(
                            onClick = { viewModel.onCategoryChanged(cat) },
                            shape   = RoundedCornerShape(50),
                            color   = if (selected) catColor.copy(alpha = 0.18f)
                                      else          Color(0xFFE2E8F0),
                            border  = if (selected)
                                          BorderStroke(1.5.dp, catColor)
                                      else
                                          null,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector        = categoryIcon(cat),
                                    contentDescription = null,
                                    tint               = if (selected) catColor else SlateMid,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text       = cat,
                                    fontSize   = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (selected) catColor else SlateMid
                                )
                            }
                        }
                    }
                }

                // ── Repeat type ────────────────────────────────────────────────
                SectionLabel("Repetición")
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = CardBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RepeatType.all.forEach { type ->
                            val selected = type == uiState.repeatType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) Primary else Color.Transparent
                                    )
                                    .clickable { viewModel.onRepeatTypeChanged(type) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = type,
                                    fontSize   = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color      = if (selected) Color.White else SlateMid,
                                    textAlign  = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // ── Notify days before ─────────────────────────────────────────
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notificar antes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${uiState.notifyDaysBefore} día(s)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Slider(
                            value         = uiState.notifyDaysBefore.toFloat(),
                            onValueChange = { viewModel.onNotifyDaysBeforeChanged(it.toInt()) },
                            valueRange    = 0f..14f,
                            steps         = 13,
                            colors        = SliderDefaults.colors(
                                thumbColor       = Primary,
                                activeTrackColor = Primary
                            )
                        )
                        // Reference labels below slider
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Mismo día", "1 Sem", "2 Sem").forEach { label ->
                                Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            } // end scrollable column

            // ── Sticky Save button ──────────────────────────────────────────────
            Surface(
                modifier      = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color         = PageBg.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                Button(
                    onClick  = viewModel::saveReminder,
                    enabled  = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Guardar recordatorio",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Small helpers ────────────────────────────────────────────────────────────

@Composable
private fun FormField(content: @Composable () -> Unit) {
    content()
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text       = text,
        fontSize   = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color      = Color(0xFF334155)
    )
}

@Composable
private fun PickerButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.outlinedButtonColors(containerColor = CardBg),
        border   = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = Primary)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF0F172A))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Primary,
    unfocusedBorderColor = Color(0xFFCBD5E1),
    focusedContainerColor   = CardBg,
    unfocusedContainerColor = CardBg
)
