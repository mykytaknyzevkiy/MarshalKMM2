package com.yama.marshal.screen.send_message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.Strings
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

    private val sendSendMessageText = mutableStateOf("")

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun content(args: List<NavArg>) {
        val dimensions = LocalAppDimens.current

        cartID = args.findInt(ARG_CART_ID) ?: return

        val currentState by remember { viewModel.currentState }.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(Sizes.screenPadding)) {
            if (currentState is SendMessageViewState.Loading)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else {
                Text("Custom message:", fontSize = dimensions.bodySmall)

                Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                Row {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = sendSendMessageText.value,
                        placeholder = {
                            Text(Strings.send_message_screen_message_text_field_label)
                        },
                        onValueChange = { sendSendMessageText.value = it },
                        enabled = currentState !is SendMessageViewState.Loading
                    )

                    IconButton(
                        onClick = {
                            viewModel.sendMessage(cartID, sendSendMessageText.value)
                        },
                        enabled = currentState !is SendMessageViewState.Loading
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                Text("Templates:", fontSize = dimensions.bodySmall)

                Spacer(modifier = Modifier.height(Sizes.screenPadding / 2))

                LazyColumn {
                    items(viewModel.messages) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = Sizes.screenPadding / 2)) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = it.message
                            )

                            IconButton(
                                onClick = {

                                },
                                enabled = currentState != SendMessageViewState.Loading
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.fillMaxWidth().height(1.dp)
                                .background(Color.LightGray)
                                .padding(vertical = Sizes.screenPadding)
                        )
                    }
                }
            }
        }

        LaunchedEffect(viewModel) {
            viewModel.loadMessages()
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
    }

    override val isToolbarEnable: Boolean = true

    @Composable
    override fun title(): String = Strings.send_message_screen_title
}