package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import com.yama.marshal.currentRootView
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertController

@Composable
internal actual fun Dialog(title: String,
                           message: String,
                           onConfirmClick: () -> Unit,
                           onCancelClick: (() -> Unit)?) {
    currentRootView.presentModalViewController(
        UIAlertController().apply {
        setTitle(title)
        setMessage(message)

        addAction(UIAlertAction().apply {
            addAction(UIAlertAction.actionWithTitle(title = "Ok", style = 1, handler = {
                onConfirmClick()
            }))
        })
        if (onCancelClick != null)
            addAction(UIAlertAction.actionWithTitle(title = "Cancel", style = 0, handler = {
                onCancelClick()
            }))
    },
        false
    )
}