package com.yama.marshal.ui.view

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.VisualTransformation
import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootViewController
import com.yama.marshal.onKeyboardOpen
import kotlinx.coroutines.delay
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSAttributedString
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
    isEnable: Boolean,
    visualTransformation: VisualTransformation,
) {
    val secondaryColor = UIColor(
        red = MaterialTheme.colorScheme.secondary.red.toDouble(),
        green = MaterialTheme.colorScheme.secondary.green.toDouble(),
        blue = MaterialTheme.colorScheme.secondary.blue.toDouble(),
        alpha = MaterialTheme.colorScheme.secondary.alpha.toDouble()
    )

    val placeholderColor = UIColor(
        red = MaterialTheme.colorScheme.secondary.red.toDouble(),
        green = MaterialTheme.colorScheme.secondary.green.toDouble(),
        blue = MaterialTheme.colorScheme.secondary.blue.toDouble(),
        alpha = 0.5
    )

    val errorColor = UIColor(
        red = MaterialTheme.colorScheme.error.red.toDouble(),
        green = MaterialTheme.colorScheme.error.green.toDouble(),
        blue = MaterialTheme.colorScheme.error.blue.toDouble(),
        alpha = MaterialTheme.colorScheme.error.alpha.toDouble()
    )

    val testDelegate = remember(modifier) {
        object : UITextFieldDelegateProtocol, NSObject() {
            override fun textFieldShouldReturn(textField: UITextField): Boolean {
                textField.resignFirstResponder()
                currentRootViewController.view.endEditing(true)
                return true
            }
        }
    }

    val uiTextField = remember(testDelegate) {
        UITextField().apply {
            this.setAttributedPlaceholder(
                NSAttributedString.create(
                    string = label,
                    attributes = mapOf(
                        Pair(
                            NSForegroundColorAttributeName,
                            placeholderColor
                        )
                    )
                )
            )

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
            this.layer.setBorderWidth(1.0)

            this.setPlaceholder(label)

            this.setSecureTextEntry(visualTransformation != VisualTransformation.None)

            this.addAction(UIAction.actionWithHandler {
                onValueChange(this.text() ?: "")
            }, UIControlEventEditingChanged)

            this.autocorrectionType = UITextAutocorrectionType.UITextAutocorrectionTypeNo

            this.layer.setZPosition(1.0)
        }
    }

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
        },
        measurePolicy = { _, _ -> layout(0, 0) {} }
    )

    uiTextField.setText(value)
    uiTextField.setUserInteractionEnabled(isEnable)

    if (isError) {
        uiTextField.layer.setBorderColor(errorColor.CGColor)
        uiTextField.setTextColor(errorColor)
    } else {
        uiTextField.layer.setBorderColor(secondaryColor.CGColor)
        uiTextField.setTextColor(secondaryColor)
    }

    DisposableEffect(modifier){
        currentRootViewController.view.addSubview(uiTextField)

        onDispose {
            uiTextField.removeFromSuperview()
        }
    }
}

@Composable
internal actual fun isKeyboardOpen(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onKeyboardOpen = {
            keyboardState.value = it
        }

        onDispose {
            onKeyboardOpen = null
        }
    }

    return keyboardState
}