package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleWordDao {
    @Query("SELECT * FROM bible_words ORDER BY id ASC")
    fun getAllWords(): Flow<List<BibleWord>>

    @Query("SELECT * FROM bible_words")
    suspend fun getWordsList(): List<BibleWord>

    @Query("SELECT COUNT(*) FROM bible_words")
    suspend fun getWordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<BibleWord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: BibleWord)

    @Query("DELETE FROM bible_words")
    suspend fun deleteAll()
}
