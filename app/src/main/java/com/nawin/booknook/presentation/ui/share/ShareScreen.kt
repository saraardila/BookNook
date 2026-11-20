package com.nawin.booknook.presentation.ui.share

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.min
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.Book
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingCard
import com.nawin.booknook.domain.model.ReadingStatus
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import java.io.ByteArrayOutputStream
import java.time.YearMonth

/**
 * "Share" (P20): a picture of the book's progress, the library card, the
 * reading card or the review, on a dark backdrop, to send with the system share
 * sheet or save to the gallery.
 */
@Composable
fun ShareScreen(
    navController: NavController,
    viewModel: ShareViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()

    val save = { scope.capture(layer, onFailure = viewModel::saveFailed, onPicture = viewModel::save) }
    // Before Android 10 a picture in the public Pictures folder needs the storage permission.
    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) save() else viewModel.saveFailed()
    }

    ShareContent(
        state = state,
        layer = layer,
        onClose = { navController.popBackStack() },
        onSelect = viewModel::select,
        onShare = { scope.capture(layer, onFailure = {}, onPicture = viewModel::share) },
        onSave = {
            val legacy = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            if (legacy && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                save()
            }
        },
    )
}

/** Stateless layout of [ShareScreen]. The card is drawn into [layer], which is what gets shared. */
@Composable
fun ShareContent(
    state: ShareUiState,
    layer: GraphicsLayer?,
    onClose: () -> Unit,
    onSelect: (ShareCardType) -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(
        modifier
            .fillMaxSize()
            .background(colors.ink)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(Icons.Filled.Close, stringResource(R.string.common_close), onClose)
            Text(
                stringResource(R.string.share_title),
                style = type.button,
                color = colors.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            Spacer(Modifier.size(LibraryDimens.roundButton))
        }

        BoxWithConstraints(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceLg),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading || state.book == null) {
                ShelfLoader()
            } else {
                val design = LibraryDimens.shareCardWidth
                val width = min(maxWidth, maxHeight * (design / LibraryDimens.shareCardHeight))
                ScaledCard(state, layer, scale = width / design)
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = LibraryDimens.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm, Alignment.CenterHorizontally),
        ) {
            state.types.forEach { card ->
                LibraryChip(stringResource(card.labelRes), selected = card == state.selected, onClick = { onSelect(card) })
            }
        }

        val working = state.status == ShareStatus.Working
        Row(
            Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceXl),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.sectionGap + LibraryDimens.sectionGap, Alignment.CenterHorizontally),
        ) {
            ShareAction(Icons.Outlined.Share, stringResource(R.string.share_action_share), enabled = !working && state.book != null, onClick = onShare)
            ShareAction(Icons.Outlined.FileDownload, stringResource(R.string.share_action_save), enabled = !working && state.book != null, onClick = onSave)
        }
        Text(
            when (state.status) {
                ShareStatus.Saved -> stringResource(R.string.share_saved)
                ShareStatus.SaveFailed -> stringResource(R.string.share_save_failed)
                ShareStatus.Working, null -> ""
            },
            style = type.handSmall,
            color = colors.accent,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = LibraryDimens.spaceMd),
        )
    }
}

/**
 * Lays the card out at its design size and scales it by [scale] through the
 * density, so text, stamps and spacing keep their proportions on any screen.
 * Font scaling is left at 1: the card is a picture with a fixed layout.
 */
@Composable
private fun ScaledCard(state: ShareUiState, layer: GraphicsLayer?, scale: Float) {
    val density = LocalDensity.current
    val recorded = if (layer == null) Modifier else Modifier.drawWithContent {
        layer.record { this@drawWithContent.drawContent() }
        drawLayer(layer)
    }
    Box(recorded) {
        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, fontScale = 1f)) {
            ShareCard(state)
        }
    }
}

/** A round paper button with its label underneath, as in the design's share row. */
@Composable
private fun ShareAction(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = LibraryTheme.colors
    Column(
        Modifier.clip(LibraryShapes.card).clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(LibraryDimens.spaceXs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(LibraryDimens.shareActionSize)
                .clip(LibraryShapes.pill)
                .background(colors.paper)
                .border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.ink)
        }
        Text(label, style = LibraryTheme.typography.chip, color = colors.onPrimary, modifier = Modifier.padding(top = LibraryDimens.spaceXs))
    }
}

/** Reads the card back from [layer] as PNG bytes, off the main thread. */
private fun CoroutineScope.capture(layer: GraphicsLayer, onFailure: () -> Unit, onPicture: (ByteArray) -> Unit) {
    launch {
        // A layer that was never drawn (card still loading) has no size to read.
        if (layer.size.width == 0 || layer.size.height == 0) {
            onFailure()
            return@launch
        }
        val png = layer.toImageBitmap().encodePng()
        onPicture(png)
    }
}

private suspend fun ImageBitmap.encodePng(): ByteArray = withContext(Dispatchers.Default) {
    ByteArrayOutputStream().also { asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, it) }.toByteArray()
}

/** Ignored for PNG, which is lossless; the API still asks for it. */
private const val PNG_QUALITY = 100

@Preview(name = "Share · progress", widthDp = 360, heightDp = 800)
@Composable
private fun ShareProgressPreview() = LibraryTheme {
    ShareContent(
        state = ShareUiState(
            isLoading = false,
            book = Book(
                id = "tog",
                title = "Throne of Glass",
                author = "Sarah J. Maas",
                coverUrl = null,
                description = null,
                publishYear = null,
                isbn = null,
                status = ReadingStatus.READING,
                currentPage = 212,
                pageCount = 406,
            ),
            latestNote = "cap. 31 · ¡no puedo parar!",
            readingCard = ReadingCard(number = 1, stamps = emptyList()),
            profile = ReaderProfile(name = "Sara", memberSince = 1_793_620_800_000, favouriteShelf = ShelfTheme.ROMANTASY),
            month = YearMonth.of(2026, 11),
            types = ShareCardType.entries,
        ),
        layer = null,
        onClose = {},
        onSelect = {},
        onShare = {},
        onSave = {},
    )
}
