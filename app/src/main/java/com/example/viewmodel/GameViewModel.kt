package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.data.UserProfileEntity
import com.example.engine.AudioHapticHelper
import com.example.engine.GameEngine2048
import com.example.engine.OnlineMatchmaker
import com.example.model.Achievement
import com.example.model.BattleOpponent
import com.example.model.BattleStatus
import com.example.model.BoardSize
import com.example.model.Direction
import com.example.model.LeaderboardEntry
import com.example.model.ThemeOption
import com.example.model.Tile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class BattleUiState(
    val status: BattleStatus = BattleStatus.IDLE,
    val opponent: BattleOpponent? = null,
    val timeLeftSeconds: Int = 90,
    val userScore: Int = 0,
    val userCombo: Int = 0,
    val userGauge: Int = 0, // 0..100
    val userTiles: List<Tile> = emptyList(),
    val opponentScore: Int = 0,
    val opponentCombo: Int = 0,
    val opponentGauge: Int = 0,
    val opponentTiles: List<Tile> = emptyList(),
    val isUserWinner: Boolean? = null,
    val rpChange: Int = 0,
    val newTier: String = "",
    val eventLogs: List<String> = emptyList(),
    val isUserGameOver: Boolean = false,
    val isOpponentGameOver: Boolean = false
)

data class DailyChallengeState(
    val movesLeft: Int = 80,
    val score: Int = 0,
    val combo: Int = 0,
    val tiles: List<Tile> = emptyList(),
    val isFinished: Boolean = false,
    val dateString: String = ""
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = GameRepository(db.appDao())
    val audioHaptic = AudioHapticHelper(application)
    private val matchmaker = OnlineMatchmaker()

    // Profile state
    val profile: StateFlow<UserProfileEntity> = repository.userProfile
        .let { flow ->
            val initial = UserProfileEntity()
            val state = MutableStateFlow(initial)
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
            state.asStateFlow()
        }

    // Classic game engine & state
    private val classicEngine = GameEngine2048()
    private val _classicBoardSize = MutableStateFlow(BoardSize.SIZE_4X4)
    val classicBoardSize = _classicBoardSize.asStateFlow()

    private val _classicTiles = MutableStateFlow<List<Tile>>(emptyList())
    val classicTiles = _classicTiles.asStateFlow()

    private val _classicScore = MutableStateFlow(0)
    val classicScore = _classicScore.asStateFlow()

    private val _classicCombo = MutableStateFlow(0)
    val classicCombo = _classicCombo.asStateFlow()

    private val _classicGameOver = MutableStateFlow(false)
    val classicGameOver = _classicGameOver.asStateFlow()

    private val _classicWon = MutableStateFlow(false)
    val classicWon = _classicWon.asStateFlow()

    private val _canUndo = MutableStateFlow(false)
    val canUndo = _canUndo.asStateFlow()

    // Battle 1v1 state
    private val _battleState = MutableStateFlow(BattleUiState())
    val battleState = _battleState.asStateFlow()

    private var battleUserEngine = GameEngine2048()
    private var battleOpponentEngine = GameEngine2048()
    private var battleTimerJob: Job? = null
    private var opponentAiJob: Job? = null

    // Daily challenge state
    private val _dailyState = MutableStateFlow(DailyChallengeState())
    val dailyState = _dailyState.asStateFlow()
    private var dailyEngine = GameEngine2048()

    // Achievements state
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    init {
        restartClassicGame()
        initDailyChallenge()
        loadAchievements()
    }

    override fun onCleared() {
        super.onCleared()
        audioHaptic.release()
        battleTimerJob?.cancel()
        opponentAiJob?.cancel()
    }

    // ==========================================
    // CLASSIC MODE
    // ==========================================

    fun setClassicBoardSize(size: BoardSize) {
        if (_classicBoardSize.value != size) {
            _classicBoardSize.value = size
            classicEngine.boardSize = size
            restartClassicGame()
        }
    }

    fun restartClassicGame() {
        classicEngine.resetGame()
        syncClassicState()
    }

    fun onClassicSwipe(direction: Direction) {
        if (classicEngine.isGameOver) return
        val currentProfile = profile.value
        val result = classicEngine.move(direction)

        if (result.moved) {
            if (result.mergedValues.isNotEmpty()) {
                audioHaptic.playMergeSound(currentProfile.soundEnabled, result.mergedValues.maxOrNull() ?: 2)
                audioHaptic.triggerVibration(currentProfile.vibrationEnabled, isMerge = true)
            } else {
                audioHaptic.playSlideSound(currentProfile.soundEnabled)
                audioHaptic.triggerVibration(currentProfile.vibrationEnabled, isMerge = false)
            }
            syncClassicState()

            if (classicEngine.hasWon && !classicEngine.continueAfterWin) {
                audioHaptic.playVictorySound(currentProfile.soundEnabled)
            }
            if (classicEngine.isGameOver) {
                audioHaptic.playGameOverSound(currentProfile.soundEnabled)
                viewModelScope.launch {
                    repository.recordGameFinished(
                        mode = "CLASSIC",
                        score = classicEngine.score,
                        maxTile = classicEngine.getLargestTile(),
                        isWin = classicEngine.hasWon
                    )
                    checkAchievements()
                }
            }
        }
    }

    fun continuePlayingAfterWin() {
        classicEngine.continueAfterWin = true
        _classicWon.value = false
    }

    fun undoClassicMove() {
        if (classicEngine.undo()) {
            syncClassicState()
        }
    }

    private fun syncClassicState() {
        _classicTiles.value = classicEngine.tiles
        _classicScore.value = classicEngine.score
        _classicCombo.value = classicEngine.combo
        _classicGameOver.value = classicEngine.isGameOver
        _classicWon.value = classicEngine.hasWon && !classicEngine.continueAfterWin
        _canUndo.value = classicEngine.canUndo()
    }

    // ==========================================
    // ONLINE 1V1 BATTLE MODE
    // ==========================================

    fun startMatchmaking() {
        battleTimerJob?.cancel()
        opponentAiJob?.cancel()

        _battleState.value = BattleUiState(status = BattleStatus.SEARCHING)

        viewModelScope.launch {
            // Simulated radar search animation delay
            delay(1500)
            val currentRp = profile.value.rp
            val opp = matchmaker.findOpponent(currentRp)

            _battleState.value = _battleState.value.copy(
                status = BattleStatus.FOUND,
                opponent = opp
            )

            delay(1200)

            // Start battle
            battleUserEngine = GameEngine2048()
            battleUserEngine.resetGame()

            battleOpponentEngine = GameEngine2048()
            battleOpponentEngine.resetGame()

            _battleState.value = BattleUiState(
                status = BattleStatus.PLAYING,
                opponent = opp,
                timeLeftSeconds = 90,
                userTiles = battleUserEngine.tiles,
                opponentTiles = battleOpponentEngine.tiles,
                eventLogs = listOf("⚔️ 對戰開始！時間 90 秒，誰的分數高誰獲勝！")
            )

            startBattleTimer()
            startOpponentAi()
        }
    }

    private fun startBattleTimer() {
        battleTimerJob = viewModelScope.launch {
            while (_battleState.value.timeLeftSeconds > 0 && _battleState.value.status == BattleStatus.PLAYING) {
                delay(1000)
                val newTime = _battleState.value.timeLeftSeconds - 1
                _battleState.value = _battleState.value.copy(timeLeftSeconds = newTime)
            }
            if (_battleState.value.status == BattleStatus.PLAYING) {
                finishBattle(timeout = true)
            }
        }
    }

    private fun startOpponentAi() {
        opponentAiJob = viewModelScope.launch {
            val opp = _battleState.value.opponent
            val moveInterval = if (opp?.rp ?: 0 > 2000) 800L else 1100L

            while (_battleState.value.status == BattleStatus.PLAYING) {
                delay(moveInterval + Random.nextLong(-150, 200))
                if (_battleState.value.status != BattleStatus.PLAYING) break

                val dir = matchmaker.chooseBestBotMove(battleOpponentEngine)
                val result = battleOpponentEngine.move(dir)

                val currentGauge = _battleState.value.opponentGauge
                val gaugeInc = if (result.mergedValues.isNotEmpty()) 15 else 5
                val newGauge = minOf(100, currentGauge + gaugeInc)

                var logs = _battleState.value.eventLogs
                var userGotObstacle = false

                // Opponent Attack mechanism
                if (newGauge >= 60 || result.mergedValues.any { it >= 128 }) {
                    // Send obstacle to user!
                    val added = battleUserEngine.addObstacle()
                    if (added) {
                        userGotObstacle = true
                        logs = (listOf("⚠️ 對手發動冰凍攻擊！你的棋盤新增 1 個障礙塊！") + logs).take(5)
                        audioHaptic.triggerVibration(profile.value.vibrationEnabled, isMerge = true)
                    }
                }

                _battleState.value = _battleState.value.copy(
                    opponentScore = battleOpponentEngine.score,
                    opponentCombo = battleOpponentEngine.combo,
                    opponentGauge = if (userGotObstacle) 0 else newGauge,
                    opponentTiles = battleOpponentEngine.tiles,
                    isOpponentGameOver = battleOpponentEngine.isGameOver,
                    userTiles = if (userGotObstacle) battleUserEngine.tiles else _battleState.value.userTiles,
                    eventLogs = logs
                )

                if (battleOpponentEngine.isGameOver) {
                    finishBattle(opponentDied = true)
                    break
                }
            }
        }
    }

    fun onBattleSwipe(direction: Direction) {
        if (_battleState.value.status != BattleStatus.PLAYING) return
        val currentProfile = profile.value
        val result = battleUserEngine.move(direction)

        if (result.moved) {
            val sound = currentProfile.soundEnabled
            if (result.mergedValues.isNotEmpty()) {
                audioHaptic.playMergeSound(sound, result.mergedValues.maxOrNull() ?: 2)
                audioHaptic.triggerVibration(currentProfile.vibrationEnabled, isMerge = true)
            } else {
                audioHaptic.playSlideSound(sound)
                audioHaptic.triggerVibration(currentProfile.vibrationEnabled, isMerge = false)
            }

            val currentGauge = _battleState.value.userGauge
            val gaugeInc = if (result.mergedValues.isNotEmpty()) 18 else 6
            val newGauge = minOf(100, currentGauge + gaugeInc)

            var logs = _battleState.value.eventLogs
            // Auto attack on big tile
            if (result.mergedValues.any { it >= 128 }) {
                battleOpponentEngine.addObstacle()
                logs = (listOf("💥 你合成了大方塊，重擊對手！對手棋盤出現障礙！") + logs).take(5)
            }

            _battleState.value = _battleState.value.copy(
                userScore = battleUserEngine.score,
                userCombo = battleUserEngine.combo,
                userGauge = newGauge,
                userTiles = battleUserEngine.tiles,
                opponentTiles = battleOpponentEngine.tiles,
                isUserGameOver = battleUserEngine.isGameOver,
                eventLogs = logs
            )

            if (battleUserEngine.isGameOver) {
                finishBattle(userDied = true)
            }
        }
    }

    // User Battle Skill: Freeze Opponent
    fun useSkillFreezeOpponent() {
        val state = _battleState.value
        if (state.status != BattleStatus.PLAYING || state.userGauge < 50) return
        battleOpponentEngine.addObstacle()
        val logs = (listOf("❄️ 你消耗 50 能量發動「暴風雪干擾」！對手陷入僵局！") + state.eventLogs).take(5)
        _battleState.value = state.copy(
            userGauge = state.userGauge - 50,
            opponentTiles = battleOpponentEngine.tiles,
            eventLogs = logs
        )
        audioHaptic.playMergeSound(profile.value.soundEnabled, 1024)
    }

    // User Battle Skill: Clear Obstacle
    fun useSkillClearObstacle() {
        val state = _battleState.value
        if (state.status != BattleStatus.PLAYING || state.userGauge < 40) return
        val count = battleUserEngine.clearObstacles()
        val logs = (listOf(if (count > 0) "🛡️ 成功清除盤面障礙冰塊！" else "🛡️ 盤面無障礙物，能量已返還。") + state.eventLogs).take(5)
        _battleState.value = state.copy(
            userGauge = if (count > 0) state.userGauge - 40 else state.userGauge,
            userTiles = battleUserEngine.tiles,
            eventLogs = logs
        )
        audioHaptic.playSlideSound(profile.value.soundEnabled)
    }

    // User Battle Skill: Remove Lowest Tile
    fun useSkillRemoveLowest() {
        val state = _battleState.value
        if (state.status != BattleStatus.PLAYING || state.userGauge < 60) return
        val removed = battleUserEngine.removeLowestTile()
        val logs = (listOf("🔨 破壁神錘！已清除場上最低數字方塊！") + state.eventLogs).take(5)
        _battleState.value = state.copy(
            userGauge = state.userGauge - 60,
            userTiles = battleUserEngine.tiles,
            eventLogs = logs
        )
        audioHaptic.playVictorySound(profile.value.soundEnabled)
    }

    private fun finishBattle(timeout: Boolean = false, userDied: Boolean = false, opponentDied: Boolean = false) {
        battleTimerJob?.cancel()
        opponentAiJob?.cancel()

        val uScore = battleUserEngine.score
        val oScore = battleOpponentEngine.score
        val isWin = when {
            userDied -> false
            opponentDied -> true
            else -> uScore >= oScore
        }

        viewModelScope.launch {
            val opp = _battleState.value.opponent
            val (rpDiff, newTier) = repository.recordBattleFinished(
                opponentName = opp?.name ?: "神秘對手",
                opponentAvatar = opp?.avatarEmoji ?: "👤",
                userScore = uScore,
                opponentScore = oScore,
                isWin = isWin
            )

            _battleState.value = _battleState.value.copy(
                status = BattleStatus.FINISHED,
                isUserWinner = isWin,
                rpChange = rpDiff,
                newTier = newTier
            )

            if (isWin) {
                audioHaptic.playVictorySound(profile.value.soundEnabled)
            } else {
                audioHaptic.playGameOverSound(profile.value.soundEnabled)
            }

            checkAchievements()
        }
    }

    fun exitBattle() {
        battleTimerJob?.cancel()
        opponentAiJob?.cancel()
        _battleState.value = BattleUiState(status = BattleStatus.IDLE)
    }

    // ==========================================
    // DAILY CHALLENGE MODE
    // ==========================================

    fun initDailyChallenge() {
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFmt.format(Date())
        val seed = dateStr.replace("-", "").toLongOrNull() ?: 20261018L
        val seededRng = Random(seed)

        dailyEngine = GameEngine2048(boardSize = BoardSize.SIZE_4X4, random = seededRng)
        dailyEngine.resetGame(seededRng)

        _dailyState.value = DailyChallengeState(
            movesLeft = 80,
            score = dailyEngine.score,
            combo = dailyEngine.combo,
            tiles = dailyEngine.tiles,
            isFinished = false,
            dateString = dateStr
        )
    }

    fun onDailySwipe(direction: Direction) {
        val state = _dailyState.value
        if (state.isFinished || state.movesLeft <= 0) return

        val result = dailyEngine.move(direction)
        if (result.moved) {
            val newMoves = state.movesLeft - 1
            val isFinished = newMoves <= 0 || dailyEngine.isGameOver

            if (result.mergedValues.isNotEmpty()) {
                audioHaptic.playMergeSound(profile.value.soundEnabled, result.mergedValues.maxOrNull() ?: 2)
            } else {
                audioHaptic.playSlideSound(profile.value.soundEnabled)
            }

            _dailyState.value = state.copy(
                movesLeft = newMoves,
                score = dailyEngine.score,
                combo = dailyEngine.combo,
                tiles = dailyEngine.tiles,
                isFinished = isFinished
            )

            if (isFinished) {
                viewModelScope.launch {
                    repository.recordGameFinished(
                        mode = "DAILY",
                        score = dailyEngine.score,
                        maxTile = dailyEngine.getLargestTile(),
                        isWin = dailyEngine.score >= 2048
                    )
                }
            }
        }
    }

    // ==========================================
    // LEADERBOARD
    // ==========================================

    fun getBattleLeaderboard(): List<LeaderboardEntry> {
        return repository.getGlobalBattleLeaderboard(profile.value)
    }

    fun getClassicLeaderboard(): List<LeaderboardEntry> {
        return repository.getClassicLeaderboard(profile.value)
    }

    fun getDailyLeaderboard(): List<LeaderboardEntry> {
        return repository.getDailyLeaderboard(profile.value, _dailyState.value.score)
    }

    // ==========================================
    // PROFILE & SETTINGS
    // ==========================================

    fun updateNickname(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(current.copy(nickname = newName.trim()))
        }
    }

    fun updateAvatar(newAvatar: String) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(current.copy(avatar = newAvatar))
        }
    }

    fun updateTheme(theme: ThemeOption) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(current.copy(selectedTheme = theme.id))
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(current.copy(soundEnabled = enabled))
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getProfile()
            repository.updateProfile(current.copy(vibrationEnabled = enabled))
        }
    }

    // ==========================================
    // ACHIEVEMENTS
    // ==========================================

    private fun loadAchievements() {
        checkAchievements()
    }

    private fun checkAchievements() {
        viewModelScope.launch {
            val p = repository.getProfile()
            val list = listOf(
                Achievement("ach_start", "初試啼聲", "完成首局 2048 遊戲", "🌱", minOf(p.gamesPlayed, 1), 1, p.gamesPlayed >= 1),
                Achievement("ach_merge_512", "進階專家", "合成 512 數值方塊", "⭐", if (p.highestTile >= 512) 1 else 0, 1, p.highestTile >= 512),
                Achievement("ach_merge_1024", "千面大師", "合成 1024 數值方塊", "💠", if (p.highestTile >= 1024) 1 else 0, 1, p.highestTile >= 1024),
                Achievement("ach_merge_2048", "榮耀 2048", "成功合成傳奇 2048！", "🏆", if (p.highestTile >= 2048) 1 else 0, 1, p.highestTile >= 2048),
                Achievement("ach_pvp_1", "首勝榮光", "在 1v1 線上對決中獲勝 1 場", "⚔️", minOf(p.battlesWon, 1), 1, p.battlesWon >= 1),
                Achievement("ach_pvp_5", "常勝將軍", "在 1v1 線上對決中獲勝 5 場", "🔥", minOf(p.battlesWon, 5), 5, p.battlesWon >= 5),
                Achievement("ach_pvp_master", "天梯霸主", "達到大師段位 (2000 RP)", "👑", minOf(p.rp, 2000), 2000, p.rp >= 2000)
            )
            _achievements.value = list
        }
    }
}
