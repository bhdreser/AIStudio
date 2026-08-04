package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun App3DLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    // Official Branding Logo matching Android Launcher Icon (3D Gold Question Mark + Emerald Tick)
    val cornerRadius = (size.value * 0.28f).dp
    val shadowElevation = (size.value * 0.12f).dp.coerceAtMost(8.dp)

    Box(
        modifier = modifier
            .size(size)
            .shadow(shadowElevation, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7C3AED),
                        Color(0xFF6D28D9),
                        Color(0xFF4C1D95)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "What's this? Logo",
            modifier = Modifier.size(size * 1.35f)
        )
    }
}

