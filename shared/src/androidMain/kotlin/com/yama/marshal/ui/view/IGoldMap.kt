package com.yama.marshal.ui.view

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.l1inc.viewer.Course3DRenderer
import com.l1inc.viewer.Course3DViewer

@Composable
internal actual fun IGoldMap(modifier: Modifier, renderData: RenderData) {
    val context = LocalContext.current

    val view = remember {
        Course3DViewer(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    AndroidView(modifier = modifier, factory = {
        view
    })

    LaunchedEffect(1) {
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