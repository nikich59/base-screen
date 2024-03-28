package uz.uzum.tezkor.courier.common

interface BackPressHandler {

    enum class BackPressResult {
        CONSUMED,
        NOT_CONSUMED,
    }

    fun onBackPressed(): BackPressResult
}
