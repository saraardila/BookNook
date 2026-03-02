package com.nawin.booknook.data.local.dao

import androidx.room.*
import com.nawin.booknook.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE status = :status ORDER BY title ASC")
    fun getBooksByStatus(status: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<BookEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBook(bookId: String)

    @Query("SELECT * FROM books WHERE status = 'READING' LIMIT 1")
    fun getCurrentlyReading(): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    fun getFavorites(): Flow<List<BookEntity>>
}