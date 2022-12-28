package com.yama.marshal.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalComposeUiApi::class)
internal class LoginScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "login"

    override val viewModel: LoginViewModel = LoginViewModel()

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        val keyboardController = LocalSoftwareKeyboardController.current

        Box(modifier = Modifier
            .fillMaxSize()
            .clickable { keyboardController?.hide() }, contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.let {
                    if (currentOrientation() == Orientation.LANDSCAPE) it.width(
                        Sizes.tablet_login_screen_content_width
                    ) else it.padding(Sizes.screenPadding)
                }, horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painterResource("img_app_logo.png"),
                    modifier = Modifier.width(Sizes.login_screen_logo_width),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.height(Sizes.screenPadding * 3))

                val userName = remember {
                    mutableStateOf("SyncwiseDisney")
                }

                UserNameField(userName)

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                val password = remember {
                    mutableStateOf("92108340")
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
                        Text(Strings.login_screen_button_login_label.uppercase())
                    }

                if (currentOrientation() == Orientation.PORTRAIT)
                    Spacer(modifier = Modifier.height(Sizes.screenPadding * 10))
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserNameField(userName: MutableState<String>) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = userName.value,
            label = {
                Text(Strings.login_screen_text_field_username_label)
            },
            onValueChange = { userName.value = it.replace("\n", "") },
            maxLines = 1
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
                Text(Strings.login_screen_text_field_password_label)
            },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password.value = it.replace("\n", "") },
            isError = currentState is LoginViewState.Error,
            maxLines = 1
        )
    }

    override val isToolbarEnable: Boolean = false
}