package com.nawin.booknook.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import com.nawin.booknook.R
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api as ExperimentalMaterial3Api1

@OptIn(ExperimentalMaterial3Api1::class)
@Composable
fun DateSection(
    startDate: Long?,
    finishDate: Long?,
    onStartDateChange: (Long) -> Unit,
    onFinishDateChange: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showFinishPicker by remember { mutableStateOf(false) }

    // DatePicker dialogs
    if (showStartPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onStartDateChange(it) }
                    showStartPicker = false
                }) {
                    Text(stringResource(R.string.detail_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showFinishPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = finishDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showFinishPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onFinishDateChange(it) }
                    showFinishPicker = false
                }) {
                    Text(stringResource(R.string.detail_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishPicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_dates),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Start date
            DateChip(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.detail_start_date),
                date = startDate?.let { dateFormat.format(Date(it)) },
                emoji = "📖",
                onClick = { showStartPicker = true }
            )

            // Finish date
            DateChip(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.detail_finish_date),
                date = finishDate?.let { dateFormat.format(Date(it)) },
                emoji = "✅",
                onClick = { showFinishPicker = true }
            )
        }
    }
}

@Composable
fun DateChip(
    modifier: Modifier = Modifier,
    label: String,
    date: String?,
    emoji: String,
    onClick: () -> Unit
) {
    CozyCard(
        modifier = modifier,
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = date ?: stringResource(R.string.detail_date_not_set),
                style = MaterialTheme.typography.labelLarge,
                color = if (date != null)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}