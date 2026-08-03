package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.theme.MyApplicationTheme

import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.engine.GameState
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.MainGameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.ScoresScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
  private val gameViewModel: GameViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val gameState by gameViewModel.gameState.collectAsState()

        when (gameState) {
          GameState.MENU -> MainMenuScreen(viewModel = gameViewModel)
          GameState.PLAYING, GameState.PAUSED, GameState.GAME_OVER -> MainGameScreen(viewModel = gameViewModel)
          GameState.GARAGE -> GarageScreen(viewModel = gameViewModel)
          GameState.SCORES -> ScoresScreen(viewModel = gameViewModel)
          GameState.SETTINGS -> SettingsScreen(viewModel = gameViewModel)
        }
      }
    }
  }
}
