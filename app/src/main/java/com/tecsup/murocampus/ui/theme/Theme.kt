package com.tecsup.murocampus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MuroCampusColorScheme = lightColorScheme(
    primary = AzulMuroCampus,
    onPrimary = Color.White,
    background = FondoMuroCampus
)

@Composable
fun MuroCampusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MuroCampusColorScheme,
        typography = Typography,
        content = content
    )
}
