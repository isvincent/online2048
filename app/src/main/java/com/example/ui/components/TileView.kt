package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tile
import com.example.ui.theme.BoardThemeConfig

@Composable
fun TileView(
    tile: Tile?,
    themeConfig: BoardThemeConfig,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(if (tile?.isNew == true) 0.3f else 1f) }

    LaunchedEffect(tile?.id, tile?.isMerged) {
        if (tile?.isNew == true) {
            scale.snapTo(0.2f)
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        } else if (tile?.isMerged == true) {
            scale.animateTo(1.22f, tween(110))
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    val shape = RoundedCornerShape(8.dp)

    if (tile == null) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(themeConfig.emptyCellColor)
        )
    } else if (tile.isObstacle) {
        // Obstacle block (Ice Block / Interference)
        Box(
            modifier = modifier
                .scale(scale.value)
                .clip(shape)
                .background(Color(0xFF64748B))
                .border(2.dp, Color(0xFF94A3B8), shape)
                .testTag("obstacle_tile"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "❄️",
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        val bgColor = themeConfig.tileColors(tile.value)
        val textColor = themeConfig.tileTextColor(tile.value)

        val fontSize = when {
            tile.value < 100 -> 30.sp
            tile.value < 1000 -> 24.sp
            tile.value < 10000 -> 19.sp
            else -> 15.sp
        }

        Box(
            modifier = modifier
                .scale(scale.value)
                .clip(shape)
                .background(bgColor)
                .testTag("tile_${tile.value}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${tile.value}",
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}
