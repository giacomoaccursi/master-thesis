private fun observeNeighborEvents() {
    coroutineScope.launch {
        environment.neighborhoods.run {
            this.onSubscription {
                initLatch.countDown()
            }.mapLatest {
                it[node.id]?.neighbors.orEmpty()
                    .flatMap { node ->
                        node.events.value
                    }
            }.collect { events ->
                val removed = observedNeighborEvents.keys - events.toSet()
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
                                updateEvent(event.tau)
                                this.notifyConsumed()
                            }
                        }
                    }
                    observedNeighborEvents[event] = job
                }
                this.notifyConsumed()
            }
        }
    }
}