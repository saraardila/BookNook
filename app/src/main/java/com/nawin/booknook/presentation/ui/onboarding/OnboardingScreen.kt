package com.nawin.booknook.presentation.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Cabin
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Fireplace
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingPlan
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.StoryEnding
import com.nawin.booknook.domain.model.StoryHook
import com.nawin.booknook.domain.model.StoryWorld
import com.nawin.booknook.presentation.components.library.ArchFrame
import com.nawin.booknook.presentation.components.library.Daisy
import com.nawin.booknook.presentation.components.library.InkStamp
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.LibraryChip
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.Shelf
import com.nawin.booknook.presentation.components.library.Sprig
import com.nawin.booknook.presentation.components.library.ThemeMini
import com.nawin.booknook.presentation.components.library.WaxSeal
import com.nawin.booknook.presentation.components.library.drawFlatShadow
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryShapes
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.code
import com.nawin.booknook.presentation.theme.nameRes
import com.nawin.booknook.presentation.theme.palette
import com.nawin.booknook.presentation.ui.settings.AppLanguage
import org.koin.compose.viewmodel.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The first-launch flow, shown by MainActivity instead of the library until it is done. */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = state.page != OnboardingPage.WELCOME) { viewModel.back() }
    OnboardingContent(
        state = state,
        actions = OnboardingActions(
            onStart = viewModel::next,
            onSkip = viewModel::finish,
            onBack = { viewModel.back() },
            onNext = viewModel::next,
            onPlan = viewModel::selectPlan,
            onWorld = viewModel::selectWorld,
            onHook = viewModel::toggleHook,
            onEnding = viewModel::selectEnding,
            onTheme = viewModel::chooseTheme,
            onName = viewModel::saveName,
            onFinish = viewModel::finish,
        ),
    )
}

/** Everything the pages can ask for, grouped to keep the signatures short. */
private data class OnboardingActions(
    val onStart: () -> Unit,
    val onSkip: () -> Unit,
    val onBack: () -> Unit,
    val onNext: () -> Unit,
    val onPlan: (ReadingPlan) -> Unit,
    val onWorld: (StoryWorld) -> Unit,
    val onHook: (StoryHook) -> Unit,
    val onEnding: (StoryEnding) -> Unit,
    val onTheme: (ShelfTheme) -> Unit,
    val onName: (String) -> Unit,
    val onFinish: () -> Unit,
)

@Composable
private fun OnboardingContent(state: OnboardingUiState, actions: OnboardingActions) {
    Box(Modifier.fillMaxSize().background(LibraryTheme.colors.background)) {
        when (state.page) {
            OnboardingPage.WELCOME -> WelcomePage(actions)
            OnboardingPage.PLAN -> QuizPage(state, actions, R.string.quiz_plan_hint, R.string.quiz_plan_title, R.string.quiz_plan_note) {
                OptionGrid(ReadingPlan.entries, state.draft.plan, actions.onPlan) { it.option }
            }
            OnboardingPage.WORLD -> QuizPage(state, actions, R.string.quiz_world_hint, R.string.quiz_world_title) {
                OptionGrid(StoryWorld.entries, state.draft.world, actions.onWorld) { it.option }
            }
            OnboardingPage.HOOKS -> QuizPage(state, actions, R.string.quiz_hooks_hint, R.string.quiz_hooks_title) {
                HookChips(state.draft.hooks, actions.onHook)
            }
            OnboardingPage.ENDING -> QuizPage(state, actions, R.string.quiz_ending_hint, R.string.quiz_ending_title, R.string.quiz_ending_note) {
                EndingRows(state.draft.ending, actions.onEnding)
            }
            OnboardingPage.RESULT -> ResultPage(state, actions)
            OnboardingPage.NAME -> NamePage(state.profile.name, actions)
            OnboardingPage.CARD -> CardPage(state.profile, actions.onFinish)
        }
    }
}

/** Scrolling body with the buttons pinned to the bottom, clear of the system bars. */
@Composable
private fun PageScaffold(
    footer: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().navigationBarsPadding()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), content = content)
        Column(
            Modifier.fillMaxWidth().padding(LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = footer,
        )
    }
}

@Composable
private fun FooterNote(text: String) {
    Text(text, style = LibraryTheme.typography.hand, color = LibraryTheme.colors.inkMuted, textAlign = TextAlign.Center)
}

// region Welcome

@Composable
private fun WelcomePage(actions: OnboardingActions) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    PageScaffold(
        footer = {
            FooterNote(stringResource(R.string.onboarding_welcome_note))
            PillButton(stringResource(R.string.onboarding_start), onClick = actions.onStart, modifier = Modifier.fillMaxWidth())
            PillButton(stringResource(R.string.onboarding_skip), onClick = actions.onSkip, primary = false, modifier = Modifier.fillMaxWidth())
        },
    ) {
        Box {
            LaceHeader(height = LibraryDimens.headerHeight + LibraryDimens.sectionGap * 2 + statusBarTop, color = colors.primaryLight)
            Column(Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = statusBarTop + LibraryDimens.spaceXl)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.onboarding_welcome_to), style = type.hand, color = colors.stamp)
                        Text(stringResource(R.string.app_name), style = type.title, color = colors.ink, modifier = Modifier.semantics { heading() })
                        Text(stringResource(R.string.onboarding_tagline), style = type.label, color = colors.inkMuted)
                    }
                    LanguageChip()
                }
                LibraryCard(
                    LibraryCardData(),
                    Modifier.padding(top = LibraryDimens.sectionGap, start = LibraryDimens.spaceSm, end = LibraryDimens.spaceSm).rotate(WELCOME_CARD_ROTATION),
                )
            }
        }
        Row(
            Modifier.padding(start = LibraryDimens.screenPadding, top = LibraryDimens.sectionGap),
            verticalAlignment = Alignment.Bottom,
        ) {
            Sprig(stem = colors.accentDeep, leaf = colors.accentLight, modifier = Modifier.rotate(SPRIG_ROTATION))
            Daisy(center = colors.highlight)
        }
        Text(
            stringResource(R.string.onboarding_welcome_body),
            style = type.body,
            color = colors.inkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.sectionGap, vertical = LibraryDimens.spaceLg),
        )
    }
}

/** Switches to the other language, named in its own language ("English" / "Español"). */
@Composable
private fun LanguageChip() {
    val other = if (AppLanguage.current() == AppLanguage.ENGLISH) AppLanguage.SPANISH else AppLanguage.ENGLISH
    val label = stringResource(if (other == AppLanguage.ENGLISH) R.string.language_english else R.string.language_spanish)
    LibraryChip(label, onClick = { AppLanguage.apply(other) })
}

// endregion

// region Quiz

@Composable
private fun QuizPage(
    state: OnboardingUiState,
    actions: OnboardingActions,
    @StringRes hint: Int,
    @StringRes title: Int,
    @StringRes note: Int? = null,
    answers: @Composable () -> Unit,
) {
    val colors = LibraryTheme.colors
    val number = state.page.questionNumber ?: return
    PageScaffold(
        modifier = Modifier.statusBarsPadding(),
        footer = {
            note?.let { FooterNote(stringResource(it)) }
            PillButton(
                stringResource(if (number < QUIZ_QUESTION_COUNT) R.string.quiz_next else R.string.quiz_see_shelf),
                onClick = actions.onNext,
                enabled = state.canContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Column(Modifier.padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceLg)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onClick = actions.onBack)
                QuizProgress(number, Modifier.weight(1f))
                Text(stringResource(R.string.quiz_progress, number, QUIZ_QUESTION_COUNT), style = LibraryTheme.typography.chip, color = colors.inkMuted)
            }
            Text(stringResource(hint), style = LibraryTheme.typography.hand, color = colors.primary, modifier = Modifier.padding(top = LibraryDimens.spaceXl))
            Text(
                stringResource(title),
                style = LibraryTheme.typography.heading,
                color = colors.ink,
                modifier = Modifier.padding(top = LibraryDimens.spaceXs, bottom = LibraryDimens.spaceXl).semantics { heading() },
            )
            answers()
        }
    }
}

/** Four segments, filled up to the current question. */
@Composable
private fun QuizProgress(current: Int, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs)) {
        repeat(QUIZ_QUESTION_COUNT) { index ->
            Box(
                Modifier
                    .weight(1f)
                    .height(LibraryDimens.quizProgressSegment)
                    .background(if (index < current) colors.primary else colors.neutralWarm, LibraryShapes.pill),
            )
        }
    }
}

/** How an answer is drawn: its text, icon and the colour of the icon's disc. */
private data class QuizOption(@StringRes val text: Int, val icon: ImageVector, val tint: @Composable () -> Color)

private val ReadingPlan.option: QuizOption
    get() = when (this) {
        ReadingPlan.FIRESIDE -> QuizOption(R.string.quiz_plan_fireside, Icons.Outlined.Fireplace) { LibraryTheme.colors.highlight }
        ReadingPlan.TRAIN -> QuizOption(R.string.quiz_plan_train, Icons.Outlined.Train) { LibraryTheme.colors.primaryLight }
        ReadingPlan.PORCH -> QuizOption(R.string.quiz_plan_porch, Icons.Outlined.WbSunny) { LibraryTheme.colors.primaryMid }
        ReadingPlan.CANDLELIT_TAROT -> QuizOption(R.string.quiz_plan_tarot, Icons.Outlined.NightsStay) { LibraryTheme.colors.blush }
    }

private val StoryWorld.option: QuizOption
    get() = when (this) {
        StoryWorld.ENCHANTED_CABIN -> QuizOption(R.string.quiz_world_cabin, Icons.Outlined.Cabin) { LibraryTheme.colors.blush }
        StoryWorld.RANCH -> QuizOption(R.string.quiz_world_ranch, Icons.Outlined.WbTwilight) { LibraryTheme.colors.highlight }
        StoryWorld.CASTLE_LIBRARY -> QuizOption(R.string.quiz_world_castle, Icons.Outlined.Castle) { LibraryTheme.colors.primaryMid }
        StoryWorld.RAINY_CITY -> QuizOption(R.string.quiz_world_city, Icons.Outlined.Umbrella) { LibraryTheme.colors.primaryLight }
    }

/** Two columns of arch-topped cards; the chosen one gets a thicker border and a seal. */
@Composable
private fun <T> OptionGrid(options: List<T>, selected: T?, onSelect: (T) -> Unit, describe: (T) -> QuizOption) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg)) {
        options.chunked(GRID_COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg)) {
                row.forEach { option ->
                    OptionCard(describe(option), option == selected, { onSelect(option) }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OptionCard(option: QuizOption, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val shape = LibraryShapes.quizOption
    Box(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = LibraryDimens.quizOptionHeight)
                .drawBehind { drawFlatShadow(colors.shadow, shape) }
                .clip(shape)
                .background(colors.paper)
                .border(if (selected) LibraryDimens.strokeStamp else LibraryDimens.stroke, if (selected) colors.primary else colors.outline, shape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .padding(horizontal = LibraryDimens.spaceMd, vertical = LibraryDimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconDisc(option.icon, option.tint(), LibraryDimens.quizOptionIcon, LibraryDimens.quizOptionIconGlyph)
            Text(
                stringResource(option.text),
                style = LibraryTheme.typography.button,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = LibraryDimens.spaceMd),
            )
        }
        if (selected) SelectedSeal(Modifier.align(Alignment.TopEnd).offset(x = LibraryDimens.spaceXs, y = -LibraryDimens.spaceSm))
    }
}

@Composable
private fun IconDisc(icon: ImageVector, fill: Color, size: Dp, glyph: Dp) {
    val colors = LibraryTheme.colors
    Box(
        Modifier.size(size).background(fill, LibraryShapes.pill).border(LibraryDimens.stroke, colors.outline, LibraryShapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.ink, modifier = Modifier.size(glyph))
    }
}

@Composable
private fun SelectedSeal(modifier: Modifier = Modifier) {
    WaxSeal(modifier, size = LibraryDimens.quizSeal, color = LibraryTheme.colors.primary) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = LibraryTheme.colors.onPrimary, modifier = Modifier.size(LibraryDimens.badgeIcon))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HookChips(selected: Set<StoryHook>, onToggle: (StoryHook) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
        verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
    ) {
        StoryHook.entries.forEach { hook ->
            LibraryChip(stringResource(hook.nameRes), selected = hook in selected, onClick = { onToggle(hook) })
        }
    }
    if (selected.isNotEmpty()) {
        Text(
            pluralStringResource(R.plurals.quiz_hooks_count, selected.size, selected.size),
            style = LibraryTheme.typography.hand,
            color = LibraryTheme.colors.accentDeep,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().padding(top = LibraryDimens.spaceLg),
        )
    }
}

@get:StringRes
private val StoryHook.nameRes: Int
    get() = when (this) {
        StoryHook.ENEMIES_TO_LOVERS -> R.string.hook_enemies_to_lovers
        StoryHook.SLOW_BURN -> R.string.hook_slow_burn
        StoryHook.FOUND_FAMILY -> R.string.hook_found_family
        StoryHook.MORALLY_GREY_VILLAIN -> R.string.hook_morally_grey_villain
        StoryHook.MAGIC_AND_FAE -> R.string.hook_magic_and_fae
        StoryHook.MYSTERY -> R.string.hook_mystery
        StoryHook.SECOND_CHANCES -> R.string.hook_second_chances
        StoryHook.LOVE_TRIANGLE -> R.string.hook_love_triangle
        StoryHook.SMALL_TOWN -> R.string.hook_small_town
        StoryHook.WITCHES -> R.string.hook_witches
        StoryHook.LETTERS_AND_DIARIES -> R.string.hook_letters_and_diaries
    }

private val StoryEnding.option: QuizOption
    get() = when (this) {
        StoryEnding.HAPPY -> QuizOption(R.string.quiz_ending_happy, Icons.Outlined.FavoriteBorder) { LibraryTheme.colors.primaryLight }
        StoryEnding.HEARTBREAK -> QuizOption(R.string.quiz_ending_heartbreak, Icons.Outlined.HeartBroken) { LibraryTheme.colors.primaryLight }
        StoryEnding.OPEN -> QuizOption(R.string.quiz_ending_open, Icons.Outlined.QuestionMark) { LibraryTheme.colors.primaryLight }
        StoryEnding.TWIST -> QuizOption(R.string.quiz_ending_twist, Icons.Outlined.Autorenew) { LibraryTheme.colors.primaryLight }
    }

/** One answer per row, round on the icon side. */
@Composable
private fun EndingRows(selected: StoryEnding?, onSelect: (StoryEnding) -> Unit) {
    val colors = LibraryTheme.colors
    val shape = LibraryShapes.quizRow
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
        StoryEnding.entries.forEach { ending ->
            val option = ending.option
            val isSelected = ending == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawFlatShadow(colors.shadow, shape) }
                    .clip(shape)
                    .background(colors.paper)
                    .border(if (isSelected) LibraryDimens.strokeStamp else LibraryDimens.stroke, if (isSelected) colors.primary else colors.outline, shape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(ending) })
                    .padding(start = LibraryDimens.spaceMd, end = LibraryDimens.spaceLg, top = LibraryDimens.spaceMd, bottom = LibraryDimens.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceLg - LibraryDimens.spaceXxs),
            ) {
                IconDisc(option.icon, option.tint(), LibraryDimens.quizRowIcon, LibraryDimens.quizRowIconGlyph)
                Text(stringResource(option.text), style = LibraryTheme.typography.button, color = colors.ink, modifier = Modifier.weight(1f))
                if (isSelected) SelectedSeal()
            }
        }
    }
}

// endregion

// region Result

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultPage(state: OnboardingUiState, actions: OnboardingActions) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val shelf = state.profile.favouriteShelf ?: state.theme
    val shelfName = stringResource(shelf.nameRes)
    PageScaffold(
        modifier = Modifier.statusBarsPadding(),
        footer = { PillButton(stringResource(R.string.onboarding_start), onClick = actions.onNext, modifier = Modifier.fillMaxWidth()) },
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth()) {
                RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onClick = actions.onBack)
            }
            Text(stringResource(R.string.result_hint), style = type.hand, color = colors.primary)
            ResultShelf(shelf, shelfName, Modifier.padding(top = LibraryDimens.spaceMd))
            val hooks = state.draft.hooks.take(RESULT_HOOK_CHIPS)
            if (hooks.isNotEmpty()) {
                FlowRow(
                    Modifier.padding(top = LibraryDimens.spaceXl),
                    horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs),
                ) {
                    hooks.forEach { LibraryChip(stringResource(it.nameRes)) }
                }
            }
            Text(
                stringResource(R.string.result_body, shelfName),
                style = type.body,
                color = colors.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = LibraryDimens.spaceMd),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .selectableGroup()
                .padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceSm),
            horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
        ) {
            ShelfTheme.entries.forEach { theme ->
                ThemeMini(
                    palette = theme.palette,
                    name = stringResource(theme.nameRes),
                    selected = theme == state.theme,
                    onClick = { actions.onTheme(theme) },
                    modifier = Modifier.width(LibraryDimens.resultThemeMini),
                )
            }
        }
    }
}

/** The favourite shelf in its arch: a label, a row of spines in its colours and an ink stamp. */
@Composable
private fun ResultShelf(shelf: ShelfTheme, shelfName: String, modifier: Modifier = Modifier) {
    val colors = LibraryTheme.colors
    val palette = shelf.palette
    Box(modifier.size(LibraryDimens.resultArchWidth, LibraryDimens.resultArchHeight)) {
        ArchFrame(Modifier.fillMaxSize(), fill = palette.tint, ring = palette.primary) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(LibraryDimens.sectionGap + LibraryDimens.spaceSm))
                PaperCard(cornerRadius = LibraryDimens.radiusLabel) {
                    Column(
                        Modifier.padding(horizontal = LibraryDimens.spaceLg - LibraryDimens.spaceXxs, vertical = LibraryDimens.spaceSm),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(R.string.result_shelf_label, shelf.code), style = LibraryTheme.typography.label, color = colors.inkMuted)
                        Text(shelfName, style = LibraryTheme.typography.heading, color = colors.ink)
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs)) {
                    listOf(palette.primary, colors.paper, palette.primary.copy(alpha = SPINE_SOFT_ALPHA), colors.accentLight).forEachIndexed { index, spine ->
                        Box(
                            Modifier
                                .size(LibraryDimens.resultSpineWidth + SPINE_NARROWING * (index % 2),LibraryDimens.coverRowHeight + SPINE_STEP * ((index + 1) % 3))
                                .background(spine, LibraryShapes.cover)
                                .border(LibraryDimens.stroke, colors.outline, LibraryShapes.cover),
                        )
                    }
                }
                Shelf(Modifier.padding(horizontal = LibraryDimens.spaceLg))
                Spacer(Modifier.height(LibraryDimens.sectionGap + LibraryDimens.spaceSm))
            }
        }
        InkStamp(
            top = stringResource(R.string.result_stamp_top),
            middle = shelf.code,
            bottom = stringResource(R.string.result_stamp_bottom),
            size = LibraryDimens.stampSize,
            color = colors.stamp,
            background = colors.background,
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = LibraryDimens.spaceXl, y = -LibraryDimens.spaceXl),
        )
    }
}

// endregion

// region Name and card

@Composable
private fun NamePage(storedName: String, actions: OnboardingActions) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    var name by rememberSaveable { mutableStateOf(storedName) }
    val canPrint = name.isNotBlank()
    PageScaffold(
        modifier = Modifier.statusBarsPadding(),
        footer = {
            PillButton(stringResource(R.string.name_continue), onClick = { actions.onName(name) }, enabled = canPrint, modifier = Modifier.fillMaxWidth())
        },
    ) {
        Column(Modifier.padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.spaceLg)) {
            RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onClick = actions.onBack)
            Text(stringResource(R.string.name_hint), style = type.hand, color = colors.primary, modifier = Modifier.padding(top = LibraryDimens.spaceXl))
            Text(
                stringResource(R.string.name_title),
                style = type.heading,
                color = colors.ink,
                modifier = Modifier.padding(top = LibraryDimens.spaceXs, bottom = LibraryDimens.spaceXl).semantics { heading() },
            )
            PaperCard(Modifier.fillMaxWidth(), cornerRadius = LibraryDimens.radiusRow) {
                val placeholder = stringResource(R.string.name_placeholder)
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = type.titleSmall.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canPrint) actions.onName(name) }),
                    modifier = Modifier.fillMaxWidth().padding(LibraryDimens.spaceLg).semantics { this.contentDescription = placeholder },
                    decorationBox = { field ->
                        if (name.isEmpty()) Text(placeholder, style = type.titleSmall, color = colors.inkMuted)
                        field()
                    },
                )
            }
        }
    }
}

@Composable
private fun CardPage(profile: ReaderProfile, onFinish: () -> Unit) {
    val colors = LibraryTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val shelf = profile.favouriteShelf
    val joined = remember(profile.memberSince) {
        Instant.ofEpochMilli(profile.memberSince ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault())
    }
    val data = LibraryCardData(
        holder = profile.name,
        shelfName = shelf?.let { stringResource(it.nameRes) },
        shelfCode = shelf?.code,
        since = joined.format(DateTimeFormatter.ofPattern(MONTH_YEAR_PATTERN, locale)),
        year = joined.year.toString(),
    )
    PageScaffold(
        modifier = Modifier.statusBarsPadding(),
        footer = { PillButton(stringResource(R.string.card_enter), onClick = onFinish, modifier = Modifier.fillMaxWidth()) },
    ) {
        Column(Modifier.padding(horizontal = LibraryDimens.screenPadding, vertical = LibraryDimens.sectionGap)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Daisy(petal = colors.primaryMid)
                Text(
                    stringResource(R.string.card_done),
                    style = LibraryTheme.typography.hand.copy(fontSize = LibraryTheme.typography.heading.fontSize),
                    color = colors.primary,
                    modifier = Modifier.semantics { heading() },
                )
                Daisy()
            }
            LibraryCard(data, Modifier.padding(top = LibraryDimens.sectionGap))
        }
    }
}

// endregion

private const val GRID_COLUMNS = 2
private const val RESULT_HOOK_CHIPS = 3
private const val WELCOME_CARD_ROTATION = -3f
private const val SPRIG_ROTATION = -10f
private const val SPINE_SOFT_ALPHA = 0.55f
private val SPINE_NARROWING = LibraryDimens.spaceXs
private val SPINE_STEP = LibraryDimens.spaceSm

/** Short month and year, e.g. "nov 2026"; LLL is the stand-alone month form. */
private const val MONTH_YEAR_PATTERN = "LLL yyyy"

@Preview(name = "Welcome", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun WelcomePreview() = LibraryTheme { OnboardingContent(OnboardingUiState(), previewActions) }

@Preview(name = "Question 1", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PlanPreview() = LibraryTheme {
    OnboardingContent(OnboardingUiState(page = OnboardingPage.PLAN, draft = QuizDraft(plan = ReadingPlan.PORCH)), previewActions)
}

@Preview(name = "Card", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun CardPreview() = LibraryTheme {
    OnboardingContent(
        OnboardingUiState(
            page = OnboardingPage.CARD,
            profile = ReaderProfile(name = "Sara", memberSince = 1_793_620_800_000, favouriteShelf = ShelfTheme.ROMANTASY),
        ),
        previewActions,
    )
}

private val previewActions = OnboardingActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
