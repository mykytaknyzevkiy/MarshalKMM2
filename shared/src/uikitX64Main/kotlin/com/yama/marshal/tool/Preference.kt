package com.yama.marshal.tool

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual val prefs: Settings
    get() {
        val delegate = NSUserDefaults.standardUserDefaults()
        return NSUserDefaultsSettings(delegate)
    }
