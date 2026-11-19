package com.nawin.booknook.presentation.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingReminder
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryToggle
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.SegmentedPills
import com.nawin.booknook.presentation.components.library.WaxSeal
import com.nawin.booknook.presentation.components.library.drawFlatShadow
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.palette
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Settings tab: language, shelf theme, reading reminder and the local account. */
@Composable
fun SettingsScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var notificationsDenied by rememberSaveable { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsDenied = !granted
        if (granted) viewModel.setReminderEnabled(true)
    }
    val exportDocument = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(CSV_MIME_TYPE)) { uri ->
        uri?.let { viewModel.exportLibrary(it.toString()) }
    }

    SettingsContent(
        state = state,
        language = AppLanguage.current(),
        notificationsDenied = notificationsDenied,
        onLanguageSelected = AppLanguage::apply,
        onThemeSelected = viewModel::setTheme,
        onReminderToggled = { enabled ->
            val needsPermission = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationsDenied = false
                viewModel.setReminderEnabled(enabled)
            }
        },
        onReminderTimeChanged = viewModel::setReminderTime,
        onNameChanged = viewModel::setReaderName,
        onExport = { exportDocument.launch(EXPORT_FILE_PREFIX + LocalDate.now() + EXPORT_FILE_EXTENSION) },
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    language: AppLanguage?,
    notificationsDenied: Boolean,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (ShelfTheme) -> Unit,
    onReminderToggled: (Boolean) -> Unit,
    onReminderTimeChanged: (Int, Int) -> Unit,
    onNameChanged: (String) -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Box {
            LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop, color = colors.accentLight)
            Column(
                Modifier.padding(
                    start = LibraryDimens.screenPadding,
                    end = LibraryDimens.screenPadding,
                    top = statusBarTop + LibraryDimens.spaceXl,
                ),
            ) {
                Text(stringResource(R.string.settings_tagline), style = type.hand, color = colors.stamp)
                Text(stringResource(R.string.settings_title), style = type.title, color = colors.ink)
            }
        }

        Column(
            Modifier.padding(
                start = LibraryDimens.screenPadding,
                end = LibraryDimens.screenPadding,
                top = LibraryDimens.sectionGap,
                bottom = LibraryDimens.sectionGap,
            ),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.sectionGap - LibraryDimens.spaceSm),
        ) {
            SettingsSection(stringResource(R.string.settings_language)) {
                LanguagePicker(language, onLanguageSelected)
            }
            SettingsSection(stringResource(R.string.settings_theme)) {
                ThemePicker(state.theme, onThemeSelected)
            }
            SettingsSection(stringResource(R.string.settings_notifications)) {
                ReminderCard(state.reminder, onReminderToggled, onReminderTimeChanged)
                if (notificationsDenied) {
                    Text(
                        stringResource(R.string.settings_notifications_denied),
                        style = type.caption,
                        color = colors.stamp,
                        modifier = Modifier.padding(horizontal = LibraryDimens.spaceXs),
                    )
                }
            }
            SettingsSection(stringResource(R.string.settings_account)) {
                AccountCard(state.profile, onNameChanged, onExport)
                state.exportStatus?.let { ExportMessage(it) }
            }
        }
    }
}

/** A small uppercase label over its settings. */
@Composable
private fun SettingsSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
        Text(label, style = LibraryTheme.typography.label, color = LibraryTheme.colors.inkMuted)
        content()
    }
}

// region Language

@Composable
private fun LanguagePicker(current: AppLanguage?, onSelect: (AppLanguage) -> Unit) {
    val languages = AppLanguage.entries
    SegmentedPills(
        options = languages.map { stringResource(it.nameRes) },
        selectedIndex = current?.let(languages::indexOf),
        onSelect = { index -> languages[index].takeIf { it != current }?.let(onSelect) },
    )
    Text(
        stringResource(R.string.settings_language_note),
        style = LibraryTheme.typography.caption,
        color = LibraryTheme.colors.inkMuted,
        modifier = Modifier.padding(horizontal = LibraryDimens.spaceXs),
    )
}

private val AppLanguage.nameRes: Int
    get() = when (this) {
        AppLanguage.SPANISH -> R.string.language_spanish
        AppLanguage.ENGLISH -> R.string.language_english
    }

// endregion

// region Theme

/** The six shelf themes as miniature screens; the chosen one wears a wax seal. */
@Composable
private fun ThemePicker(selected: ShelfTheme, onSelect: (ShelfTheme) -> Unit) {
    Row(
        Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
    ) {
        ShelfTheme.entries.forEach { theme ->
            ThemeMini(
                theme = theme,
                selected = theme == selected,
                onClick = { onSelect(theme) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeMini(theme: ShelfTheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val palette = theme.palette
    val shape = LibraryShapes.label
    Column(
        modifier
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(THEME_MINI_ASPECT)
                .drawBehind { drawFlatShadow(colors.shadow, CornerRadius(LibraryDimens.radiusLabel.toPx()), LibraryDimens.shadowOffsetSmall) }
                .clip(shape)
                .background(palette.background)
                .border(
                    if (selected) LibraryDimens.strokeStamp else LibraryDimens.stroke,
                    if (selected) colors.ink else colors.outline,
                    shape,
                ),
        ) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                // A tiny screen: header band, a white card and two colour dots.
                Box(Modifier.fillMaxWidth().weight(THEME_MINI_HEADER).background(palette.primary))
                Spacer(Modifier.weight(THEME_MINI_GAP))
                Box(
                    Modifier
                        .fillMaxWidth(THEME_MINI_CARD_WIDTH)
                        .weight(THEME_MINI_CARD)
                        .background(colors.photoFrame, LibraryShapes.cover)
                        .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.cover),
                )
                Spacer(Modifier.weight(THEME_MINI_GAP))
                Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXxs)) {
                    Box(
                        Modifier
                            .size(LibraryDimens.themeMiniDot)
                            .background(palette.tint, LibraryShapes.pill)
                            .border(LibraryDimens.strokeHairline, colors.outline, LibraryShapes.pill),
                    )
                    Box(Modifier.size(LibraryDimens.themeMiniDot).background(palette.primary, LibraryShapes.pill))
                }
                Spacer(Modifier.weight(THEME_MINI_FOOT))
            }
            if (selected) {
                WaxSeal(
                    Modifier.align(Alignment.TopEnd).padding(LibraryDimens.spaceXxs),
                    size = LibraryDimens.themeMiniSeal,
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(LibraryDimens.spaceMd))
                }
            }
        }
        Text(
            stringResource(theme.nameRes),
            style = LibraryTheme.typography.navLabel,
            color = if (selected) colors.ink else colors.inkMuted,
            maxLines = 1,
        )
    }
}

private val ShelfTheme.nameRes: Int
    get() = when (this) {
        ShelfTheme.STANDARD -> R.string.theme_standard
        ShelfTheme.ROMANTASY -> R.string.theme_romantasy
        ShelfTheme.WITCHY -> R.string.theme_witchy
        ShelfTheme.COWBOY -> R.string.theme_cowboy
        ShelfTheme.ROMANCE -> R.string.theme_romance
        ShelfTheme.THRILLER -> R.string.theme_thriller
    }

// Proportions of the miniature screen, from the design (height = 1.45 × width).
private const val THEME_MINI_ASPECT = 1f / 1.45f
private const val THEME_MINI_HEADER = 0.30f
private const val THEME_MINI_GAP = 0.08f
private const val THEME_MINI_CARD = 0.22f
private const val THEME_MINI_CARD_WIDTH = 0.72f
private const val THEME_MINI_FOOT = 0.22f

// endregion

// region Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(
    reminder: ReadingReminder,
    onToggle: (Boolean) -> Unit,
    onTimeChanged: (Int, Int) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val title = stringResource(R.string.settings_reminder)
    val time = remember(reminder.hour, reminder.minute, locale) {
        LocalTime.of(reminder.hour, reminder.minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    }

    if (pickingTime) {
        val pickerState = rememberTimePickerState(reminder.hour, reminder.minute, is24Hour = DateFormat.is24HourFormat(context))
        AlertDialog(
            onDismissRequest = { pickingTime = false },
            title = { Text(stringResource(R.string.settings_reminder_pick_time)) },
            text = { TimePicker(pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChanged(pickerState.hour, pickerState.minute)
                    pickingTime = false
                }) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { pickingTime = false }) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    SettingsCard {
        SettingRow(
            title = title,
            subtitle = stringResource(R.string.settings_reminder_time, time),
            onClick = { pickingTime = true },
            onClickLabel = stringResource(R.string.settings_reminder_pick_time),
        ) {
            LibraryToggle(checked = reminder.enabled, onCheckedChange = onToggle, contentDescription = title)
        }
    }
}

// endregion

// region Account

@Composable
private fun AccountCard(profile: ReaderProfile, onNameChanged: (String) -> Unit, onExport: () -> Unit) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    var editingName by rememberSaveable { mutableStateOf(false) }
    val since = profile.memberSince?.let { millis ->
        remember(millis, locale) {
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN, locale))
        }
    }

    if (editingName) {
        NameDialog(profile.name, onSave = { onNameChanged(it); editingName = false }, onDismiss = { editingName = false })
    }

    SettingsCard {
        SettingRow(
            title = profile.name.ifBlank { stringResource(R.string.settings_your_name) },
            titleColor = if (profile.name.isBlank()) colors.inkMuted else colors.ink,
            subtitle = since?.let { stringResource(R.string.settings_reader_since, it) },
            onClick = { editingName = true },
            onClickLabel = stringResource(R.string.settings_your_name),
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.inkMuted)
        }
        HorizontalDivider(thickness = LibraryDimens.strokeHairline, color = colors.outline)
        SettingRow(title = stringResource(R.string.settings_export), onClick = onExport) {
            Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = colors.inkMuted)
        }
    }
}

@Composable
private fun NameDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_your_name)) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.common_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

@Composable
private fun ExportMessage(status: ExportStatus) {
    val text = when (status) {
        ExportStatus.Running -> stringResource(R.string.settings_exporting)
        is ExportStatus.Done -> pluralStringResource(R.plurals.settings_exported, status.bookCount, status.bookCount)
        ExportStatus.Failed -> stringResource(R.string.settings_export_failed)
    }
    Text(
        text,
        style = LibraryTheme.typography.handSmall,
        color = if (status is ExportStatus.Failed) LibraryTheme.colors.stamp else LibraryTheme.colors.accentDeep,
        modifier = Modifier.padding(horizontal = LibraryDimens.spaceXs),
    )
}

// endregion

// region Rows

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    PaperCard(Modifier.fillMaxWidth(), cornerRadius = LibraryDimens.radiusRow) {
        Column(content = content)
    }
}

/** A row in a settings card: title, optional subtitle and a trailing control. */
@Composable
private fun SettingRow(
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = LibraryTheme.colors.ink,
    onClickLabel: String? = null,
    trailing: @Composable () -> Unit,
) {
    val type = LibraryTheme.typography
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = LibraryDimens.touchTarget)
            .padding(start = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, end = LibraryDimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                .padding(vertical = LibraryDimens.spaceMd - LibraryDimens.spaceXxs),
        ) {
            Text(title, style = type.button, color = titleColor)
            if (subtitle != null) {
                Text(subtitle, style = type.caption, color = LibraryTheme.colors.inkMuted)
            }
        }
        trailing()
    }
}

// endregion

private const val CSV_MIME_TYPE = "text/csv"
private const val EXPORT_FILE_PREFIX = "booknook-"
private const val EXPORT_FILE_EXTENSION = ".csv"

/** Short month and year, e.g. "nov 2026"; LLL is the stand-alone month form. */
private const val MONTH_YEAR_PATTERN = "LLL yyyy"

@Preview(name = "Settings · Spanish", locale = "es", widthDp = 360, heightDp = 900, showBackground = true)
@Preview(name = "Settings · English", locale = "en", widthDp = 360, heightDp = 900, showBackground = true)
@Composable
private fun SettingsPreview() = LibraryTheme {
    SettingsContent(
        state = SettingsUiState(
            theme = ShelfTheme.ROMANTASY,
            reminder = ReadingReminder(enabled = true),
            profile = ReaderProfile(name = "Sara", memberSince = 1_793_620_800_000),
            exportStatus = ExportStatus.Done(7),
        ),
        language = AppLanguage.SPANISH,
        notificationsDenied = false,
        onLanguageSelected = {},
        onThemeSelected = {},
        onReminderToggled = {},
        onReminderTimeChanged = { _, _ -> },
        onNameChanged = {},
        onExport = {},
    )
}
