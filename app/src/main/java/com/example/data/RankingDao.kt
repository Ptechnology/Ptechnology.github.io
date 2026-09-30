package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RankingDao {
    @Query("SELECT * FROM rankings ORDER BY score DESC, timestamp DESC LIMIT :limit")
    fun getTopRankings(limit: Int = 10): Flow<List<RankingEntry>>

    @Query("SELECT * FROM rankings ORDER BY score DESC, timestamp DESC")
    fun getAllRankings(): Flow<List<RankingEntry>>

    @Query("SELECT * FROM rankings ORDER BY score DESC, timestamp DESC")
    suspend fun getAllRankingsList(): List<RankingEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRanking(ranking: RankingEntry): Long

    @Query("DELETE FROM rankings")
    suspend fun clearRankings()
}
