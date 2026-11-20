package com.nawin.booknook

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.nawin.booknook.presentation.navigation.LibraryBottomBar
import com.nawin.booknook.presentation.navigation.NavGraph
import com.nawin.booknook.presentation.theme.BookNookTheme
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.theme.libraryColors
import com.nawin.booknook.presentation.ui.main.MainViewModel
import com.nawin.booknook.presentation.ui.onboarding.OnboardingScreen
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Single activity. It extends AppCompatActivity because AppCompat applies the
 * per-app language chosen in Settings (and stores it on Android 12 and older).
 */
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            viewModel.preferences.value == null
        }
        setContent {
            // The splash screen stays up until this is loaded, so null never draws a frame.
            val preferences = viewModel.preferences.collectAsStateWithLifecycle().value ?: return@setContent

            // The shelf theme recolours both the redesign tokens (LibraryTheme) and
            // Material, which the dialogs and the screens not redesigned yet still use.
            val colors = remember(preferences.theme) { preferences.theme.libraryColors() }
            BookNookTheme(colors) {
                LibraryTheme(colors) {
                    if (!preferences.onboardingDone) {
                        OnboardingScreen()
                        return@LibraryTheme
                    }
                    val navController = rememberNavController()
                    // No top inset here: redesigned screens draw their header behind
                    // the status bar; NavGraph pads the legacy screens itself.
                    Scaffold(
                        bottomBar = { LibraryBottomBar(navController) },
                        contentWindowInsets = WindowInsets(0),
                        containerColor = LibraryTheme.colors.background,
                    ) { paddingValues ->
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()),
                        )
                    }
                }
            }
        }
    }
}
