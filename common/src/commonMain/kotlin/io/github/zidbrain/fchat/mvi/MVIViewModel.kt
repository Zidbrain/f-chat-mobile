package io.github.zidbrain.fchat.mvi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

abstract class MVIViewModel<in Action : Any, out State : Any, Event>(initialState: State) : ViewModel() {

    private var init = false

    private val _state = MutableStateFlow(initialState)
    val state by lazy {
        getStateUpdateJob(onInit())
        _state.asStateFlow()
    }

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val flowHolders = mutableMapOf<KClass<out Action>, Job>()

    private fun getStateUpdateJob(updateWith: Flow<State>) =
        viewModelScope.launch {
            _state.emitAll(updateWith)
        }

    fun sendAction(action: Action) {
        flowHolders[action::class]?.cancel()
        flowHolders[action::class] = getStateUpdateJob(handleAction(action))
    }

    protected fun sendEvent(event: Event) = viewModelScope.launch {
        _events.emit(event)
    }

    protected abstract fun handleAction(action: Action): Flow<State>
    protected open fun onInit(): Flow<State> = emptyFlow()

    protected inline fun Flow<@UnsafeVariance State>.errorState(crossinline getErrorState: (Throwable) -> @UnsafeVariance State) = catch {
        if (it !is CancellationException) emit(getErrorState(it))
    }
    protected inline fun Flow<@UnsafeVariance State>.errorEvent(crossinline getErrorEvent: (Throwable) -> Event) = catch {
        if (it !is CancellationException) sendEvent(getErrorEvent(it))
    }

    open class Actionless<out State : Any>(initialState: State) :
        MVIViewModel<Nothing, State, Nothing>(initialState) {
        override fun handleAction(action: Nothing) = emptyFlow<State>()
    }
}