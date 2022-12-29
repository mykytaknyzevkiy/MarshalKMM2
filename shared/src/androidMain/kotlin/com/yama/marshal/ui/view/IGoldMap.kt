package com.yama.marshal.ui.view

import android.graphics.Color
import android.location.Location
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import co.touchlab.kermit.Logger
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DRendererBase
import com.l1inc.viewer.Course3DViewer
import com.yama.marshal.tool.onEachList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
internal actual fun IGoldMap(
    modifier: Modifier,
    renderData: RenderData,
    hole: Flow<Int>,
    carts: Flow<List<Cart>>
) {
    val context = LocalContext.current

    val mapView = remember(renderData) {
        Course3DViewer(context)
            .apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                this.setIsCalloutOverlay(false)
            }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            FrameLayout(context).apply {
                addView(mapView)
            }
        }
    )

    LaunchedEffect(context) {
        mapView.onOrientationChanged()

        mapView.init(
            hashMapOf(
                //CourseGPSVectorDetailsRequest:vectorGPSObject
                Pair(renderData.idCourse, renderData.vectors),
                Pair(Course3DRenderer.COURSE_ID, renderData.idCourse)
            ),
            false,
            false,
            null,
            null,
            false
        )

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

        carts
            .onEach {
                Logger.i("IGoldMap", message = {
                    "Update carts"
                })
            }
            .onEachList {
                mapView.updateCart(
                    it.id,
                    it.name,
                    Location("").apply {
                        latitude = it.location.first
                        longitude = it.location.second
                    }
                )
            }
            .launchIn(this)
    }
}