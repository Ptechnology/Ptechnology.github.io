package com.example.game

import com.example.data.BibleWord
import com.example.data.RoundRecord

enum class ScreenState {
    HOME,
    MULTIPLAYER_LOBBY,
    GAME_PLAY,
    RANKINGS,
    ABOUT
}

data class AnswerFeedback(
    val isCorrect: Boolean,
    val pointsAwarded: Int,
    val bonusTimePoints: Int,
    val message: String,
    val isTimeout: Boolean = false,
    val gainedExtraLife: Boolean = false
)

data class GameState(
    val currentScreen: ScreenState = ScreenState.HOME,
    val playerName: String = "",
    val gameMode: GameMode = GameMode.SOLO,
    val roomCode: String = "",
    val isHost: Boolean = true,
    
    // Multiplayer teams
    val teams: List<Team> = emptyList(),
    val currentTeamIndex: Int = 0,
    val pendingPlayers: List<String> = emptyList(),
    val currentRoundNumber: Int = 1,
    
    // Game in progress
    val gameId: String = "",
    val allWords: List<BibleWord> = emptyList(),
    val currentWordIndex: Int = 0,
    val currentWord: BibleWord? = null,
    val revealedIndices: Set<Int> = emptySet(),
    val userGuessedChars: Set<Char> = emptySet(),
    
    // Timer & Scoring
    val timeRemaining: Int = 60,
    val isTimerActive: Boolean = false,
    val isTimeExpired: Boolean = false,
    val lives: Int = 3,
    val score: Int = 0,
    val correctAnswersCount: Int = 0,
    val streak: Int = 0,
    
    // Match History for "Sistema" tab
    val roundHistory: List<RoundRecord> = emptyList(),
    
    // Feedback & End-game
    val isGameOver: Boolean = false,
    val answerFeedback: AnswerFeedback? = null,
    val lastSavedRankingId: Long? = null,
    val lastPlayerPosition: Int? = null,
    val winningTeamName: String? = null,
    
    // Sync state
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val syncSuccess: Boolean? = null,
    val sheetId: String = "1S7Mx8QfuN6IsHLBV7YlHlsg6oZ67koWwvgmP9WrfS40",
    val appsScriptUrl: String = "",
    val lastSyncCount: Int = 0,
    val lastSyncTime: String = ""
) {
    val currentTeam: Team?
        get() = if (gameMode == GameMode.MULTIPLAYER && teams.isNotEmpty()) {
            teams.getOrNull(currentTeamIndex)
        } else null
}
