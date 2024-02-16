override suspend fun collect(collector: FlowCollector<T>): Nothing {
    val slot = allocateSlot()
    try {
        if (collector is SubscribedFlowCollector) collector.onSubscription()
        val collectorJob = currentCoroutineContext()[Job]
        var oldState: Any? = null
        while (true) {
            val newState = _state.value
            collectorJob?.ensureActive()
            if (oldState == null || oldState != newState) {
                collector.emit(NULL.unbox(newState))
                oldState = newState
            }
            if (!slot.takePending()) { 
                slot.awaitPending()
            }
        }
    } finally {
        freeSlot(slot)
    }
}