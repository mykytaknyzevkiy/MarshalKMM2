package com.yama.marshal.ui.view

import platform.Foundation.NSDictionary
import platform.UIKit.UIViewController

interface IgolfMapNativeRenderView {
    fun renderNUIViewController(): UIViewController

    fun setVectors(json: String)

    fun setHole(hole: Int)
}