package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KronAccent
import com.example.ui.theme.KronCard
import com.example.ui.theme.KronNegative
import com.example.ui.theme.KronPositive
import com.example.ui.theme.KronText

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ControlButtons(
    modifier: Modifier = Modifier,
    onSteerLeftState: (Boolean) -> Unit,
    onSteerRightState: (Boolean) -> Unit,
    onBrakeState: (Boolean) -> Unit,
    onBoostState: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Column: Steer Left Arrow [ ← ] and Brake [ Brake ]
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Left Arrow Button [ ← ]
            ControlButtonBox(
                onStateChanged = onSteerLeftState,
                sizeDp = 76
            ) { isPressed ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Steer Left",
                    tint = if (isPressed) KronAccent else KronText,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Brake Button [ Brake ]
            ControlButtonBox(
                onStateChanged = onBrakeState,
                sizeDp = 76,
                backgroundColor = KronCard.copy(alpha = 0.65f),
                activeColor = KronNegative.copy(alpha = 0.85f)
            ) { isPressed ->
                Text(
                    text = "Brake",
                    color = if (isPressed) KronText else KronNegative,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
            }
        }

        // Right Column: Steer Right Arrow [ → ] and Boost [ ⚡ ]
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Right Arrow Button [ → ]
            ControlButtonBox(
                onStateChanged = onSteerRightState,
                sizeDp = 76
            ) { isPressed ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Steer Right",
                    tint = if (isPressed) KronAccent else KronText,
                    modifier = Modifier.size(38.dp)
                )
            }

            // Nitro Boost Button [ ⚡ ]
            ControlButtonBox(
                onStateChanged = onBoostState,
                sizeDp = 76,
                backgroundColor = KronCard.copy(alpha = 0.65f),
                activeColor = KronPositive.copy(alpha = 0.85f)
            ) { isPressed ->
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Nitro Boost",
                    tint = if (isPressed) KronAccent else KronPositive,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ControlButtonBox(
    onStateChanged: (Boolean) -> Unit,
    sizeDp: Int = 76,
    backgroundColor: Color = KronCard.copy(alpha = 0.65f),
    activeColor: Color = KronCard.copy(alpha = 0.95f),
    content: @Composable (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isPressed) activeColor else backgroundColor)
            .border(
                width = 2.dp,
                color = if (isPressed) KronAccent else Color(0x55FFFFFF),
                shape = RoundedCornerShape(22.dp)
            )
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        onStateChanged(true)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        onStateChanged(false)
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content(isPressed)
    }
}

