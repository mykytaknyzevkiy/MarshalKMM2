package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import com.yama.marshal.currentRootView
import kotlinx.coroutines.flow.Flow
import platform.CoreGraphics.CGRectMake
import platform.UIKit.setBounds
import igolf.render.CourseRenderView
import platform.UIKit.addSubview
import platform.UIKit.didMoveToSuperview
import platform.UIKit.willRemoveSubview

@Composable
internal actual fun IGoldMap(modifier: Modifier,
                             renderData: RenderData,
                             hole: Flow<Int>,
                             carts: Flow<List<Cart>>) {
    val renderView = CourseRenderView()

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
                  JSONSE
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
