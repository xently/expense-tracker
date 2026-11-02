package co.ke.xently.expense.tracker.server.sse

import co.ke.xently.expense.tracker.model.ServerEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class EventBroadcaster {
    private val _events = MutableSharedFlow<ServerEvent>(
        replay = 0,
        extraBufferCapacity = 128
    )
    val events: SharedFlow<ServerEvent> = _events.asSharedFlow()

    fun broadcast(event: ServerEvent) {
        _events.tryEmit(event)
    }
}
