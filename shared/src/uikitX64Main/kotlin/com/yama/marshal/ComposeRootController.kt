package com.yama.marshal

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Application
import androidx.compose.ui.window.Popup
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.NSObject

internal val currentRootView by lazy {
    Application(title = "Nek") {
        App()
    }
}

fun getRootController() = ComposeRootController()

class ComposeRootController : UIViewController(null, null) {
    private val keyboardVisibilityListener = object : NSObject() {
        @Suppress("unused")
        @ObjCAction
        fun keyboardWillShow(arg: NSNotification) {
            val (width, height) = getViewFrameSize()

            view.setClipsToBounds(true)

            composeView.layer.setBounds(
                CGRectMake(
                    x = 0.0,
                    y = 0.0,
                    width = width.toDouble(),
                    height = height.toDouble()
                )
            )
        }

        @Suppress("unused")
        @ObjCAction
        fun keyboardWillHide(arg: NSNotification) {
            val (width, height) = getViewFrameSize()
            view.layer.setBounds(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
        }

        @Suppress("unused")
        @ObjCAction
        fun keyboardDidHide(arg: NSNotification) {
            view.setClipsToBounds(false)
        }
    }

    private val composeView = currentRootView.view

    override fun viewDidLoad() {
        super.viewDidLoad()

        this.view.addSubview(composeView)
    }

    private fun getViewFrameSize(): IntSize {
        val (width, height) = view.frame().useContents { this.size.width to this.size.height }
        return IntSize(width.toInt(), height.toInt())
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        NSNotificationCenter.defaultCenter.addObserver(
            observer = keyboardVisibilityListener,
            selector = NSSelectorFromString("keyboardWillShow:"),
            name = UIKeyboardWillShowNotification,
            `object` = null
        )
    }
}