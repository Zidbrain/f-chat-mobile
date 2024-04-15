package io.github.zidbrain.fchat.common.util

fun <T> List<T>.replaceAt(index: Int, with: (T) -> T): List<T> = mapIndexed { i, it ->
    if (i == index) with(it)
    else it
}