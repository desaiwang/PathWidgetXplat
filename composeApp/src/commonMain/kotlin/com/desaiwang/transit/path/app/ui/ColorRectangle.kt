package com.desaiwang.transit.path.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.model.ColorWrapper
import com.desaiwang.transit.path.model.unwrap
import com.desaiwang.transit.path.util.conditional

@Composable
fun ColorRectangle(
    colors: List<ColorWrapper>, 
    modifier: Modifier = Modifier,
    cornerRadius: Float = 12f
) {
    Column(
        modifier
            .width(8.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = cornerRadius.dp, bottomStart = cornerRadius.dp))
            .conditional(isSystemInDarkTheme()) {
                border(1.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(topStart = cornerRadius.dp, bottomStart = cornerRadius.dp))
            }
    ) {
        when (colors.size) {
            1 -> {
                // Single color - fill entire rectangle
                colors.firstOrNull()?.let { color ->
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .background(color.unwrap())
                    )
                }
            }
            2 -> {
                // Two colors - split vertically
                colors.getOrNull(0)?.let { color ->
                    Box(
                        Modifier
                            .weight(1f)
                            .background(color.unwrap())
                    )
                }
                colors.getOrNull(1)?.let { color ->
                    Box(
                        Modifier
                            .weight(1f)
                            .background(color.unwrap())
                    )
                }
            }
            else -> {
                // Three or more colors - use quarters
                colors.getOrNull(0)?.let { color ->
                    Box(
                        Modifier
                            .weight(2f) // Top half
                            .background(color.unwrap())
                    )
                }
                colors.getOrNull(1)?.let { color ->
                    Box(
                        Modifier
                            .weight(1f) // Bottom quarter
                            .background(color.unwrap())
                    )
                }
                colors.getOrNull(2)?.let { color ->
                    Box(
                        Modifier
                            .weight(1f) // Bottom quarter
                            .background(color.unwrap())
                    )
                }
            }
        }
    }
}
