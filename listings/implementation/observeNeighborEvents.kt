private fun observeNeighborEvents() {
    coroutineScope.launch {
        environment.neighborhoods.run {
            this.onSubscription {
                initLatch.countDown()
            // The updated neighborhood is mapped to a set of events within it.
            }.mapLatest {
                it[node.id]?.neighbors.orEmpty()
                    .flatMap { node ->
                        node.events.value
                    }
            }.collect { events ->
                // Events that no longer belong to the neighborhood are identified.
                val removed = observedNeighborEvents.keys - events.toSet()
                // Events that are now part of the neighborhood are identified.
                val added = events.toSet() - observedNeighborEvents.keys
                removed.forEach { event ->
                    observedNeighborEvents[event]?.cancelAndJoin()
                    observedNeighborEvents.remove(event)
                }
                added.forEach { event ->
                    val job = launch {
                        val executionFlow = event.observeExecution()
                        executionFlow.run {
                            this.collect { event ->
                                // Following the execution of an event on which it is dependent, it need to update its execution time.
                                updateEvent(event.tau)
                                // Notification to the flow of actual consumption.
                                this.notifyConsumed()
                            }
                        }
                    }
                    observedNeighborEvents[event] = job
                }
                // Notification to the flow of actual consumption.
                this.notifyConsumed()
            }
        }
    }
}