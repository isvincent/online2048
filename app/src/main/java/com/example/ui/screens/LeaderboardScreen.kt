package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LeaderboardEntry
import com.example.model.ThemeOption
import com.example.ui.theme.BoardThemeConfig
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

@Composable
fun LeaderboardScreen(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val currentTheme = ThemeOption.entries.firstOrNull { it.id == profile.selectedTheme } ?: ThemeOption.CLASSIC
    val themeConfig = remember(currentTheme) { ThemeManager.getConfig(currentTheme) }

    val entries = when (selectedTab) {
        0 -> viewModel.getBattleLeaderboard()
        1 -> viewModel.getClassicLeaderboard()
        else -> viewModel.getDailyLeaderboard()
    }

    val userEntry = entries.firstOrNull { it.isUser }

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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "🏆 全球線上天梯榜",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = themeConfig.textPrimary
            )
            Text(
                text = "與世界各地的 2048 頂尖大師角逐巔峰",
                fontSize = 12.sp,
                color = themeConfig.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = themeConfig.accent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = themeConfig.accent
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "⚔️ 1v1 對抗",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_pvp")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "⭐ 經典高分",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_classic")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "📅 每日挑戰",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_daily")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Podium for Top 3 (if at least 3 players)
            if (entries.size >= 3) {
                PodiumSection(topThree = entries.take(3), themeConfig = themeConfig)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Scrollable List of entries (from #4 onwards or all)
            val listItems = if (entries.size >= 3) entries.drop(3) else entries

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listItems, key = { "${it.playerName}_${it.rank}" }) { entry ->
                    LeaderboardRow(entry = entry, themeConfig = themeConfig)
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        // Sticky User Rank Card at bottom
        if (userEntry != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeConfig.accent.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#${userEntry.rank}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(userEntry.avatarEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    userEntry.playerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    userEntry.tierTitle,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Text(
                            text = "${userEntry.score} ${if (selectedTab == 0) "RP" else "pt"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(
    topThree: List<LeaderboardEntry>,
    themeConfig: BoardThemeConfig
) {
    val first = topThree.getOrNull(0) ?: return
    val second = topThree.getOrNull(1) ?: return
    val third = topThree.getOrNull(2) ?: return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place (Silver)
        PodiumItem(
            entry = second,
            rankLabel = "🥈 2",
            pedestalHeight = 65.dp,
            badgeColor = Color(0xFFC0C0C0),
            themeConfig = themeConfig,
            modifier = Modifier.weight(1f)
        )

        // 1st Place (Gold)
        PodiumItem(
            entry = first,
            rankLabel = "👑 1",
            pedestalHeight = 85.dp,
            badgeColor = Color(0xFFFFD700),
            themeConfig = themeConfig,
            modifier = Modifier.weight(1.15f)
        )

        // 3rd Place (Bronze)
        PodiumItem(
            entry = third,
            rankLabel = "🥉 3",
            pedestalHeight = 55.dp,
            badgeColor = Color(0xFFCD7F32),
            themeConfig = themeConfig,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    rankLabel: String,
    pedestalHeight: androidx.compose.ui.unit.Dp,
    badgeColor: Color,
    themeConfig: BoardThemeConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.2f))
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.avatarEmoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = entry.playerName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = themeConfig.textPrimary
        )
        Text(
            text = "${entry.score}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = themeConfig.accent
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Pedestal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(badgeColor.copy(alpha = 0.25f))
                .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(rankLabel, fontSize = 14.sp, fontWeight = FontWeight.Black, color = themeConfig.textPrimary)
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    themeConfig: BoardThemeConfig
) {
    val bgColor = if (entry.isUser) {
        themeConfig.accent.copy(alpha = 0.15f)
    } else {
        themeConfig.cardSurface
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${entry.rank}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = themeConfig.textSecondary,
                    modifier = Modifier.width(36.dp)
                )

                Text(entry.avatarEmoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.playerName,
                            fontSize = 14.sp,
                            fontWeight = if (entry.isUser) FontWeight.Bold else FontWeight.Medium,
                            color = themeConfig.textPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(entry.countryFlag, fontSize = 12.sp)
                    }
                    Text(
                        text = "${entry.tierTitle} · 最大方塊: ${entry.maxTile}",
                        fontSize = 11.sp,
                        color = themeConfig.textSecondary
                    )
                }
            }

            Text(
                text = "${entry.score}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeConfig.accent
            )
        }
    }
}
