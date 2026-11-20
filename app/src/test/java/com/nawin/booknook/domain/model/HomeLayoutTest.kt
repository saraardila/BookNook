package com.nawin.booknook.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class HomeLayoutTest {

    @Test
    fun `nothing saved gives the default home with the quote switched off`() {
        val layout = HomeLayout.fromNames(null, null)

        assertEquals(HomeLayout.DEFAULT_ORDER, layout.order)
        assertEquals(HomeLayout.DEFAULT_ORDER - HomeBlock.QUOTE_OF_THE_DAY, layout.visible)
    }

    @Test
    fun `unknown and repeated names are dropped and missing blocks go back in`() {
        val layout = HomeLayout.fromNames(
            storedOrder = listOf("STREAK", "CHALLENGE", "CURRENTLY_READING", "STREAK"),
            storedHidden = setOf("CHALLENGE", "STREAK"),
        )

        // LICENCES (index 2), RECENTLY_FINISHED (3) and QUOTE (4) were missing: inserted at their default places.
        assertEquals(
            listOf(HomeBlock.STREAK, HomeBlock.CURRENTLY_READING, HomeBlock.LICENCES, HomeBlock.RECENTLY_FINISHED, HomeBlock.QUOTE_OF_THE_DAY),
            layout.order,
        )
        // A block she never saw takes its default visibility: the quote starts hidden.
        assertEquals(setOf(HomeBlock.STREAK, HomeBlock.QUOTE_OF_THE_DAY), layout.hidden)
    }

    @Test
    fun `a block she switched on stays on`() {
        val stored = HomeLayout.DEFAULT_ORDER.map { it.name }

        assertEquals(emptySet<HomeBlock>(), HomeLayout.fromNames(stored, emptySet()).hidden)
    }

    @Test
    fun `moving shifts the blocks in between and toggling flips one block`() {
        val moved = HomeLayout().move(from = 0, to = 3)

        assertEquals(
            listOf(HomeBlock.STREAK, HomeBlock.LICENCES, HomeBlock.RECENTLY_FINISHED, HomeBlock.CURRENTLY_READING, HomeBlock.QUOTE_OF_THE_DAY),
            moved.order,
        )
        assertEquals(emptySet<HomeBlock>(), moved.toggle(HomeBlock.QUOTE_OF_THE_DAY).hidden)
        val outOfRange = HomeLayout()
        assertSame(outOfRange, outOfRange.move(0, 9))
    }
}
