package com.yama.marshal.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.screen.YamaViewModel
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.theme.Sizes

internal abstract class YamaScreen(protected val navigationController: NavigationController) {
    abstract val route: String

    @Composable
    open fun toolbarColor() = MaterialTheme.colorScheme.primary

    @Composable
    open fun title(): String = ""

    @Composable
    open fun titleContent() {
        Text(
            text = title().uppercase(),
            fontSize = Sizes.title,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    open fun actions() {

    }

    abstract val viewModel: YamaViewModel

    open val isToolbarEnable: Boolean = false

    @Composable
    protected abstract fun content(args: List<NavArg>)

    @Composable
    fun contentReal(args: List<NavArg>) {
        content(args)

        DisposableEffect(viewModel) {
            onDispose {
                viewModel.onClear()
            }
        }
    }

    @Composable
    open fun bottomBar() {

    }
}