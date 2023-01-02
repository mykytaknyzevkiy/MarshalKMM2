package com.yama.marshal_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.yama.marshal.AndroidView
import com.yama.marshal.AppDelegate


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidView()
        }
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("AppDelegate.onBackPresse()", "com.yama.marshal.AppDelegate")
    )
    override fun onBackPressed() {
        //super.onBackPressed()
        AppDelegate.onBackPresse()
    }
}