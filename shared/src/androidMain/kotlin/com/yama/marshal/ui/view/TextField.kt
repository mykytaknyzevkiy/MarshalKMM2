package com.yama.marshal.ui.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    label: String,
    isError: Boolean,
    visualTransformation: VisualTransformation,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        label = {
            Text(label)
        },
        visualTransformation = visualTransformation,
        onValueChange = onValueChange,
        isError = isError
    )
}