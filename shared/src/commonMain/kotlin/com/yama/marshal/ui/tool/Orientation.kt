package com.yama.marshal.ui.tool

enum class Orientation {
    LANDSCAPE,
    PORTRAIT
}

internal expect fun currentOrientation(): Orientation