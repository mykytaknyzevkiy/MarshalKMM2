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
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.tool.Orientation
import com.yama.marshal.ui.tool.currentOrientation
import com.yama.marshal.ui.view.YamaScreen

internal class LoginScreen(navigationController: NavigationController) :
    YamaScreen(navigationController) {
    override val route: String = "login"

    override val viewModel: YamaViewModel = LoginViewModel()

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

                UserNameField()

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                PasswordField()

                Spacer(modifier = Modifier.height(Sizes.screenPadding))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {}, shape = RoundedCornerShape(0.dp)
                ) {
                    Text("Log in".uppercase())
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UserNameField() {
        var userName by remember {
            mutableStateOf("")
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
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