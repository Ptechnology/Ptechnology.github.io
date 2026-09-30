package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BibleWord
import com.example.data.BibleWordRepository
import com.example.data.RankingEntry
import com.example.data.RoundRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BibleWordRepository
    private var timerJob: Job? = null

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val topRankings: StateFlow<List<RankingEntry>>
    val allRankings: StateFlow<List<RankingEntry>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BibleWordRepository(
            database.bibleWordDao(),
            database.rankingDao(),
            database.systemLogDao(),
            application
        )

        topRankings = repository.topRankings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allRankings = repository.allRankings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        _gameState.update {
            it.copy(
                sheetId = repository.googleSheetsManager.sheetId,
                appsScriptUrl = repository.googleSheetsManager.appsScriptWebhookUrl,
                lastSyncCount = repository.googleSheetsManager.lastSyncCount,
                lastSyncTime = repository.googleSheetsManager.lastSyncTime
            )
        }

        viewModelScope.launch {
            repository.initializeDatabase()
            repository.allWords.collect { words ->
                _gameState.update { it.copy(allWords = words) }
            }
        }
    }

    /**
     * Consulta a planilha Google especificada (aba "Palavras", colunas A e B)
     */
    fun refreshWordsFromSpreadsheet(showFeedback: Boolean = false) {
        viewModelScope.launch {
            _gameState.update { it.copy(isSyncing = true, syncMessage = if (showFeedback) "Conectando à planilha Google..." else null) }
            val result = repository.consultSpreadsheetWords()
            val gsm = repository.googleSheetsManager
            if (result.isSuccess) {
                val count = result.getOrNull() ?: 0
                _gameState.update {
                    it.copy(
                        isSyncing = false,
                        syncSuccess = true,
                        syncMessage = "$count palavras e dicas carregadas com sucesso da aba 'Palavras'!",
                        lastSyncCount = count,
                        lastSyncTime = gsm.lastSyncTime
                    )
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro ao conectar à planilha"
                _gameState.update {
                    it.copy(
                        isSyncing = false,
                        syncSuccess = false,
                        syncMessage = error
                    )
                }
            }
        }
    }

    fun updateSheetConfig(newSheetId: String, newAppsScriptUrl: String) {
        repository.googleSheetsManager.sheetId = newSheetId
        repository.googleSheetsManager.appsScriptWebhookUrl = newAppsScriptUrl
        _gameState.update {
            it.copy(
                sheetId = repository.googleSheetsManager.sheetId,
                appsScriptUrl = repository.googleSheetsManager.appsScriptWebhookUrl
            )
        }
        refreshWordsFromSpreadsheet(showFeedback = true)
    }

    fun getAppsScriptTemplateCode(): String {
        return com.example.data.GoogleSheetsManager.APPS_SCRIPT_TEMPLATE
    }

    fun navigateTo(screen: ScreenState) {
        if (screen == ScreenState.HOME) {
            refreshWordsFromSpreadsheet()
        }
        _gameState.update { it.copy(currentScreen = screen) }
    }

    fun setPlayerName(name: String) {
        _gameState.update { it.copy(playerName = name) }
    }

    fun setGameMode(mode: GameMode) {
        _gameState.update { it.copy(gameMode = mode) }
    }

    fun setRoomCode(code: String) {
        _gameState.update { it.copy(roomCode = code.uppercase()) }
    }

    fun createMultiplayerRoom() {
        val code = WordUtils.generateRoomCode()
        val defaultTeams = createDefaultTeams()
        _gameState.update {
            it.copy(
                roomCode = code,
                isHost = true,
                teams = defaultTeams,
                currentScreen = ScreenState.MULTIPLAYER_LOBBY
            )
        }
    }

    fun joinMultiplayerRoom(code: String) {
        val defaultTeams = createDefaultTeams()
        _gameState.update {
            it.copy(
                roomCode = code.uppercase(),
                isHost = false,
                teams = defaultTeams,
                currentScreen = ScreenState.MULTIPLAYER_LOBBY
            )
        }
    }

    private fun createDefaultTeams(): List<Team> {
        return listOf(
            Team(1, "Leão de Judá", 0xFF1E3A8A),
            Team(2, "Monte Sião", 0xFFD97706),
            Team(3, "Oliveiras", 0xFF3F6212),
            Team(4, "Cedros do Líbano", 0xFF991B1B)
        )
    }

    /**
     * Distribui jogadores automaticamente entre até 4 equipes de 5 componentes
     */
    fun distributePlayers(playerNames: List<String>, teamCount: Int) {
        val clampedCount = teamCount.coerceIn(2, 4)
        val defaultTeams = createDefaultTeams().take(clampedCount).map {
            it.copy(members = mutableListOf())
        }

        val allNames = mutableListOf<String>()
        val mainPlayer = _gameState.value.playerName.ifBlank { "Jogador 1" }
        allNames.add(mainPlayer)

        for (name in playerNames) {
            val trimmed = name.trim()
            if (trimmed.isNotBlank() && !allNames.contains(trimmed)) {
                allNames.add(trimmed)
            }
        }

        // Limita a no máximo teamCount * 5 componentes (máx 5 por equipe)
        val maxTotalPlayers = clampedCount * 5
        val validPlayers = allNames.take(maxTotalPlayers)

        // Distribuição balanceada automática
        for (i in validPlayers.indices) {
            val teamIndex = i % clampedCount
            val p = Player(
                id = "p_$i",
                name = validPlayers[i],
                teamId = defaultTeams[teamIndex].id
            )
            defaultTeams[teamIndex].members.add(p)
        }

        _gameState.update {
            it.copy(
                teams = defaultTeams,
                pendingPlayers = validPlayers
            )
        }
    }

    /**
     * Inicia a partida:
     * - As palavras não se repetem no mesmo jogo
     * - Ao serem usadas todas as palavras cadastradas, o jogo termina
     */
    fun startGame() {
        val words = _gameState.value.allWords.shuffled()
        if (words.isEmpty()) {
            _gameState.update {
                it.copy(syncMessage = "Nenhuma palavra disponível. Verifique a conexão com a planilha.")
            }
            return
        }

        val firstWord = words.first()
        val revealed = WordUtils.computeInitialRevealedIndices(firstWord.word)
        val gameId = "P-" + UUID.randomUUID().toString().take(8).uppercase()

        _gameState.update {
            it.copy(
                currentScreen = ScreenState.GAME_PLAY,
                gameId = gameId,
                allWords = words,
                currentWordIndex = 0,
                currentWord = firstWord,
                revealedIndices = revealed,
                userGuessedChars = emptySet(),
                timeRemaining = 60,
                isTimerActive = true,
                isTimeExpired = false,
                lives = 3,
                score = 0,
                correctAnswersCount = 0,
                streak = 0,
                isGameOver = false,
                answerFeedback = null,
                lastSavedRankingId = null,
                lastPlayerPosition = null,
                winningTeamName = null,
                roundHistory = emptyList(),
                currentTeamIndex = 0,
                currentRoundNumber = 1
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_gameState.value.timeRemaining > 0 && _gameState.value.isTimerActive) {
                delay(1000)
                _gameState.update {
                    if (it.timeRemaining > 0 && it.isTimerActive) {
                        val newTime = it.timeRemaining - 1
                        it.copy(timeRemaining = newTime)
                    } else it
                }
            }

            // Se o tempo terminou (0s), bloqueia e dá a resposta como INCORRETA!
            if (_gameState.value.timeRemaining == 0 && _gameState.value.isTimerActive) {
                handleTimeExpired()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _gameState.update { it.copy(isTimerActive = false) }
    }

    /**
     * Tratamento de tempo esgotado:
     * - Impede resposta após término do tempo
     * - Dá a resposta como incorreta (-1 vida)
     * - Elimina a regra dos 5 pontos
     */
    private fun handleTimeExpired() {
        stopTimer()
        _gameState.update { it.copy(isTimeExpired = true) }

        val state = _gameState.value
        val word = state.currentWord ?: return

        // Registra rodada com tempo esgotado
        val currentActor = if (state.gameMode == GameMode.MULTIPLAYER) {
            state.currentTeam?.name ?: "Equipe"
        } else {
            state.playerName.ifBlank { "Jogador" }
        }

        val record = RoundRecord(
            roundNumber = state.currentWordIndex + 1,
            teamOrPlayer = currentActor,
            questionClue = word.clue,
            correctWord = word.word,
            givenAnswer = "(Tempo Esgotado)",
            isCorrect = false,
            pointsAwarded = 0,
            timeRemaining = 0
        )

        val updatedHistory = state.roundHistory + record
        val newLives = state.lives - 1

        if (newLives <= 0) {
            _gameState.update {
                it.copy(
                    lives = 0,
                    streak = 0,
                    isGameOver = true,
                    roundHistory = updatedHistory,
                    answerFeedback = AnswerFeedback(
                        isCorrect = false,
                        pointsAwarded = 0,
                        bonusTimePoints = 0,
                        message = "Tempo de 60s esgotado! Resposta considerada incorreta. Vidas esgotadas.",
                        isTimeout = true
                    )
                )
            }
            saveCurrentGameResults()
        } else {
            _gameState.update {
                it.copy(
                    lives = newLives,
                    streak = 0,
                    roundHistory = updatedHistory,
                    answerFeedback = AnswerFeedback(
                        isCorrect = false,
                        pointsAwarded = 0,
                        bonusTimePoints = 0,
                        message = "Tempo de 60s esgotado! Resposta considerada incorreta (-1 vida).",
                        isTimeout = true
                    )
                )
            }
        }
    }

    fun submitFullWordGuess(guess: String) {
        val state = _gameState.value
        val word = state.currentWord?.word ?: return
        if (state.isGameOver || state.isTimeExpired) return

        val normGuess = WordUtils.normalizeText(guess)
        val normWord = WordUtils.normalizeText(word)

        if (normGuess == normWord) {
            handleCorrectAnswer(guess)
        } else {
            handleIncorrectAnswer("Palavra informada incorreta! Você perdeu 1 vida.", guess)
        }
    }

    fun guessLetter(letter: Char) {
        val state = _gameState.value
        val word = state.currentWord?.word ?: return
        if (state.isGameOver || state.isTimeExpired) return

        val upperLetter = WordUtils.normalizeText(letter.toString()).firstOrNull() ?: letter.uppercaseChar()
        if (state.userGuessedChars.contains(upperLetter)) return

        val updatedGuessedChars = state.userGuessedChars + upperLetter
        val normWord = WordUtils.normalizeText(word)

        if (normWord.contains(upperLetter)) {
            _gameState.update { it.copy(userGuessedChars = updatedGuessedChars) }
            if (WordUtils.isWordFullyGuessed(word, state.revealedIndices, updatedGuessedChars)) {
                handleCorrectAnswer("Letra: $upperLetter (Palavra completada)")
            }
        } else {
            _gameState.update { it.copy(userGuessedChars = updatedGuessedChars) }
            handleIncorrectAnswer("A letra '$upperLetter' não está na palavra! -1 vida.", "Letra: $upperLetter")
        }
    }

    private fun handleCorrectAnswer(userAnswerText: String) {
        stopTimer()
        val state = _gameState.value
        val remaining = state.timeRemaining

        // Pontuação: 10 pontos + segundos restantes de tempo bônus
        val pointsAwarded = 10 + remaining

        val newCorrectCount = state.correctAnswersCount + 1
        var newLives = state.lives
        var gainedExtraLife = false

        // A cada 20 acertos ganha 1 vida extra até o limite máximo de 5 vidas
        if (newCorrectCount % 20 == 0 && newLives < 5) {
            newLives++
            gainedExtraLife = true
        }

        val newScore = state.score + pointsAwarded

        // Atualiza pontuação da equipe atual no multiplayer
        val updatedTeams = state.teams.mapIndexed { idx, team ->
            if (idx == state.currentTeamIndex) {
                team.copy(totalScore = team.totalScore + pointsAwarded)
            } else team
        }

        // Registra histórico da rodada
        val currentActor = if (state.gameMode == GameMode.MULTIPLAYER) {
            state.currentTeam?.name ?: "Equipe"
        } else {
            state.playerName.ifBlank { "Jogador" }
        }

        val record = RoundRecord(
            roundNumber = state.currentWordIndex + 1,
            teamOrPlayer = currentActor,
            questionClue = state.currentWord?.clue ?: "",
            correctWord = state.currentWord?.word ?: "",
            givenAnswer = userAnswerText,
            isCorrect = true,
            pointsAwarded = pointsAwarded,
            timeRemaining = remaining
        )

        _gameState.update {
            it.copy(
                score = newScore,
                correctAnswersCount = newCorrectCount,
                lives = newLives,
                streak = it.streak + 1,
                teams = updatedTeams,
                roundHistory = it.roundHistory + record,
                answerFeedback = AnswerFeedback(
                    isCorrect = true,
                    pointsAwarded = pointsAwarded,
                    bonusTimePoints = remaining,
                    message = "Excelente memorização! +$pointsAwarded pontos (+10 base +$remaining bônus de tempo)!",
                    gainedExtraLife = gainedExtraLife
                )
            )
        }
    }

    private fun handleIncorrectAnswer(message: String, userAnswerText: String) {
        val state = _gameState.value
        val newLives = state.lives - 1

        val currentActor = if (state.gameMode == GameMode.MULTIPLAYER) {
            state.currentTeam?.name ?: "Equipe"
        } else {
            state.playerName.ifBlank { "Jogador" }
        }

        val record = RoundRecord(
            roundNumber = state.currentWordIndex + 1,
            teamOrPlayer = currentActor,
            questionClue = state.currentWord?.clue ?: "",
            correctWord = state.currentWord?.word ?: "",
            givenAnswer = userAnswerText,
            isCorrect = false,
            pointsAwarded = 0,
            timeRemaining = state.timeRemaining
        )

        val updatedHistory = state.roundHistory + record

        if (newLives <= 0) {
            stopTimer()
            _gameState.update {
                it.copy(
                    lives = 0,
                    streak = 0,
                    isGameOver = true,
                    roundHistory = updatedHistory,
                    answerFeedback = AnswerFeedback(
                        isCorrect = false,
                        pointsAwarded = 0,
                        bonusTimePoints = 0,
                        message = "Você perdeu todas as vidas! Fim de jogo."
                    )
                )
            }
            saveCurrentGameResults()
        } else {
            _gameState.update {
                it.copy(
                    lives = newLives,
                    streak = 0,
                    roundHistory = updatedHistory,
                    answerFeedback = AnswerFeedback(
                        isCorrect = false,
                        pointsAwarded = 0,
                        bonusTimePoints = 0,
                        message = message
                    )
                )
            }
        }
    }

    fun dismissFeedbackAndNextWord() {
        val state = _gameState.value
        _gameState.update { it.copy(answerFeedback = null) }

        if (state.isGameOver) {
            navigateTo(ScreenState.RANKINGS)
            return
        }

        advanceToNextWord()
    }

    fun skipWordWithPenalty() {
        val state = _gameState.value
        if (state.isGameOver || state.isTimeExpired) return
        handleIncorrectAnswer("Palavra pulada (-1 vida).", "(Palavra Pulada)")
        if (_gameState.value.lives > 0) {
            advanceToNextWord()
        }
    }

    /**
     * Avança para a próxima palavra:
     * - Não repete palavras durante o mesmo jogo
     * - No Multi-jogador, as questões são alternadas entre as equipes na ordem criada,
     *   garantindo oportunidade de todos responderem o mesmo número de questões
     */
    private fun advanceToNextWord() {
        val state = _gameState.value
        val nextIndex = state.currentWordIndex + 1

        // Se foram usadas todas as palavras cadastradas, o jogo termina
        if (nextIndex >= state.allWords.size) {
            stopTimer()
            _gameState.update {
                it.copy(
                    isGameOver = true,
                    answerFeedback = AnswerFeedback(
                        isCorrect = true,
                        pointsAwarded = 0,
                        bonusTimePoints = 0,
                        message = "Todas as palavras da lista foram completadas com sucesso!"
                    )
                )
            }
            saveCurrentGameResults()
            return
        }

        val nextWord = state.allWords[nextIndex]
        val revealed = WordUtils.computeInitialRevealedIndices(nextWord.word)

        // Alterna entre as equipes na ordem dos grupos
        val nextTeamIndex = if (state.teams.isNotEmpty()) {
            (state.currentTeamIndex + 1) % state.teams.size
        } else 0

        val nextRoundNumber = if (state.teams.isNotEmpty()) {
            (nextIndex / state.teams.size) + 1
        } else nextIndex + 1

        _gameState.update {
            it.copy(
                currentWordIndex = nextIndex,
                currentWord = nextWord,
                revealedIndices = revealed,
                userGuessedChars = emptySet(),
                timeRemaining = 60,
                isTimerActive = true,
                isTimeExpired = false,
                currentTeamIndex = nextTeamIndex,
                currentRoundNumber = nextRoundNumber,
                answerFeedback = null
            )
        }

        startTimer()
    }

    /**
     * Salva resultados tanto na aba "Rankings" quanto na aba "Sistema"
     */
    private fun saveCurrentGameResults() {
        viewModelScope.launch {
            val state = _gameState.value
            val isMulti = state.gameMode == GameMode.MULTIPLAYER

            // Determina a equipe vencedora no multi-jogador
            val winningTeam = if (isMulti && state.teams.isNotEmpty()) {
                state.teams.maxByOrNull { it.totalScore }
            } else null

            val winningTeamDesc = if (winningTeam != null) {
                "${winningTeam.name} (${winningTeam.totalScore} pts)"
            } else "-"

            val teamName = if (isMulti) {
                state.currentTeam?.name ?: winningTeam?.name ?: "-"
            } else "-"

            val playerNamesList = if (isMulti) {
                state.teams.joinToString("; ") { team ->
                    "${team.name}: ${team.members.joinToString(", ") { it.name }}"
                }
            } else state.playerName.ifBlank { "Jogador" }

            val playerNameForRanking = if (isMulti) {
                winningTeam?.name ?: state.playerName.ifBlank { "Equipe" }
            } else state.playerName.ifBlank { "Jogador" }

            val scoreForRanking = if (isMulti && winningTeam != null) {
                winningTeam.totalScore
            } else state.score

            // 1. Registra na aba "Rankings"
            val (rankingId, rankPos) = repository.recordRanking(
                playerName = playerNameForRanking,
                score = scoreForRanking,
                correctWords = state.correctAnswersCount,
                gameMode = if (isMulti) "Multi-Jogador" else "Solo",
                teamName = teamName,
                playerNames = playerNamesList
            )

            // 2. Registra na aba "Sistema"
            val qaSummary = state.roundHistory.joinToString("\n") { round ->
                "[#${round.roundNumber} - ${round.teamOrPlayer}] Dica: ${round.questionClue} | Esperada: ${round.correctWord} | Resposta: ${round.givenAnswer} | ${if (round.isCorrect) "ACERTO (+${round.pointsAwarded} pts, ${round.timeRemaining}s rest)" else "ERRO"}"
            }

            repository.recordSystemLog(
                gameId = state.gameId,
                gameMode = if (isMulti) "Multi-Jogador" else "Solo",
                playersAndTeams = playerNamesList,
                winningTeam = winningTeamDesc,
                questionsAndAnswers = qaSummary,
                finalScore = scoreForRanking
            )

            _gameState.update {
                it.copy(
                    lastSavedRankingId = rankingId,
                    lastPlayerPosition = rankPos,
                    winningTeamName = winningTeamDesc
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
