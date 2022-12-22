package com.yama.marshal.screen.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.yama.marshal.ui.navigation.NavArg
import com.yama.marshal.ui.navigation.NavigationController
import com.yama.marshal.ui.navigation.findInt
import com.yama.marshal.ui.navigation.findString
import com.yama.marshal.ui.view.IGoldMap
import com.yama.marshal.ui.view.RenderData
import com.yama.marshal.ui.view.YamaScreen

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
    override fun content(args: List<NavArg>) {
        val holeID = args.findInt(ARG_HOLE_ID)
        val courseID = args.findString(ARG_COURSE_ID) ?: return
        val cartID = args.findInt(ARG_CART_ID)

        LaunchedEffect(viewModel) {
            viewModel.loadCourse(courseID)

            if (holeID != null)
                viewModel.loadHole(holeID, courseID)
        }

        val course by remember(viewModel) {
            viewModel.courseState
        }.collectAsState()

        if (course == null) {
            CircularProgressIndicator()
            return
        }

        IGoldMap(
            modifier = Modifier.fillMaxSize(),
            renderData = RenderData(
                idCourse = courseID,
                vectors = course!!.vectors
            )
        )
    }

    override val isToolbarEnable: Boolean = true
}