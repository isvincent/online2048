package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BoardSize
import com.example.model.ThemeOption
import com.example.ui.components.DirectionControls
import com.example.ui.components.GameBoard
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

@Composable
fun ClassicGameScreen(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    val tiles by viewModel.classicTiles.collectAsState()
    val score by viewModel.classicScore.collectAsState()
    val combo by viewModel.classicCombo.collectAsState()
    val isGameOver by viewModel.classicGameOver.collectAsState()
    val hasWon by viewModel.classicWon.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val currentSize by viewModel.classicBoardSize.collectAsState()

    val currentTheme = ThemeOption.entries.firstOrNull { it.id == profile.selectedTheme } ?: ThemeOption.CLASSIC
    val themeConfig = remember(currentTheme) { ThemeManager.getConfig(currentTheme) }

    var showDirectionPad by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(themeConfig.backgroundStart, themeConfig.backgroundEnd)
                )
            )
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Section: Title & Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "2048",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = themeConfig.textPrimary
                    )
                    Text(
                        text = "滑動以合併數字方塊",
                        fontSize = 12.sp,
                        color = themeConfig.textSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreBox(
                        title = "得分",
                        score = score,
                        bgColor = themeConfig.boardBackground,
                        textColor = Color.White
                    )
                    ScoreBox(
                        title = "歷史最高",
                        score = maxOf(score, profile.bestScoreClassic),
                        bgColor = themeConfig.boardBackground.copy(alpha = 0.85f),
                        textColor = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Board Size Chips & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BoardSize.entries.forEach { sizeOption ->
                        FilterChip(
                            selected = currentSize == sizeOption,
                            onClick = { viewModel.setClassicBoardSize(sizeOption) },
                            label = {
                                Text(
                                    sizeOption.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (currentSize == sizeOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeConfig.accent,
                                selectedLabelColor = Color.White,
                                containerColor = themeConfig.cardSurface.copy(alpha = 0.6f),
                                labelColor = themeConfig.textPrimary
                            ),
                            modifier = Modifier.testTag("size_${sizeOption.size}")
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Toggle Direction Pad
                    IconButton(
                        onClick = { showDirectionPad = !showDirectionPad },
                        modifier = Modifier.testTag("toggle_dpad_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "切換按鈕控制",
                            tint = if (showDirectionPad) themeConfig.accent else themeConfig.textSecondary
                        )
                    }

                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undoClassicMove() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "復原上一步",
                            tint = if (canUndo) themeConfig.accent else themeConfig.textSecondary.copy(alpha = 0.4f)
                        )
                    }

                    // Restart Button
                    IconButton(
                        onClick = { viewModel.restartClassicGame() },
                        modifier = Modifier.testTag("restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "重新開始",
                            tint = themeConfig.accent
                        )
                    }
                }
            }

            // Combo Banner
            AnimatedVisibility(
                visible = combo >= 2,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeConfig.accent.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 連擊 Combo x$combo! (額外 +${combo * 2} 分)",
                        color = themeConfig.accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Interactive 2048 Game Board
            GameBoard(
                size = currentSize.size,
                tiles = tiles,
                themeConfig = themeConfig,
                onSwipe = { dir -> viewModel.onClassicSwipe(dir) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Optional Directional Keypad
            if (showDirectionPad) {
                Spacer(modifier = Modifier.height(14.dp))
                DirectionControls(
                    onDirectionClick = { dir -> viewModel.onClassicSwipe(dir) },
                    accentColor = themeConfig.accent
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Win Dialog
        if (hasWon) {
            AlertDialog(
                onDismissRequest = { /* Require action */ },
                title = {
                    Text("🎉 傳奇達成！2048！", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("太厲害了！你已成功合成 2048 方塊！你可以選擇繼續邁向 4096 或重新挑戰！")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.continuePlayingAfterWin() },
                        colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accent),
                        modifier = Modifier.testTag("continue_endless_button")
                    ) {
                        Text("無盡模式 (繼續挑戰)")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.restartClassicGame() },
                        modifier = Modifier.testTag("new_game_after_win_button")
                    ) {
                        Text("重啟新局")
                    }
                }
            )
        }

        // Game Over Dialog
        if (isGameOver) {
            AlertDialog(
                onDismissRequest = { /* Require action */ },
                title = {
                    Text("💀 遊戲結束", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("盤面已滿且無法再進行合成！\n最終得分: $score 分\n最高方塊: ${tiles.maxOfOrNull { it.value } ?: 0}")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.restartClassicGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accent),
                        modifier = Modifier.testTag("game_over_restart_button")
                    ) {
                        Text("再來一局")
                    }
                }
            )
        }
    }
}

@Composable
fun ScoreBox(
    title: String,
    score: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.75f)
            )
            Text(
                text = "$score",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
