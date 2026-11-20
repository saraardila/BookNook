package com.nawin.booknook.presentation.ui.note

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ReadingMood
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Where the reader is in the book, shown at the top of the note sheet.
 *
 * @property pageCount Total pages, or null when the catalogue does not know it.
 */
data class PageDraft(val currentPage: Int, val pageCount: Int?)

/**
 * "Jot it down" bottom sheet: the page she is on, how the reading left her and a
 * handwritten note.
 *
 * @param bookTitle Shown under the tagline; null for a loose thought.
 * @param page The page stepper, or null to leave it out (no book, or not reading it).
 * @param onSave Page (null when there is no stepper), mood and note text.
 * @param onFinish "I finished it!", or null to hide that button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSheet(
    bookTitle: String?,
    page: PageDraft?,
    onDismiss: () -> Unit,
    onSave: (page: Int?, mood: ReadingMood?, content: String) -> Unit,
    onFinish: ((mood: ReadingMood?, content: String) -> Unit)? = null,
) {
    val colors = LibraryTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    // Slides the sheet away before acting, instead of making it vanish.
    val closeThen: (() -> Unit) -> Unit = { action ->
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = LibraryShapes.bottomSheet,
        containerColor = colors.paper,
        contentColor = colors.ink,
        dragHandle = { SheetHandle() },
    ) {
        NoteSheetContent(
            bookTitle = bookTitle,
            page = page,
            onClose = { closeThen(onDismiss) },
            onSave = { p, mood, text -> closeThen { onSave(p, mood, text) } },
            onFinish = onFinish?.let { finish -> { mood, text -> closeThen { finish(mood, text) } } },
        )
    }
}

/** Stateless body of [NoteSheet]; it keeps the draft until the reader saves it. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteSheetContent(
    bookTitle: String?,
    page: PageDraft?,
    onClose: () -> Unit,
    onSave: (page: Int?, mood: ReadingMood?, content: String) -> Unit,
    onFinish: ((mood: ReadingMood?, content: String) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    var pageText by rememberSaveable { mutableStateOf(page?.currentPage?.toString().orEmpty()) }
    var mood by rememberSaveable { mutableStateOf<ReadingMood?>(null) }
    var content by rememberSaveable { mutableStateOf("") }
    val maxPage = page?.pageCount ?: Int.MAX_VALUE
    val draftPage = page?.let { (pageText.toIntOrNull() ?: it.currentPage).coerceIn(0, maxPage) }

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = LibraryDimens.screenPadding)
            .navigationBarsPadding(),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.note_sheet_tagline), style = type.hand, color = colors.accentDeep)
                Text(bookTitle ?: stringResource(R.string.note_sheet_loose_thought), style = type.heading, color = colors.ink)
            }
            RoundIconButton(Icons.Filled.Close, stringResource(R.string.common_close), onClose)
        }

        if (page != null && draftPage != null) {
            Spacer(Modifier.height(LibraryDimens.spaceXl))
            Text(stringResource(R.string.note_sheet_page_question), style = type.label, color = colors.inkMuted)
            PageStepper(
                page = draftPage,
                pageCount = page.pageCount,
                pageText = pageText,
                onPageTextChange = { pageText = it.filter(Char::isDigit).take(PAGE_DIGITS) },
                onStep = { delta -> pageText = (draftPage + delta).coerceIn(0, maxPage).toString() },
            )
            Spacer(Modifier.height(LibraryDimens.spaceMd))
            LibraryChip(
                pluralStringResource(R.plurals.note_sheet_add_pages, QUICK_PAGES, QUICK_PAGES),
                onClick = { pageText = (draftPage + QUICK_PAGES).coerceIn(0, maxPage).toString() },
            )
        }

        Spacer(Modifier.height(LibraryDimens.spaceXl))
        Text(stringResource(R.string.note_sheet_mood_question), style = type.label, color = colors.inkMuted)
        FlowRow(
            Modifier.padding(top = LibraryDimens.spaceSm),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        ) {
            ReadingMood.entries.forEach { option ->
                LibraryChip(
                    stringResource(option.labelRes),
                    selected = option == mood,
                    onClick = { mood = if (option == mood) null else option },
                )
            }
        }

        Spacer(Modifier.height(LibraryDimens.spaceXl))
        NoteField(content, onValueChange = { content = it })

        Spacer(Modifier.height(LibraryDimens.sectionGap))
        Row(
            Modifier.fillMaxWidth().padding(bottom = LibraryDimens.spaceLg),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        ) {
            PillButton(
                stringResource(R.string.common_save),
                onClick = { onSave(draftPage, mood, content) },
                modifier = Modifier.weight(1f),
            )
            if (onFinish != null) {
                PillButton(
                    stringResource(R.string.note_sheet_finished),
                    onClick = { onFinish(mood, content) },
                    primary = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Minus and plus around the page, which can also be typed, and how far through the book it is. */
@Composable
private fun PageStepper(
    page: Int,
    pageCount: Int?,
    pageText: String,
    onPageTextChange: (String) -> Unit,
    onStep: (Int) -> Unit,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val fieldDescription = stringResource(R.string.note_sheet_page_field)
    Column {
        Row(
            Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.Filled.Remove, stringResource(R.string.cd_page_back), { onStep(-1) })
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BasicTextField(
                    value = pageText,
                    onValueChange = onPageTextChange,
                    textStyle = type.title.copy(color = colors.ink, textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.width(LibraryDimens.pageFieldWidth).semantics { contentDescription = fieldDescription },
                )
                if (pageCount != null && pageCount > 0) {
                    Text(
                        stringResource(R.string.note_sheet_page_of, pageCount, (page * PERCENT / pageCount.toFloat()).roundToInt()),
                        style = type.chip,
                        color = colors.inkMuted,
                    )
                }
            }
            RoundIconButton(Icons.Filled.Add, stringResource(R.string.cd_page_forward), { onStep(1) }, fill = colors.highlight)
        }
        if (pageCount != null && pageCount > 0) {
            ReadingProgress(page.toFloat() / pageCount, Modifier.padding(top = LibraryDimens.spaceMd))
        }
    }
}

/** The note itself, in handwriting on a tinted card. */
@Composable
private fun NoteField(value: String, onValueChange: (String) -> Unit) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val label = stringResource(R.string.note_sheet_note_label)
    Text(label, style = type.label, color = colors.inkMuted)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = type.hand.copy(color = colors.ink),
        minLines = NOTE_MIN_LINES,
        cursorBrush = SolidColor(colors.primary),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = LibraryDimens.spaceSm)
            .clip(LibraryShapes.row)
            .background(colors.primaryLight)
            .border(LibraryDimens.stroke, colors.outline, LibraryShapes.row)
            .padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)
            .semantics { contentDescription = label },
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(stringResource(R.string.note_sheet_note_hint), style = type.hand, color = colors.inkMuted)
                }
                inner()
            }
        },
    )
}

/** The short bar at the top of the sheet that hints it can be dragged down. */
@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .padding(vertical = LibraryDimens.spaceMd)
            .size(LibraryDimens.sheetHandleWidth, LibraryDimens.sheetHandleHeight)
            .clip(LibraryShapes.pill)
            .background(LibraryTheme.colors.outline),
    )
}

/** Name of the mood as shown on its chip. */
@get:StringRes
val ReadingMood.labelRes: Int
    get() = when (this) {
        ReadingMood.SMITTEN -> R.string.mood_smitten
        ReadingMood.SHOCKED -> R.string.mood_shocked
        ReadingMood.IN_TEARS -> R.string.mood_in_tears
        ReadingMood.HOOKED -> R.string.mood_hooked
    }

private const val QUICK_PAGES = 10
private const val PAGE_DIGITS = 5
private const val PERCENT = 100
private const val NOTE_MIN_LINES = 3

// region Previews

@Preview(name = "Note sheet · Spanish", locale = "es", widthDp = 390, heightDp = 700, showBackground = true)
@Preview(name = "Note sheet · English", locale = "en", widthDp = 390, heightDp = 700, showBackground = true)
@Composable
private fun NoteSheetPreview() = LibraryTheme {
    Box(Modifier.background(LibraryTheme.colors.paper)) {
        NoteSheetContent("Throne of Glass", PageDraft(240, 404), {}, { _, _, _ -> }, { _, _ -> })
    }
}

// endregion
