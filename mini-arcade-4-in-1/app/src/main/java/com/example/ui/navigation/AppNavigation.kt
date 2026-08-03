package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.ArcadeViewModel
import com.example.ui.games.blockblast.BlockBlastGame
import com.example.ui.games.funrace.FunRaceGame
import com.example.ui.games.pizzaready.PizzaReadyGame
import com.example.ui.games.sliceit.SliceItGame
import com.example.ui.hub.GameHubScreen

object Routes {
    const val HUB = "hub"
    const val BLOCK_BLAST = "block_blast"
    const val SLICE_IT = "slice_it"
    const val PIZZA_READY = "pizza_ready"
    const val FUN_RACE = "fun_race"
}

@Composable
fun AppNavigation(viewModel: ArcadeViewModel) {
    val navController = rememberNavController()

    val wallet by viewModel.wallet.collectAsStateWithLifecycle()
    val gameStats by viewModel.gameStats.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val dailyMissions by viewModel.dailyMissions.collectAsStateWithLifecycle()
    val gameHistory by viewModel.gameHistory.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.HUB
    ) {
        composable(Routes.HUB) {
            GameHubScreen(
                wallet = wallet,
                gameStats = gameStats,
                userSettings = userSettings,
                dailyMissions = dailyMissions,
                gameHistory = gameHistory,
                onLaunchGame = { gameId ->
                    navController.navigate(gameId)
                },
                onClaimMissionReward = { missionId ->
                    viewModel.claimMissionReward(missionId)
                },
                onBuySkin = { type, skinId, cost ->
                    viewModel.buySkin(type, skinId, cost) { success ->
                        // Optional feedback
                    }
                },
                onToggleSound = { enabled ->
                    viewModel.setSoundEnabled(enabled)
                },
                onToggleMusic = { enabled ->
                    viewModel.setMusicEnabled(enabled)
                },
                onToggleDarkMode = { enabled ->
                    viewModel.setDarkMode(enabled)
                },
                onSelectLanguage = { lang ->
                    viewModel.setLanguage(lang)
                },
                onLoginGoogle = { email, name ->
                    viewModel.loginGoogle(email, name)
                },
                onLogoutGoogle = {
                    viewModel.logoutGoogle()
                }
            )
        }

        composable(Routes.BLOCK_BLAST) {
            val stat = gameStats.find { it.gameId == "block_blast" }
            BlockBlastGame(
                highScore = stat?.highScore ?: 0,
                currentTheme = userSettings.blockTheme,
                isTr = userSettings.language.uppercase() == "TR",
                soundEnabled = userSettings.soundEnabled,
                onSelectTheme = { theme ->
                    viewModel.setBlockTheme(theme)
                },
                onBack = { navController.popBackStack() },
                onGameOver = { score, coins ->
                    viewModel.recordGameScore("block_blast", "Blok Patlat", score, coins)
                }
            )
        }

        composable(Routes.SLICE_IT) {
            val stat = gameStats.find { it.gameId == "slice_it" }
            SliceItGame(
                highScore = stat?.highScore ?: 0,
                onBack = { navController.popBackStack() },
                onGameOver = { score, coins ->
                    viewModel.recordGameScore("slice_it", "Slice It All", score, coins)
                }
            )
        }

        composable(Routes.PIZZA_READY) {
            val stat = gameStats.find { it.gameId == "pizza_ready" }
            PizzaReadyGame(
                highScore = stat?.highScore ?: 0,
                onBack = { navController.popBackStack() },
                onGameOver = { score, coins ->
                    viewModel.recordGameScore("pizza_ready", "Pizza Ready", score, coins)
                }
            )
        }

        composable(Routes.FUN_RACE) {
            val stat = gameStats.find { it.gameId == "fun_race" }
            FunRaceGame(
                highScore = stat?.highScore ?: 0,
                onBack = { navController.popBackStack() },
                onGameOver = { score, coins ->
                    viewModel.recordGameScore("fun_race", "Fun Race 3D", score, coins)
                }
            )
        }
    }
}
