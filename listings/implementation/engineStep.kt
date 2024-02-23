private suspend fun doStep() {
    val nextEvent = scheduler.getNext()
    if (nextEvent == null) {
        status = Status.TERMINATED
    } else {
        val scheduledTime = nextEvent.tau
        if (scheduledTime.toDouble() < currentTime.toDouble()) {
            error("next event is scheduled in the past")
        }
        currentTime = scheduledTime
        if (nextEvent.canExecute()) {
            nextEvent.execute()
            nextEvent.updateEvent(currentTime)
            scheduler.eventsUpdated()
        }
    }
    currentStep += 1
}