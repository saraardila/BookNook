package com.nawin.booknook.domain.model

/** The pieces the reader can show, hide and reorder on the home screen (P11). */
enum class HomeBlock {
    CURRENTLY_READING,
    STREAK,
    LICENCES,
    RECENTLY_FINISHED,
    QUOTE_OF_THE_DAY,
}

/**
 * Which home blocks are shown and in what order.
 *
 * @property order Every [HomeBlock] exactly once, top to bottom.
 * @property hidden Blocks the reader switched off; they keep their place in [order].
 */
data class HomeLayout(
    val order: List<HomeBlock> = DEFAULT_ORDER,
    val hidden: Set<HomeBlock> = DEFAULT_HIDDEN,
) {
    /** The blocks to draw, top to bottom. */
    val visible: List<HomeBlock> get() = order.filterNot { it in hidden }

    fun toggle(block: HomeBlock): HomeLayout =
        copy(hidden = if (block in hidden) hidden - block else hidden + block)

    /** Moves the block at [from] to [to], shifting the ones in between. Out-of-range indexes change nothing. */
    fun move(from: Int, to: Int): HomeLayout {
        if (from == to || from !in order.indices || to !in order.indices) return this
        return copy(order = order.toMutableList().apply { add(to, removeAt(from)) })
    }

    companion object {
        /** The home as it was before it could be edited, plus the licences. The quote starts switched off. */
        val DEFAULT_ORDER = listOf(
            HomeBlock.CURRENTLY_READING,
            HomeBlock.STREAK,
            HomeBlock.LICENCES,
            HomeBlock.RECENTLY_FINISHED,
            HomeBlock.QUOTE_OF_THE_DAY,
        )
        val DEFAULT_HIDDEN = setOf(HomeBlock.QUOTE_OF_THE_DAY)

        /**
         * Rebuilds a layout from stored block names. Unknown names (a block removed
         * in a later version) are dropped and duplicates kept once; blocks missing
         * from [storedOrder] (added in a later version) go in at their default
         * position, with their default visibility. Null means nothing was saved yet.
         */
        fun fromNames(storedOrder: List<String>?, storedHidden: Set<String>?): HomeLayout {
            if (storedOrder == null) return HomeLayout()
            val known = storedOrder.mapNotNull { name -> HomeBlock.entries.firstOrNull { it.name == name } }.distinct()
            val order = known.toMutableList()
            DEFAULT_ORDER.forEachIndexed { index, block ->
                if (block !in order) order.add(index.coerceAtMost(order.size), block)
            }
            val hidden = storedHidden.orEmpty().mapNotNullTo(mutableSetOf()) { name -> HomeBlock.entries.firstOrNull { it.name == name } }
            // A block she has never seen gets its default visibility.
            hidden += DEFAULT_HIDDEN.filter { it !in known }
            return HomeLayout(order, hidden)
        }
    }
}
