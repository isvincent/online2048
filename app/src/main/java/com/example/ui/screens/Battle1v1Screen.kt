package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BattleStatus
import com.example.model.ThemeOption
import com.example.ui.components.GameBoard
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

@Composable
fun Battle1v1Screen(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    val battleState by viewModel.battleState.collectAsState()

    val currentTheme = ThemeOption.entries.firstOrNull { it.id == profile.selectedTheme } ?: ThemeOption.CLASSIC
    val themeConfig = remember(currentTheme) { ThemeManager.getConfig(currentTheme) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(themeConfig.backgroundStart, themeConfig.backgroundEnd)
                )
            )
            .padding(horizontal = 14.dp)
    ) {
        when (battleState.status) {
            BattleStatus.IDLE -> {
                BattleLobbyView(
                    profile = profile,
                    themeConfig = themeConfig,
                    onStartMatchmaking = { viewModel.startMatchmaking() }
                )
            }
            BattleStatus.SEARCHING -> {
                BattleSearchingView(
                    themeConfig = themeConfig,
                    onCancel = { viewModel.exitBattle() }
                )
            }
            BattleStatus.FOUND -> {
                BattleMatchFoundView(
                    profile = profile,
                    opponent = battleState.opponent,
                    themeConfig = themeConfig
                )
            }
            BattleStatus.PLAYING -> {
                BattleArenaView(
                    viewModel = viewModel,
                    themeConfig = themeConfig
                )
            }
            BattleStatus.FINISHED -> {
                BattleResultView(
                    viewModel = viewModel,
                    themeConfig = themeConfig
                )
            }
        }
    }
}

@Composable
private fun BattleLobbyView(
    profile: com.example.data.UserProfileEntity,
    themeConfig: com.example.ui.theme.BoardThemeConfig,
    onStartMatchmaking: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚔️ 1v1 線上天梯對決",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = themeConfig.textPrimary
        )
        Text(
            text = "即時匹配全球好手 · 90秒競速對抗",
            fontSize = 13.sp,
            color = themeConfig.textSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Player Tier Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(themeConfig.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(profile.avatar, fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    profile.nickname,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeConfig.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(profile.country, fontSize = 14.sp)
                            }
                            Text(
                                profile.tier,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = themeConfig.accent
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("天梯積分", fontSize = 11.sp, color = themeConfig.textSecondary)
                        Text(
                            "${profile.rp} RP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = themeConfig.accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("勝場數", fontSize = 11.sp, color = themeConfig.textSecondary)
                        Text("${profile.battlesWon}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeConfig.textPrimary)
                    }
                    val winRate = if (profile.gamesPlayed > 0) {
                        (profile.battlesWon * 100 / profile.gamesPlayed)
                    } else 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("勝率", fontSize = 11.sp, color = themeConfig.textSecondary)
                        Text("$winRate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeConfig.textPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("最高數字", fontSize = 11.sp, color = themeConfig.textSecondary)
                        Text("${profile.highestTile}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeConfig.textPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Match Rules Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("💡 競技對戰規則", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = themeConfig.textPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• 倒數 90 秒，雙方在相同起跑線上競速合成！", fontSize = 12.sp, color = themeConfig.textSecondary)
                Text("• 合成 128 以上大方塊或連擊，自動向對手發射「冰凍障礙塊」！", fontSize = 12.sp, color = themeConfig.textSecondary)
                Text("• 蓄能值達到門檻可手動發動干擾、破冰與神錘技能！", fontSize = 12.sp, color = themeConfig.textSecondary)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Matchmaking Button
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Button(
            onClick = onStartMatchmaking,
            colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accent),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(54.dp)
                .scale(pulseScale)
                .testTag("start_matchmaking_button")
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("開始線上匹配對決", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BattleSearchingView(
    themeConfig: com.example.ui.theme.BoardThemeConfig,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(rotation)
                .clip(CircleShape)
                .background(themeConfig.accent.copy(alpha = 0.15f))
                .border(2.dp, themeConfig.accent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = themeConfig.accent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(110.dp)
            )
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = themeConfig.accent,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "正在搜尋全球天梯實力相當的對手...",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = themeConfig.textPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "匹配範圍: ±200 RP · 延遲: < 50ms",
            fontSize = 13.sp,
            color = themeConfig.textSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))
        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.testTag("cancel_matchmaking_button")
        ) {
            Text("取消匹配", color = themeConfig.textSecondary)
        }
    }
}

@Composable
private fun BattleMatchFoundView(
    profile: com.example.data.UserProfileEntity,
    opponent: com.example.model.BattleOpponent?,
    themeConfig: com.example.ui.theme.BoardThemeConfig
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚡ 對手已配對成功！",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = themeConfig.accent
        )

        Spacer(modifier = Modifier.height(28.dp))

        // VS Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Player side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                        .border(2.dp, Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profile.avatar, fontSize = 34.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(profile.nickname, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = themeConfig.textPrimary)
                Text("${profile.country} · ${profile.rp} RP", fontSize = 12.sp, color = themeConfig.textSecondary)
            }

            Text(
                "VS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = themeConfig.accent
            )

            // Opponent side
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                        .border(2.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(opponent?.avatarEmoji ?: "👤", fontSize = 34.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(opponent?.name ?: "神秘對手", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = themeConfig.textPrimary)
                Text("${opponent?.countryFlag} · ${opponent?.rp} RP (${opponent?.pingMs}ms)", fontSize = 12.sp, color = themeConfig.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = "戰鬥即將打響，準備滑動！",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = themeConfig.textSecondary
        )
    }
}

@Composable
private fun BattleArenaView(
    viewModel: GameViewModel,
    themeConfig: com.example.ui.theme.BoardThemeConfig
) {
    val battleState by viewModel.battleState.collectAsState()
    val opponent = battleState.opponent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Timer & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⚔️ 1v1 天梯賽",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = themeConfig.textPrimary
            )

            val timeColor = if (battleState.timeLeftSeconds <= 20) Color(0xFFEF4444) else themeConfig.accent
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(timeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⏳ ${battleState.timeLeftSeconds}s",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = timeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Opponent Live Card (Mini)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(opponent?.avatarEmoji ?: "👤", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = opponent?.name ?: "對手",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = themeConfig.textPrimary
                        )
                        Text(
                            text = "得分: ${battleState.opponentScore} pt",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                // Mini board of opponent (4x4 cells preview)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(themeConfig.boardBackground)
                        .padding(2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        for (r in 0 until 4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                for (c in 0 until 4) {
                                    val tile = battleState.opponentTiles.firstOrNull { it.row == r && it.col == c }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize()
                                            .background(
                                                when {
                                                    tile == null -> themeConfig.emptyCellColor
                                                    tile.isObstacle -> Color(0xFF64748B)
                                                    else -> themeConfig.tileColors(tile.value)
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Skills & Energy Gauge Row
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚡ 技能能量: ${battleState.userGauge}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeConfig.accent
                )
                Text(
                    "得分: ${battleState.userScore} pt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = themeConfig.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { battleState.userGauge / 100f },
                color = themeConfig.accent,
                trackColor = themeConfig.boardBackground.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 3 Skill Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Skill 1: Freeze
                Button(
                    onClick = { viewModel.useSkillFreezeOpponent() },
                    enabled = battleState.userGauge >= 50,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("skill_freeze_button")
                ) {
                    Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("暴風雪 (50)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Skill 2: Clear Obstacle
                Button(
                    onClick = { viewModel.useSkillClearObstacle() },
                    enabled = battleState.userGauge >= 40,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("skill_clear_obstacle_button")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("破冰 (40)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Skill 3: Destroy Lowest
                Button(
                    onClick = { viewModel.useSkillRemoveLowest() },
                    enabled = battleState.userGauge >= 60,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("skill_destroy_lowest_button")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("神錘 (60)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live combat log message
        val latestLog = battleState.eventLogs.firstOrNull() ?: ""
        if (latestLog.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = latestLog,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = themeConfig.accent,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // User Interactive 4x4 Game Board
        GameBoard(
            size = 4,
            tiles = battleState.userTiles,
            themeConfig = themeConfig,
            onSwipe = { dir -> viewModel.onBattleSwipe(dir) },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun BattleResultView(
    viewModel: GameViewModel,
    themeConfig: com.example.ui.theme.BoardThemeConfig
) {
    val battleState by viewModel.battleState.collectAsState()
    val isWin = battleState.isUserWinner == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isWin) "🏆 榮耀大捷！VICTORY" else "💔 惜敗！DEFEAT",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("你的得分", fontSize = 12.sp, color = themeConfig.textSecondary)
                        Text("${battleState.userScore}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = themeConfig.textPrimary)
                    }
                    Text("VS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = themeConfig.textSecondary)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("對手得分", fontSize = 12.sp, color = themeConfig.textSecondary)
                        Text("${battleState.opponentScore}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = themeConfig.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val rpText = if (battleState.rpChange > 0) "+${battleState.rpChange} RP" else "${battleState.rpChange} RP"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isWin) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "天梯積分變動: $rpText (${battleState.newTier})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isWin) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.exitBattle() },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("exit_battle_lobby_button")
            ) {
                Text("返回大廳")
            }

            Button(
                onClick = { viewModel.startMatchmaking() },
                colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("rematch_battle_button")
            ) {
                Text("再來一局", fontWeight = FontWeight.Bold)
            }
        }
    }
}
