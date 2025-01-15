package io.github.zidbrain.fchat

enum class LogLevel {
    Info, Error
}

expect fun log(level: LogLevel, tag: String, msg: String, throwable: Throwable? = null)