package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ThemeOption
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

enum class MainTab(val index: Int, val label: String, val icon: ImageVector, val tag: String) {
    CLASSIC(0, "經典 2048", Icons.Default.GridOn, "nav_classic"),
    BATTLE(1, "1v1 對抗", Icons.Default.SportsEsports, "nav_battle"),
    DAILY(2, "每日挑戰", Icons.Default.CalendarToday, "nav_daily"),
    LEADERBOARD(3, "天梯榜", Icons.Default.Leaderboard, "nav_leaderboard"),
    PROFILE(4, "我的", Icons.Default.Person, "nav_profile")
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    var selectedTab by remember { mutableIntStateOf(MainTab.CLASSIC.index) }
    val profile by viewModel.profile.collectAsState()

    val currentTheme = ThemeOption.entries.firstOrNull { it.id == profile.selectedTheme } ?: ThemeOption.CLASSIC
    val themeConfig = remember(currentTheme) { ThemeManager.getConfig(currentTheme) }

    // Back button behavior: if on other tab, navigate back to Classic tab
    BackHandler(enabled = selectedTab != MainTab.CLASSIC.index) {
        selectedTab = MainTab.CLASSIC.index
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = themeConfig.cardSurface,
                contentColor = themeConfig.textPrimary,
                tonalElevation = 8.dp
            ) {
                MainTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab.index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab.index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isSelected) themeConfig.accent else themeConfig.textSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = themeConfig.accent,
                            indicatorColor = themeConfig.accent,
                            unselectedIconColor = themeConfig.textSecondary,
                            unselectedTextColor = themeConfig.textSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.CLASSIC.index -> ClassicGameScreen(viewModel = viewModel)
                MainTab.BATTLE.index -> Battle1v1Screen(viewModel = viewModel)
                MainTab.DAILY.index -> DailyChallengeScreen(viewModel = viewModel)
                MainTab.LEADERBOARD.index -> LeaderboardScreen(viewModel = viewModel)
                MainTab.PROFILE.index -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}
