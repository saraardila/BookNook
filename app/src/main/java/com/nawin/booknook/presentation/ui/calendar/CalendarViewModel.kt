package com.nawin.booknook.presentation.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.usecase.GetMyLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMyLibraryUseCase: GetMyLibraryUseCase
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    val uiState: StateFlow<CalendarUiState> = combine(
        getMyLibraryUseCase(),
        _currentMonth
    ) { books, calendar ->
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        // Libros empezados este mes
        val startedThisMonth = books.filter { book ->
            book.startDate?.let { date ->
                val cal = Calendar.getInstance().apply { timeInMillis = date }
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
            } ?: false
        }

        // Libros terminados este mes
        val finishedThisMonth = books.filter { book ->
            book.finishDate?.let { date ->
                val cal = Calendar.getInstance().apply { timeInMillis = date }
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
            } ?: false
        }

        // Mapa día -> libros
        val booksByDay = mutableMapOf<Int, MutableList<Book>>()
        startedThisMonth.forEach { book ->
            val cal = Calendar.getInstance().apply { timeInMillis = book.startDate!! }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            booksByDay.getOrPut(day) { mutableListOf() }.add(book)
        }
        finishedThisMonth.forEach { book ->
            val cal = Calendar.getInstance().apply { timeInMillis = book.finishDate!! }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            if (booksByDay[day]?.none { it.id == book.id } != false) {
                booksByDay.getOrPut(day) { mutableListOf() }.add(book)
            }
        }

        CalendarUiState(
            year = year,
            month = month,
            booksByDay = booksByDay,
            startedThisMonth = startedThisMonth.size,
            finishedThisMonth = finishedThisMonth.size,
            currentlyReading = books.count { it.status == ReadingStatus.READING }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun previousMonth() {
        _currentMonth.update { cal ->
            Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.MONTH, -1)
            }
        }
    }

    fun nextMonth() {
        _currentMonth.update { cal ->
            Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.MONTH, 1)
            }
        }
    }
}

data class CalendarUiState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH),
    val booksByDay: Map<Int, List<Book>> = emptyMap(),
    val startedThisMonth: Int = 0,
    val finishedThisMonth: Int = 0,
    val currentlyReading: Int = 0
)