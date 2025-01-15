package io.github.zidbrain.fchat

import android.util.Log

actual fun log(level: LogLevel, tag: String, msg: String, throwable: Throwable?) {
    when (level) {
        LogLevel.Info -> Log.i(tag, msg, throwable)
        LogLevel.Error -> Log.e(tag, msg, throwable)
    }
}