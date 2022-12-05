package com.yama.marshal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.fontResources

@Composable
internal fun Typography() = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(fontResources("bebas_neue.ttf")),
        fontWeight = FontWeight.Normal,
        fontSize = LocalAppDimens.current.bodyLarge
    ),

    bodySmall = TextStyle(
        fontFamily = FontFamily(fontResources("bebas_neue.ttf")),
        fontWeight = FontWeight.Normal,
        fontSize = LocalAppDimens.current.bodySmall
    ),

    labelLarge = TextStyle(
        fontFamily = FontFamily(fontResources("bebas_neue.ttf")),
        fontWeight = FontWeight.Normal,
        fontSize = LocalAppDimens.current.labelLarge
    ),

)