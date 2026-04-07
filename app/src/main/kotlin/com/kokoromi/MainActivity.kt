package com.kokoromi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.data.model.ThemePreference
import com.kokoromi.data.repository.PreferencesRepository
import com.kokoromi.ui.KokoromiNavigation
import com.kokoromi.ui.theme.KokoromiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by preferencesRepository.getUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (prefs?.theme) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                else -> systemDark
            }
            KokoromiTheme(darkTheme = darkTheme) {
                KokoromiNavigation()
            }
        }
    }
}
