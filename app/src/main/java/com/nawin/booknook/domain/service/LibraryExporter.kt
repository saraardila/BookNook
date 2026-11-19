package com.nawin.booknook.domain.service

/** Writes the reader's whole library to a file they chose, for backup or other apps. */
fun interface LibraryExporter {
    /**
     * Writes the library to [destination], a platform location such as a
     * content URI, and returns how many books were written.
     */
    suspend fun export(destination: String): Int
}
