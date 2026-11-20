package com.nawin.booknook.domain.model

/** How a reading session left the reader, offered as quick tags when writing a note. */
enum class ReadingMood {
    SMITTEN,
    SHOCKED,
    IN_TEARS,
    HOOKED;

    companion object {
        /** The mood stored as [name], or null when it is missing or no longer exists. */
        fun fromName(name: String?): ReadingMood? = entries.firstOrNull { it.name == name }
    }
}
