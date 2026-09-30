package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BibleWordRepository(
    private val bibleWordDao: BibleWordDao,
    private val rankingDao: RankingDao,
    private val systemLogDao: SystemLogDao,
    context: Context
) {
    val allWords: Flow<List<BibleWord>> = bibleWordDao.getAllWords()
    val topRankings: Flow<List<RankingEntry>> = rankingDao.getTopRankings(10)
    val allRankings: Flow<List<RankingEntry>> = rankingDao.getAllRankings()
    val allSystemLogs: Flow<List<SystemGameLog>> = systemLogDao.getAllLogs()

    val googleSheetsManager = GoogleSheetsManager(context)

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        val count = bibleWordDao.getWordCount()
        if (count == 0) {
            // Tenta consultar a planilha Google primeiro
            val syncResult = consultSpreadsheetWords()
            if (syncResult.isFailure || bibleWordDao.getWordCount() == 0) {
                bibleWordDao.insertWords(DefaultBibleWords.list)
            }
        } else {
            // Se já há palavras salvas, tenta atualizar silenciosamente com a planilha Google
            consultSpreadsheetWords()
        }
    }

    /**
     * Consulta a aba "Palavras" da planilha Google especificada.
     * Palavra (resposta) na Coluna A e Dica (questão) na Coluna B.
     * Substitui o banco local pelas palavras oficiais da planilha.
     */
    suspend fun consultSpreadsheetWords(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val result = googleSheetsManager.fetchWordsFromSheet()
            if (result.isSuccess) {
                val words = result.getOrNull() ?: emptyList()
                if (words.isNotEmpty()) {
                    bibleWordDao.deleteAll()
                    bibleWordDao.insertWords(words)
                    Log.i("BibleWordRepository", "Sincronizadas com sucesso ${words.size} palavras da planilha.")
                    return@withContext Result.success(words.size)
                }
            } else {
                val ex = result.exceptionOrNull()
                Log.w("BibleWordRepository", "Falha ao buscar palavras da planilha: ${ex?.message}")
                return@withContext Result.failure(ex ?: Exception("Falha na sincronização da planilha."))
            }
            Result.failure(Exception("Nenhuma palavra encontrada na aba 'Palavras' da planilha."))
        } catch (e: Exception) {
            Log.w("BibleWordRepository", "Falha ao consultar planilha online: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Registra o ranking na aba "Rankings" e salva localmente
     * Colunas: Data, Hora, Nome, Pontuação, Tipo de Jogo, Nome da Equipe, Nomes dos Jogadores, Posição no Ranking
     */
    suspend fun recordRanking(
        playerName: String,
        score: Int,
        correctWords: Int,
        gameMode: String,
        teamName: String = "-",
        playerNames: String = "-"
    ): Pair<Long, Int> = withContext(Dispatchers.IO) {
        val now = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val dateStr = dateFormat.format(now)
        val timeStr = timeFormat.format(now)

        // Calcula a posição no ranking com base na pontuação
        val currentAll = rankingDao.getAllRankingsList()
        val rankPos = currentAll.count { it.score > score } + 1

        val entry = RankingEntry(
            date = dateStr,
            time = timeStr,
            playerName = playerName,
            score = score,
            correctWords = correctWords,
            gameMode = gameMode,
            teamName = teamName,
            playerNames = playerNames,
            rankPosition = rankPos,
            timestamp = now.time
        )

        val insertedId = rankingDao.insertRanking(entry)

        // Envia para a aba "Rankings" da planilha Google
        try {
            googleSheetsManager.postRankingToSheet(entry)
        } catch (e: Exception) {
            Log.e("BibleWordRepository", "Erro ao postar ranking no Google Sheets: ${e.message}")
        }

        Pair(insertedId, rankPos)
    }

    /**
     * Registra a partida na aba "Sistema" e salva localmente
     * Colunas: ID da partida, Data e Hora, Tipo de jogo, Jogadores e Equipes, Equipe vencedora, Perguntas e respostas dadas, Pontuação final
     */
    suspend fun recordSystemLog(
        gameId: String,
        gameMode: String,
        playersAndTeams: String,
        winningTeam: String,
        questionsAndAnswers: String,
        finalScore: Int
    ): Long = withContext(Dispatchers.IO) {
        val now = Date()
        val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateTimeFormat.format(now)

        val log = SystemGameLog(
            gameId = gameId,
            date = dateStr,
            gameMode = gameMode,
            playersAndTeams = playersAndTeams,
            winningTeam = winningTeam,
            questionsAndAnswers = questionsAndAnswers,
            finalScore = finalScore,
            timestamp = now.time
        )

        val logId = systemLogDao.insertLog(log)

        // Envia para a aba "Sistema" da planilha Google
        try {
            googleSheetsManager.postSystemLogToSheet(log)
        } catch (e: Exception) {
            Log.e("BibleWordRepository", "Erro ao postar log do sistema no Google Sheets: ${e.message}")
        }

        logId
    }

    suspend fun getAllRankingsList(): List<RankingEntry> = withContext(Dispatchers.IO) {
        rankingDao.getAllRankingsList()
    }
}
