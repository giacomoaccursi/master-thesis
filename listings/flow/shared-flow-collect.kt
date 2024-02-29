override suspend fun collect(collector: FlowCollector<T>): Nothing {
    ...
    try {
        ...
        while (true) {
            var newValue: Any?
            while (true) {
                newValue = tryTakeValue(slot) 
                if (newValue !== NO_VALUE) break
                ...
            }
            ...
        }
    } finally {
        ...
    }
}