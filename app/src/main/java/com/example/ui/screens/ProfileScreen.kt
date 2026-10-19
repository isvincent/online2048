package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.Achievement
import com.example.model.ThemeOption
import com.example.ui.theme.ThemeManager
import com.example.viewmodel.GameViewModel

@Composable
fun ProfileScreen(viewModel: GameViewModel) {
    val profile by viewModel.profile.collectAsState()
    val achievements by viewModel.achievements.collectAsState()

    val currentTheme = ThemeOption.entries.firstOrNull { it.id == profile.selectedTheme } ?: ThemeOption.CLASSIC
    val themeConfig = remember(currentTheme) { ThemeManager.getConfig(currentTheme) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    val avatarOptions = listOf("🎮", "🦊", "⚡", "👑", "🧙‍♂️", "🔥", "🐱", "🤖", "🦁", "🦄", "🎯", "🚀")

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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "👤 個人戰績與設置",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = themeConfig.textPrimary
            )
            Text(
                text = "自訂玩家檔案、主題風格與成就榮譽",
                fontSize = 12.sp,
                color = themeConfig.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar (Clickable to change)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(themeConfig.accent.copy(alpha = 0.18f))
                            .border(2.dp, themeConfig.accent, CircleShape)
                            .clickable { showAvatarPicker = true }
                            .testTag("avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.avatar, fontSize = 38.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = profile.nickname,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeConfig.textPrimary
                        )
                        IconButton(
                            onClick = {
                                tempName = profile.nickname
                                showEditNameDialog = true
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("edit_nickname_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "編輯暱稱",
                                tint = themeConfig.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${profile.country} · ${profile.tier} (${profile.rp} RP)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeConfig.accent
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(title = "總遊玩場次", value = "${profile.gamesPlayed}", themeConfig = themeConfig)
                        StatItem(title = "1v1 勝場", value = "${profile.battlesWon}", themeConfig = themeConfig)
                        val winRate = if (profile.gamesPlayed > 0) (profile.battlesWon * 100 / profile.gamesPlayed) else 0
                        StatItem(title = "勝率", value = "$winRate%", themeConfig = themeConfig)
                        StatItem(title = "歷史最高方塊", value = "${profile.highestTile}", themeConfig = themeConfig)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Theme Switcher Section
            Text(
                text = "🎨 棋盤外觀主題",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = themeConfig.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ThemeOption.entries) { themeOption ->
                    val isSelected = currentTheme == themeOption
                    val optionConfig = ThemeManager.getConfig(themeOption)

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) themeConfig.accent.copy(alpha = 0.2f) else themeConfig.cardSurface
                        ),
                        modifier = Modifier
                            .width(130.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) themeConfig.accent else Color.LightGray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.updateTheme(themeOption) }
                            .testTag("theme_${themeOption.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mini palette preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                listOf(2, 8, 64, 2048).forEach { v ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(optionConfig.tileColors(v))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = themeOption.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeConfig.textPrimary
                            )
                            Text(
                                text = themeOption.description,
                                fontSize = 10.sp,
                                color = themeConfig.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound & Vibration Settings
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = themeConfig.cardSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("⚙️ 遊戲設置", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = themeConfig.textPrimary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = themeConfig.accent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("滑動與合成音效", fontSize = 14.sp, color = themeConfig.textPrimary)
                        }
                        Switch(
                            checked = profile.soundEnabled,
                            onCheckedChange = { viewModel.toggleSound(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = themeConfig.accent),
                            modifier = Modifier.testTag("sound_toggle")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = themeConfig.accent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("觸覺振動回饋", fontSize = 14.sp, color = themeConfig.textPrimary)
                        }
                        Switch(
                            checked = profile.vibrationEnabled,
                            onCheckedChange = { viewModel.toggleVibration(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = themeConfig.accent),
                            modifier = Modifier.testTag("vibration_toggle")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Achievements List
            Text(
                text = "🎖️ 成就榮譽殿堂",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = themeConfig.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            achievements.forEach { achievement ->
                AchievementRow(achievement = achievement, themeConfig = themeConfig)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Edit Nickname Dialog
        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("修改玩家暱稱", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("玩家名稱") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateNickname(tempName)
                            showEditNameDialog = false
                        }
                    ) {
                        Text("確認儲存")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("取消")
                    }
                }
            )
        }

        // Avatar Picker Dialog
        if (showAvatarPicker) {
            AlertDialog(
                onDismissRequest = { showAvatarPicker = false },
                title = { Text("選擇頭像圖示", fontWeight = FontWeight.Bold) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        avatarOptions.chunked(4).forEach { col ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                col.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (profile.avatar == emoji) themeConfig.accent.copy(alpha = 0.25f)
                                                else Color.LightGray.copy(alpha = 0.15f)
                                            )
                                            .clickable {
                                                viewModel.updateAvatar(emoji)
                                                showAvatarPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 24.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAvatarPicker = false }) {
                        Text("關閉")
                    }
                }
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    themeConfig: com.example.ui.theme.BoardThemeConfig
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = themeConfig.textPrimary)
        Text(title, fontSize = 10.sp, color = themeConfig.textSecondary)
    }
}

@Composable
private fun AchievementRow(
    achievement: Achievement,
    themeConfig: com.example.ui.theme.BoardThemeConfig
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) themeConfig.accent.copy(alpha = 0.12f) else themeConfig.cardSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) themeConfig.accent.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(achievement.icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = achievement.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.textPrimary
                    )
                    Text(
                        text = if (achievement.isUnlocked) "已達成 ✅" else "${achievement.current}/${achievement.target}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (achievement.isUnlocked) Color(0xFF10B981) else themeConfig.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = achievement.description,
                    fontSize = 11.sp,
                    color = themeConfig.textSecondary
                )

                if (!achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = (achievement.current.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = themeConfig.accent,
                        trackColor = themeConfig.boardBackground.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
