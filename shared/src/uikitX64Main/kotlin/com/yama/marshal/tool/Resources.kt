package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.Font
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.posix.memcpy
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

@Composable
internal actual fun fontResources(
    font: String
): Font = Font(0)

@Composable
internal actual fun painterResource(path: String): Painter {
    /*val (filename, type) = when (val lastPeriodIndex = path.lastIndexOf('.')) {
        0 -> {
            null to path.drop(1)
        }
        in 1..Int.MAX_VALUE -> {
            path.take(lastPeriodIndex) to path.drop(lastPeriodIndex + 1)
        }
        else -> {
            path to null
        }
    }

    val pathBundle = mainBundle.pathForResource(filename, type) ?: error(
        "Couldn't get path of $path (parsed as: ${listOfNotNull(filename,type).joinToString(".")})"
    )*/

    val image = UIImage.imageNamed(path) ?: error(
        "Couldn't getUIImage.imageNamed $path)"
    )
    val data = UIImagePNGRepresentation(image)!!
    val byteArray = ByteArray(data.length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), data.bytes, data.length)
        }
    }
    return BitmapPainter(
        org.jetbrains.skia.Image.makeFromEncoded(byteArray).toComposeImageBitmap()
    )
}