package com.nawin.booknook.presentation.ui.readingcard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.CardStamp
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.ZoneId

/** "My reading card": one ink stamp per book finished or put down, [ReadingCard.SLOTS] per card. */
@Composable
fun ReadingCardScreen(
    navController: NavController,
    viewModel: ReadingCardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReadingCardContent(state.card, onBack = { navController.popBackStack() })
}

/** Stateless layout of [ReadingCardScreen]; [card] is null while loading. */
@Composable
fun ReadingCardContent(card: ReadingCard?, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
            Row(
                Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = statusBarTop + LibraryDimens.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            ) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onBack)
                Text(stringResource(R.string.reading_card_tagline), style = type.hand, color = colors.stamp)
            }
        }
        Box(Modifier.padding(horizontal = LibraryDimens.screenPadding).offset(y = -LibraryDimens.sectionGap)) {
            if (card == null) ShelfLoader(Modifier.align(Alignment.Center)) else StampCard(card)
        }
    }
}

/** The card itself: title and number, the grid of stamps and empty spaces, and how many are left. */
@Composable
internal fun StampCard(card: ReadingCard) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Box {
        PaperCard(Modifier.fillMaxWidth(), cornerRadius = LibraryDimens.radiusSheet) {
            Column(Modifier.padding(horizontal = LibraryDimens.spaceMd + LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceLg)) {
                Text(stringResource(R.string.reading_card_title), style = type.heading, color = colors.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(
                    stringResource(R.string.reading_card_number, card.number),
                    style = type.label,
                    color = colors.inkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceXxs),
                )
                HorizontalDivider(
                    Modifier.padding(horizontal = LibraryDimens.spaceSm, vertical = LibraryDimens.spaceMd),
                    thickness = LibraryDimens.stroke,
                    color = colors.ink.copy(alpha = RULE_ALPHA),
                )
                (0 until ReadingCard.SLOTS).chunked(COLUMNS).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceXs + LibraryDimens.spaceXxs),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        row.forEach { slot ->
                            val stamp = card.stamps.getOrNull(slot)
                            if (stamp != null) CardStampView(stamp, slot) else EmptySlot(slot + 1)
                        }
                    }
                }
                DottedRule(Modifier.padding(top = LibraryDimens.spaceMd))
                Row(
                    Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceSm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(pluralStringResource(R.plurals.reading_card_count, ReadingCard.SLOTS, card.stamps.size, ReadingCard.SLOTS), style = type.button, color = colors.ink)
                    Text(
                        if (card.complete) stringResource(R.string.reading_card_complete)
                        else pluralStringResource(R.plurals.reading_card_left, card.remaining, card.remaining),
                        style = type.handSmall,
                        color = colors.accentDeep,
                        textAlign = TextAlign.End,
                        // Takes what is left after the count, wrapping instead of running past the card.
                        modifier = Modifier.weight(1f).padding(start = LibraryDimens.spaceSm),
                    )
                }
            }
        }
        WashiTape(Modifier.offset(x = LibraryDimens.spaceXl + LibraryDimens.spaceSm, y = -LibraryDimens.spaceSm))
    }
}

/** A stamp in the library's inks, each slightly askew like a real one; put-down books are crossed out. */
@Composable
private fun CardStampView(stamp: CardStamp, slot: Int) {
    val colors = LibraryTheme.colors
    val inks = listOf(colors.stamp, colors.primary, colors.accentDeep)
    DateStamp(
        label = stringResource(if (stamp.abandoned) R.string.stamp_put_down else R.string.stamp_read),
        epochMillis = stamp.date,
        size = LibraryDimens.stampSize,
        rotation = STAMP_TILTS[slot % STAMP_TILTS.size],
        color = if (stamp.abandoned) colors.inkMuted else inks[slot % inks.size],
        struck = stamp.abandoned,
    )
}

/** A dashed circle with the number of the space still to fill. */
@Composable
private fun EmptySlot(number: Int) {
    val colors = LibraryTheme.colors
    val description = stringResource(R.string.cd_card_slot_empty, number)
    Box(
        Modifier.size(LibraryDimens.stampSize).clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                colors.outline,
                radius = size.minDimension / 2 - LibraryDimens.stroke.toPx(),
                style = Stroke(LibraryDimens.stroke.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH.toPx(), GAP.toPx()))),
            )
        }
        Text(number.toString(), style = LibraryTheme.typography.hand, color = colors.outline)
    }
}

@Composable
private fun DottedRule(modifier: Modifier = Modifier, color: Color = LibraryTheme.colors.outline) {
    Canvas(modifier.fillMaxWidth().height(LibraryDimens.stroke)) {
        drawLine(
            color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = LibraryDimens.stroke.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DOT.toPx(), DOT.toPx())),
        )
    }
}

private const val COLUMNS = 4
private const val RULE_ALPHA = 0.55f
private val STAMP_TILTS = floatArrayOf(-8f, 6f, -3f, 9f, -5f, 4f, -7f, 5f)
private val DASH = 3.dp
private val GAP = 2.5.dp
private val DOT = 2.dp

// region Previews

@Preview(name = "Reading card · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Preview(name = "Reading card · English", locale = "en", widthDp = 390, heightDp = 844)
@Composable
private fun ReadingCardPreview() = LibraryTheme {
    val zone = ZoneId.systemDefault()
    fun day(month: Int, dayOfMonth: Int) = LocalDate.of(2026, month, dayOfMonth).atStartOfDay(zone).toInstant().toEpochMilli()
    ReadingCardContent(
        ReadingCard(
            number = 1,
            stamps = listOf(
                CardStamp("1", "Heir of Fire", day(10, 2), abandoned = false),
                CardStamp("2", "Fourth Wing", day(10, 19), abandoned = false),
                CardStamp("3", "Six of Crows", day(10, 22), abandoned = true),
                CardStamp("4", "A Court of Mist and Fury", day(11, 1), abandoned = false),
                CardStamp("5", "Throne of Glass", day(11, 11), abandoned = false),
            ),
        ),
        onBack = {},
    )
}

// endregion
