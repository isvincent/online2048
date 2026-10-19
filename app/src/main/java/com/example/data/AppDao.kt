package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Insert
    suspend fun insertGameRecord(record: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 30")
    fun getRecentGameRecords(): Flow<List<GameRecordEntity>>

    @Query("SELECT MAX(score) FROM game_records WHERE mode = 'CLASSIC'")
    suspend fun getHighestClassicScore(): Int?

    @Insert
    suspend fun insertBattleHistory(history: BattleHistoryEntity)

    @Query("SELECT * FROM battle_history ORDER BY timestamp DESC LIMIT 20")
    fun getBattleHistory(): Flow<List<BattleHistoryEntity>>
}
