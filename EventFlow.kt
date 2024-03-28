package uz.uzum.tezkor.courier.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

interface EventFlow<T> : Flow<UiEvent<T>>

class MutableEventFlow<T> :
    MutableSharedFlow<UiEvent<T>>,
    EventFlow<T> {

    val impl = MutableSharedFlow<UiEvent<T>>(replay = 1)

    override val subscriptionCount: StateFlow<Int>
        get() = impl.subscriptionCount

    override suspend fun emit(value: UiEvent<T>) {
        impl.emit(value)
    }

    @JvmName("emitEvent")
    suspend fun emit(data: T) {
        impl.emit(UiEvent(data))
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        impl.resetReplayCache()
    }

    override fun tryEmit(value: UiEvent<T>): Boolean {
        return impl.tryEmit(value)
    }

    override val replayCache: List<UiEvent<T>>
        get() = impl.replayCache

    override suspend fun collect(collector: FlowCollector<UiEvent<T>>): Nothing {
        impl.collect(collector)
    }
}

fun <T> wrapAsEventFlow(flow: Flow<UiEvent<T>>): EventFlow<T> = object : EventFlow<T> {
    override suspend fun collect(collector: FlowCollector<UiEvent<T>>) {
        flow.collect(collector)
    }
}

@JvmName("wrapAsEventFlowExtension")
fun <T> Flow<UiEvent<T>>.wrapAsEventFlow(): EventFlow<T> = wrapAsEventFlow(this)
