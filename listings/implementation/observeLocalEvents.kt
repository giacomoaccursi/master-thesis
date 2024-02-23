private fun observeLocalEvents() {
    coroutineScope.launch {
        node.events.run {
            this.onSubscription {
                initLatch.countDown()
            }.collect {
                val removed = observedLocalEvents.keys - it.toSet() - setOf(this@EventImpl)
                val added = it.toSet() - setOf(this@EventImpl) - observedLocalEvents.keys
                removed.forEach { event ->
                    observedLocalEvents[event]?.cancelAndJoin()
                    observedLocalEvents.remove(event)
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
                    observedLocalEvents[event] = job
                }
                this.notifyConsumed()
            }
        }
    }
}