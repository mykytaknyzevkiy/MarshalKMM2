package com.yama.marshal

import android.content.Context

object AndroidBuilder {

    internal lateinit var applicationContext: Context

    fun build(context: Context) {
        this.applicationContext = context
    }

}