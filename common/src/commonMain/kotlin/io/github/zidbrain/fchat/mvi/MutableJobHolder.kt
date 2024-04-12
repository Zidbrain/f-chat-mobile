package io.github.zidbrain.fchat.mvi

import kotlinx.coroutines.Job

internal class MutableJobHolder(list: MutableList<Job> = mutableListOf()) : MutableList<Job> by list {

    fun cancel() {
        forEach { it.cancel() }
        clear()
    }
}