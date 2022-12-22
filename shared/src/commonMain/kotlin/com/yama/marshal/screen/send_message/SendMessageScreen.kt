package com.yama.marshal.screen.send_message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.PopupPositionProvider
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.Dialog
import com.yama.marshal.ui.view.MarshalList
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SendMessageScreen(navigationController: NavigationController)
    : YamaScreen(navigationController) {
    companion object {
        const val ROUTE = "send_message"
        const val ARG_CART_ID = "cart_id"
    }

    override val route: String = ROUTE

    override val viewModel: SendMessageViewModel = SendMessageViewModel()

    private var cartID: Int = 0

    private val sendSendMessageText = mutableStateOf("")

    @Composable
    override fun content(args: List<NavArg>) {
        cartID = args.findInt(ARG_CART_ID) ?: return

        val currentState by remember { viewModel.currentState }.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            if (currentState is SendMessageViewState.Loading)
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            MarshalList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                list = viewModel.messages,
                itemContent = { message ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .clickable { sendSendMessageText.value = message.message },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(message.message, modifier = Modifier.padding(Sizes.screenPadding))
                    }
                }
            )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun titleContent() {
       // val currentState by remember { viewModel.currentState }.collectAsState()

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(vertical = Sizes.screenPadding / 2),
            value = sendSendMessageText.value,
            placeholder = {
                Text(Strings.send_message_screen_message_text_field_label)
            },
            onValueChange = { sendSendMessageText.value = it },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.background,
                unfocusedBorderColor = Color.LightGray,
                placeholderColor = Color.LightGray,
                focusedTrailingIconColor = MaterialTheme.colorScheme.background,
                unfocusedTrailingIconColor = Color.LightGray
            ),
            //enabled = currentState !is SendMessageViewState.Loading
        )
    }

    @Composable
    override fun actions() {
        val currentState by remember { viewModel.currentState }.collectAsState()

        IconButton(onClick = {
            viewModel.sendMessage(cartID, sendSendMessageText.value)
        }, enabled = currentState !is SendMessageViewState.Loading) {
            Icon(
                Icons.Default.Send,
                contentDescription = null
            )
        }
    }
}