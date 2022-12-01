package com.yama.marshal.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yama.marshal.tool.fontResources

@Composable
internal fun Typography() = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(fontResources("bebas_neue.ttf")),
        fontWeight = FontWeight.Normal,
        fontSize = 30.sp
    ),

    labelLarge = TextStyle(
        fontFamily = FontFamily(fontResources("bebas_neue.ttf")),
        fontWeight = FontWeight.Normal,
        fontSize = 37.sp
    )
)