package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "2048 挑戰者",
    val avatar: String = "🎮",
    val country: String = "🇹🇼",
    val tier: String = "黃金 III",
    val rp: Int = 1250,
    val gamesPlayed: Int = 0,
    val battlesWon: Int = 0,
    val bestScoreClassic: Int = 0,
    val highestTile: Int = 0,
    val selectedTheme: String = "classic",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val score: Int,
    val maxTile: Int,
    val isWin: Boolean,
    val timestamp: Long
)

@Entity(tableName = "battle_history")
data class BattleHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opponentName: String,
    val opponentAvatar: String,
    val opponentScore: Int,
    val userScore: Int,
    val isWin: Boolean,
    val rpChange: Int,
    val timestamp: Long
)
