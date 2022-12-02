package com.yama.marshal

import android.app.Application

class MarshalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidBuilder.build(applicationContext)
    }

}