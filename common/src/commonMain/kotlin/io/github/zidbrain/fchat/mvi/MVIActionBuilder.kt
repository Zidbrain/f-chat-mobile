package io.github.zidbrain.fchat.mvi

import kotlin.coroutines.cancellation.CancellationException

@Suppress("UNCHECKED_CAST")
class MVIActionBuilder<State : Any, Event> internal constructor(
    private val viewModel: MVIViewModel<*, State, Event>,
    internal val block: suspend MVIActionHandler<State, Event>.() -> Unit
) {

    operator fun plus(other: MVIActionBuilder<out State, Event>): MVIActionBuilder<State, Event> =
        MVIActionBuilder(viewModel) {
            block()
            (other.block as suspend MVIActionHandler<out State, Event>.() -> Unit)()
        }

    fun catch(handle: suspend MVIActionHandler<State, Event>.(Throwable) -> Unit): MVIActionBuilder<State, Event> =
        MVIActionBuilder(viewModel) {
            try {
                this.block()
            } catch (t: Throwable) {
                if (t !is CancellationException)
                    handle(t)
            }
        }

    fun onErrorSet(state: (Throwable) -> State): MVIActionBuilder<State, Event> =
        catch { setState(state(it)) }

    fun onErrorRaise(event: (Throwable) -> Event): MVIActionBuilder<State, Event> =
        catch { raiseEvent(event(it)) }

    var cancelable = true
        private set

    fun cancelable(isCancelable: Boolean): MVIActionBuilder<State, Event> = MVIActionBuilder(viewModel, block).apply {
        cancelable = isCancelable
    }
}