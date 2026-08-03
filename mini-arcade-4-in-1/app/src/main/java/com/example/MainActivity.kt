package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ArcadeViewModel
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MiniArcadeTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ArcadeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
            MiniArcadeTheme(darkTheme = userSettings.darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}


