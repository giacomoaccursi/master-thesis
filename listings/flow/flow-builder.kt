val coldFlow1 = flowOf(1, 2, 3, 4, 5)


val coldfFlow2 = (1..5).asFlow()


val coldFlow3 = flow {
    (0..10).forEach {
        emit(it)
    }
}

val sharedFlow = MutableSharedFlow<Int>()

val stateFlow = MutableStateFlow(0)
