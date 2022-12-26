package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootView
import kotlinx.coroutines.flow.Flow
import platform.CoreGraphics.CGRectMake
import platform.UIKit.setBounds
import igolf.render.CourseRenderView
import igolf.render.CourseRenderViewDelegateProtocol
import platform.Foundation.*
import platform.UIKit.addSubview
import platform.UIKit.didMoveToSuperview
import platform.UIKit.willRemoveSubview
import platform.darwin.NSObject


@Composable
internal actual fun IGoldMap(
    modifier: Modifier,
    renderData: RenderData,
    hole: Flow<Int>,
    carts: Flow<List<Cart>>
) {
    val renderView = remember {
        CourseRenderView().apply {
            setDelegate(object : CourseRenderViewDelegateProtocol, NSObject() {
                override fun courseRenderViewDidLoadHoleData() {
                    Logger.d("NEKAAA", message = {
                        "courseRenderViewDidLoadHoleData"
                    })
                }
            })
        }
    }


    val density = LocalDensity.current.density

    Layout(
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero).round()
            val size = coordinates.size

            renderView.setBounds(
                CGRectMake(
                    (location.x / density).toDouble(),
                    (location.y / density).toDouble(),
                    (size.width / density).toDouble(),
                    (size.height / density).toDouble()
                )
            )
        },
        content = {
            NSString
                .create(string = renderData.vectors)
                .dataUsingEncoding(1)!!.let {
                    NSJSONSerialization.JSONObjectWithData(data = it, options = 0, error = null)
                }!!.let {
                    it as Map<Any?, *>
                }.also {
                    renderView.viewCartWithGpsVectorData(it)
                    renderView.setCurrentHole(1)
                }


        },
        measurePolicy = { _, _ -> layout(0, 0) {} }
    )

    DisposableEffect(renderView) {
        currentRootView.view.addSubview(renderView)
        renderView.didMoveToSuperview()

        onDispose {
            currentRootView.view.willRemoveSubview(renderView)
        }
    }
}

/*
private class IGoldMapViewController: UIViewController(null, null) {
    private val renderView = CourseRenderView()

    override fun viewDidLoad() {
        super.viewDidLoad()
        setView(renderView)
    }
}*/
