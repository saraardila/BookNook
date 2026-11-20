package com.nawin.booknook.domain.model

/**
 * Consecutive days of reading.
 *
 * Days are epoch days (days since 1970-01-01 in the reader's time zone), so this stays
 * plain Kotlin and the caller decides what "today" is.
 *
 * @property current Days in a row up to today. Today not counting yet does not break it:
 * a streak that reached yesterday is still alive until today ends.
 * @property record Longest run ever, never below [current].
 * @property daysRead Every day that counts towards a streak.
 */
data class ReadingStreak(
    val current: Int = 0,
    val record: Int = 0,
    val daysRead: Set<Long> = emptySet(),
) {
    companion object {
        /** Pages needed on a day for it to count. */
        const val MIN_PAGES_PER_DAY = 5

        /** Builds the streak from the pages read per epoch day, as seen on [today]. */
        fun from(pagesPerDay: Map<Long, Int>, today: Long): ReadingStreak {
            val days = pagesPerDay.filterValues { it >= MIN_PAGES_PER_DAY }.keys
            val lastDay = if (today in days) today else today - 1
            val current = generateSequence(lastDay) { it - 1 }.takeWhile { it in days }.count()
            return ReadingStreak(current = current, record = maxOf(longestRun(days), current), daysRead = days)
        }

        private fun longestRun(days: Set<Long>): Int {
            var longest = 0
            var run = 0
            var previous: Long? = null
            for (day in days.sorted()) {
                run = if (previous != null && day == previous + 1) run + 1 else 1
                longest = maxOf(longest, run)
                previous = day
            }
            return longest
        }
    }
}
