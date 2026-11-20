package com.nawin.booknook.presentation.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nawin.booknook.data.local.PreferencesDataStore
import com.nawin.booknook.domain.model.ReaderProfile
import com.nawin.booknook.domain.model.ReadingPlan
import com.nawin.booknook.domain.model.ShelfQuiz
import com.nawin.booknook.domain.model.ShelfQuizAnswers
import com.nawin.booknook.domain.model.ShelfTheme
import com.nawin.booknook.domain.model.StoryEnding
import com.nawin.booknook.domain.model.StoryHook
import com.nawin.booknook.domain.model.StoryWorld
import com.nawin.booknook.domain.usecase.GetAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pages of the welcome flow, in order. ACCOUNT is left out when the build has no account server. */
enum class OnboardingPage { WELCOME, ACCOUNT, PLAN, WORLD, HOOKS, ENDING, RESULT, NAME, CARD }

/** The test questions, numbered for the "1 / 4" progress. */
val OnboardingPage.questionNumber: Int?
    get() = when (this) {
        OnboardingPage.PLAN -> 1
        OnboardingPage.WORLD -> 2
        OnboardingPage.HOOKS -> 3
        OnboardingPage.ENDING -> 4
        else -> null
    }

const val QUIZ_QUESTION_COUNT = 4

/** What the reader has answered so far. */
data class QuizDraft(
    val plan: ReadingPlan? = null,
    val world: StoryWorld? = null,
    val hooks: Set<StoryHook> = emptySet(),
    val ending: StoryEnding? = null,
) {
    fun complete(): ShelfQuizAnswers? {
        return ShelfQuizAnswers(plan ?: return null, world ?: return null, hooks, ending ?: return null)
    }
}

data class OnboardingUiState(
    val page: OnboardingPage = OnboardingPage.WELCOME,
    val draft: QuizDraft = QuizDraft(),
    val theme: ShelfTheme = ShelfTheme.STANDARD,
    val profile: ReaderProfile = ReaderProfile(),
) {
    /** Whether the current page has what it needs to move on. */
    val canContinue: Boolean
        get() = when (page) {
            OnboardingPage.PLAN -> draft.plan != null
            OnboardingPage.WORLD -> draft.world != null
            OnboardingPage.ENDING -> draft.ending != null
            else -> true
        }
}

/**
 * The first-launch flow: welcome, the four-question test, its result, the
 * reader's name and the library card. Finishing or skipping it marks it done,
 * and MainActivity then shows the library instead.
 */
class OnboardingViewModel(
    private val preferencesDataStore: PreferencesDataStore,
    getAccount: GetAccountUseCase,
) : ViewModel() {

    /** The pages this build shows, in order. */
    private val pages = OnboardingPage.entries.filter { it != OnboardingPage.ACCOUNT || getAccount.isAvailable }

    private val page = MutableStateFlow(OnboardingPage.WELCOME)
    private val draft = MutableStateFlow(QuizDraft())

    val uiState: StateFlow<OnboardingUiState> =
        combine(page, draft, preferencesDataStore.userPreferences) { page, draft, preferences ->
            OnboardingUiState(page, draft, preferences.theme, preferences.profile)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), OnboardingUiState())

    fun selectPlan(plan: ReadingPlan) = draft.update { it.copy(plan = plan) }

    fun selectWorld(world: StoryWorld) = draft.update { it.copy(world = world) }

    fun toggleHook(hook: StoryHook) = draft.update {
        it.copy(hooks = if (hook in it.hooks) it.hooks - hook else it.hooks + hook)
    }

    fun selectEnding(ending: StoryEnding) = draft.update { it.copy(ending = ending) }

    /**
     * Goes to the next page. Leaving the last question scores the test and
     * saves the result, which also dresses the app in the winning theme.
     */
    fun next() {
        val current = page.value
        if (current == OnboardingPage.ENDING) {
            val answers = draft.value.complete() ?: return
            viewModelScope.launch {
                preferencesDataStore.saveQuizResult(ShelfQuiz.favouriteShelf(answers), answers.hooks)
                page.value = OnboardingPage.RESULT
            }
            return
        }
        pages.getOrNull(pages.indexOf(current) + 1)?.let { page.value = it }
    }

    /** Goes back one page. Returns false on the first page, so the system can close the app. */
    fun back(): Boolean {
        val current = page.value
        if (current == OnboardingPage.WELCOME) return false
        page.value = pages[pages.indexOf(current) - 1]
        return true
    }

    /** On the result page the reader may dress the library in another theme. */
    fun chooseTheme(theme: ShelfTheme) {
        viewModelScope.launch { preferencesDataStore.saveTheme(theme) }
    }

    /** Saves the name and shows the library card. A blank name keeps the one already stored. */
    fun saveName(name: String) {
        viewModelScope.launch {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty()) preferencesDataStore.saveReaderName(trimmed)
            page.value = OnboardingPage.CARD
        }
    }

    /** Ends the flow, from the card or by skipping the test on the welcome page. */
    fun finish() {
        viewModelScope.launch { preferencesDataStore.completeOnboarding() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
