package com.rc.axiom.ui.component.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rc.axiom.ui.theme.LetteraMonoLL

object EmptyViewDefaults {
    val IconSize = 48.dp

    @Composable
    fun defaultColors(
        iconColor: Color = MaterialTheme.colorScheme.secondary,
        iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleColor: Color = MaterialTheme.colorScheme.onSurface,
        textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): EmptyViewColors {
        return EmptyViewColors(
            iconColor = iconColor,
            iconContainerColor = iconContainerColor,
            titleColor = titleColor,
            textColor = textColor
        )
    }
}

data class EmptyViewColors(
    val iconColor: Color,
    val iconContainerColor: Color,
    val titleColor: Color,
    val textColor: Color
)


@Composable
fun EmptyView(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    colors: EmptyViewColors = EmptyViewDefaults.defaultColors(),
    iconSize: Dp = EmptyViewDefaults.IconSize,
    button: @Composable () -> Unit = {}
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize().background(Color.Black)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 200.dp, height = 48.dp)
                .background(Color.Black)
                .border(1.dp, Color(0xFFD71921))
        ) {
            Text(
                text = "[ NULL_RESPONSE ]",
                color = Color(0xFFD71921),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontFamily = LetteraMonoLL
                )
            )
        }
    }
}