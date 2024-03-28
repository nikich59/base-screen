package uz.uzum.tezkor.courier.common

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uz.uzum.tezkor.courier.base_screen.R
import kotlin.reflect.KClass

abstract class BaseDialogFragment<
    ViewModel : androidx.lifecycle.ViewModel,
    Binding : ViewBinding,
    > :
    DialogFragment() {

    protected val viewModel: ViewModel by lazy {
        val viewModelProvider = ViewModelProvider(
            this,
            getViewModelProviderFactory(),
        )

        viewModelProvider[viewModelKClass().java]
    }

    protected var binding: Binding? = null
        private set

    private val dialogScope = CoroutineScope(Dispatchers.Main)

    protected abstract fun viewModelKClass(): KClass<ViewModel>
    protected abstract fun createViewModel(): ViewModel
    protected abstract fun isCancellable(): Boolean
    protected abstract fun inflateBinding(inflater: LayoutInflater, parentView: ViewGroup?): Binding
    protected abstract fun initializeUi(binding: Binding, savedInstanceState: Bundle?)
    protected abstract fun observeData()

    protected open fun layoutWidth(): Int = ViewGroup.LayoutParams.MATCH_PARENT
    protected open fun layoutHeight(): Int = ViewGroup.LayoutParams.WRAP_CONTENT

    protected open fun onDialogShown() {}

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setLayout(layoutWidth(), layoutHeight())
        dialog?.setCancelable(isCancellable())

        return view
    }

    final override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(isCancellable())

        dialog.setOnShowListener {
            onDialogShown()
        }

        val binding = inflateBinding(layoutInflater, null)
        this.binding = binding

        dialog.setContentView(binding.root)

        initializeUi(binding, savedInstanceState)

        observeData()

        return dialog
    }

    final override fun getTheme(): Int {
        return R.style.ThemeOverlay_Uzum_BottomSheetDialog_NoShape
    }

    final override fun onDismiss(dialog: DialogInterface) {
        dialogScope.cancel()
        super.onDismiss(dialog)
    }

    protected fun requireBinding(): Binding {
        return binding!!
    }

    protected fun <T> observe(flow: Flow<T>, onEach: (T) -> Unit) {
        dialogScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow
                    .onEach(onEach)
                    .launchIn(this)
            }
        }
    }

    @Suppress("DeprecatedCallableAddReplaceWith", "UNUSED_PARAMETER")
    @Deprecated(
        "Observe events via observeEvent",
        level = DeprecationLevel.ERROR,
    )
    @JvmName("observeEventDeprecated")
    protected fun <T : UiEvent<*>> observe(flow: Flow<T>, action: (T) -> Unit) {
        throw RuntimeException("Use observeEvent for events")
    }

    protected fun <T> observeEvent(eventFlow: Flow<UiEvent<T>>, action: (T) -> Unit) {
        dialogScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventFlow
                    .onEach { event ->
                        event.doWithData(action)
                    }
                    .launchIn(this)
            }
        }
    }

    private fun getViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                if (modelClass == viewModelKClass().java) {
                    @Suppress("UNCHECKED_CAST")
                    return createViewModel() as T
                } else {
                    throw IllegalArgumentException("Bad ViewModel class: <${modelClass}>")
                }
            }
        }
    }
}
