package com.nawin.booknook.presentation.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.presentation.theme.*
import com.nawin.booknook.presentation.ui.main.MainViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val preferences by mainViewModel.preferences.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Personalise",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Make BookNook yours ✨",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Theme selector ───
        Text(
            text = "App Theme",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Changes the entire look of your app",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeCard(
                theme = BookTheme.BOTANICAL,
                name = "🌿 Botanical",
                description = "Sage green & warm cream",
                gradient = Brush.horizontalGradient(listOf(
                    BotanicalColors.background,
                    BotanicalColors.primaryContainer,
                    BotanicalColors.secondary
                )),
                accentColor = BotanicalColors.primary,
                isSelected = preferences.theme == BookTheme.BOTANICAL,
                onClick = { mainViewModel.setTheme(BookTheme.BOTANICAL) }
            )
            ThemeCard(
                theme = BookTheme.SUMMER,
                name = "☀️ Summer",
                description = "Warm yellows & coral",
                gradient = Brush.horizontalGradient(listOf(
                    SummerColors.background,
                    SummerColors.primaryContainer,
                    SummerColors.tertiary
                )),
                accentColor = SummerColors.primary,
                isSelected = preferences.theme == BookTheme.SUMMER,
                onClick = { mainViewModel.setTheme(BookTheme.SUMMER) }
            )
            ThemeCard(
                theme = BookTheme.MIDNIGHT,
                name = "🌙 Midnight",
                description = "Dark blue & gold",
                gradient = Brush.horizontalGradient(listOf(
                    MidnightColors.background,
                    MidnightColors.surfaceVariant,
                    MidnightColors.primaryContainer
                )),
                accentColor = MidnightColors.primary,
                isSelected = preferences.theme == BookTheme.MIDNIGHT,
                onClick = { mainViewModel.setTheme(BookTheme.MIDNIGHT) }
            )
            ThemeCard(
                theme = BookTheme.BLOSSOM,
                name = "🌸 Blossom",
                description = "Soft rose & sage",
                gradient = Brush.horizontalGradient(listOf(
                    BlossomColors.background,
                    BlossomColors.primaryContainer,
                    BlossomColors.secondaryContainer
                )),
                accentColor = BlossomColors.primary,
                isSelected = preferences.theme == BookTheme.BLOSSOM,
                onClick = { mainViewModel.setTheme(BookTheme.BLOSSOM) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Journal style ───
        Text(
            text = "Journal Paper",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Style of your journal pages",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            JournalStyleCard(
                modifier = Modifier.weight(1f),
                style = JournalStyle.PLAIN,
                label = "Plain",
                isSelected = preferences.journalStyle == JournalStyle.PLAIN,
                onClick = { mainViewModel.setJournalStyle(JournalStyle.PLAIN) }
            )
            JournalStyleCard(
                modifier = Modifier.weight(1f),
                style = JournalStyle.LINED,
                label = "Lined",
                isSelected = preferences.journalStyle == JournalStyle.LINED,
                onClick = { mainViewModel.setJournalStyle(JournalStyle.LINED) }
            )
            JournalStyleCard(
                modifier = Modifier.weight(1f),
                style = JournalStyle.DOTTED,
                label = "Dotted",
                isSelected = preferences.journalStyle == JournalStyle.DOTTED,
                onClick = { mainViewModel.setJournalStyle(JournalStyle.DOTTED) }
            )
            JournalStyleCard(
                modifier = Modifier.weight(1f),
                style = JournalStyle.GRID,
                label = "Grid",
                isSelected = preferences.journalStyle == JournalStyle.GRID,
                onClick = { mainViewModel.setJournalStyle(JournalStyle.GRID) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ─── Ink color ───
        Text(
            text = "Ink Color",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Color of your journal writing",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InkColorDot(
                color = BotanicalColors.inkDark,
                label = "Dark",
                isSelected = preferences.journalInkColor == JournalInkColor.DARK,
                onClick = { mainViewModel.setInkColor(JournalInkColor.DARK) }
            )
            InkColorDot(
                color = BotanicalColors.inkSage,
                label = "Sage",
                isSelected = preferences.journalInkColor == JournalInkColor.SAGE,
                onClick = { mainViewModel.setInkColor(JournalInkColor.SAGE) }
            )
            InkColorDot(
                color = BlossomColors.primary,
                label = "Rose",
                isSelected = preferences.journalInkColor == JournalInkColor.ROSE,
                onClick = { mainViewModel.setInkColor(JournalInkColor.ROSE) }
            )
            InkColorDot(
                color = MidnightColors.primary,
                label = "Gold",
                isSelected = preferences.journalInkColor == JournalInkColor.MIDNIGHT,
                onClick = { mainViewModel.setInkColor(JournalInkColor.MIDNIGHT) }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ThemeCard(
    theme: BookTheme,
    name: String,
    description: String,
    gradient: Brush,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, accentColor) else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gradient preview
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient)
            )

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Check
            AnimatedVisibility(visible = isSelected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun JournalStyleCard(
    modifier: Modifier = Modifier,
    style: JournalStyle,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(0.75f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Paper preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                JournalStylePreview(style = style)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun JournalStylePreview(style: JournalStyle) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = android.graphics.Color.parseColor("#CCCCCC")
        val paint = androidx.compose.ui.graphics.Paint().apply {
            color = Color(lineColor)
            strokeWidth = 1f
        }
        when (style) {
            JournalStyle.PLAIN  -> { /* nothing */ }
            JournalStyle.LINED  -> {
                val step = size.height / 6
                for (i in 1..5) {
                    drawLine(
                        color = Color(0xFFCCCCCC),
                        start = androidx.compose.ui.geometry.Offset(4f, i * step),
                        end = androidx.compose.ui.geometry.Offset(size.width - 4f, i * step),
                        strokeWidth = 1f
                    )
                }
            }
            JournalStyle.DOTTED -> {
                val stepX = size.width / 4
                val stepY = size.height / 5
                for (x in 1..3) {
                    for (y in 1..4) {
                        drawCircle(
                            color = Color(0xFFAAAAAA),
                            radius = 2f,
                            center = androidx.compose.ui.geometry.Offset(x * stepX, y * stepY)
                        )
                    }
                }
            }
            JournalStyle.GRID   -> {
                val stepX = size.width / 4
                val stepY = size.height / 5
                for (x in 1..3) {
                    drawLine(
                        color = Color(0xFFCCCCCC),
                        start = androidx.compose.ui.geometry.Offset(x * stepX, 0f),
                        end = androidx.compose.ui.geometry.Offset(x * stepX, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 1..4) {
                    drawLine(
                        color = Color(0xFFCCCCCC),
                        start = androidx.compose.ui.geometry.Offset(0f, y * stepY),
                        end = androidx.compose.ui.geometry.Offset(size.width, y * stepY),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}

@Composable
fun InkColorDot(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) Modifier.border(
                        3.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            // 👇 if en vez de AnimatedVisibility dentro de Box
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}