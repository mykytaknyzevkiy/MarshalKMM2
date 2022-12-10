package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
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