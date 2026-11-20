package com.nawin.booknook.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ShelfQuizTest {

    @Test
    fun `the theme with most votes wins`() {
        val answers = ShelfQuizAnswers(
            plan = ReadingPlan.PORCH, // Cowboy
            world = StoryWorld.CASTLE_LIBRARY, // Romantasy
            hooks = setOf(StoryHook.ENEMIES_TO_LOVERS, StoryHook.MAGIC_AND_FAE, StoryHook.SLOW_BURN), // 2 Romantasy, 1 Romance
            ending = StoryEnding.HAPPY, // Romance
        )

        assertEquals(ShelfTheme.ROMANTASY, ShelfQuiz.favouriteShelf(answers))
    }

    @Test
    fun `hooks alone can outvote the single-choice answers`() {
        val answers = ShelfQuizAnswers(
            plan = ReadingPlan.TRAIN, // Thriller
            world = StoryWorld.RAINY_CITY, // Thriller
            hooks = setOf(StoryHook.SLOW_BURN, StoryHook.SECOND_CHANCES, StoryHook.LOVE_TRIANGLE), // 3 Romance
            ending = StoryEnding.HAPPY, // Romance
        )

        assertEquals(ShelfTheme.ROMANCE, ShelfQuiz.favouriteShelf(answers))
    }

    @Test
    fun `a tie goes to the theme picked more often in the single-choice questions`() {
        val answers = ShelfQuizAnswers(
            plan = ReadingPlan.CANDLELIT_TAROT, // Witchy
            world = StoryWorld.ENCHANTED_CABIN, // Witchy
            hooks = setOf(StoryHook.MAGIC_AND_FAE, StoryHook.FOUND_FAMILY), // 2 Romantasy
            ending = StoryEnding.TWIST, // Thriller
        )

        // Witchy 2, Romantasy 2: Witchy came from deliberate single picks.
        assertEquals(ShelfTheme.WITCHY, ShelfQuiz.favouriteShelf(answers))
    }

    @Test
    fun `a full tie goes to the earlier theme`() {
        val answers = ShelfQuizAnswers(
            plan = ReadingPlan.PORCH, // Cowboy
            world = StoryWorld.CASTLE_LIBRARY, // Romantasy
            hooks = emptySet(),
            ending = StoryEnding.TWIST, // Thriller
        )

        assertEquals(ShelfTheme.ROMANTASY, ShelfQuiz.favouriteShelf(answers))
    }

    @Test
    fun `the standard theme never wins`() {
        ReadingPlan.entries.forEach { plan ->
            StoryWorld.entries.forEach { world ->
                StoryEnding.entries.forEach { ending ->
                    val result = ShelfQuiz.favouriteShelf(ShelfQuizAnswers(plan, world, emptySet(), ending))
                    assertNotEquals(ShelfTheme.STANDARD, result)
                }
            }
        }
    }
}
