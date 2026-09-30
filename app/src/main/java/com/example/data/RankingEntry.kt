package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rankings")
data class RankingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,             // Data: dd/MM/yyyy
    val time: String,             // Hora: HH:mm
    val playerName: String,       // Nome
    val score: Int,               // Pontuação
    val gameMode: String,         // Tipo de jogo: "Solo" ou "Multi-Jogador"
    val teamName: String = "-",   // Nome da equipe
    val playerNames: String = "-",// Nomes dos Jogadores (no caso de Multi-Jogador)
    val rankPosition: Int = 0,    // Posição no ranking
    val correctWords: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
