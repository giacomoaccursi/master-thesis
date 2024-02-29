override suspend fun collect(collector: FlowCollector<T>): Nothing {
    ...
    try {
        ...
        while (true) {
            val newState = _state.value
            ...
            if (oldState == null || oldState != newState) {
                collector.emit(NULL.unbox(newState))
                oldState = newState
            }
            ...
        }
    } finally {
       ...
    }
}