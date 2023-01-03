package com.yama.marshal.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.closeKeyboard
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class LoginScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "login"

    override val viewModel: LoginViewModel = LoginViewModel()

    @Composable
    override fun title(): String = ""

    @ExperimentalMaterial3Api
    @Composable
    override fun content(args: List<NavArg>) {
        val orientation = currentOrientation()

        val currentState by remember {
            viewModel.currentState
        }.collectAsState()

        val userName = remember {
            mutableStateOf("")
        }
        val password = remember {
            mutableStateOf("")
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                detectTapGestures {
                    closeKeyboard()
                }
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painterResource("img_app_logo.png"),
                modifier = Modifier
                    .fillMaxWidth(
                        if (orientation == Orientation.LANDSCAPE)
                            0.2f
                        else 0.5f
                    ).padding(Sizes.screenPadding),
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )

            com.yama.marshal.ui.view.TextField(
                value = userName.value,
                label = Strings.login_screen_text_field_username_label,
                modifier = Modifier.padding(Sizes.screenPadding),
                isError = false,
                visualTransformation = VisualTransformation.None,
                onValueChange = {
                    userName.value = it.replace("\n", "").replace(" ", "")
                },
                isEnable = currentState !is LoginViewState.Loading
            )

            com.yama.marshal.ui.view.TextField(
                value = password.value,
                label = Strings.login_screen_text_field_password_label,
                modifier = Modifier.padding(bottom = Sizes.screenPadding),
                isError = currentState is LoginViewState.Error,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    password.value = it.replace("\n", "")
                },
                isEnable = currentState !is LoginViewState.Loading
            )

            if (currentState is LoginViewState.Loading)
                LinearProgressIndicator(modifier = Modifier.wrapContentWidth())
            else
                Button(
                    onClick = {
                        closeKeyboard()
                        viewModel.login(userName = userName.value, password = password.value)
                    },
                    enabled = userName.value.isNotBlank() && password.value.isNotBlank(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(Strings.login_screen_button_login_label.uppercase())
                }
        }

        LaunchedEffect(0) {
            viewModel.currentState
                .onEach {
                    if (it is LoginViewState.OK)
                        navigationController.navigateToAndFinish("main")
                }
                .launchIn(this)
        }
    }

    override val isToolbarEnable: Boolean = false
}