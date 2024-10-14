package io.github.zidbrain.fchat.mvi

import kotlin.coroutines.cancellation.CancellationException

@Suppress("UNCHECKED_CAST")
class MVIActionBuilder<State : Any, Event> internal constructor(
    private val viewModel: MVIViewModel<*, State, Event>,
    private val log: (tag: String, msg: String, throwable: Exception) -> Unit,
    internal val block: suspend MVIActionHandler<State, Event>.() -> Unit
) {

    private inline fun copy(crossinline block: suspend MVIActionHandler<State, Event>.() -> Unit) =
        MVIActionBuilder(viewModel, log) {
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
        handle: suspend MVIActionHandler<State, Event>.(Exception) -> Unit
    ): MVIActionBuilder<State, Event> = copy {
        try {
            this.block()
        } catch (t: Exception) {
            if (t !is CancellationException) {
                log(
                    viewModel::class.simpleName ?: "ViewModel",
                    "An error occurred when executing action $actionDebug",
                    t
                )
                handle(t)
            }
            else throw t
        }
    }

    fun retry(
        retries: Int = Int.MAX_VALUE,
        shouldRetry: suspend MVIActionHandler<State, Event>.(Exception) -> Boolean
    ) = copy {
        do {
            var count = 0
            val retry = try {
                this.block()
                false
            } catch (t: Exception) {
                if (t is CancellationException)
                    throw t
                else {
                    log(
                        viewModel::class.simpleName ?: "ViewModel",
                        "An error occurred when executing action $actionDebug",
                        t
                    )
                    count++
                    shouldRetry(t)
                }
            }
        } while (retry && count < retries)
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