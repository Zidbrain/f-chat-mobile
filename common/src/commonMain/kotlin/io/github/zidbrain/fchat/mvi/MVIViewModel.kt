package io.github.zidbrain.fchat.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

abstract class MVIViewModel<in Action : Any, State : Any, Event>(initialState: State) :
    ViewModel() {

    private var init = false

    internal val mState = MutableStateFlow(initialState)
    val state by lazy {
        initAction?.let { getStateUpdateJob(it) }
        mState.asStateFlow()
    }

    internal val mEvents = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = mEvents.asSharedFlow()

    private val flowHolders = mutableMapOf<KClass<out Action>, MutableJobHolder>()

    private fun getStateUpdateJob(action: MVIActionBuilder<State, Event>): Job {
        val handler = MVIActionHandler(this)
        return viewModelScope.launch {
            action.block(handler)
        }
    }

    fun sendAction(action: Action) {
        val builder = handleAction(action)
        val holder = flowHolders.getOrPut(action::class) { MutableJobHolder() }
        if (builder.cancelable)
            holder.cancel()
        holder += getStateUpdateJob(handleAction(action))
    }

    fun cancelAction(action: KClass<out Action>) {
        flowHolders[action]?.cancel()
    }

    protected fun buildAction(block: suspend MVIActionHandler<State, Event>.() -> Unit = {}): MVIActionBuilder<State, Event> =
        MVIActionBuilder(this, block)

    protected abstract fun handleAction(action: Action): MVIActionBuilder<State, Event>
    protected open val initAction: MVIActionBuilder<State, Event>? = null

    open class Actionless<State : Any>(initialState: State) :
        MVIViewModel<Nothing, State, Nothing>(initialState) {
        override fun handleAction(action: Nothing) = buildAction()
    }
}