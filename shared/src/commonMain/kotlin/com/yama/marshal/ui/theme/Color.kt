package com.yama.marshal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal object YamaColor {
    val fleet_navigation_card_bg = Color(35, 100, 165)
    val hole_navigation_card_bg = Color(71,71,71)
    val alert_navigation_card_bg = Color(255, 119, 0)

    val pace_ahead_color = Color(27, 139, 0)
    val pace_behind_color = Color(183, 17, 21)

    val cart_shut_down_bg = Color(255, 0, 0)

    @Composable
    fun itemColor(position: Int) = if (position % 2 == 0) Color(
        238,
        238,
        238
    ) else MaterialTheme.colorScheme.background
}