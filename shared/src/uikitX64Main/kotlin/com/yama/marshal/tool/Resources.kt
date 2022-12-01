package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.darwin.NSObject
import platform.darwin.NSObjectMeta
import platform.posix.memcpy

private val bundle: NSBundle = NSBundle.bundleForClass(BundleMarker)

private class BundleMarker : NSObject() {
    companion object : NSObjectMeta()
}

@Composable
internal actual fun fontResources(
    font: String
): Font = Font(0)

@Composable
internal actual fun painterResource(path: String): Painter {
    val path = bundle.pathForResource("img_app_logo", "png", "drawable") ?: error(
        "Couldn't get path of"
    )

    /*
    memScoped {
        val errorPtr = alloc<ObjCObjectVar<NSError?>>()

        NSString.stringWithContentsOfFile(
            path,
            encoding = NSUTF8StringEncoding,
            error = errorPtr.ptr
        ) ?: run {
            error("Couldn't load resource: $name. Error: ${errorPtr.value?.localizedDescription} - ${errorPtr.value}")
        }
    }
     */

    return BitmapPainter(ImageBitmap(0, 0))
}