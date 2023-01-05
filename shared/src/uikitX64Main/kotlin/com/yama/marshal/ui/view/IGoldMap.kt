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
import co.touchlab.kermit.Logger
import com.yama.marshal.currentRootViewController
import com.yama.marshal.tool.igolfMapNativeRenderView
import com.yama.marshal.tool.onEachList
import com.yama.marshal.ui.theme.Sizes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import platform.CoreGraphics.CGRectMake
import platform.UIKit.*

@Composable
internal actual fun IGoldMap(
    modifier: Modifier,
    renderData: RenderData,
    hole: Flow<Int>,
    carts: Flow<List<Cart>>
) {
    val cartsIDs = remember(renderData) {
        arrayListOf<Int>()
    }

    val density = LocalDensity.current.density

    val screenPadding = Sizes.screenPadding.value

    Layout(
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val coordinates = childCoordinates.parentCoordinates!!
            val location = coordinates.localToWindow(Offset.Zero)
            val size = coordinates.size

            igolfMapNativeRenderView.renderNUIViewController().view.setFrame(
                CGRectMake(
                    (location.x / density).toDouble(),
                    (location.y / density).toDouble(),
                    (size.width / density).toDouble(),
                    (size.height / density - (screenPadding * 3)).toDouble()
                )
            )
        },
        content = {},
        measurePolicy = { _, _ ->
            layout(0, 0) {}
        }
    )

    DisposableEffect(igolfMapNativeRenderView) {
        currentRootViewController.addChildViewController(igolfMapNativeRenderView.renderNUIViewController())
        currentRootViewController.view.addSubview(igolfMapNativeRenderView.renderNUIViewController().view)
        igolfMapNativeRenderView.renderNUIViewController().didMoveToParentViewController(currentRootViewController)

        igolfMapNativeRenderView.setVectors(renderData.vectors)

        onDispose {
            cartsIDs.forEach {
                igolfMapNativeRenderView.removeCart(it)
            }

            igolfMapNativeRenderView.renderNUIViewController().removeFromParentViewController()
            igolfMapNativeRenderView.renderNUIViewController().view.removeFromSuperview()
        }
    }

    LaunchedEffect(renderData) {
        hole
            .filter {
                it >= 0
            }
            .onEach {
                Logger.i("IGoldMap", message = {
                    "Move to hole $it"
                })
                igolfMapNativeRenderView.setHole(
                    it
                )
            }
            .launchIn(this)

        carts
            .onEach {
                Logger.i("IGoldMap", message = {
                    "Update carts"
                })

                cartsIDs.forEach { id ->
                    if (!it.any { c -> c.id == id })
                        igolfMapNativeRenderView.removeCart(id)
                }

                cartsIDs.clear()
                cartsIDs.addAll(it.map { c -> c.id })
            }
            .onEachList {
                igolfMapNativeRenderView.addCart(
                    id = it.id,
                    name = it.name,
                    lat = it.location.first,
                    lng = it.location.second
                )
            }
            .launchIn(this)
    }
}
