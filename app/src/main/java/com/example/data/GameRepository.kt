package com.example.data

import com.example.model.LeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: AppDao) {

    val userProfile: Flow<UserProfileEntity> = dao.getUserProfileFlow().map { entity ->
        entity ?: UserProfileEntity()
    }

    val battleHistory: Flow<List<BattleHistoryEntity>> = dao.getBattleHistory()
    val recentRecords: Flow<List<GameRecordEntity>> = dao.getRecentGameRecords()

    suspend fun getProfile(): UserProfileEntity {
        return dao.getUserProfile() ?: UserProfileEntity().also {
            dao.insertOrUpdateProfile(it)
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun recordGameFinished(
        mode: String,
        score: Int,
        maxTile: Int,
        isWin: Boolean
    ) {
        dao.insertGameRecord(
            GameRecordEntity(
                mode = mode,
                score = score,
                maxTile = maxTile,
                isWin = isWin,
                timestamp = System.currentTimeMillis()
            )
        )

        val current = getProfile()
        val newGames = current.gamesPlayed + 1
        val newMaxTile = maxOf(current.highestTile, maxTile)
        val newBestClassic = if (mode == "CLASSIC") maxOf(current.bestScoreClassic, score) else current.bestScoreClassic

        dao.insertOrUpdateProfile(
            current.copy(
                gamesPlayed = newGames,
                highestTile = newMaxTile,
                bestScoreClassic = newBestClassic
            )
        )
    }

    suspend fun recordBattleFinished(
        opponentName: String,
        opponentAvatar: String,
        userScore: Int,
        opponentScore: Int,
        isWin: Boolean
    ): Pair<Int, String> {
        val current = getProfile()
        val rpChange = if (isWin) 30 else -15
        val newRp = maxOf(0, current.rp + rpChange)
        val newBattlesWon = if (isWin) current.battlesWon + 1 else current.battlesWon
        val newGames = current.gamesPlayed + 1

        val newTier = calculateTier(newRp)

        dao.insertBattleHistory(
            BattleHistoryEntity(
                opponentName = opponentName,
                opponentAvatar = opponentAvatar,
                opponentScore = opponentScore,
                userScore = userScore,
                isWin = isWin,
                rpChange = rpChange,
                timestamp = System.currentTimeMillis()
            )
        )

        dao.insertOrUpdateProfile(
            current.copy(
                rp = newRp,
                tier = newTier,
                battlesWon = newBattlesWon,
                gamesPlayed = newGames
            )
        )

        return Pair(rpChange, newTier)
    }

    fun calculateTier(rp: Int): String {
        return when {
            rp >= 2800 -> "👑 宗師"
            rp >= 2200 -> "💎 大師 I"
            rp >= 2000 -> "💎 大師 II"
            rp >= 1800 -> "💠 鑽石 I"
            rp >= 1600 -> "💠 鑽石 II"
            rp >= 1400 -> "🔷 白金 I"
            rp >= 1200 -> "🔷 白金 II"
            rp >= 1000 -> "⭐ 黃金 I"
            rp >= 800 -> "⭐ 黃金 II"
            rp >= 500 -> "⭐ 黃金 III"
            else -> "🥉 白銀"
        }
    }

    fun getGlobalBattleLeaderboard(userProfile: UserProfileEntity): List<LeaderboardEntry> {
        val top = listOf(
            LeaderboardEntry(1, "TileKing_Hans", "👑", "🇩🇪", 2950, 4096, "👑 宗師"),
            LeaderboardEntry(2, "MathWizard_Alex", "🧙‍♂️", "🇺🇸", 2780, 4096, "👑 宗師"),
            LeaderboardEntry(3, "櫻花戰士 Yuki", "🦊", "🇯🇵", 2520, 2048, "💎 大師 I"),
            LeaderboardEntry(4, "極速大腦 Kevin", "⚡", "🇹🇼", 2390, 2048, "💎 大師 I"),
            LeaderboardEntry(5, "NeonMaster_99", "👾", "🇰🇷", 2180, 2048, "💎 大師 II"),
            LeaderboardEntry(6, "合體狂人 阿豪", "🔥", "🇹🇼", 2050, 2048, "💎 大師 III"),
            LeaderboardEntry(7, "TileQueen_Lily", "👸", "🇬🇧", 1920, 2048, "💠 鑽石 I"),
            LeaderboardEntry(8, "數字獵人 Leo", "🦁", "🇸🇬", 1810, 1024, "💠 鑽石 II"),
            LeaderboardEntry(9, "幻影快手 Chloe", "🐱", "🇫🇷", 1740, 1024, "💠 鑽石 III"),
            LeaderboardEntry(10, "星空漫遊者 Ava", "✨", "🇹🇼", 1680, 1024, "🔷 白金 I"),
            LeaderboardEntry(11, "CyberPixel_Z", "🤖", "🇭🇰", 1520, 1024, "🔷 白金 II")
        )

        // Find user placement
        val userEntry = LeaderboardEntry(
            rank = 0,
            playerName = "${userProfile.nickname} (你)",
            avatarEmoji = userProfile.avatar,
            countryFlag = userProfile.country,
            score = userProfile.rp,
            maxTile = userProfile.highestTile,
            tierTitle = userProfile.tier,
            isUser = true
        )

        val merged = (top + userEntry).sortedByDescending { it.score }
        return merged.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    fun getClassicLeaderboard(userProfile: UserProfileEntity): List<LeaderboardEntry> {
        val top = listOf(
            LeaderboardEntry(1, "NumberGod_999", "🏆", "🇨🇦", 84520, 8192, "殿堂傳奇"),
            LeaderboardEntry(2, "Merge_Empress", "👑", "🇯🇵", 72480, 4096, "宗師"),
            LeaderboardEntry(3, "PuzzleSensei", "🥋", "🇹🇼", 61200, 4096, "大師"),
            LeaderboardEntry(4, "BlockBuster", "💥", "🇺🇸", 54320, 2048, "鑽石"),
            LeaderboardEntry(5, "ZenMaster_4096", "🧘", "🇬🇧", 48900, 2048, "白金"),
            LeaderboardEntry(6, "FastFinger_Ken", "⚡", "🇰🇷", 42100, 2048, "白金"),
            LeaderboardEntry(7, "LuckySeven_TW", "🍀", "🇹🇼", 36800, 2048, "黃金")
        )

        val userEntry = LeaderboardEntry(
            rank = 0,
            playerName = "${userProfile.nickname} (你)",
            avatarEmoji = userProfile.avatar,
            countryFlag = userProfile.country,
            score = userProfile.bestScoreClassic,
            maxTile = userProfile.highestTile,
            tierTitle = userProfile.tier,
            isUser = true
        )

        val merged = (top + userEntry).sortedByDescending { it.score }
        return merged.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }

    fun getDailyLeaderboard(userProfile: UserProfileEntity, todayScore: Int): List<LeaderboardEntry> {
        val top = listOf(
            LeaderboardEntry(1, "DailyMaster_Ken", "⚡", "🇯🇵", 24800, 2048, "今日冠軍"),
            LeaderboardEntry(2, "SwiftSolver", "🦊", "🇹🇼", 22640, 2048, "亞軍"),
            LeaderboardEntry(3, "DailyStar_Aki", "⭐", "🇰🇷", 21200, 2048, "季軍"),
            LeaderboardEntry(4, "BrainPower", "🧠", "🇸🇬", 19500, 1024, "前 5%"),
            LeaderboardEntry(5, "MorningPuzzle", "☕", "🇺🇸", 17800, 1024, "前 10%")
        )

        val userEntry = LeaderboardEntry(
            rank = 0,
            playerName = "${userProfile.nickname} (你)",
            avatarEmoji = userProfile.avatar,
            countryFlag = userProfile.country,
            score = todayScore,
            maxTile = userProfile.highestTile,
            tierTitle = if (todayScore > 0) "今日挑戰者" else "尚未挑戰",
            isUser = true
        )

        val merged = (top + userEntry).sortedByDescending { it.score }
        return merged.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }
}
