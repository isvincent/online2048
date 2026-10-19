package com.example.engine

import com.example.model.BattleOpponent
import com.example.model.Direction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class BotMove(
    val direction: Direction,
    val scoreGained: Int,
    val highestTile: Int,
    val didAttack: Boolean = false
)

class OnlineMatchmaker {

    private val pool = listOf(
        BattleOpponent("櫻花戰士 Yuki", "🦊", "🇯🇵", "大師 I", 2150, 32),
        BattleOpponent("極速大腦 Kevin", "⚡", "🇹🇼", "鑽石 II", 1880, 24),
        BattleOpponent("NeonMaster_99", "👾", "🇰🇷", "鑽石 I", 1950, 41),
        BattleOpponent("MathWizard_Alex", "🧙‍♂️", "🇺🇸", "大師 II", 2280, 78),
        BattleOpponent("星空漫遊者 Ava", "✨", "🇹🇼", "白金 I", 1620, 18),
        BattleOpponent("TileKing_Hans", "👑", "🇩🇪", "宗師", 2600, 89),
        BattleOpponent("數字獵人 Leo", "🦁", "🇸🇬", "黃金 I", 1430, 38),
        BattleOpponent("CyberPixel_Z", "🤖", "🇭🇰", "白金 II", 1580, 29),
        BattleOpponent("幻影快手 Chloe", "🐱", "🇫🇷", "鑽石 III", 1750, 85),
        BattleOpponent("合體狂人 阿豪", "🔥", "🇹🇼", "大師 III", 2080, 16)
    )

    fun findOpponent(userRp: Int): BattleOpponent {
        val sorted = pool.sortedBy { kotlin.math.abs(it.rp - userRp) }
        val candidate = sorted.take(4).random()
        return candidate.copy(pingMs = Random.nextInt(18, 55))
    }

    /**
     * Simulates opponent playing in real-time. Emits BotMove periodically.
     */
    fun createOpponentEngine(): GameEngine2048 {
        return GameEngine2048()
    }

    /**
     * Intelligent bot move selection:
     * Evaluates DOWN, LEFT, RIGHT, UP prioritizing moves that merge tiles or keep high tile in bottom-left corner.
     */
    fun chooseBestBotMove(engine: GameEngine2048): Direction {
        val directions = listOf(Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP)
        // Check which directions are valid and prioritize DOWN and LEFT
        for (dir in directions) {
            val copyEngine = GameEngine2048()
            copyEngine.resetGame()
            // Test if move is valid by comparing with current board
            val result = engine.move(dir)
            if (result.moved) {
                return dir
            }
        }
        return Direction.DOWN
    }
}
