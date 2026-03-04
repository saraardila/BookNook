package com.nawin.booknook.presentation.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.presentation.navigation.Screen
import java.util.Calendar
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.dp
import com.nawin.booknook.presentation.components.CozyButton
import com.nawin.booknook.presentation.components.CozyCard

// ─── Elementos flotantes ────────────────────────────────
data class FloatingElement(
    val emoji: String,
    val x: Float,       // 0f..1f — posición relativa horizontal
    val y: Float,       // 0f..1f — posición relativa vertical
    val size: Float,    // tamaño en sp
    val rotationOffset: Float,
    val animDelay: Int
)

val cozyElements = listOf(
    FloatingElement("🐉", 0.05f, 0.08f, 28f, -15f, 0),
    FloatingElement("⚔️", 0.88f, 0.05f, 22f, 20f, 200),
    FloatingElement("🕯️", 0.15f, 0.22f, 20f, -5f, 400),
    FloatingElement("☕", 0.82f, 0.18f, 24f, 8f, 100),
    FloatingElement("🌿", 0.04f, 0.42f, 22f, -12f, 600),
    FloatingElement("🌸", 0.90f, 0.38f, 20f, 15f, 300),
    FloatingElement("🍓", 0.20f, 0.72f, 18f, -8f, 500),
    FloatingElement("🦋", 0.78f, 0.65f, 22f, 10f, 700),
    FloatingElement("❤️", 0.50f, 0.06f, 16f, 0f, 150),
    FloatingElement("✨", 0.35f, 0.15f, 18f, 5f, 250),
    FloatingElement("📚", 0.92f, 0.55f, 24f, -10f, 450),
    FloatingElement("✨", 0.70f, 0.10f, 14f, 0f, 350),
    FloatingElement("🤠", 0.08f, 0.60f, 22f, -5f, 550),
    FloatingElement("🌸", 0.55f, 0.75f, 16f, 12f, 650),
    FloatingElement("⭐", 0.40f, 0.82f, 14f, 0f, 750),
    FloatingElement("🐉", 0.75f, 0.85f, 20f, 8f, 800)
)

@Composable
fun FloatingBackground(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier = modifier) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        cozyElements.forEach { element ->
            val infiniteTransition = rememberInfiniteTransition(
                label = "float_${element.animDelay}"
            )
            val offsetY by infiniteTransition.animateFloat(
                initialValue = -6f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 3000 + element.animDelay,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "offsetY_${element.animDelay}"
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = element.rotationOffset - 3f,
                targetValue = element.rotationOffset + 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 4000 + element.animDelay,
                        easing = EaseInOutSine
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation_${element.animDelay}"
            )

            Text(
                text = element.emoji,
                fontSize = element.size.sp,
                modifier = Modifier
                    .offset(
                        x = screenWidth * element.x,
                        y = screenHeight * element.y + offsetY.dp
                    )
                    .rotate(rotation)
                    .
                    alpha(0.30f)
            )
        }
    }
}
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // Fondo flotante — detrás de todo
        FloatingBackground(
            modifier = Modifier.fillMaxSize()
        )

        // Contenido encima
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            GreetingHeader()

            Spacer(modifier = Modifier.height(24.dp))

            StatsRow(
                totalBooks = uiState.totalBooks,
                finished = uiState.booksFinished,
                reading = uiState.booksReading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.home_currently_reading),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.currentlyReading != null) {
                CurrentlyReadingCard(
                    book = uiState.currentlyReading!!,
                    onClick = {
                        navController.navigate(
                            Screen.BookDetail.createRoute(uiState.currentlyReading!!.id)
                        )
                    }
                )
            } else {
                EmptyReadingCard(
                    onClick = { navController.navigate(Screen.Search.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.recentlyFinished.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_recently_finished),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { navController.navigate(Screen.Library.route) }) {
                        Text(
                            text = stringResource(R.string.home_see_all),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.recentlyFinished, key = { it.id }) { book ->
                        SmallBookCard(book = book, onClick = {
                            navController.navigate(Screen.BookDetail.createRoute(book.id))
                        })
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GreetingHeader() {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11  -> stringResource(R.string.greeting_morning)
        in 12..17 -> stringResource(R.string.greeting_afternoon)
        in 18..21 -> stringResource(R.string.greeting_evening)
        else      -> stringResource(R.string.greeting_night)
    }
    val emoji = when (hour) {
        in 5..11  -> "☀️"
        in 12..17 -> "🌤️"
        in 18..21 -> "🌙"
        else      -> "✨"
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "your reading companion 🌿",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
@Composable
fun StatsRow(totalBooks: Int, finished: Int, reading: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$totalBooks",
            label = stringResource(R.string.stat_total),
            icon = "📚",
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$reading",
            label = stringResource(R.string.stat_reading),
            icon = "📖",
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "$finished",
            label = stringResource(R.string.stat_finished),
            icon = "✅",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: String,
    containerColor: androidx.compose.ui.graphics.Color
) {
    CozyCard(
        modifier = modifier,
        containerColor = containerColor,
        useGradient = true,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@Composable
fun CurrentlyReadingCard(book: Book, onClick: () -> Unit) {
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (book.coverUrl != null) {
                    AsyncImage(
                        model = book.coverUrl,
                        contentDescription = stringResource(R.string.common_book_cover),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Book, null,
                        modifier = Modifier.size(40.dp).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (book.pageCount != null && book.pageCount > 0) {
                    val progress = (book.currentPage.toFloat() / book.pageCount).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.home_page_progress, book.currentPage, book.pageCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun EmptyReadingCard(onClick: () -> Unit) {
    CozyCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = PaddingValues(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📖", style = MaterialTheme.typography.displayMedium)
            Text(
                text = stringResource(R.string.home_no_book_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.home_no_book_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            CozyButton(
                text = "Search a book",
                onClick = onClick,
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Composable
fun SmallBookCard(book: Book, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (book.coverUrl != null) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = stringResource(R.string.common_book_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (book.rating > 0f) {
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
    }
}