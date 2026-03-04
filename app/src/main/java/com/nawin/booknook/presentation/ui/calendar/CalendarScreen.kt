package com.nawin.booknook.presentation.ui.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.presentation.navigation.Screen
import java.text.DateFormatSymbols
import java.util.Calendar

@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "your reading journey 📅",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats rápidos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthStatChip(
                modifier = Modifier.weight(1f),
                emoji = "📖",
                value = "${uiState.startedThisMonth}",
                label = stringResource(R.string.calendar_started)
            )
            MonthStatChip(
                modifier = Modifier.weight(1f),
                emoji = "✅",
                value = "${uiState.finishedThisMonth}",
                label = stringResource(R.string.calendar_finished)
            )
            MonthStatChip(
                modifier = Modifier.weight(1f),
                emoji = "📚",
                value = "${uiState.currentlyReading}",
                label = stringResource(R.string.calendar_reading)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Month navigator
        MonthNavigator(
            year = uiState.year,
            month = uiState.month,
            onPrevious = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar grid
        CalendarGrid(
            year = uiState.year,
            month = uiState.month,
            booksByDay = uiState.booksByDay,
            onBookClick = { book ->
                navController.navigate(Screen.BookDetail.createRoute(book.id))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Books this month list
        if (uiState.booksByDay.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = stringResource(R.string.calendar_this_month),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                val allBooks = uiState.booksByDay.values
                    .flatten()
                    .distinctBy { it.id }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    allBooks.forEach { book ->
                        CalendarBookRow(
                            book = book,
                            onClick = {
                                navController.navigate(Screen.BookDetail.createRoute(book.id))
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ─── Month navigator ─────────────────────────────────────
@Composable
fun MonthNavigator(
    year: Int,
    month: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthName = DateFormatSymbols().months[month]

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.calendar_prev_month),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = monthName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "$year",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNext) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.calendar_next_month),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ─── Calendar Grid ───────────────────────────────────────
@Composable
fun CalendarGrid(
    year: Int,
    month: Int,
    booksByDay: Map<Int, List<Book>>,
    onBookClick: (Book) -> Unit
) {
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    // Calcular primer día y total días del mes
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(year, month, 1)
    }
    var firstWeekDay = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 2
    if (firstWeekDay < 0) firstWeekDay = 6

    val daysInMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = firstWeekDay + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filas del calendario
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstWeekDay + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day in 1..daysInMonth) {
                            val books = booksByDay[day]
                            CalendarDayCell(
                                day = day,
                                books = books,
                                onBookClick = onBookClick
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Day Cell ────────────────────────────────────────────
@Composable
fun CalendarDayCell(
    day: Int,
    books: List<Book>?,
    onBookClick: (Book) -> Unit
) {
    val today = Calendar.getInstance()
    val isToday = today.get(Calendar.DAY_OF_MONTH) == day

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (books != null && books.isNotEmpty()) {
            // Día con libro — muestra portada
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onBookClick(books.first()) }
            ) {
                if (books.first().coverUrl != null) {
                    AsyncImage(
                        model = books.first().coverUrl,
                        contentDescription = books.first().title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Número del día
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(2.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$day",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }

                // Indicador de múltiples libros
                if (books.size > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${books.size - 1}",
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            // Día vacío
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isToday) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "$day",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// ─── Month Stat Chip ─────────────────────────────────────
@Composable
fun MonthStatChip(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Calendar Book Row ───────────────────────────────────
@Composable
fun CalendarBookRow(
    book: Book,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (book.coverUrl != null) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = statusEmoji(book.status),
            fontSize = 20.sp
        )
    }
}

private fun statusEmoji(status: com.nawin.booknook.domain.model.ReadingStatus) = when (status) {
    com.nawin.booknook.domain.model.ReadingStatus.READING      -> "📖"
    com.nawin.booknook.domain.model.ReadingStatus.FINISHED     -> "✅"
    com.nawin.booknook.domain.model.ReadingStatus.WANT_TO_READ -> "🔖"
    com.nawin.booknook.domain.model.ReadingStatus.DNF          -> "🚫"
}