package com.yama.marshal.ui.tool

internal enum class Orientation {
    LANDSCAPE,
    PORTRAIT
}

internal data class ScreenSize(
    val width: Int,
    val height: Int
)

internal expect fun currentOrientation(): Orientation

internal expect fun screenSize(): ScreenSize