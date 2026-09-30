package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_words")
data class BibleWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val clue: String,
    val reference: String = "",
    val category: String = "Bíblico"
)
