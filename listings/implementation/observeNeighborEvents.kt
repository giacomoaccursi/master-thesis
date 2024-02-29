private fun observeNeighborEvents() {
    coroutineScope.launch {
        environment.neighborhoods.run {
            this.onSubscription {
                initLatch.countDown()
            // il vicinato aggiornato viene mappato in un set di eventi al suo interno
            }.mapLatest {
                it[node.id]?.neighbors.orEmpty()
                    .flatMap { node ->
                        node.events.value
                    }
            }.collect { events ->
                 // vengono identificati gli eventi che non appartengono più al vicinato.
                val removed = observedNeighborEvents.keys - events.toSet()
                // vengono identificati gli eventi che ora fanno parte del vicinato.
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
                                /*
                                 a seguito dell'esecuzione di un evento da cui si è dipendenti
                                 occorre aggiornare il proprio tempo di esecuzione
                                 */
                                updateEvent(event.tau)
                                // notifica al flow dell'effettivo consumo.
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