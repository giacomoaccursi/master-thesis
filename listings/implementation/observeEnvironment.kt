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
                // The new neighborhoods are recalculated based on the actual nodes and their positions.
                val newNeighborhoods = environment.getAllNodes().associate { node ->
                    node.id to SimpleNeighborhood(node, computeNeighbors(node, environment))
                }
                environment.updateNeighborhoods(newNeighborhoods)
                // Notification to the flow of actual consumption.
                this.notifyConsumed()
            }
        }
    }
}