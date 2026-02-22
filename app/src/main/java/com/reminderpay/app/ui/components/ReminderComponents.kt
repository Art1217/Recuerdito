package com.reminderpay.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reminderpay.app.data.model.*
import com.reminderpay.app.ui.theme.*
import com.reminderpay.app.utils.DateUtils

// ─── Category helpers ────────────────────────────────────────────────────────

fun categoryColor(category: String): Color = when (category) {
    ReminderCategory.PAGO_LUZ         -> Color(0xFFF59E0B)
    ReminderCategory.PAGO_AGUA        -> Color(0xFF0EA5E9)
    ReminderCategory.PAGO_INTERNET    -> Color(0xFF8B5CF6)
    ReminderCategory.PAGO_GAS         -> Color(0xFFEF4444)
    ReminderCategory.PAGO_UNIVERSIDAD -> Color(0xFF3B82F6)
    ReminderCategory.PERSONAL         -> Color(0xFFEC4899)
    ReminderCategory.TRABAJO          -> Color(0xFF10B981)
    ReminderCategory.ESTUDIO          -> Color(0xFF22C55E)
    else                              -> Color(0xFF64748B)
}

fun categoryIcon(category: String) = when (category) {
    ReminderCategory.PAGO_LUZ         -> Icons.Default.Bolt
    ReminderCategory.PAGO_AGUA        -> Icons.Default.Water
    ReminderCategory.PAGO_INTERNET    -> Icons.Default.Wifi
    ReminderCategory.PAGO_GAS         -> Icons.Default.LocalFireDepartment
    ReminderCategory.PAGO_UNIVERSIDAD -> Icons.Default.School
    ReminderCategory.PERSONAL         -> Icons.Default.Person
    ReminderCategory.TRABAJO          -> Icons.Default.Work
    ReminderCategory.ESTUDIO          -> Icons.AutoMirrored.Filled.MenuBook
    else                              -> Icons.AutoMirrored.Filled.Label
}

// ─── Urgency helpers ─────────────────────────────────────────────────────────

private fun urgencyBadgeColor(diffMs: Long): Color = when {
    diffMs < 0                     -> Color(0xFFEF4444) // Vencido
    diffMs <= DateUtils.HOURS_1_MS -> Color(0xFFF97316) // < 1 hora
    diffMs <= DateUtils.HOURS_24_MS-> Color(0xFFF97316) // < 24 h
    diffMs <= DateUtils.DAYS_3_MS  -> Color(0xFFF59E0B) // < 3 días
    else                           -> Color(0xFF10B981) // OK
}

private fun urgencyBadgeLabel(diffMs: Long): String = when {
    diffMs < 0                     -> "VENCIDO"
    diffMs <= DateUtils.HOURS_1_MS -> "¡MENOS DE 1 HORA!"
    diffMs <= DateUtils.HOURS_24_MS-> DateUtils.formatTimeRemaining(diffMs)
    diffMs <= DateUtils.DAYS_3_MS  -> DateUtils.formatTimeRemaining(diffMs)
    else                           -> DateUtils.formatTimeRemaining(diffMs)
}

private fun priorityBarColor(diffMs: Long): Color = when {
    diffMs < 0                     -> Color(0xFFEF4444)
    diffMs <= DateUtils.HOURS_24_MS-> Color(0xFFF97316)
    diffMs <= DateUtils.DAYS_3_MS  -> Color(0xFFF59E0B)
    else                           -> Color(0xFF10B981)
}

// ─── ReminderCard ─────────────────────────────────────────────────────────────

/**
 * Improved card with:
 * - Bold title hierarchy
 * - Soft pill for category (dark text on soft background for legibility)
 * - Colored urgency badge with background
 * - Interactive complete button with filled background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val due     = DateUtils.combineDateAndTime(reminder.date, reminder.time)
    val diffMs  = due - System.currentTimeMillis()
    val barColor  = priorityBarColor(diffMs)
    val catColor  = categoryColor(reminder.category)
    val badgeColor = urgencyBadgeColor(diffMs)
    val badgeLabel = urgencyBadgeLabel(diffMs)

    Card(
        onClick   = onClick,
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Priority accent bar ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(90.dp)
                    .background(barColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // Row 1: category pill + urgency badge
                Row(
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category pill — soft bg, dark text for legibility
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = catColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector        = categoryIcon(reminder.category),
                                contentDescription = null,
                                tint               = catColor,
                                modifier           = Modifier.size(11.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text       = reminder.category.uppercase(),
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = catColor
                            )
                        }
                    }

                    // Urgency badge — colored background
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text       = badgeLabel,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = badgeColor,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Row 2: Bold title
                Text(
                    text       = reminder.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF0F172A),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                // Row 3: Description (subtle)
                if (reminder.description.isNotBlank()) {
                    Text(
                        text     = reminder.description,
                        fontSize = 12.sp,
                        color    = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Row 4: Date + time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Schedule,
                        contentDescription = null,
                        tint               = Color(0xFF94A3B8),
                        modifier           = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = "${DateUtils.formatDate(reminder.date)} • ${DateUtils.formatTime(reminder.time)}",
                        fontSize = 11.sp,
                        color    = Color(0xFF94A3B8)
                    )
                }
            }

            // ── Complete button ───────────────────────────────────────────
            if (onComplete != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF22C55E).copy(alpha = 0.12f))
                        .clickable { onComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Check,
                        contentDescription = "Marcar como completado",
                        tint               = Color(0xFF16A34A),
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─── CategoryChip ─────────────────────────────────────────────────────────────

@Composable
fun CategoryChip(category: String, modifier: Modifier = Modifier) {
    val color = categoryColor(category)
    Surface(
        modifier  = modifier,
        shape     = RoundedCornerShape(50),
        color     = color.copy(alpha = 0.12f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector        = categoryIcon(category),
                contentDescription = null,
                tint               = color,
                modifier           = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text       = category,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color      = color
            )
        }
    }
}

// ─── PriorityIndicator ────────────────────────────────────────────────────────

@Composable
fun PriorityIndicator(reminder: Reminder, modifier: Modifier = Modifier) {
    val due    = DateUtils.combineDateAndTime(reminder.date, reminder.time)
    val diffMs = due - System.currentTimeMillis()
    val color  = priorityBarColor(diffMs)
    val label  = when {
        diffMs < 0                     -> "VENCIDO"
        diffMs <= DateUtils.HOURS_24_MS-> "URGENTE"
        diffMs <= DateUtils.DAYS_3_MS  -> "PRONTO"
        else                           -> "PRÓXIMO"
    }
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(50),
        color    = color.copy(alpha = 0.15f)
    ) {
        Text(
            text       = label,
            fontSize   = 10.sp,
            color      = color,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ─── SectionHeader ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, count: Int = 0, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = title,
            fontSize   = 13.sp,
            color      = Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.weight(1f)
        )
        if (count > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text     = "$count",
                    fontSize = 11.sp,
                    color    = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// ─── EmptyStateView ───────────────────────────────────────────────────────────

@Composable
fun EmptyStateView(
    icon: @Composable () -> Unit,
    message: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(
            text       = message,
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color(0xFF334155)
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text     = subtitle,
                fontSize = 13.sp,
                color    = Color(0xFF94A3B8)
            )
        }
    }
}
