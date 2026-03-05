package com.nawin.booknook.presentation.ui.library

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.presentation.components.BookCover
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.ui.search.EmptyState

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.library_title),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.library_collection_count, uiState.totalBooks),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LibraryFilter.entries) { filter ->
                val label = when (filter) {
                    LibraryFilter.ALL          -> stringResource(R.string.filter_all)
                    LibraryFilter.READING      -> stringResource(R.string.filter_reading)
                    LibraryFilter.FINISHED     -> stringResource(R.string.filter_finished)
                    LibraryFilter.WANT_TO_READ -> stringResource(R.string.filter_want_to_read)
                    LibraryFilter.DNF          -> stringResource(R.string.filter_dnf)
                    LibraryFilter.FAVORITES    -> stringResource(R.string.filter_favourites)
                }
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) },
                    label = {
                        Text(
                            text = "${filter.emoji} $label",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(
            targetState = uiState.isEmpty,
            label = "library_content"
        ) { isEmpty ->
            if (isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(
                        icon = "📭",
                        title = stringResource(R.string.library_empty_title),
                        subtitle = stringResource(R.string.library_empty_subtitle)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        LibraryBookItem(
                            book = book,
                            onClick = {
                                navController.navigate(
                                    Screen.BookDetail.createRoute(book.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryBookItem(
    book: Book,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ─── Portada con badges encima ───────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
        ) {
            BookCover(
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 10.dp
            )

            // Status badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
                shape = RoundedCornerShape(4.dp),
                color = statusColor(book.status)
            ) {
                Text(
                    text = statusEmoji(book.status),
                    modifier = Modifier.padding(2.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Favorite
            if (book.isFavorite) {
                Text(
                    text = "❤️",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Progress bar
            if (book.status == ReadingStatus.READING && book.pageCount != null && book.pageCount > 0) {
                val progress = (book.currentPage.toFloat() / book.pageCount).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
            }

            // Rating badge
            if (book.rating > 0f && book.status == ReadingStatus.FINISHED) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("⭐", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = book.rating.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = book.author,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun statusColor(status: ReadingStatus) = when (status) {
    ReadingStatus.READING      -> MaterialTheme.colorScheme.secondaryContainer
    ReadingStatus.FINISHED     -> MaterialTheme.colorScheme.primaryContainer
    ReadingStatus.WANT_TO_READ -> MaterialTheme.colorScheme.tertiaryContainer
    ReadingStatus.DNF          -> MaterialTheme.colorScheme.surfaceVariant
}

private fun statusEmoji(status: ReadingStatus) = when (status) {
    ReadingStatus.READING      -> "📖"
    ReadingStatus.FINISHED     -> "✅"
    ReadingStatus.WANT_TO_READ -> "🔖"
    ReadingStatus.DNF          -> "🚫"
}