package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.round
import com.yama.marshal.currentRootView
import platform.CoreGraphics.CGRectMake
import platform.UIKit.*

@Composable
internal actual fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    label: String,
    isError: Boolean,
    visualTransformation: VisualTransformation,
) {
    val uiTextField = remember {
        UITextField().apply {
            this.setPlaceholder(label)
            this.setBorderStyle(UITextBorderStyle.UITextBorderStyleRoundedRect)
        }
    }
    val density = LocalDensity.current.density

    Layout(
        content = {
            uiTextField.layer.setZPosition(Double.MAX_VALUE)
        },
        modifier = modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size

            uiTextField.setFrame(
                CGRectMake(
                    x = (location.x / density).toDouble(),
                    y = (location.y / density).toDouble(),
                    width = (size.width / density).toDouble(),
                    height = (size.height / density).toDouble()
                )
            )
            uiTextField.layer.setZPosition(Double.MAX_VALUE)
        },
        measurePolicy = { _, _ -> layout(0, 0) {} }
    )

    uiTextField.setText(value)

    DisposableEffect(uiTextField) {
        currentRootView.view.addSubview(uiTextField)
        uiTextField.layer.setZPosition(Double.MAX_VALUE)
        onDispose {
            currentRootView.view.willRemoveSubview(uiTextField)
        }
    }
}