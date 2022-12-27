package com.yama.marshal.ui.view

import platform.UIKit.UIViewController

interface IgolfMapNativeRenderView {
    fun renderNUIViewController(): UIViewController

    fun setVectors(json: String)

    fun setHole(hole: Int)

    fun addCart(id: Int, name: String, lat: Double, lng: Double)

    fun removeCart(id: Int)
}