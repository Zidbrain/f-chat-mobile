package io.github.zidbrain.fchat.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.zidbrain.fchat.logError
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

    private var viewModelSavedStateProvider: ViewModelSavedStateProvider<State>? = null

    constructor(savedDataProvider: ViewModelSavedStateProvider<State>) : this(savedDataProvider.getSavedStateOrDefault()) {
        viewModelSavedStateProvider = savedDataProvider
    }

    override fun onCleared() {
        viewModelSavedStateProvider?.saveState(mState.value)
    }

    internal val mState = MutableStateFlow(initialState)
    val state by lazy {
        sendInitAction()
        mState.asStateFlow()
    }

    internal val mEvents = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = mEvents.asSharedFlow()

    private val flowHolders = mutableMapOf<KClass<out Action>, MutableJobHolder>()
    private val initHolder = MutableJobHolder()

    private fun getStateUpdateJob(builder: MVIActionBuilder<State, Event>, action: Any?): Job {
        val handler = MVIActionHandler(this, action)
        return viewModelScope.launch {
            builder.block(handler)
        }
    }

    fun sendAction(action: Action) {
        val builder = handleAction(action)
        val holder = flowHolders.getOrPut(action::class) { MutableJobHolder() }
        if (builder.cancelable)
            holder.cancel()
        holder += getStateUpdateJob(builder, action)
    }

    fun cancelAction(action: KClass<out Action>) {
        flowHolders[action]?.cancel()
    }

    protected fun buildAction(
        cancellable: Boolean = true,
        logger: (tag: String, msg: String, throwable: Exception) -> Unit = ::logError,
        block: suspend MVIActionHandler<State, Event>.() -> Unit
    ): MVIActionBuilder<State, Event> =
        MVIActionBuilder(this, logger, block).also { it.cancelable(cancellable) }

    protected abstract fun handleAction(action: Action): MVIActionBuilder<State, Event>
    protected open val initAction: MVIActionBuilder<State, Event>? = null

    protected fun sendInitAction() {
        initAction?.let {
            if (it.cancelable)
                initHolder.cancel()
            initHolder += getStateUpdateJob(it, "Init Action")
        }
    }

    open class Actionless<State : Any>(initialState: State) :
        MVIViewModel<Nothing, State, Nothing>(initialState) {
        override fun handleAction(action: Nothing) = buildAction { }
    }
}