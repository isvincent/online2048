package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Direction
import com.example.model.Tile
import com.example.ui.theme.BoardThemeConfig
import kotlin.math.abs

@Composable
fun GameBoard(
    size: Int,
    tiles: List<Tile>,
    themeConfig: BoardThemeConfig,
    onSwipe: (Direction) -> Unit,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true
) {
    val gestureModifier = if (isInteractive) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var dragX = 0f
                    var dragY = 0f
                    var handled = false
                    val pointerId = down.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || !change.pressed) {
                            break
                        }
                        val positionDelta = change.positionChange()
                        dragX += positionDelta.x
                        dragY += positionDelta.y

                        if (!handled) {
                            val absX = abs(dragX)
                            val absY = abs(dragY)
                            if (absX > 30f || absY > 30f) {
                                handled = true
                                change.consume()
                                if (absX > absY) {
                                    if (dragX > 0) {
                                        onSwipe(Direction.RIGHT)
                                    } else {
                                        onSwipe(Direction.LEFT)
                                    }
                                } else {
                                    if (dragY > 0) {
                                        onSwipe(Direction.DOWN)
                                    } else {
                                        onSwipe(Direction.UP)
                                    }
                                }
                            }
                        } else {
                            change.consume()
                        }
                    }
                }
            }
        }
    } else Modifier

    // Map tiles by (row, col)
    val tileMap = remember(tiles) {
        tiles.associateBy { it.row to it.col }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(themeConfig.boardBackground)
            .padding(8.dp)
            .then(gestureModifier)
            .testTag("game_board_grid")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 0 until size) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (c in 0 until size) {
                        val tile = tileMap[r to c]
                        TileView(
                            tile = tile,
                            themeConfig = themeConfig,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
