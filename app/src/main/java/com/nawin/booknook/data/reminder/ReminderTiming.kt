package com.nawin.booknook.data.reminder

import java.time.Duration
import java.time.ZonedDateTime

/**
 * Time from [now] until the next [hour]:[minute]: later today, or tomorrow if
 * that time has already passed. Zoned, so daylight-saving changes are respected.
 */
internal fun delayUntilNext(now: ZonedDateTime, hour: Int, minute: Int): Duration {
    val today = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    val next = if (today.isAfter(now)) today else today.plusDays(1)
    return Duration.between(now, next)
}
