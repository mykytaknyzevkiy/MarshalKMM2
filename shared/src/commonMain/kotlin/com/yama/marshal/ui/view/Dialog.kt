package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable

@Composable
internal expect fun Dialog(title: String,
                           message: String,
                           onConfirmClick: () -> Unit,
                           onCancelClick: (() -> Unit)?)