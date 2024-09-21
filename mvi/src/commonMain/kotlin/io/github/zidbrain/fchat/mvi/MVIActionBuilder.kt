package io.github.zidbrain.fchat.mvi

import io.github.zidbrain.fchat.logError
import kotlin.coroutines.cancellation.CancellationException

@Suppress("UNCHECKED_CAST")
class MVIActionBuilder<State : Any, Event> internal constructor(
    private val viewModel: MVIViewModel<*, State, Event>,
    internal val block: suspend MVIActionHandler<State, Event>.() -> Unit
) {

    private inline fun copy(crossinline block: suspend MVIActionHandler<State, Event>.() -> Unit) =
        MVIActionBuilder(viewModel) {
            block()
        }.also {
            it.cancelable = cancelable
        }

    operator fun plus(other: MVIActionBuilder<out State, Event>): MVIActionBuilder<State, Event> =
        copy {
            block()
            (other.block as suspend MVIActionHandler<out State, Event>.() -> Unit)()
        }

    fun catch(
        log: (tag: String, msg: String, throwable: Throwable) -> Unit = ::logError,
        handle: suspend MVIActionHandler<State, Event>.(Throwable) -> Unit
    ): MVIActionBuilder<State, Event> = copy {
        try {
            this.block()
        } catch (t: Throwable) {
            if (t !is CancellationException) {
                log(
                    viewModel::class.simpleName ?: "ViewModel",
                    "An error occurred when executing action $actionDebug",
                    t
                )
                handle(t)
            }
        }
    }

    fun onErrorSet(state: (Throwable) -> State): MVIActionBuilder<State, Event> =
        catch { setState(state(it)) }

    fun onErrorRaise(event: (Throwable) -> Event): MVIActionBuilder<State, Event> =
        catch { raiseEvent(event(it)) }

    var cancelable = true
        private set

    fun cancelable(isCancelable: Boolean): MVIActionBuilder<State, Event> = copy(block).apply {
        cancelable = isCancelable
    }
}