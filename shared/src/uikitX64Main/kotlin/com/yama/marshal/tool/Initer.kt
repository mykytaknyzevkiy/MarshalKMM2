package com.yama.marshal.tool

import com.yama.marshal.ui.view.IgolfMapNativeRenderView
import platform.Foundation.NSBundle

internal lateinit var mainBundle: NSBundle
internal lateinit var igolfMapNativeRenderView: IgolfMapNativeRenderView

fun initMe(bundle: NSBundle) {
    mainBundle = bundle
}

fun setCourseRenderView(igolfMapNativeRenderViewN: IgolfMapNativeRenderView) {
    igolfMapNativeRenderView = igolfMapNativeRenderViewN
}