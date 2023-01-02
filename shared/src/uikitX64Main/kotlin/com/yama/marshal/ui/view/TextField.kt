package com.yama.marshal.ui.view

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
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
import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootView
import com.yama.marshal.ui.theme.Sizes
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSAttributedString
import platform.Foundation.NSAttributedStringKey
import platform.Foundation.create
import platform.UIKit.*
import platform.darwin.NSObject

@ExperimentalMaterial3Api
@Composable
internal actual fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    label: String,
    isError: Boolean,
    visualTransformation: VisualTransformation,
) {
    val secondary = UIColor(
        red = MaterialTheme.colorScheme.secondary.red.toDouble(),
        green = MaterialTheme.colorScheme.secondary.green.toDouble(),
        blue = MaterialTheme.colorScheme.secondary.blue.toDouble(),
        alpha = MaterialTheme.colorScheme.secondary.alpha.toDouble()
    )

    val primaryColor = UIColor(
        red = MaterialTheme.colorScheme.primary.red.toDouble(),
        green = MaterialTheme.colorScheme.primary.green.toDouble(),
        blue = MaterialTheme.colorScheme.primary.blue.toDouble(),
        alpha = MaterialTheme.colorScheme.primary.alpha.toDouble()
    )

    val backgroundColor = UIColor(
        red = MaterialTheme.colorScheme.background.red.toDouble(),
        green = MaterialTheme.colorScheme.background.green.toDouble(),
        blue = MaterialTheme.colorScheme.background.blue.toDouble(),
        alpha = MaterialTheme.colorScheme.background.alpha.toDouble()
    )

    val testDelegate = remember {
        object : UITextFieldDelegateProtocol, NSObject() {
            override fun textFieldShouldReturn(textField: UITextField): Boolean {
                textField.resignFirstResponder()
                currentRootView.view.endEditing(true)
                Logger.i("NEKAA", message = {
                    "onReturn click"
                })
                return true
            }

            override fun textFieldDidBeginEditing(textField: UITextField) {
                onValueChange(textField.text() ?: "")
            }
        }
    }

    val uiTextField = remember {
        UITextField().apply {
            /*this.setAttributedPlaceholder(
                NSAttributedString.create(
                    string = label,
                    attributes = mapOf(
                        Pair(
                            NSForegroundColorAttributeName,
                            secondary.apply { alpha = 0.5 }
                        )
                    )
                )
            )*/


            this.setTextColor(secondary)

            this.setBackgroundColor(
                UIColor(
                    red = 1.0,
                    green = 1.0,
                    blue = 1.0,
                    alpha = 0.0
                )
            )

            this.setDelegate(testDelegate)

            this.setBorderStyle(UITextBorderStyle.UITextBorderStyleRoundedRect)
            this.layer.setBorderColor(primaryColor.CGColor)
            this.layer.setBorderWidth(1.0)

            this.setPlaceholder(label)
        }
    }

    if (uiTextField.superview != null)
        uiTextField.removeFromSuperview()

    val density = LocalDensity.current.density

    Layout(
        content = {},
        modifier = modifier.defaultMinSize(
            minWidth = TextFieldDefaults.MinWidth,
            minHeight = TextFieldDefaults.MinHeight
        ).onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero)
            val size = coordinates.size

            uiTextField.setFrame(
                CGRectMake(
                    (location.x / density).toDouble(),
                    (location.y / density).toDouble(),
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
            uiTextField.removeFromSuperview()
        }
    }
}