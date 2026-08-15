package com.rc.axiom.ui.component.compose

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun AxiomSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderBottomOnly: Boolean = false,
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    val view = LocalView.current
    var isTapped by remember { mutableStateOf(false) }

    LaunchedEffect(isTapped) {
        if (isTapped) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            delay(50)
            isTapped = false
        }
    }

    val backgroundColor = if (isTapped) Color.White else Color.Black
    val contentInverted = isTapped

    Box(
        modifier = modifier
            .background(backgroundColor)
            .drawBehind {
                if (borderBottomOnly) {
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            .then(
                if (!borderBottomOnly) {
                    Modifier.border(1.dp, Color.White)
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isTapped = true
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        content(contentInverted)
    }
}

@Composable
fun AxiomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable BoxScope.(Boolean) -> Unit
) {
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val backgroundColor = if (isPressed) Color(0xFFD71921) else Color.Black
    val borderColor = if (isPressed) Color(0xFFD71921) else Color.White
    val contentInverted = isPressed

    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(backgroundColor)
            .border(1.dp, borderColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(contentInverted)
    }
}

@Composable
fun AxiomToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val containerColor = if (checked) Color(0xFFD71921) else Color.Black
    val thumbColor = if (checked) Color.Black else Color.White

    val targetOffset = if (checked) 24.dp else 0.dp

    Box(
        modifier = modifier
            .size(width = 48.dp, height = 24.dp)
            .background(containerColor)
            .border(1.dp, Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onCheckedChange(!checked)
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = targetOffset.toPx().roundToInt(), y = 0) }
                .size(22.dp)
                .background(thumbColor)
        )
    }
}
