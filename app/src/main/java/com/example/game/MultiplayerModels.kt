package com.example.game

data class Player(
    val id: String,
    val name: String,
    var teamId: Int = 1,
    var score: Int = 0
)

data class Team(
    val id: Int,
    val name: String,
    val colorHex: Long,
    val members: MutableList<Player> = mutableListOf(),
    var totalScore: Int = 0
) {
    val memberCount: Int
        get() = members.size
}

enum class GameMode {
    SOLO,
    MULTIPLAYER
}
