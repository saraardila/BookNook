package com.nawin.booknook.presentation.ui.account

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nawin.booknook.R
import com.nawin.booknook.domain.model.AccountError
import com.nawin.booknook.presentation.components.library.LaceHeader
import com.nawin.booknook.presentation.components.library.PaperCard
import com.nawin.booknook.presentation.components.library.PillButton
import com.nawin.booknook.presentation.components.library.RoundIconButton
import com.nawin.booknook.presentation.components.library.SegmentedPills
import com.nawin.booknook.presentation.theme.LibraryDimens
import com.nawin.booknook.presentation.theme.LibraryTheme
import org.koin.compose.viewmodel.koinViewModel

/** Where the account form is shown; it changes the wording and what "done" leads to. */
enum class AccountEntry { ONBOARDING, SETTINGS }

/**
 * Create an account or sign in (P03).
 *
 * @param onDone Called when she is signed in, or has read the "confirm your email" note.
 * @param onSkip Only in the welcome flow: carry on without an account.
 */
@Composable
fun AccountScreen(
    entry: AccountEntry,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onSkip: (() -> Unit)? = null,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) {
        if (state.done == AccountDone.SIGNED_IN) onDone()
    }
    AccountContent(
        state = state,
        entry = entry,
        actions = AccountActions(
            onBack = onBack,
            onMode = viewModel::selectMode,
            onName = viewModel::updateName,
            onEmail = viewModel::updateEmail,
            onPassword = viewModel::updatePassword,
            onSubmit = viewModel::submit,
            onDone = onDone,
            onSkip = onSkip,
        ),
    )
}

private data class AccountActions(
    val onBack: () -> Unit,
    val onMode: (AccountMode) -> Unit,
    val onName: (String) -> Unit,
    val onEmail: (String) -> Unit,
    val onPassword: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onDone: () -> Unit,
    val onSkip: (() -> Unit)?,
)

@Composable
private fun AccountContent(state: AccountUiState, entry: AccountEntry, actions: AccountActions) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val checkEmail = state.done == AccountDone.CHECK_EMAIL

    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box {
                LaceHeader(height = LibraryDimens.headerHeightCompact + statusBarTop)
                Column(Modifier.padding(start = LibraryDimens.screenPadding, end = LibraryDimens.screenPadding, top = statusBarTop + LibraryDimens.spaceLg)) {
                    // Back and the handwritten line share a row so the title fits inside the compact lace.
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd)) {
                        RoundIconButton(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back), onClick = actions.onBack)
                        Text(
                            stringResource(if (entry == AccountEntry.ONBOARDING) R.string.account_tagline_onboarding else R.string.account_tagline_settings),
                            style = type.hand,
                            color = colors.stamp,
                        )
                    }
                    Text(
                        stringResource(if (state.mode == AccountMode.SIGN_UP) R.string.account_title_sign_up else R.string.account_title_sign_in),
                        style = type.heading,
                        color = colors.ink,
                        modifier = Modifier.padding(top = LibraryDimens.spaceSm).semantics { heading() },
                    )
                }
            }
            Column(
                Modifier.padding(
                    start = LibraryDimens.screenPadding,
                    end = LibraryDimens.screenPadding,
                    top = LibraryDimens.spaceXl,
                    bottom = LibraryDimens.spaceLg,
                ),
                verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceMd),
            ) {
                if (checkEmail) {
                    CheckEmailNote(state.email)
                } else {
                    AccountForm(state, actions)
                }
            }
        }
        Column(
            Modifier.fillMaxWidth().padding(LibraryDimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceSm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (checkEmail) {
                PillButton(
                    stringResource(if (entry == AccountEntry.ONBOARDING) R.string.account_continue_to_test else R.string.account_got_it),
                    onClick = actions.onDone,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                PillButton(
                    text = if (state.isWorking) stringResource(R.string.account_working) else stringResource(submitLabel(state.mode, entry)),
                    onClick = actions.onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth(),
                )
                actions.onSkip?.let { skip ->
                    PillButton(stringResource(R.string.account_skip), onClick = skip, primary = false, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@StringRes
private fun submitLabel(mode: AccountMode, entry: AccountEntry): Int = when {
    mode == AccountMode.SIGN_UP && entry == AccountEntry.ONBOARDING -> R.string.account_submit_sign_up_test
    mode == AccountMode.SIGN_IN && entry == AccountEntry.ONBOARDING -> R.string.account_submit_sign_in_test
    mode == AccountMode.SIGN_UP -> R.string.account_mode_sign_up
    else -> R.string.account_mode_sign_in
}

@Composable
private fun ColumnScope.AccountForm(state: AccountUiState, actions: AccountActions) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    SegmentedPills(
        options = listOf(stringResource(R.string.account_mode_sign_up), stringResource(R.string.account_mode_sign_in)),
        selectedIndex = state.mode.ordinal,
        onSelect = { actions.onMode(AccountMode.entries[it]) },
    )
    if (state.mode == AccountMode.SIGN_UP) {
        AccountField(
            label = stringResource(R.string.account_name),
            value = state.name,
            onValueChange = actions.onName,
            contentType = ContentType.PersonFullName,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        )
    }
    AccountField(
        label = stringResource(R.string.account_email),
        value = state.email,
        onValueChange = actions.onEmail,
        placeholder = stringResource(R.string.account_email_placeholder),
        contentType = ContentType.EmailAddress + ContentType.Username,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, autoCorrectEnabled = false, imeAction = ImeAction.Next),
    )
    PasswordField(state, actions)
    state.error?.let { error ->
        Text(
            stringResource(error.messageRes),
            style = type.body,
            color = colors.stamp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
    Text(
        stringResource(R.string.account_note),
        style = type.caption,
        color = colors.inkMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(state: AccountUiState, actions: AccountActions) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val signingUp = state.mode == AccountMode.SIGN_UP
    AccountField(
        label = stringResource(R.string.account_password),
        value = state.password,
        onValueChange = actions.onPassword,
        hint = if (signingUp) pluralStringResource(R.plurals.account_password_hint, AccountUiState.MIN_PASSWORD_LENGTH, AccountUiState.MIN_PASSWORD_LENGTH) else null,
        contentType = if (signingUp) ContentType.NewPassword else ContentType.Password,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { actions.onSubmit() }),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(if (visible) R.string.cd_hide_password else R.string.cd_show_password),
                    tint = LibraryTheme.colors.inkMuted,
                )
            }
        },
    )
}

/** A labelled text field on paper, like the name page of the welcome flow. */
@Composable
private fun AccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    contentType: ContentType,
    keyboardOptions: KeyboardOptions,
    placeholder: String? = null,
    hint: String? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LibraryTheme.colors
    val type = LibraryTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(LibraryDimens.spaceXs)) {
        Text(label, style = type.label, color = colors.inkMuted)
        PaperCard(Modifier.fillMaxWidth(), cornerRadius = LibraryDimens.radiusRow) {
            Row(
                Modifier.heightIn(min = LibraryDimens.buttonMinHeight).padding(start = LibraryDimens.spaceLg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = type.body.copy(color = colors.ink),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = LibraryDimens.spaceMd)
                        .padding(end = if (trailing == null) LibraryDimens.spaceLg else LibraryDimens.spaceXs)
                        .semantics {
                            this.contentType = contentType
                            this.contentDescription = label
                        },
                    decorationBox = { field ->
                        if (value.isEmpty() && placeholder != null) Text(placeholder, style = type.body, color = colors.inkMuted)
                        field()
                    },
                )
                trailing?.invoke()
            }
        }
        hint?.let { Text(it, style = type.caption, color = colors.inkMuted) }
    }
}

@Composable
private fun CheckEmailNote(email: String) {
    val colors = LibraryTheme.colors
    PaperCard(Modifier.fillMaxWidth(), color = colors.primaryLight) {
        Text(
            stringResource(R.string.account_check_email, email.trim()),
            style = LibraryTheme.typography.body,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(LibraryDimens.spaceLg).semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

private val AccountError.messageRes: Int
    @StringRes get() = when (this) {
        AccountError.INVALID_CREDENTIALS -> R.string.account_error_credentials
        AccountError.EMAIL_TAKEN -> R.string.account_error_email_taken
        AccountError.WEAK_PASSWORD -> R.string.account_error_weak_password
        AccountError.INVALID_EMAIL -> R.string.account_error_invalid_email
        AccountError.EMAIL_NOT_CONFIRMED -> R.string.account_error_not_confirmed
        AccountError.TOO_MANY_ATTEMPTS -> R.string.account_error_too_many
        AccountError.NO_CONNECTION -> R.string.account_error_connection
        AccountError.NOT_AVAILABLE, AccountError.UNKNOWN -> R.string.account_error_unknown
    }

private val previewActions = AccountActions({}, {}, {}, {}, {}, {}, {}, {})

@Preview(name = "Sign up", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SignUpPreview() = LibraryTheme {
    AccountContent(AccountUiState(name = "Sara", email = "sara@"), AccountEntry.ONBOARDING, previewActions)
}

@Preview(name = "Sign in with error", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun SignInPreview() = LibraryTheme {
    AccountContent(
        AccountUiState(mode = AccountMode.SIGN_IN, email = "sara@example.com", password = "secret", error = AccountError.INVALID_CREDENTIALS),
        AccountEntry.SETTINGS,
        previewActions,
    )
}

@Preview(name = "Check email", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun CheckEmailPreview() = LibraryTheme {
    AccountContent(AccountUiState(email = "sara@example.com", done = AccountDone.CHECK_EMAIL), AccountEntry.ONBOARDING, previewActions)
}
