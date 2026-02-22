package com.reminderpay.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.reminderpay.app.data.model.*
import com.reminderpay.app.ui.theme.*
import com.reminderpay.app.utils.DateUtils

// ─── Priority color helpers ──────────────────────────────────────────────────

fun reminderPriorityColor(reminder: Reminder): Color {
    val due  = DateUtils.combineDateAndTime(reminder.date, reminder.time)
    val diff = due - System.currentTimeMillis()
    return when {
        diff <= DateUtils.HOURS_24_MS -> UrgentRed
        diff <= DateUtils.DAYS_3_MS   -> SoonOrange
        else                          -> UpcomingBlue
    }
}

fun reminderPriorityBgColor(reminder: Reminder): Color {
    val due  = DateUtils.combineDateAndTime(reminder.date, reminder.time)
    val diff = due - System.currentTimeMillis()
    return when {
        diff <= DateUtils.HOURS_24_MS -> UrgentRedBg
        diff <= DateUtils.DAYS_3_MS   -> SoonOrangeBg
        else                          -> UpcomingBlueBg
    }
}

fun categoryColor(category: String): Color = when (category) {
    ReminderCategory.PAYMENT  -> PaymentColor
    ReminderCategory.PERSONAL -> PersonalColor
    ReminderCategory.WORK     -> WorkColor
    ReminderCategory.STUDY    -> StudyColor
    else                      -> OtherColor
}

fun categoryIcon(category: String) = when (category) {
    ReminderCategory.PAYMENT  -> Icons.Default.CreditCard
    ReminderCategory.PERSONAL -> Icons.Default.Person
    ReminderCategory.WORK     -> Icons.Default.Work
    ReminderCategory.STUDY    -> Icons.Default.MenuBook
    else                      -> Icons.Default.Label
}

// ─── ReminderCard ─────────────────────────────────────────────────────────────

/**
 * Main card displayed in list views. Shows priority indicator, category chip,
 * title, date, and time-remaining label.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val priorityColor = reminderPriorityColor(reminder)
    val prioBg        = reminderPriorityBgColor(reminder)
    val due           = DateUtils.combineDateAndTime(reminder.date, reminder.time)
    val timeLabel     = DateUtils.formatTimeRemaining(due - System.currentTimeMillis())

    Card(
        onClick      = onClick,
        modifier     = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape        = RoundedCornerShape(16.dp),
        elevation    = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors       = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority side bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(88.dp)
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CategoryChip(category = reminder.category)
                    Text(
                        text  = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text     = reminder.title,
                    style    = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (reminder.description.isNotBlank()) {
                    Text(
                        text     = reminder.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.Schedule,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text  = "${DateUtils.formatDate(reminder.date)} • ${DateUtils.formatTime(reminder.time)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Optional complete button
            if (onComplete != null) {
                IconButton(onClick = onComplete, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(
                        imageVector        = Icons.Default.CheckCircleOutline,
                        contentDescription = "Mark as complete",
                        tint               = CompletedGreen
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
                text  = category,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── PriorityIndicator ────────────────────────────────────────────────────────

@Composable
fun PriorityIndicator(reminder: Reminder, modifier: Modifier = Modifier) {
    val color = reminderPriorityColor(reminder)
    val label = when {
        reminderPriorityBgColor(reminder) == UrgentRedBg  -> "URGENTE"
        reminderPriorityBgColor(reminder) == SoonOrangeBg -> "PRONTO"
        else                                               -> "PRÓXIMO"
    }
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(50),
        color    = color.copy(alpha = 0.15f)
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.labelSmall,
            color     = color,
            fontWeight= FontWeight.Bold,
            modifier  = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ─── SectionHeader ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, count: Int = 0, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (count > 0) {
            Badge { Text("$count") }
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
        modifier              = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
