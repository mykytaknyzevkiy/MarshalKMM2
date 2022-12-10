package com.yama.marshal.ui.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
internal actual fun Dialog(title: String,
                           message: String,
                           onConfirmClick: () -> Unit,
                           onCancelClick: (() -> Unit)?) {
    AlertDialog(
        confirmButton = {
            Button(onClick = onConfirmClick) {
                Text("OK")
            }
        },
        dismissButton = if (onCancelClick != null) {
            { Button(onClick = onCancelClick) {
                Text("Cancel")
            } }
        } else null,
        title = {
            Text(title)
        },
        text = {
            Text(message)
        },
        onDismissRequest = {
            if (onCancelClick != null)
                onCancelClick()
            else
                onConfirmClick()
        }
    )
}