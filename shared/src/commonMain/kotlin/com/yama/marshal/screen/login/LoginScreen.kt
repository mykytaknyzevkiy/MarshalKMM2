package com.yama.marshal.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.yama.marshal.LocalAppDimens
import com.yama.marshal.tool.Strings
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Dimensions
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
        val orientation = currentOrientation()

        val keyboardController = LocalSoftwareKeyboardController.current

        Column(
            modifier = Modifier
                .clickable { keyboardController?.hide() }
                .fillMaxSize()
                .padding(Sizes.screenPadding),
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
                    ),
                contentScale = ContentScale.FillWidth,
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(Sizes.screenPadding * 2))

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
                    onClick = {
                        viewModel.login(userName = userName.value, password = password.value)
                    },
                    enabled = userName.value.isNotBlank() && password.value.isNotBlank(),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(Strings.login_screen_button_login_label.uppercase())
                }

            if (orientation == Orientation.PORTRAIT)
                Spacer(modifier = Modifier.height(Sizes.screenPadding * 5))


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
        val keyboardController = LocalSoftwareKeyboardController.current

        val currentState by remember {
            viewModel.currentState
        }.collectAsState()

        OutlinedTextField(
            value = userName.value,
            enabled = currentState !is LoginViewState.Loading,
            label = {
                Text(Strings.login_screen_text_field_username_label)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            onValueChange = { userName.value = it.replace("\n", "").replace(" ", "") },
            maxLines = 1,
            singleLine = true
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PasswordField(password: MutableState<String>) {
        val keyboardController = LocalSoftwareKeyboardController.current

        val currentState by remember {
            viewModel.currentState
        }.collectAsState()

        OutlinedTextField(
            value = password.value,
            enabled = currentState !is LoginViewState.Loading,
            label = {
                Text(Strings.login_screen_text_field_password_label)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }),
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password.value = it.replace("\n", "") },
            isError = currentState is LoginViewState.Error,
            maxLines = 1,
            singleLine = true
        )
    }

    override val isToolbarEnable: Boolean = false
}