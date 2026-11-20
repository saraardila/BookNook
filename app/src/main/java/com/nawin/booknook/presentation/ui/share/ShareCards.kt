package com.nawin.booknook.presentation.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.PlaceholderCover
import com.nawin.booknook.presentation.components.library.DateStamp
import com.nawin.booknook.presentation.components.library.InkStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.Polaroid
import com.nawin.booknook.presentation.components.library.RATING_MAX
import com.nawin.booknook.presentation.components.library.ReadingProgress
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.StarRating
import com.nawin.booknook.presentation.components.library.WashiTape
import com.nawin.booknook.presentation.components.library.WashiPattern
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.ui.onboarding.LibraryCard
import com.nawin.booknook.presentation.ui.onboarding.libraryCardData
import com.nawin.booknook.presentation.ui.readingcard.StampCard
import java.time.YearMonth
import java.time.format.TextStyle

/**
 * The picture that gets shared, laid out at [LibraryDimens.shareCardWidth] x
 * [LibraryDimens.shareCardHeight]; the screen scales it to fit.
 */
@Composable
fun ShareCard(state: ShareUiState, modifier: Modifier = Modifier) {
    val book = state.book ?: return
    when (state.selected) {
        ShareCardType.PROGRESS -> ProgressCard(book, state.latestNote, state.month, state.profile, modifier)
        ShareCardType.LIBRARY_CARD -> LibraryCardShare(state.profile, modifier)
        ShareCardType.READING_CARD -> state.readingCard?.let { ReadingCardShare(it, state.month, state.profile, modifier) }
        ShareCardType.REVIEW -> ReviewCard(book, state.profile, modifier)
    }
}

/** The label of each card in the picker. */
val ShareCardType.labelRes: Int
    get() = when (this) {
        ShareCardType.PROGRESS -> R.string.share_type_progress
        ShareCardType.LIBRARY_CARD -> R.string.share_type_library_card
        ShareCardType.READING_CARD -> R.string.share_type_reading_card
        ShareCardType.REVIEW -> R.string.share_type_review
    }

/** "What I'm reading · November": the cover with its stamp, progress and the latest note. */
@Composable
private fun ProgressCard(book: Book, latestNote: String?, month: YearMonth?, profile: ReaderProfile, modifier: Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    CardFrame(
        tagline = stringResource(book.status.progressTaglineRes),
        title = month?.month?.getDisplayName(TextStyle.FULL_STANDALONE, locale),
        profile = profile,
        modifier = modifier,
    ) {
        Box {
            ShareCover(book, LibraryDimens.coverShareWidth, LibraryDimens.coverShareHeight)
            WashiTape(
                Modifier.align(Alignment.TopCenter).offset(y = -LibraryDimens.spaceSm),
                pattern = WashiPattern.Dots,
            )
            StatusStamp(
                book,
                Modifier.align(Alignment.BottomEnd).offset(x = LibraryDimens.sectionGap + LibraryDimens.spaceSm, y = LibraryDimens.spaceLg),
            )
        }
        Spacer(Modifier.height(LibraryDimens.sectionGap))
        BookTitle(book)
        val pageCount = book.pageCount
        if (book.status == ReadingStatus.READING && pageCount != null && pageCount > 0) {
            ReadingProgress(
                book.currentPage.toFloat() / pageCount,
                Modifier.padding(horizontal = LibraryDimens.sectionGap, vertical = LibraryDimens.spaceMd),
            )
        }
        latestNote?.let {
            Text(
                stringResource(R.string.share_quote, it.trim()),
                style = type.hand,
                color = colors.stamp,
                textAlign = TextAlign.Center,
                maxLines = NOTE_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = LibraryDimens.spaceXs),
            )
        }
    }
}

/** Reading: percent and page. Finished or put down: the day it ended. Want to read: no stamp. */
@Composable
private fun StatusStamp(book: Book, modifier: Modifier) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val finishDate = book.finishDate
    when (book.status) {
        ReadingStatus.READING -> {
            val pageCount = book.pageCount
            val known = pageCount != null && pageCount > 0
            InkStamp(
                top = stringResource(R.string.status_reading).uppercase(locale),
                middle = if (known) stringResource(R.string.share_stamp_percent, book.currentPage * PERCENT / pageCount!!) else book.currentPage.toString(),
                bottom = if (known) stringResource(R.string.share_stamp_page, book.currentPage) else stringResource(R.string.share_stamp_page_label),
                modifier = modifier,
                background = colors.background,
                color = colors.stamp,
            )
        }
        ReadingStatus.FINISHED, ReadingStatus.DNF -> if (finishDate != null) {
            val putDown = book.status == ReadingStatus.DNF
            DateStamp(
                label = stringResource(if (putDown) R.string.stamp_put_down else R.string.stamp_read),
                epochMillis = finishDate,
                modifier = modifier,
                background = colors.background,
                color = if (putDown) colors.inkMuted else colors.stamp,
                struck = putDown,
            )
        }
        ReadingStatus.WANT_TO_READ -> Unit
    }
}

/** The reader's library card, as printed at the end of the welcome. */
@Composable
private fun LibraryCardShare(profile: ReaderProfile, modifier: Modifier) {
    CardFrame(
        tagline = stringResource(R.string.share_library_card_tagline),
        title = stringResource(R.string.share_library_card_title),
        profile = profile,
        modifier = modifier,
    ) {
        LibraryCard(libraryCardData(profile), Modifier.padding(end = LibraryDimens.spaceSm))
        Sprig(Modifier.padding(top = LibraryDimens.sectionGap + LibraryDimens.spaceXl))
    }
}

/** The reading card being filled, under this month like the progress card. */
@Composable
private fun ReadingCardShare(card: ReadingCard, month: YearMonth?, profile: ReaderProfile, modifier: Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    CardFrame(
        tagline = stringResource(R.string.share_reading_card_tagline),
        title = month?.month?.getDisplayName(TextStyle.FULL_STANDALONE, locale),
        profile = profile,
        modifier = modifier,
    ) {
        StampCard(card)
    }
}

/** The cover as a polaroid, her stars and her review in handwriting. */
@Composable
private fun ReviewCard(book: Book, profile: ReaderProfile, modifier: Modifier) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    CardFrame(
        tagline = stringResource(R.string.share_review_tagline),
        title = stringResource(R.string.share_review_title),
        profile = profile,
        modifier = modifier,
    ) {
        Polaroid { ShareCover(book, LibraryDimens.coverDetailWidth, LibraryDimens.coverDetailHeight) }
        Spacer(Modifier.height(LibraryDimens.spaceXl))
        BookTitle(book)
        if (book.rating > 0f) {
            StarRating(
                book.rating,
                pluralStringResource(R.plurals.cd_rating, RATING_MAX, book.rating.toInt(), RATING_MAX),
                Modifier.padding(top = LibraryDimens.spaceSm),
            )
        }
        book.review?.takeIf { it.isNotBlank() }?.let {
            Text(
                it.trim(),
                style = type.hand,
                color = colors.stamp,
                textAlign = TextAlign.Center,
                maxLines = REVIEW_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = LibraryDimens.spaceMd),
            )
        }
    }
}

/**
 * Shared frame: background with a lace header, tagline and optional title,
 * [content] centred in the space left, and the library's name at the bottom.
 */
@Composable
private fun CardFrame(
    tagline: String,
    title: String?,
    profile: ReaderProfile,
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val locale = LocalConfiguration.current.locales[0]
    val shape = RoundedCornerShape(LibraryDimens.radiusSheet)
    Box(
        modifier
            .size(LibraryDimens.shareCardWidth, LibraryDimens.shareCardHeight)
            .clip(shape)
            .background(colors.background, shape),
    ) {
        LaceHeader(height = LibraryDimens.shareHeaderHeight)
        Column(Modifier.fillMaxSize().padding(horizontal = LibraryDimens.spaceXl + LibraryDimens.spaceXs)) {
            Text(tagline, style = type.hand, color = colors.stamp, modifier = Modifier.padding(top = LibraryDimens.spaceXl))
            title?.let { Text(it, style = type.title, color = colors.ink) }
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(top = LibraryDimens.laceScallop + LibraryDimens.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = content,
            )
            Text(
                if (profile.name.isBlank()) stringResource(R.string.share_footer_anonymous)
                else stringResource(R.string.share_footer, profile.name.trim()).uppercase(locale),
                style = type.label,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceLg),
            )
        }
    }
}

@Composable
private fun BookTitle(book: Book) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Text(book.title, style = type.heading, color = colors.ink, textAlign = TextAlign.Center, maxLines = TITLE_MAX_LINES, overflow = TextOverflow.Ellipsis)
    Text(book.author, style = type.body, color = colors.inkMuted, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/**
 * The cover loaded as a software bitmap: the card is copied into a picture,
 * and hardware bitmaps cannot be read back on every Android version.
 */
@Composable
private fun ShareCover(book: Book, width: Dp, height: Dp) {
    val modifier = Modifier.size(width, height).clip(RoundedCornerShape(LibraryDimens.radiusCover))
    val url = book.coverUrl
    if (url == null) {
        PlaceholderCover(title = book.title, author = book.author, modifier = modifier)
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(url).allowHardware(false).build(),
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

private val ReadingStatus.progressTaglineRes: Int
    get() = when (this) {
        ReadingStatus.READING -> R.string.share_tagline_reading
        ReadingStatus.FINISHED -> R.string.share_tagline_finished
        ReadingStatus.WANT_TO_READ -> R.string.share_tagline_want_to_read
        ReadingStatus.DNF -> R.string.share_tagline_put_down
    }

private const val PERCENT = 100
private const val NOTE_MAX_LINES = 3
private const val REVIEW_MAX_LINES = 6
private const val TITLE_MAX_LINES = 2
