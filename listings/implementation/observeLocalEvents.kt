private fun observeLocalEvents() {
    coroutineScope.launch {
        node.events.run {
            this.onSubscription { initLatch.countDown() }.collect {
                // Events that no longer belong to the neighborhood are identified.
                val removed = observedLocalEvents.keys - it.toSet() - setOf(this@EventImpl)
                // Events that are now part of the neighborhood are identified.
                val added = it.toSet() - setOf(this@EventImpl) - observedLocalEvents.keys
                // Observations of events on which it no longer depends are deleted.
                removed.forEach { event ->
                    observedLocalEvents[event]?.cancelAndJoin()
                    observedLocalEvents.remove(event)
                }
                // A new observation is started for each new event on which it is dependent.
                added.forEach { event ->
                    val job = launch {
                        val executionFlow = event.observeExecution()
                        executionFlow.run {
                            this.collect { event ->
                                // Following the execution of an event on which it is dependent, the execution time must be updated.
                                updateEvent(event.tau)
                                // Notification to the flow of actual consumption.
                                this.notifyConsumed()
                            }
                        }
                    }
                    observedLocalEvents[event] = job
                }
                // Notification to the flow of actual consumption.
                this.notifyConsumed()
            }
        }
    }
}