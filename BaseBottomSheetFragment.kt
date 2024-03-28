package uz.uzum.tezkor.courier.common

import CoreUiColor
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uz.uzum.tezkor.courier.base_screen.R
import uz.uzum.tezkor.uikit.util.dpToPx
import kotlin.math.roundToInt
import kotlin.reflect.KClass

abstract class BaseBottomSheetFragment<
    ViewModel : androidx.lifecycle.ViewModel,
    Binding : ViewBinding,
    > :
    BottomSheetDialogFragment() {

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
    protected abstract fun inflateBinding(inflater: LayoutInflater, parentView: ViewGroup?): Binding
    protected abstract fun initializeUi(binding: Binding, savedInstanceState: Bundle?)
    protected abstract fun observeData()
    protected abstract fun isFullScreenBottomSheet(): Boolean

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = inflateBinding(inflater, container)
        this.binding = binding

        initializeUi(binding, savedInstanceState)

        observeData()

        if (isFullScreenBottomSheet()) {
            val defaultHeight = requireContext().dpToPx(540f).roundToInt()

            binding.root.post {
                val decorView = dialog?.window?.decorView

                if (decorView == null) {
                    binding.root.updateLayoutParams {
                        height = defaultHeight
                    }
                } else {
                    val topInset = WindowInsetsCompat.toWindowInsetsCompat(decorView.rootWindowInsets)
                        .getInsets(WindowInsetsCompat.Type.statusBars())
                        .top

                    val bottomInset = WindowInsetsCompat.toWindowInsetsCompat(decorView.rootWindowInsets)
                        .getInsets(WindowInsetsCompat.Type.systemBars())
                        .bottom

                    binding.root.updatePadding(
                        bottom = bottomInset,
                    )
                    binding.root.updateLayoutParams {
                        height = decorView.height - topInset
                    }
                }
            }
        }

        return binding.root
    }

    final override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.skipCollapsed = true
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val window = dialog.window
        if (window != null) {
            val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.navigationBarColor = window.context.getColor(CoreUiColor.neutral_00)
                windowInsetsController.isAppearanceLightNavigationBars = true
            } else {
                window.navigationBarColor = window.context.getColor(CoreUiColor.neutral_400)
            }
        }
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
