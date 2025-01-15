package io.github.zidbrain.fchat

fun logError(tag: String, msg: String, throwable: Throwable) = log(LogLevel.Error, tag, msg, throwable)