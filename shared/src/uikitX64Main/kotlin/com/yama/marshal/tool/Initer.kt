package com.yama.marshal.tool

import platform.Foundation.NSBundle

internal lateinit var mainBundle: NSBundle

fun initMe(bundle: NSBundle) {
    mainBundle = bundle
}