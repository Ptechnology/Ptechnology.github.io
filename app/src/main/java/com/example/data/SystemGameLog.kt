package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemGameLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: String,           // ID da partida
    val date: String,             // Data e hora
    val gameMode: String,         // Tipo de jogo (Solo ou Multi-Jogador)
    val playersAndTeams: String,  // Nomes dos jogadores e suas equipes
    val winningTeam: String,      // Equipe vencedora (no caso de Multi-Jogador)
    val questionsAndAnswers: String, // Perguntas e respostas dadas
    val finalScore: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class RoundRecord(
    val roundNumber: Int,
    val teamOrPlayer: String,
    val questionClue: String,
    val correctWord: String,
    val givenAnswer: String,
    val isCorrect: Boolean,
    val pointsAwarded: Int,
    val timeRemaining: Int
)
