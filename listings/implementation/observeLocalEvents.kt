private fun observeLocalEvents() {
    coroutineScope.launch {
        node.events.run {
            this.onSubscription {
                initLatch.countDown()
            }.collect {
                // vengono identificati gli eventi che non occorre più osservare.
                val removed = observedLocalEvents.keys - it.toSet() - setOf(this@EventImpl)
                // vengono identificati gli eventi che occorre iniziare ad osservare.
                val added = it.toSet() - setOf(this@EventImpl) - observedLocalEvents.keys
                // le osservazioni degli eventi da cui non si dipende più vengono cancellate.
                removed.forEach { event ->
                    observedLocalEvents[event]?.cancelAndJoin()
                    observedLocalEvents.remove(event)
                }
                // viene avviata una nuova osservazione per ciascun nuovo evento da cui si è dipendenti.
                added.forEach { event ->
                    val job = launch {
                        val executionFlow = event.observeExecution()
                        executionFlow.run {
                            this.collect { event ->
                                // a seguito dell'esecuzione di un evento da cui si è dipendenti occorre aggiornare il proprio tempo di esecuzione
                                updateEvent(event.tau)
                                // notifica al flow dell'effettivo consumo.
                                this.notifyConsumed()
                            }
                        }
                    }
                    observedLocalEvents[event] = job
                }
                // notifica al flow dell'effettivo consumo.
                this.notifyConsumed()
            }
        }
    }
}