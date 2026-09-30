package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.game.GameState
import com.example.game.GameViewModel
import com.example.game.ScreenState
import com.example.ui.AboutScreen
import com.example.ui.GamePlayScreen
import com.example.ui.HomeScreen
import com.example.ui.MultiplayerLobbyScreen
import com.example.ui.RankingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val gameState by viewModel.gameState.collectAsState()
                    AppContent(viewModel = viewModel, gameState = gameState)
                }
            }
        }
    }
}

@Composable
fun AppContent(
    viewModel: GameViewModel,
    gameState: GameState
) {
    when (gameState.currentScreen) {
        ScreenState.HOME -> HomeScreen(viewModel = viewModel, gameState = gameState)
        ScreenState.MULTIPLAYER_LOBBY -> MultiplayerLobbyScreen(viewModel = viewModel, gameState = gameState)
        ScreenState.GAME_PLAY -> GamePlayScreen(viewModel = viewModel, gameState = gameState)
        ScreenState.RANKINGS -> RankingsScreen(viewModel = viewModel, gameState = gameState)
        ScreenState.ABOUT -> AboutScreen(viewModel = viewModel)
    }
}
