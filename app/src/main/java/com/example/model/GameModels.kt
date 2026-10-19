package com.example.model

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class GameMode {
    CLASSIC,
    BATTLE_1V1,
    DAILY_CHALLENGE
}

enum class BoardSize(val size: Int, val label: String) {
    SIZE_3X3(3, "3x3 極速"),
    SIZE_4X4(4, "4x4 經典"),
    SIZE_5X5(5, "5x5 浩瀚")
}

enum class ThemeOption(val id: String, val title: String, val description: String) {
    CLASSIC("classic", "經典原木", "溫潤典雅的經典 2048 配色"),
    CYBER_NEON("cyber_neon", "賽博霓虹", "高對比的炫彩霓虹與流光特效"),
    COSMIC_DARK("cosmic_dark", "暗黑星空", "深邃星際藍紫，沉浸專注"),
    MACARON("macaron", "馬卡龍甜品", "清新柔和的糖果粉嫩色調"),
    LUXURY_GOLD("luxury_gold", "黑金奢華", "尊貴曜石黑底搭配流金數字")
}

data class Tile(
    val id: Long,
    val value: Int,
    val row: Int,
    val col: Int,
    val isMerged: Boolean = false,
    val isObstacle: Boolean = false, // 障礙物冰塊 (1v1 對戰技能產物)
    val isNew: Boolean = false
)

data class MoveResult(
    val moved: Boolean,
    val scoreEarned: Int,
    val mergedValues: List<Int>,
    val largestTile: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val avatarEmoji: String,
    val countryFlag: String,
    val score: Int,
    val maxTile: Int,
    val tierTitle: String,
    val isUser: Boolean = false
)

enum class BattleStatus {
    IDLE,
    SEARCHING,
    FOUND,
    PLAYING,
    FINISHED
}

data class BattleOpponent(
    val name: String,
    val avatarEmoji: String,
    val countryFlag: String,
    val tier: String,
    val rp: Int,
    val pingMs: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val current: Int,
    val target: Int,
    val isUnlocked: Boolean = false
)
