package com.nawin.booknook.presentation.ui.homeedit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.HomeBlock
import com.nawin.booknook.domain.model.HomeLayout
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryToggle
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.ShelfLoader
import com.nawin.booknook.presentation.navigation.Screen
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel

/** "Edit your home" (P11): show, hide and reorder the home blocks, then save. */
@Composable
fun EditHomeScreen(
    navController: NavController,
    viewModel: EditHomeViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) {
        if (state.saved) navController.popBackStack()
    }
    EditHomeContent(
        layout = state.layout,
        onBack = { navController.popBackStack() },
        onChangeTheme = { navController.navigate(Screen.Settings.route) },
        onToggle = viewModel::toggle,
        onMove = viewModel::move,
        onSave = viewModel::save,
    )
}

/** Stateless layout of [EditHomeScreen]; [layout] is null while it loads. */
@Composable
fun EditHomeContent(
    layout: HomeLayout?,
    onBack: () -> Unit,
    onChangeTheme: () -> Unit,
    onToggle: (HomeBlock) -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(modifier.fillMaxSize().background(colors.background).navigationBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box {
                LaceHeader(height = LibraryDimens.headerHeight + statusBarTop)
                Column(Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = statusBarTop + LibraryDimens.spaceSm)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm)) {
                        RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onBack)
                        Text(stringResource(R.string.edit_home_tagline), style = type.hand, color = colors.stamp)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        Text(
                            stringResource(R.string.edit_home_title),
                            style = type.title,
                            color = colors.ink,
                            modifier = Modifier.weight(1f).semantics { heading() },
                        )
                        Text(
                            stringResource(R.string.edit_home_change_theme),
                            style = type.chip,
                            color = colors.primary,
                            modifier = Modifier
                                .clip(LibraryShapes.pill)
                                .clickable(onClick = onChangeTheme)
                                .padding(horizontal = LibraryDimens.spaceXs, vertical = LibraryDimens.spaceMd),
                        )
                    }
                }
            }
            Box(Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.sectionGap)) {
                if (layout == null) {
                    ShelfLoader(Modifier.align(Alignment.Center))
                } else {
                    BlockList(layout, onToggle, onMove)
                }
            }
        }
        PillButton(
            stringResource(R.string.edit_home_save),
            onClick = onSave,
            enabled = layout != null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceLg),
        )
    }
}

/**
 * The blocks in their order. Dragging a row by its handle moves it: once it has
 * travelled half a row it swaps with its neighbour, and the offset is reduced by
 * one row so it stays under the finger. Screen readers get "move up/down" actions.
 */
@Composable
private fun BlockList(layout: HomeLayout, onToggle: (HomeBlock) -> Unit, onMove: (Int, Int) -> Unit) {
    val order by rememberUpdatedState(layout.order)
    val move by rememberUpdatedState(onMove)
    var dragged by remember { mutableStateOf<HomeBlock?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rowHeight by remember { mutableIntStateOf(0) }
    val gap = LibraryDimens.spaceSm
    val step = rowHeight + with(LocalDensity.current) { gap.toPx() }
    // The gesture lambda outlives recompositions, so it reads the latest row size through this.
    val currentStep by rememberUpdatedState(step)
    val moveUp = stringResource(R.string.cd_move_up)
    val moveDown = stringResource(R.string.cd_move_down)

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        layout.order.forEachIndexed { index, block ->
            key(block) {
                val isDragged = dragged == block
                val grip = Modifier.pointerInput(block) {
                        detectDragGestures(
                            onDragStart = { dragged = block; dragOffset = 0f },
                            onDragEnd = { dragged = null; dragOffset = 0f },
                            onDragCancel = { dragged = null; dragOffset = 0f },
                        ) { change, amount: Offset ->
                            change.consume()
                            dragOffset += amount.y
                            val current = order.indexOf(block)
                            if (currentStep > 0f && dragOffset > currentStep / 2 && current < order.lastIndex) {
                                move(current, current + 1)
                                dragOffset -= currentStep
                            } else if (currentStep > 0f && dragOffset < -currentStep / 2 && current > 0) {
                                move(current, current - 1)
                                dragOffset += currentStep
                            }
                        }
                    }
                BlockRow(
                    block = block,
                    shown = block !in layout.hidden,
                    onToggle = { onToggle(block) },
                    dragHandle = { DragHandle(grip) },
                    modifier = Modifier
                        .onSizeChanged { rowHeight = it.height }
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffset else 0f
                            val lift = if (isDragged) DRAG_SCALE else 1f
                            scaleX = lift
                            scaleY = lift
                        }
                        .semantics {
                            customActions = buildList {
                                if (index > 0) add(CustomAccessibilityAction(moveUp) { onMove(index, index - 1); true })
                                if (index < layout.order.lastIndex) add(CustomAccessibilityAction(moveDown) { onMove(index, index + 1); true })
                            }
                        },
                )
            }
        }
    }
}

/** One block: drag handle, icon, name and what it shows, and its switch. Faded while hidden. */
@Composable
private fun BlockRow(
    block: HomeBlock,
    shown: Boolean,
    onToggle: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val name = stringResource(block.nameRes)
    PaperCard(modifier.fillMaxWidth().alpha(if (shown) 1f else HIDDEN_ALPHA), cornerRadius = LibraryDimens.radiusRow) {
        Row(
            Modifier.padding(end = LibraryDimens.spaceSm, top = LibraryDimens.spaceXs, bottom = LibraryDimens.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            dragHandle()
            Box(
                Modifier
                    .size(LibraryDimens.homeBlockIcon)
                    .background(if (shown) colors.primaryLight else colors.neutralWarm, LibraryShapes.pill)
                    .border(LibraryDimens.strokeThin, colors.outline, LibraryShapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(block.icon, contentDescription = null, tint = colors.ink, modifier = Modifier.size(LibraryDimens.navIcon))
            }
            Column(Modifier.weight(1f).padding(horizontal = LibraryDimens.spaceMd)) {
                Text(name, style = type.button, color = colors.ink)
                Text(stringResource(block.descriptionRes), style = type.caption, color = colors.inkMuted)
            }
            LibraryToggle(checked = shown, onCheckedChange = { onToggle() }, contentDescription = name)
        }
    }
}

/** The grip a row is dragged by, sized as a touch target. */
@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
    Box(modifier.size(LibraryDimens.touchTarget), contentAlignment = Alignment.Center) { DragDots() }
}

/** Six dots in two columns, the usual "drag me" mark. */
@Composable
private fun DragDots() {
    val color = LibraryTheme.colors.inkMuted
    val dot = LibraryDimens.dragHandleDot
    val gap = LibraryDimens.dragHandleGap
    Canvas(Modifier.size(dot * DOT_COLUMNS + gap * (DOT_COLUMNS - 1), dot * DOT_ROWS + gap * (DOT_ROWS - 1))) {
        val radius = dot.toPx() / 2
        val pitch = (dot + gap).toPx()
        for (column in 0 until DOT_COLUMNS) for (row in 0 until DOT_ROWS) {
            drawCircle(color, radius, Offset(radius + column * pitch, radius + row * pitch))
        }
    }
}

private val HomeBlock.nameRes: Int
    get() = when (this) {
        HomeBlock.CURRENTLY_READING -> R.string.home_block_reading
        HomeBlock.STREAK -> R.string.home_block_streak
        HomeBlock.LICENCES -> R.string.home_block_licences
        HomeBlock.RECENTLY_FINISHED -> R.string.home_block_recently_finished
        HomeBlock.QUOTE_OF_THE_DAY -> R.string.home_block_quote
    }

private val HomeBlock.descriptionRes: Int
    get() = when (this) {
        HomeBlock.CURRENTLY_READING -> R.string.home_block_reading_hint
        HomeBlock.STREAK -> R.string.home_block_streak_hint
        HomeBlock.LICENCES -> R.string.home_block_licences_hint
        HomeBlock.RECENTLY_FINISHED -> R.string.home_block_recently_finished_hint
        HomeBlock.QUOTE_OF_THE_DAY -> R.string.home_block_quote_hint
    }

private val HomeBlock.icon: ImageVector
    get() = when (this) {
        HomeBlock.CURRENTLY_READING -> Icons.Outlined.Badge
        HomeBlock.STREAK -> Icons.Outlined.LocalFireDepartment
        HomeBlock.LICENCES -> Icons.Outlined.WorkspacePremium
        HomeBlock.RECENTLY_FINISHED -> Icons.Outlined.AutoStories
        HomeBlock.QUOTE_OF_THE_DAY -> Icons.Outlined.FormatQuote
    }

private const val HIDDEN_ALPHA = 0.7f
private const val DRAG_SCALE = 1.03f
private const val DOT_COLUMNS = 2
private const val DOT_ROWS = 3

@Preview(name = "Edit home", locale = "es", widthDp = 390, heightDp = 844)
@Composable
private fun EditHomePreview() = LibraryTheme {
    EditHomeContent(HomeLayout(), onBack = {}, onChangeTheme = {}, onToggle = {}, onMove = { _, _ -> }, onSave = {})
}
