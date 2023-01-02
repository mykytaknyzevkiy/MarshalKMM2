package com.yama.marshal.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yama.marshal.LocalAppDimens

private val LightColorScheme = lightColorScheme(
    background = Color(172, 173, 173),

    primary = Color(35, 100, 165),

    onPrimary = Color.White,

    secondary = Color.Black,

    onBackground = Color.Black,

    error = Color.Red
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

internal object Sizes {
    private val dimensions: Dimensions
        @Composable
        get() = LocalAppDimens.current

    val screenPadding
        @Composable
        get() = dimensions.screenPadding

    val buttonSize
        @Composable
        get() = dimensions.buttonSize

    val button_icon_size
        @Composable
        get() = dimensions.button_icon_size

    val title
        @Composable
        get() = dimensions.title

    val fleet_view_holder_height
        @Composable
        get() = dimensions.fleet_view_holder_height
}

sealed class Dimensions(
    val bodyLarge: TextUnit = 22.sp,

    val bodySmall: TextUnit = 16.sp,

    val labelLarge: TextUnit = 24.sp,

    val screenPadding: Dp = 24.dp,

    val buttonSize: Dp = 55.dp,

    val button_icon_size: Dp = 32.dp,

    val title: TextUnit = 32.sp,

    val toolbar_height: Dp = 85.dp,

    val fleet_view_holder_height: Dp = 75.dp
) {
    object Phone: Dimensions(
        bodyLarge = 16.sp,

        bodySmall = 12.sp,

        labelLarge = 18.sp,

        title = 22.sp,

        screenPadding = 16.dp,

        button_icon_size = 24.dp,

        buttonSize = 45.dp,

        toolbar_height = 80.dp,

        fleet_view_holder_height = 65.dp
    )

    object PhoneSmall: Dimensions(
        bodyLarge = 12.sp,

        bodySmall = 8.sp,

        labelLarge = 14.sp,

        title = 16.sp,

        screenPadding = 16.dp,

        button_icon_size = 24.dp,

        buttonSize = 45.dp,

        toolbar_height = 75.dp,

        fleet_view_holder_height = 65.dp
    )

    object Tablet: Dimensions()
}