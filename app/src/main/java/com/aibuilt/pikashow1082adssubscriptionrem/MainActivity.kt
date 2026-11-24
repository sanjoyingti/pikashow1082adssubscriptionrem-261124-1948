package com.aibuilt.pikashow1082adssubscriptionrem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aibuilt.pikashow1082adssubscriptionrem.ui.PikaNavHost
import com.aibuilt.pikashow1082adssubscriptionrem.ui.theme.PikaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as PikaApp).container
        setContent {
            val settings by container.settingsState.value.collectAsState()
            PikaTheme(themeMode = settings.themeMode) {
                PikaNavHost()
            }
        }
    }
}
