package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.Direction

@Composable
fun DirectionControls(
    onDirectionClick: (Direction) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val buttonColors = IconButtonDefaults.iconButtonColors(
        containerColor = accentColor.copy(alpha = 0.18f),
        contentColor = accentColor
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = { onDirectionClick(Direction.UP) },
            colors = buttonColors,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("control_up")
        ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = "往上滑動")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDirectionClick(Direction.LEFT) },
                colors = buttonColors,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .testTag("control_left")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "往左滑動")
            }

            Box(modifier = Modifier.size(24.dp))

            IconButton(
                onClick = { onDirectionClick(Direction.RIGHT) },
                colors = buttonColors,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .testTag("control_right")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "往右滑動")
            }
        }

        IconButton(
            onClick = { onDirectionClick(Direction.DOWN) },
            colors = buttonColors,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .testTag("control_down")
        ) {
            Icon(Icons.Default.ArrowDownward, contentDescription = "往下滑動")
        }
    }
}
