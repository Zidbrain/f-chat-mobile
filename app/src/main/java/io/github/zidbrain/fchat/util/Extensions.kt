package io.github.zidbrain.fchat.util

inline fun <reified T> Any.takeIfType(block: (T) -> Boolean): Boolean =
    this is T && block(this)