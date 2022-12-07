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
import androidx.compose.ui.text.font.FontFamily
import co.touchlab.kermit.Logger
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSURL
import platform.Foundation.NSXMLParser

private const val TAG = "ResourcesIOS"

@Composable
internal actual fun fontResources(
    font: String
): Font = Font(0)

@Composable
internal actual fun painterResource(path: String): Painter {
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

@Composable
internal actual fun stringResource(key: String): String {
    val path = mainBundle.pathForResource("strings", "xml")

    if (path == null) {
        Logger.e(tag = TAG, message = {
            "Cannot find strings.xml in mainBundle"
        })
        return key
    }

    return key
}