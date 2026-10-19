package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ThemeOption
import com.example.ui.components.GameBoard
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

@Composable
fun DailyChallengeScreen(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    val dailyState by viewModel.dailyState.collectAsState()

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
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📅 每日線上同步賽",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = themeConfig.textPrimary
                    )
                    Text(
                        text = "${dailyState.dateString} · 全球玩家共用同一起跑種子",
                        fontSize = 12.sp,
                        color = themeConfig.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeConfig.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "剩餘步數: ${dailyState.movesLeft}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Score & Star Milestones Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("今日積分: ${dailyState.score}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = themeConfig.textPrimary)

                        // Stars
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            val star1 = dailyState.score >= 1000
                            val star2 = dailyState.score >= 2048
                            val star3 = dailyState.score >= 4096

                            Icon(Icons.Default.Star, contentDescription = null, tint = if (star1) Color(0xFFFFB703) else Color.LightGray)
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (star2) Color(0xFFFFB703) else Color.LightGray)
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (star3) Color(0xFFFFB703) else Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = (dailyState.score.toFloat() / 4096f).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = themeConfig.accent,
                        trackColor = themeConfig.boardBackground.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("里程碑: 1000分 (⭐) · 2048分 (⭐⭐) · 4096分 (⭐⭐⭐)", fontSize = 10.sp, color = themeConfig.textSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Combo Banner
            AnimatedVisibility(
                visible = dailyState.combo >= 2,
                enter = fadeIn() + scaleIn()
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(themeConfig.accent.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔥 連擊 Combo x${dailyState.combo}!",
                        color = themeConfig.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive 4x4 Board
            GameBoard(
                size = 4,
                tiles = dailyState.tiles,
                themeConfig = themeConfig,
                onSwipe = { dir -> viewModel.onDailySwipe(dir) },
                isInteractive = !dailyState.isFinished,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Completion Banner
            if (dailyState.isFinished) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = themeConfig.accent.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 今日挑戰已結算！", fontWeight = FontWeight.Black, fontSize = 18.sp, color = themeConfig.accent)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("最終成績: ${dailyState.score} 分", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeConfig.textPrimary)
                        Text("成績已上傳至每日線上天梯排行榜！", fontSize = 12.sp, color = themeConfig.textSecondary)

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.initDailyChallenge() },
                            colors = ButtonDefaults.buttonColors(containerColor = themeConfig.accent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("retry_daily_challenge_button")
                        ) {
                            Text("重新練習今日種子")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
