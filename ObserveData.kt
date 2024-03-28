package uz.uzum.tezkor.courier.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun Fragment.launchAndRepeatOnViewLifecycle(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit,
) = viewLifecycleOwner.launchAndRepeatOnLifecycle(state, block)

private fun LifecycleOwner.launchAndRepeatOnLifecycle(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit,
) = lifecycle.launchAndRepeatOnLifecycle(state, block)

private fun Lifecycle.launchAndRepeatOnLifecycle(
    state: Lifecycle.State,
    block: suspend CoroutineScope.() -> Unit,
) {
    coroutineScope.launch {
        repeatOnLifecycle(state, block)
    }
}
