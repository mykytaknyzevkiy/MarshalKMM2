package com.yama.marshal.screen.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.tool.painterResource
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.YamaScreen

internal class LoginScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    override val route: String = "login"

    override val viewModel: YamaViewModel = LoginViewModel()

    @Composable
    override fun title(): String = ""

    @Composable
    override fun content(args: List<NavArg>) {
        Column(modifier = Modifier.fillMaxSize().padding(Sizes.screenPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource("img_app_logo.png"),
                contentDescription = null
            )

            Spacer(modifier = Modifier.height(Sizes.screenPadding * 2))

            UserNameField()

            Spacer(modifier = Modifier.height(Sizes.screenPadding))

            PasswordField()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserNameField() {
        var userName by remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            value = userName,
            label = {
                Text("Login")
            },
            onValueChange = { userName = it }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun PasswordField() {
        var password by remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            value = password,
            label = {
                Text("Password")
            },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password = it }
        )
    }

    override val isToolbarEnable: Boolean = false
}