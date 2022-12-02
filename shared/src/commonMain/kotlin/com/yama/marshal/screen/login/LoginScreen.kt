package com.yama.marshal.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.painterResource
import com.yama.marshal.tool.stringResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.YamaScreen

internal class LoginScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "login"

    override val viewModel: LoginViewModel = LoginViewModel()

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.let {
                    if (currentOrientation() == Orientation.LANDSCAPE) it.width(
                        Sizes.login_screen_content_width
                    ) else it.padding(Sizes.screenPadding)
                },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painterResource("img_app_logo.png"),
                    modifier = Modifier.width(Sizes.login_screen_logo_width),
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(Sizes.screenPadding * 3))

                val userName = remember {
                    mutableStateOf("")
                }

                UserNameField(userName)

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                val password = remember {
                    mutableStateOf("")
                }

                PasswordField(password)

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                val currentState by remember {
                    viewModel.currentState
                }.collectAsState()

                if (currentState is LoginViewState.Loading)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                else
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.login(userName = userName.value, password = password.value)
                        },
                        enabled = userName.value.isNotBlank() && password.value.isNotBlank(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(stringResource("login_screen_button_login_label").uppercase())
                    }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserNameField(userName: MutableState<String>) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = userName.value,
            label = {
                Text(stringResource("login_screen_text_field_username_label"))
            },
            onValueChange = { userName.value = it }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PasswordField(password: MutableState<String>) {
        val currentState by remember {
            viewModel.currentState
        }.collectAsState()

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password.value,
            label = {
                Text(stringResource("login_screen_text_field_password_label"))
            },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password.value = it },
            isError = currentState is LoginViewState.Error
        )
    }

    override val isToolbarEnable: Boolean = false
}