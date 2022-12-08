package com.yama.marshal_app

import android.app.Application
import com.yama.marshal.AndroidBuilder

class MarshalApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AndroidBuilder.build(applicationContext)
    }

}