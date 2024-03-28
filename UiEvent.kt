package uz.uzum.tezkor.courier.common

class UiEvent<T>(
    private val data: T,
) {

    @Volatile
    private var isConsumed: Boolean = false

    fun doWithData(action: (T) -> Unit) {
        mapData(action)
    }

    fun <R> mapData(action: (T) -> R): R? {
        return if (isConsumed) {
            // Do nothing
            null
        } else {
            consume()
            action(data)
        }
    }

    fun getData(): T? {
        return mapData { it }
    }

    private fun consume() {
        isConsumed = true
    }
}
