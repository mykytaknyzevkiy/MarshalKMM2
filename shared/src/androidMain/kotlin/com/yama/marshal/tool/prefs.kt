package com.yama.marshal.tool

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.yama.marshal.AndroidBuilder

internal actual val prefs: Settings = SharedPreferencesSettings(
    AndroidBuilder.applicationContext.getSharedPreferences("yama_marshal", Context.MODE_PRIVATE)
)
