package com.yama.marshal.screen.send_message

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.yama.marshal.tool.Strings
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.MarshalList
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class SendMessageScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    companion object {
        const val ROUTE = "send_message"
        const val ARG_CART_ID = "cart_id"
        const val ARG_COURSE_ID = "course_id"
    }

    override val route: String = ROUTE

    override val viewModel: SendMessageViewModel = SendMessageViewModel()

    @Composable
    override fun content(args: List<NavArg>) {
        val cartID = args.findInt(ARG_CART_ID) ?: return
        val courseID = args.findString(ARG_COURSE_ID)

        Column(modifier = Modifier.fillMaxSize()) {
            MarshalList(
                modifier = Modifier.fillMaxWidth().weight(1f),
                list = emptyList<String>()
            ) { _, _ ->

            }
        }
    }

    override val isToolbarEnable: Boolean = true

    @Composable
    override fun title(): String = Strings.send_message_screen_title

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun titleContent() {
        val sendSendMessageText = remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(vertical = Sizes.screenPadding / 2),
            value = sendSendMessageText.value,
            placeholder = {
                Text(Strings.send_message_screen_message_text_field_label)
            },
            onValueChange = { viewModel.updateMessage(it) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.background,
                unfocusedBorderColor = Color.LightGray,
                placeholderColor = Color.LightGray,
                focusedTrailingIconColor = MaterialTheme.colorScheme.background,
                unfocusedTrailingIconColor = Color.LightGray
            )
        )

        LaunchedEffect(viewModel) {
            viewModel
                .currentMessage
                .onEach {
                    sendSendMessageText.value = it
                }
                .launchIn(this)
        }
    }

    @Composable
    override fun actions() {
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.Send,
                contentDescription = null
            )
        }
    }
}