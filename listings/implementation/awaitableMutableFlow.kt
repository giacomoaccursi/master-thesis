abstract class AbstractAwaitableMutableFlow<T>(
    private val flow: MutableSharedFlow<T>,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) {
    private var emitLatch: CountDownLatch = CountDownLatch(0)
    /**
     * Perform emit waiting for notification.
     */
    suspend fun emitAndWait(value: T) {
        emitLatch = CountDownLatch(flow.subscriptionCount.value)
        flow.emit(value)
        withContext(ioDispatcher) {
            emitLatch.await()
        }
        emitLatch = CountDownLatch(0)
    }
    /**
     * A function for notifying the consumption.
     */
    fun notifyConsumed() {
        emitLatch.countDown()
    }
}

class AwaitableMutableSharedFlow<T>(
    private val sharedFlow: MutableSharedFlow<T>,
) : AbstractAwaitableMutableFlow<T>(sharedFlow), MutableSharedFlow<T> by sharedFlow {

    override suspend fun emit(value: T) {
        this.emitAndWait(value)
    }
}

class AwaitableMutableStateFlow<T>(
    private val stateFlow: MutableStateFlow<T>,
) : AbstractAwaitableMutableFlow<T>(stateFlow), MutableStateFlow<T> by stateFlow {

    override suspend fun emit(value: T) {
        this.emitAndWait(value)
    }
}