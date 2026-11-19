package com.nawin.booknook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.nawin.booknook.presentation.navigation.LibraryBottomBar
import com.nawin.booknook.presentation.navigation.NavGraph
import com.nawin.booknook.presentation.theme.BookNookTheme
import com.nawin.booknook.presentation.theme.LibraryTheme
import com.nawin.booknook.presentation.ui.main.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            viewModel.preferences.value == null
        }
        setContent {
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()

            // BookNookTheme (Material) still styles the screens not redesigned yet;
            // LibraryTheme provides the redesign's tokens to the migrated ones.
            BookNookTheme(userPreferences = preferences) {
                LibraryTheme {
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
