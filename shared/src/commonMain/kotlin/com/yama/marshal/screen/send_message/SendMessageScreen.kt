package com.yama.marshal.screen.send_message

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.closeKeyboard
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.*
import com.yama.marshal.ui.view.Dialog
import com.yama.marshal.ui.view.TextField
import com.yama.marshal.ui.view.YamaScreen

internal class SendMessageScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    companion object {
        const val ROUTE = "send_message"
        const val ARG_CART_ID = "cart_id"
    }

    override val route: String = ROUTE

    override val viewModel: SendMessageViewModel = SendMessageViewModel()

    private var cartID: Int = 0

    @Composable
    @ExperimentalMaterial3Api
    override fun content(args: List<NavArg>) = Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures {
                closeKeyboard()
            }
        }
    ) {
        val dimensions = LocalAppDimens.current

        cartID = args.findInt(ARG_CART_ID) ?: return

        val currentState by remember { viewModel.currentState }.collectAsState()

        Column {
            Text(
                "Custom message:",
                Modifier.fillMaxWidth().padding(Sizes.screenPadding),
                fontSize = dimensions.bodySmall
            )

            val currentMessage = remember(viewModel) {
                viewModel.currentMessage
            }.collectAsState()

            TextField(
                value = currentMessage.value,
                label = Strings.send_message_screen_message_text_field_label,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Sizes.screenPadding),
                isError = false,
                visualTransformation = VisualTransformation.None,
                onValueChange = { viewModel.setMessage(it) },
                isEnable = currentState != SendMessageViewState.Loading,
            )

            Text(
                "Templates:",
                Modifier.fillMaxWidth().padding(Sizes.screenPadding),
                fontSize = dimensions.bodySmall
            )

            PlatformList(
                listState = viewModel.messages,
                itemContent = {
                    MarshalItemText(
                        text = it.message,
                        weight = 1f,
                        textAlign = TextAlign.Start
                    )
                },
                onTapItem = {
                    closeKeyboard()

                    if (currentState != SendMessageViewState.Loading)
                        viewModel.setMessage(it.message)
                },
                key = { item ->
                    item.id
                }
            )
        }

        if (currentState is SendMessageViewState.Success)
            Dialog(
                title = "Success",
                message = "Message have sent",
                onConfirmClick = {
                    viewModel.emptyState()
                },
                onCancelClick = null
            )

        LaunchedEffect(viewModel) {
            viewModel.loadMessages()
        }

        DisposableEffect(Unit) {
            onDispose {
                closeKeyboard()
            }
        }
    }

    override val isToolbarEnable: Boolean = true

    @Composable
    override fun title(): String = Strings.send_message_screen_title

    @Composable
    override fun actions() {
        val currentState by remember { viewModel.currentState }.collectAsState()

        val currentMessage by remember(viewModel) {
            viewModel.currentMessage
        }.collectAsState()

        if (currentState is SendMessageViewState.Loading)
            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.button_icon_size),
                color = MaterialTheme.colorScheme.onPrimary
            )
        else
            IconButton(
                onClick = {
                    closeKeyboard()
                    viewModel.sendMessage(cartID)
                },
                enabled = currentState !is SendMessageViewState.Loading && currentMessage.isNotBlank()
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null
                )
            }
    }
}