package uz.uzum.tezkor.courier.common

import androidx.lifecycle.ViewModelProvider

inline fun <reified VM> getViewModelProviderFactory(crossinline viewModelProvider: () -> VM): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass == VM::class.java) {
                @Suppress("UNCHECKED_CAST")
                return viewModelProvider() as T
            } else {
                throw IllegalArgumentException("Bad ViewModel class: <${modelClass}>")
            }
        }
    }
}
