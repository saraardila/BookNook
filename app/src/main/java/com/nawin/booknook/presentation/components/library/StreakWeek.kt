package com.nawin.booknook.presentation.components.library

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nawin.booknook.R
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields

/**
 * The current week as a row of day circles: ticked and slightly askew where the reader
 * read enough that day, dashed where not. The week starts on the locale's first day.
 *
 * @param today The day whose week is shown.
 * @param daysRead Epoch days that count towards the streak.
 */
@Composable
fun StreakWeek(
    today: LocalDate,
    daysRead: Set<Long>,
    modifier: Modifier = Modifier,
    daySize: Dp = LibraryDimens.streakDay,
) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val weekStart = today.minusDays(((today.dayOfWeek.value - firstDay.value + DAYS_IN_WEEK) % DAYS_IN_WEEK).toLong())

    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(DAYS_IN_WEEK) { index ->
            val day = weekStart.plusDays(index.toLong())
            val read = day.toEpochDay() in daysRead
            val dayName = day.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            val description = stringResource(if (read) R.string.cd_streak_day_read else R.string.cd_streak_day_unread, dayName)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clearAndSetSemantics { contentDescription = description },
            ) {
                Box(
                    Modifier.size(daySize).rotate(if (read) TILTS[index] else 0f),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val inset = LibraryDimens.stroke.toPx()
                        drawCircle(
                            color = if (read) colors.accentDeep else colors.outline,
                            radius = size.minDimension / 2 - inset,
                            style = Stroke(
                                width = if (read) STROKE_READ.toPx() else LibraryDimens.stroke.toPx(),
                                pathEffect = if (read) null else PathEffect.dashPathEffect(floatArrayOf(DASH.toPx(), GAP.toPx())),
                            ),
                        )
                    }
                    if (read) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = colors.accentDeep, modifier = Modifier.size(LibraryDimens.streakDayCheck))
                    }
                }
                Spacer(Modifier.height(LibraryDimens.spaceXs))
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.NARROW, locale).uppercase(locale),
                    style = LibraryTheme.typography.label,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

private const val DAYS_IN_WEEK = 7

/** Hand-stamped look: each ticked day leans a little differently. */
private val TILTS = floatArrayOf(-8f, 6f, -4f, 9f, -6f, 5f, -3f)
private val STROKE_READ = 2.dp
private val DASH = 3.dp
private val GAP = 2.5.dp
