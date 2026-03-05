package com.nawin.booknook.presentation.ui.journal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.IndieFlower
import com.nawin.booknook.presentation.theme.Nunito
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(
    navController: NavController,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Fondo con líneas tipo cuaderno
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Líneas de cuaderno decorativas
        NotebookLines()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.journal_title),
                    fontFamily = IndieFlower,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.journal_subtitle, uiState.books.size),
                    fontFamily = IndieFlower,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.books.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(text = "📖", fontSize = 64.sp)
                        Text(
                            text = stringResource(R.string.journal_empty_title),
                            fontFamily = IndieFlower,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.journal_empty_subtitle),
                            fontFamily = IndieFlower,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        JournalBookCard(
                            book = book,
                            onClick = {
                                navController.navigate(Screen.BookDetail.createRoute(book.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Líneas de cuaderno ──────────────────────────────────
@Composable
fun NotebookLines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = Color(0xFFD4B8A8).copy(alpha = 0.25f)
        val lineSpacing = 36.dp.toPx()
        var y = lineSpacing * 3
        while (y < size.height) {
            drawLine(
                color = lineColor,
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += lineSpacing
        }
        // Línea vertical roja izquierda — margen del cuaderno
        drawLine(
            color = Color(0xFFD4826A).copy(alpha = 0.2f),
            start = androidx.compose.ui.geometry.Offset(56.dp.toPx(), 0f),
            end = androidx.compose.ui.geometry.Offset(56.dp.toPx(), size.height),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

// ─── Journal Book Card ───────────────────────────────────
@Composable
fun JournalBookCard(
    book: Book,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rotationAngle = remember(book.id) {
        (book.id.hashCode() % 3 - 1) * 0.8f  // -0.8, 0, o +0.8 grados
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(rotationAngle)
    ) {
        // Sombra papel
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 2.dp, y = 2.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x18000000))
        )

        // Card principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFDF8),
                            Color(0xFFFAF5EE)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFFD4B8A8).copy(alpha = 0.4f),
                    RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Portada
                BookCover(
                    title = book.title,
                    author = book.author,
                    coverUrl = book.coverUrl,
                    modifier = Modifier
                        .width(80.dp)
                        .height(115.dp),
                    cornerRadius = 10.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Título
                    Text(
                        text = book.title,
                        fontFamily = IndieFlower,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E1208),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )

                    // Autor
                    Text(
                        text = book.author,
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = Color(0xFF8A6A58)
                    )

                    // Estrellas
                    if (book.rating > 0f) {
                        StarDisplay(rating = book.rating)
                    }

                    // Fechas
                    if (book.finishDate != null) {
                        Text(
                            text = "finished ${dateFormat.format(Date(book.finishDate))}",
                            fontFamily = IndieFlower,
                            fontSize = 12.sp,
                            color = Color(0xFFB89080)
                        )
                    }

                    // Preview de review
                    if (!book.review.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${book.review}\"",
                            fontFamily = IndieFlower,
                            fontSize = 13.sp,
                            color = Color(0xFF6B4E3D),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.journal_no_review),
                            fontFamily = IndieFlower,
                            fontSize = 12.sp,
                            color = Color(0xFFB89080).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

// ─── Star Display ────────────────────────────────────────
@Composable
fun StarDisplay(rating: Float, maxStars: Int = 5) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..maxStars) {
            Text(
                text = if (i <= rating) "⭐" else "☆",
                fontSize = if (i <= rating) 14.sp else 13.sp,
                color = if (i <= rating) Color(0xFFD4826A)
                else Color(0xFFB89080).copy(alpha = 0.4f)
            )
        }
    }
}