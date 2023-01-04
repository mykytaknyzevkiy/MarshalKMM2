package com.yama.marshal.screen.send_message

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.VisualTransformation
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.closeKeyboard
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.Dialog
import com.yama.marshal.ui.view.MarshalList
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

        if (currentState is SendMessageViewState.Loading)
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        else {
            Column {
                Text(
                    "Custom message:",
                    Modifier.fillMaxWidth().padding(Sizes.screenPadding),
                    fontSize = dimensions.bodySmall
                )

                val currentMessage by remember(viewModel) {
                    viewModel.currentMessage
                }.collectAsState()

                com.yama.marshal.ui.view.TextField(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Sizes.screenPadding),
                    value = currentMessage,
                    label = Strings.send_message_screen_message_text_field_label,
                    onValueChange = { viewModel.setMessage(it) },
                    isEnable = currentState !is SendMessageViewState.Loading,
                    visualTransformation = VisualTransformation.None,
                    isError = false
                )

                Text(
                    "Templates:",
                    Modifier.fillMaxWidth().padding(Sizes.screenPadding),
                    fontSize = dimensions.bodySmall
                )

                MarshalList(
                    modifier = Modifier.weight(1f),
                    list = viewModel.messages,
                    itemContent = {
                        Text(
                            modifier = Modifier.padding(Sizes.screenPadding),
                            text = it.message
                        )
                    },
                    onTapItem = {
                        closeKeyboard()
                        viewModel.setMessage(it.message)
                    }
                )

                /*LazyColumn {
                    items(viewModel.messages) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = Sizes.screenPadding / 2)
                                .fillMaxWidth()
                                .clickable {
                                    closeKeyboard()
                                    viewModel.setMessage(it.message)
                                }
                                .border(1.dp, Color.LightGray)
                        ) {
                            Text(
                                modifier = Modifier.padding(Sizes.screenPadding / 2),
                                text = it.message
                            )
                        }
                    }
                }*/
            }
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