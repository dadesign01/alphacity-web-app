package com.alphacity.stamptour

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.alphacity.stamptour.ui.navigation.AppNavigation
import com.alphacity.stamptour.ui.theme.AlphaCityTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var deepLinkProgramId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        deepLinkProgramId = parseDeepLinkProgramId(intent)

        setContent {
            AlphaCityTheme {
                AppNavigation(deepLinkProgramId = deepLinkProgramId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseDeepLinkProgramId(intent)?.let { programId ->
            deepLinkProgramId = programId
            setContent {
                AlphaCityTheme {
                    AppNavigation(deepLinkProgramId = programId)
                }
            }
        }
    }

    private fun parseDeepLinkProgramId(intent: Intent?): Int? {
        val data = intent?.data ?: return null
        if (data.scheme == "alphacity" && data.host == "program") {
            return data.lastPathSegment?.toIntOrNull()
        }
        return null
    }
}
