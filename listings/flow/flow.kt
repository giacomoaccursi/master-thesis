public interface Flow<out T> {
    
    public suspend fun collect(collector: FlowCollector<T>)
}