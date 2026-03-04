package com.nawin.booknook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nawin.booknook.presentation.theme.Nunito

// Paleta de gradientes cálidos — basada en la paleta cozy
private val coverGradients = listOf(
    listOf(Color(0xFFF5DDD6), Color(0xFFE8C4B8)),  // rosa melocotón
    listOf(Color(0xFFCCE8D4), Color(0xFFADD4BA)),  // verde sage
    listOf(Color(0xFFE8DEF8), Color(0xFFCFBFEA)),  // lavanda
    listOf(Color(0xFFFFF0D4), Color(0xFFEDD9A3)),  // amarillo mantequilla
    listOf(Color(0xFFDDE8F0), Color(0xFFB8CDD8)),  // azul pizarra
    listOf(Color(0xFFF5E8E0), Color(0xFFE2C9B8)),  // crema terracota
)

// Elige gradiente basado en el título — siempre el mismo para el mismo libro
private fun gradientForTitle(title: String): List<Color> {
    val index = Math.abs(title.hashCode()) % coverGradients.size
    return coverGradients[index]
}

@Composable
fun BookCover(
    title: String,
    author: String,
    coverUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = coverUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlaceholderCover(
                title = title,
                author = author,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PlaceholderCover(
    title: String,
    author: String,
    modifier: Modifier = Modifier
) {
    val gradient = remember(title) { gradientForTitle(title) }

    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(colors = gradient)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Línea decorativa top
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(Color(0xFF8A6A58).copy(alpha = 0.4f))
            )

            // Título
            Text(
                text = title,
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF2E1F14),
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            // Línea decorativa middle
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(1.dp)
                    .background(Color(0xFF8A6A58).copy(alpha = 0.3f))
            )

            // Autor
            Text(
                text = author,
                fontFamily = Nunito,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                color = Color(0xFF6B4E3D),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}