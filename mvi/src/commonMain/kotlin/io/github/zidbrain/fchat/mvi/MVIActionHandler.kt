package io.github.zidbrain.fchat.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update

@Suppress("UNCHECKED_CAST")
class MVIActionHandler<State : Any, Event> internal constructor(
    private val viewModel: MVIViewModel<*, State, Event>,
    internal val actionDebug: Any?
) {
    fun setState(update: (State).() -> State) {
        viewModel.mState.update { it.update() }
    }

    fun setState(state: State) {
        viewModel.mState.update { state }
    }

    val state: State
        get() = viewModel.state.value

    inline fun <T : State> setState(state: T, block: MVIActionHandler<T, Event>.() -> Unit) {
        setState(state)
        (this as MVIActionHandler<T, Event>).block()
    }

    inline fun <T : State> requireState(block: MVIActionHandler<T, Event>.() -> Unit) {
        (this as MVIActionHandler<T, Event>).block()
    }

    suspend fun setStateBy(action: MVIActionBuilder<out State, Event>) {
        (action as MVIActionBuilder<State, Event>).block(this)
    }

    suspend fun setStateBy(flow: Flow<State>) {
        flow.collect {
            setState(it)
        }
    }

    suspend fun raiseEvent(event: Event) {
        viewModel.mEvents.emit(event)
    }
}