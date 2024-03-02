private fun observeNeighborEvents() {
    coroutineScope.launch {
        environment.neighborhoods.run {
            this.onSubscription {
                initLatch.countDown()
            }.mapLatest {
                // The updated neighborhood is mapped to a set of nodes.
                it[node.id]?.neighbors.orEmpty()
            }.collect { neighbors ->
                // Stop to observe the event that no longer belong to the neighborhood.
                stopToObserveOldNeighbors(neighbors)
                // Events that are now part of the neighborhood are identified and observed.
                val addedNeighbors = neighbors - observedNeighbors.keys
                addedNeighbors.forEach { node ->
                    val job = launch {
                        node.events.run {
                            this.collect { events ->
                                stopToObserveRemovedEvents(events)
                                val added = events.toSet() - observedNeighborEvents[node]?.keys.orEmpty()
                                added.forEach { event ->
                                    val job = launch {
                                        val executionFlow = event.observeExecution()
                                        executionFlow.run {
                                            this.collect { ev ->
                                                // Following the execution of an event on which it is dependent, it need to update its execution time.
                                                updateEvent(ev.tau)
                                                // Notification to the flow of actual consumption.
                                                this.notifyConsumed()
                                            }
                                        }
                                    }
                                    observedNeighborEvents[node]?.set(event, job)
                                }
                                this.notifyConsumed()
                            }
                        }
                    }
                    observedNeighbors[node] = job
                    observedNeighborEvents[node] = hashMapOf()
                }
                this.notifyConsumed()
            }
        }
    }
}