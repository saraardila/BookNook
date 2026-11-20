package com.nawin.booknook.presentation.ui.progress

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ReadingStreak
import com.nawin.booknook.domain.model.ShelfLicence
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.Rosette
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.components.library.StreakWeek
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.code
import com.nawin.booknook.presentation.theme.nameRes
import com.nawin.booknook.presentation.theme.palette
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate

/** "Your progress": the reading streak with its record and the licence of each shelf. */
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressContent(
        state,
        onBack = { navController.popBackStack() },
        onOpenReadingCard = { navController.navigate(Screen.ReadingCard.route) },
    )
}

/** Stateless layout of [ProgressScreen]. */
@Composable
fun ProgressContent(
    state: ProgressUiState,
    onBack: () -> Unit,
    onOpenReadingCard: () -> Unit,
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
                    top = statusBarTop + LibraryDimens.spaceSm,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
                    RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onBack)
                    Text(stringResource(R.string.progress_tagline), style = type.hand, color = colors.stamp)
                }
                Text(stringResource(R.string.progress_title), style = type.title, color = colors.ink)
            }
        }

        Column(
            Modifier.padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.sectionGap),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.sectionGap - LibraryDimens.spaceSm),
        ) {
            if (state.isLoading) {
                ShelfLoader(Modifier.align(Alignment.CenterHorizontally))
            } else {
                StreakSummary(state.streak, state.today)
                Licences(state.licences)
                PillButton(
                    stringResource(R.string.progress_see_card),
                    onClick = onOpenReadingCard,
                    primary = false,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Big day count, the record, this week and the small rule of how a day counts. */
@Composable
private fun StreakSummary(streak: ReadingStreak, today: LocalDate) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    PaperCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceMd)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
                    Text(streak.current.toString(), style = type.title, color = colors.ink)
                    Text(
                        pluralStringResource(R.plurals.progress_streak_days, streak.current),
                        style = type.button,
                        color = colors.ink,
                        modifier = Modifier.padding(bottom = LibraryDimens.spaceSm),
                    )
                }
                Text(stringResource(R.string.progress_record, streak.record), style = type.handSmall, color = colors.accentDeep)
            }
            Spacer(Modifier.height(LibraryDimens.spaceSm))
            StreakWeek(today = today, daysRead = streak.daysRead, daySize = LibraryDimens.streakDayLarge)
            Spacer(Modifier.height(LibraryDimens.spaceSm))
            Text(
                pluralStringResource(R.plurals.streak_today_hint, ReadingStreak.MIN_PAGES_PER_DAY, ReadingStreak.MIN_PAGES_PER_DAY),
                style = type.handSmall,
                color = colors.inkMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

/** One rosette per genre shelf: earned, on its way or not started. */
@Composable
private fun Licences(licences: List<ShelfLicence>) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.progress_licences), style = type.label, color = colors.inkMuted)
            Text(
                pluralStringResource(R.plurals.progress_licences_hint, ShelfLicence.BOOKS_PER_LICENCE, ShelfLicence.BOOKS_PER_LICENCE),
                style = type.handSmall,
                color = colors.accentDeep,
            )
        }
        // Inset a little: the scalloped edge of a rosette reaches past its box.
        Row(Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.spaceXs), horizontalArrangement = Arrangement.SpaceBetween) {
            licences.forEachIndexed { index, licence -> LicenceBadge(licence, ROSETTE_TILTS[index % ROSETTE_TILTS.size]) }
        }
    }
}

@Composable
private fun LicenceBadge(licence: ShelfLicence, rotation: Float) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val palette = licence.shelf.palette
    val name = stringResource(licence.shelf.nameRes)
    val description = if (licence.earned) {
        stringResource(R.string.cd_licence_shelf_earned, name)
    } else {
        // The noun agrees with the total ("2 of 5 books"), so the quantity is the total.
        pluralStringResource(R.plurals.cd_licence_progress, ShelfLicence.BOOKS_PER_LICENCE, name, licence.progress, ShelfLicence.BOOKS_PER_LICENCE)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        when {
            licence.earned -> Rosette(fill = palette.primary, rotation = rotation, ring = colors.onPrimary) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(LibraryDimens.streakDayCheck))
            }
            licence.booksRead > 0 -> Rosette(fill = palette.tint, rotation = rotation) {
                Text(licence.shelf.code, style = type.label, color = palette.primary)
            }
            else -> Rosette(dashed = true, rotation = rotation) {
                Text(stringResource(R.string.progress_licence_unknown), style = type.hand, color = colors.inkMuted)
            }
        }
        Spacer(Modifier.height(LibraryDimens.spaceXs))
        Text(name, style = type.label, color = colors.ink)
        Text(
            if (licence.earned) stringResource(R.string.progress_licence_earned)
            else stringResource(R.string.licence_progress, licence.progress, ShelfLicence.BOOKS_PER_LICENCE),
            style = type.caption,
            color = colors.inkMuted,
        )
    }
}

private val ROSETTE_TILTS = floatArrayOf(-6f, 4f, -3f, 6f, -4f)

// region Previews

@Preview(name = "Progress · Spanish", locale = "es", widthDp = 390, heightDp = 844)
@Preview(name = "Progress · English", locale = "en", widthDp = 390, heightDp = 844)
@Composable
private fun ProgressPreview() = LibraryTheme {
    val today = LocalDate.of(2026, 11, 20)
    val day = today.toEpochDay()
    ProgressContent(
        ProgressUiState(
            streak = ReadingStreak(current = 6, record = 12, daysRead = (day - 5..day).toSet()),
            licences = listOf(
                ShelfLicence(ShelfTheme.ROMANTASY, 5),
                ShelfLicence(ShelfTheme.WITCHY, 1),
                ShelfLicence(ShelfTheme.COWBOY, 0),
                ShelfLicence(ShelfTheme.ROMANCE, 3),
                ShelfLicence(ShelfTheme.THRILLER, 2),
            ),
            today = today,
            isLoading = false,
        ),
        onBack = {},
        onOpenReadingCard = {},
    )
}

// endregion
