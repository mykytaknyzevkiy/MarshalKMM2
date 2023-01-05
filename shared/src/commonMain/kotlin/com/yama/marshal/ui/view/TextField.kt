package com.yama.marshal.ui.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Composable
@ExperimentalMaterial3Api
internal expect fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    label: String,
    isError: Boolean,
    isEnable: Boolean,
    visualTransformation: VisualTransformation
)

@Composable
internal expect fun isKeyboardOpen(): State<Boolean>