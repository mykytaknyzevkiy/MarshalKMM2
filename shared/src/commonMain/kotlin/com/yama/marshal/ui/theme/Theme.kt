package com.yama.marshal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    background = Color(0xFFFFFBFE),

    primary = Color.Red,

    onPrimary = Color.White,

    secondary = Color.Blue,

    onBackground = Color.Black,

    error = Color(255, 95, 21)

   // primaryContainer = Color.Red
)

@Composable
internal fun YamaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography(),
        shapes = Shapes(
            extraLarge = RoundedCornerShape(0.dp),
            large = RoundedCornerShape(0.dp),
            extraSmall = RoundedCornerShape(0.dp),
            small = RoundedCornerShape(0.dp),
            medium = RoundedCornerShape(0.dp)
        ),
        content = content
    )
}

object Sizes {
    val set_player_list_height = 280.dp

    val screenPadding = 24.dp

    val buttonSize = 75.dp

    val button_icon_size = 32.dp

    val scoreboard_sides_tab_width = 180.dp

    val title = 44.sp

    object ScoreBoardScreen {
        const val PLAYER_CARD_WEIGHT = 1.8f
        const val TOTAL_SCORE_CARD_WEIGHT = 1.3f
    }

    val player_card_player_name = 34.sp
    val player_card_score = 44.sp

    val score_hole_card_score = 66.sp

    val set_score_alert_score_text = 74.sp

    val system_dialog_width = 940.dp
    val system_dialog_height = 520.dp

    val login_screen_logo_width = 400.dp

    val login_screen_content_width = 430.dp

}