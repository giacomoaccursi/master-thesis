private fun observeNodes() {
    startToObserveFlow(environment.nodesToPosition)
}

private fun observeNodesPosition() {
    startToObserveFlow(environment.nodes)
}

private fun startToObserveFlow(flow: AwaitableMutableStateFlow<*>) {
    coroutineScope.launch {
        flow.run {
            this.onSubscription {
                initLatch.countDown()
            }.collect {
                // i nuovi vicinati vengono ricalcolati in base ai nodi presenti e alle loro posizioni.
                val newNeighborhoods = environment.getAllNodes().associate { node ->
                    node.id to SimpleNeighborhood(node, computeNeighbors(node, environment))
                }
                environment.updateNeighborhoods(newNeighborhoods)
                // notifica del consumo del flow.
                this.notifyConsumed()
            }
        }
    }
}