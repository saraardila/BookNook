package com.nawin.booknook.domain.service

/**
 * Hands a picture of a share card (PNG bytes) to the platform. The app draws
 * the card itself; each platform decides how it is shared and where it is saved.
 */
interface CardSharer {

    /** Opens the system share sheet with the picture, so the reader picks the app (stories, a chat…). */
    suspend fun share(png: ByteArray)

    /**
     * Saves the picture to the photo gallery as [fileName] (without extension).
     * Returns false when it could not be written.
     */
    suspend fun saveToGallery(png: ByteArray, fileName: String): Boolean
}
