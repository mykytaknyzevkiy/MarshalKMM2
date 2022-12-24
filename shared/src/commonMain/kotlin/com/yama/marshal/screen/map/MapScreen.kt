package com.yama.marshal.screen.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.theme.Sizes
import com.yama.marshal.ui.view.IGoldMap
import com.yama.marshal.ui.view.RenderData
import com.yama.marshal.ui.view.YamaScreen
import kotlinx.coroutines.flow.map

internal class MapScreen(navigationController: NavigationController) : YamaScreen(navigationController) {
    companion object {
        const val ARG_HOLE_ID = "hole_id"
        const val ARG_CART_ID = "cart_id"
        const val ARG_COURSE_ID = "course_id"

        const val route = "map"
    }

    override val route: String = MapScreen.route

    override val viewModel: MapViewModel = MapViewModel()

    @Composable
    override fun titleContent() {
        val course by remember(viewModel) {
            viewModel.courseState
        }.collectAsState()
        val hole by remember(viewModel) {
            viewModel.holeState
        }.collectAsState()

        Row {
            if (course != null)
                Text(
                    modifier = Modifier.padding(horizontal = Sizes.screenPadding),
                    text = course!!.courseName.uppercase(),
                    fontSize = Sizes.title,
                    textAlign = TextAlign.Center
                )

            if (hole != null)
                Text(
                    modifier = Modifier.padding(horizontal = Sizes.screenPadding),
                    text = "Hole: ${hole?.holeNumber}",
                    fontSize = Sizes.title,
                    textAlign = TextAlign.Center
                )
        }
    }

    @Composable
    override fun content(args: List<NavArg>) {
        val holeID = args.findInt(ARG_HOLE_ID)
        val courseID = args.findString(ARG_COURSE_ID) ?: return
        val cartID = args.findInt(ARG_CART_ID)

        val course by remember(viewModel) {
            viewModel.courseState
        }.collectAsState()

        if (course == null)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        else
            IGoldMap(
                modifier = Modifier.fillMaxSize(),
                renderData = RenderData(
                    idCourse = courseID,
                    vectors = course!!.vectors
                ),
                hole = viewModel
                    .holeState
                    .map { hole ->
                        hole?.holeNumber ?: -1
                    }
            )

        LaunchedEffect(viewModel) {
            viewModel.loadCourse(courseID)

            if (holeID != null)
                viewModel.loadHole(holeID, courseID)
            else if (cartID != null)
                viewModel.loadCart(cartID)
        }
    }

    override val isToolbarEnable: Boolean = true
}