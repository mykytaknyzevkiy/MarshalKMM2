package com.yama.marshal

import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Application
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.*
import platform.darwin.NSObject

internal val currentRootViewController by lazy {
    ComposeRootController()
}

internal var onKeyboardOpen: ((Boolean) -> Unit)? = null

fun getRootController() = currentRootViewController

class ComposeRootController internal constructor(): UIViewController(null, null) {

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

            onKeyboardOpen?.invoke(true)
        }

        @Suppress("unused")
        @ObjCAction
        fun keyboardWillHide(arg: NSNotification) {
            val (width, height) = getViewFrameSize()
            view.layer.setBounds(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))

            onKeyboardOpen?.invoke(false)
        }

        @Suppress("unused")
        @ObjCAction
        fun keyboardDidHide(arg: NSNotification) {
            view.setClipsToBounds(false)

            onKeyboardOpen?.invoke(false)

        }
    }

    private val composeView = Application(title = "Nek") {
        App()
    }.view

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

        NSNotificationCenter.defaultCenter.addObserver(
            observer = keyboardVisibilityListener,
            selector = NSSelectorFromString("keyboardWillHide:"),
            name = UIKeyboardWillHideNotification,
            `object` = null
        )
    }
}