package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nawin.booknook.R
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import java.time.Month
import java.time.format.TextStyle as DateTextStyle

/**
 * Sample data for previews only. Book titles, names and notes are user data,
 * not UI copy, so they are not string resources.
 */
private object PreviewSample {
    const val READER_NAME = "Sara"
    const val BOOK_TITLE = "Throne of Glass"
    const val READER_NOTE = "cap. 31 · ¡no puedo parar!"
    const val LOAN_NUMBER = 14
    const val PROGRESS = 0.52f
    const val SHELF_CODE = "ROM"
    const val AUTHOR_CODE = "MAA"
}

@Composable
private fun shortMonth(month: Month): String =
    month.getDisplayName(DateTextStyle.SHORT, LocalConfiguration.current.locales[0]).uppercase()

@Preview(name = "Components · English", locale = "en", widthDp = 390, heightDp = 900, showBackground = true)
@Preview(name = "Components · Spanish", locale = "es", widthDp = 390, heightDp = 900, showBackground = true)
@Composable
private fun LibraryComponentsPreview() = LibraryTheme {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(Modifier.background(colors.background).fillMaxWidth()) {
        Box {
            LaceHeader(height = 120.dp)
            Column(Modifier.padding(start = LibraryDimens.screenPadding, top = 36.dp)) {
                Text(stringResource(R.string.home_greeting_afternoon, PreviewSample.READER_NAME), style = type.hand, color = colors.stamp)
                Text(stringResource(R.string.home_my_library), style = type.title, color = colors.ink)
            }
        }
        Spacer(Modifier.height(28.dp))
        Column(
            Modifier.padding(horizontal = LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXl),
        ) {
            Box {
                PaperCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(LibraryDimens.spaceLg - LibraryDimens.spaceXxs)) {
                        Text(stringResource(R.string.loan_card_title, PreviewSample.LOAN_NUMBER), style = type.label, color = colors.primary)
                        Text(PreviewSample.BOOK_TITLE, style = type.titleSmall, color = colors.ink, modifier = Modifier.padding(top = LibraryDimens.spaceSm))
                        Text(PreviewSample.READER_NOTE, style = type.handSmall, color = colors.washi)
                        Spacer(Modifier.height(LibraryDimens.spaceSm))
                        ReadingProgress(PreviewSample.PROGRESS, Modifier.width(200.dp))
                    }
                }
                WashiTape(Modifier.offset(x = 24.dp, y = (-9).dp))
                InkStamp(
                    top = stringResource(R.string.stamp_return),
                    middle = "30",
                    bottom = shortMonth(Month.NOVEMBER),
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-18).dp),
                    size = 64.dp,
                    rotation = 12f,
                    background = colors.paper,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd), verticalAlignment = Alignment.CenterVertically) {
                Rosette(fill = colors.highlight, rotation = -6f) {
                    Text(stringResource(R.string.licence_progress, 3, 5), style = type.label, color = colors.ink)
                }
                Rosette(fill = colors.stamp, ring = colors.onPrimary, rotation = 4f) {
                    Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.cd_licence_earned), tint = colors.onPrimary)
                }
                Rosette(dashed = true) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.cd_licence_locked), tint = colors.inkMuted)
                }
                InkStamp(top = stringResource(R.string.stamp_read), middle = "02", bottom = shortMonth(Month.OCTOBER))
                WaxSeal(size = 56.dp) {
                    Icon(Icons.Filled.Favorite, contentDescription = stringResource(R.string.cd_wax_seal), tint = colors.onPrimary)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm), verticalAlignment = Alignment.CenterVertically) {
                ShelfLabel(stringResource(R.string.shelf_romantasy))
                Daisy()
                Daisy(petal = colors.primaryMid)
                Sprig()
            }
            ArchFrame(Modifier.size(width = 200.dp, height = 170.dp)) {
                Column(Modifier.align(Alignment.BottomCenter).padding(LibraryDimens.spaceLg)) {
                    Box(Modifier.align(Alignment.End)) { SpineLabel(PreviewSample.SHELF_CODE, PreviewSample.AUTHOR_CODE) }
                    Spacer(Modifier.height(LibraryDimens.spaceSm))
                    Shelf()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
                LibraryChip(stringResource(R.string.shelf_romantasy), selected = true, onClick = {})
                LibraryChip(stringResource(R.string.shelf_fantasy), onClick = {})
                LibraryChip(stringResource(R.string.trope_slow_burn))
            }
            PillButton(stringResource(R.string.onboarding_get_card), onClick = {}, modifier = Modifier.fillMaxWidth())
            PillButton(stringResource(R.string.onboarding_have_card), onClick = {}, primary = false, modifier = Modifier.fillMaxWidth())
            PillNavBar(
                tabs = listOf(
                    NavTab(stringResource(R.string.nav_home), Icons.Outlined.Home),
                    NavTab(stringResource(R.string.nav_shelves), Icons.AutoMirrored.Outlined.MenuBook),
                    NavTab(stringResource(R.string.nav_log), Icons.Outlined.Add, highlighted = true),
                    NavTab(stringResource(R.string.nav_search), Icons.Outlined.Search),
                    NavTab(stringResource(R.string.nav_card), Icons.Outlined.Badge),
                ),
                selectedIndex = 0,
                onTabSelected = {},
            )
        }
    }
}
