package com.yama.marshal.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.round
import com.yama.marshal.currentRootView
import com.yama.marshal.tool.igolfMapNativeRenderView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*


@Composable
internal actual fun IGoldMap(
    modifier: Modifier,
    renderData: RenderData,
    hole: Flow<Int>,
    carts: Flow<List<Cart>>
) {

    val density = LocalDensity.current.density

    Layout(
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero)
            val size = coordinates.size

            igolfMapNativeRenderView.renderNUIViewController().view.setBounds(
                CGRectMake(
                    (location.x / density).toDouble(),
                    (location.y / density).toDouble(),
                    (size.width / density).toDouble(),
                    (size.height / density).toDouble()
                )
            )
        },
        content = {},
        measurePolicy = { _, _ -> layout(0, 0) {} }
    )

    DisposableEffect(igolfMapNativeRenderView) {
        currentRootView.addChildViewController(igolfMapNativeRenderView.renderNUIViewController())
        currentRootView.view.addSubview(igolfMapNativeRenderView.renderNUIViewController().view)
        igolfMapNativeRenderView.renderNUIViewController().didMoveToParentViewController(currentRootView)

        igolfMapNativeRenderView.setVectors(renderData.vectors)
        igolfMapNativeRenderView.setHole(1)

        onDispose {
            //currentRootView.view.willRemoveSubview(renderView)
        }
    }

    val scope = rememberCoroutineScope()

    scope.launch(Dispatchers.Default) {
    }
}
