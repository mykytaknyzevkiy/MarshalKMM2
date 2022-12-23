package com.yama.marshal.ui.view

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import co.touchlab.kermit.Logger
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DViewer
import kotlinx.coroutines.flow.*

@Composable
internal actual fun IGoldMap(
    modifier: Modifier,
    renderData: RenderData,
    hole: Flow<Int>
) {
    val context = LocalContext.current

    val mapView = remember(renderData) {
        Course3DViewer(context)
            .apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setIsCalloutOverlay(false)
        }
            .also { view ->
                view.init(
                    hashMapOf(
                        Pair(renderData.idCourse, renderData.vectors),
                        Pair(Course3DRenderer.COURSE_ID, renderData.idCourse)
                    ),
                    false,
                    true,
                    null,
                    null,
                    false
                )
            }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView }
    )

    LaunchedEffect(context) {
        hole
            .filter {
                it >= 0
            }
            .onEach {
                Logger.i("IGoldMap", message = {
                    "Move to hole $it"
                })
                mapView.setCurrentHole(
                    it,
                    Course3DRendererBase.NavigationMode.NavigationMode2D,
                    true,
                    0
                )
            }
            .launchIn(this)
    }
}