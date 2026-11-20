package com.nawin.booknook.domain.model

/*
 * The four questions of the welcome test. Each answer votes for one shelf
 * theme; the theme with most votes becomes the reader's favourite shelf.
 * STANDARD never wins: it is the theme for readers who skip the test.
 */

/** Question 1: the perfect plan for reading. */
enum class ReadingPlan(val shelf: ShelfTheme) {
    FIRESIDE(ShelfTheme.ROMANCE),
    TRAIN(ShelfTheme.THRILLER),
    PORCH(ShelfTheme.COWBOY),
    CANDLELIT_TAROT(ShelfTheme.WITCHY),
}

/** Question 2: where the reader would live inside a book. */
enum class StoryWorld(val shelf: ShelfTheme) {
    ENCHANTED_CABIN(ShelfTheme.WITCHY),
    RANCH(ShelfTheme.COWBOY),
    CASTLE_LIBRARY(ShelfTheme.ROMANTASY),
    RAINY_CITY(ShelfTheme.THRILLER),
}

/** Question 3 (several answers): what hooks the reader in a story. */
enum class StoryHook(val shelf: ShelfTheme) {
    ENEMIES_TO_LOVERS(ShelfTheme.ROMANTASY),
    SLOW_BURN(ShelfTheme.ROMANCE),
    FOUND_FAMILY(ShelfTheme.ROMANTASY),
    MORALLY_GREY_VILLAIN(ShelfTheme.ROMANTASY),
    MAGIC_AND_FAE(ShelfTheme.ROMANTASY),
    MYSTERY(ShelfTheme.THRILLER),
    SECOND_CHANCES(ShelfTheme.ROMANCE),
    LOVE_TRIANGLE(ShelfTheme.ROMANCE),
    SMALL_TOWN(ShelfTheme.COWBOY),
    WITCHES(ShelfTheme.WITCHY),
    LETTERS_AND_DIARIES(ShelfTheme.ROMANCE),
}

/** Question 4: how the reader likes a story to end. */
enum class StoryEnding(val shelf: ShelfTheme) {
    HAPPY(ShelfTheme.ROMANCE),
    HEARTBREAK(ShelfTheme.ROMANTASY),
    OPEN(ShelfTheme.WITCHY),
    TWIST(ShelfTheme.THRILLER),
}

/** A finished test. */
data class ShelfQuizAnswers(
    val plan: ReadingPlan,
    val world: StoryWorld,
    val hooks: Set<StoryHook>,
    val ending: StoryEnding,
)

object ShelfQuiz {

    /**
     * One vote per answer, hooks included. A tie goes to the theme with more
     * votes from the single-choice questions (1, 2 and 4), since those are a
     * deliberate pick; if that also ties, the earlier theme in [ShelfTheme] wins.
     */
    fun favouriteShelf(answers: ShelfQuizAnswers): ShelfTheme {
        val singleChoice = listOf(answers.plan.shelf, answers.world.shelf, answers.ending.shelf)
        val all = singleChoice + answers.hooks.map { it.shelf }
        val votes = all.groupingBy { it }.eachCount()
        val pickedVotes = singleChoice.groupingBy { it }.eachCount()
        return ShelfTheme.entries
            .filter { it in votes }
            .maxWith(compareBy<ShelfTheme>({ votes.getValue(it) }, { pickedVotes[it] ?: 0 }).thenByDescending { it.ordinal })
    }
}
