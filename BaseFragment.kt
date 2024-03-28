package uz.uzum.tezkor.courier.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.reflect.KClass

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseFragment<
        ViewModel : androidx.lifecycle.ViewModel,
        Binding : ViewBinding,
        ScreenData : BaseScreenData,
        > :
    Fragment() {

    private val uiListeners = mutableListOf<UiListener<Binding>>()

    protected abstract fun viewModelKClass(): KClass<out ViewModel>

    /**
     * Specify this key if you need to create multiple fragments of the same class,
     * otherwise they will all share the same view model
     *
     * A good idea is to derive this key from [getScreenData]
     */
    protected open fun viewModelKey(): String = ""
    protected abstract fun createViewModel(): ViewModel
    protected abstract fun inflateBinding(inflater: LayoutInflater, parentView: ViewGroup?): Binding

    /** Setup you view using the [binding]: set background, set text etc. in this method */
    protected abstract fun initializeUi(binding: Binding, savedInstanceState: Bundle?)

    /**
     * Subscribe to Flows from the [viewModel] in this method
     *
     * Commonly used as follows:
     * ```
     * override fun observeData() {
     *     observe(viewModel.titleFlow) { title ->
     *         myTextView.setText(title)
     *     }
     * }
     * ```
     */
    protected abstract fun observeData()

    protected fun <T> observe(flow: Flow<T>, action: (T) -> Unit) {
        launchAndRepeatOnViewLifecycle(Lifecycle.State.STARTED) {
            flow
                .onEach { value ->
                    action(value)
                }
                .launchIn(this)
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
        launchAndRepeatOnViewLifecycle(Lifecycle.State.STARTED) {
            eventFlow
                .onEach { event ->
                    event.doWithData(action)
                }
                .launchIn(this)
        }
    }

    protected fun doWhenUiAvailable(action: (Binding) -> Unit) {
        binding
            ?.also(action)
            ?: run {
                uiListeners.add(UiListener { action(it) })
            }
    }

    protected fun getScreenData(): ScreenData {
        // getParcelable(String) is deprecated: in favor of getParcelable(String, Class). However,
        // we don't know the ScreenData class here, and it cannot be a reified parameter of an abstract class
        @Suppress("DEPRECATION")
        return arguments
            ?.getParcelable(SCREEN_DATA_BUNDLE_KEY)
            ?: throw IllegalStateException("Did you forget to call setScreenData on this fragment?")
    }

    protected open fun afterCreate() {

    }

    protected open fun beforeDestroy() {

    }

    protected open fun beforeViewDestroy() {

    }

    protected var binding: Binding? = null
        private set

    protected val viewModel: ViewModel by lazy {
        val viewModelProvider = ViewModelProvider(
            this,
            getViewModelProviderFactory(),
        )

        return@lazy viewModelProvider[viewModelKey(), viewModelKClass().java]
    }

    protected fun requireBinding(): Binding {
        return binding!!
    }

    fun setScreenData(screenData: BaseScreenData) {
        arguments = Bundle().also {
            it.putParcelable(SCREEN_DATA_BUNDLE_KEY, screenData)
        }
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        afterCreate()
    }

    final override fun onDestroy() {
        beforeDestroy()

        super.onDestroy()
    }

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = inflateBinding(inflater, container)
        this.binding = binding

        return binding.root
    }

    final override fun onDestroyView() {
        beforeViewDestroy()
        this.binding = null
        super.onDestroyView()
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiListeners.forEach { it.onUiAvailable(binding!!) }
        uiListeners.clear()
        initializeUi(binding!!, savedInstanceState)

        observeData()
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

private const val SCREEN_DATA_BUNDLE_KEY = "screen_data"

private fun interface UiListener<Binding> {
    fun onUiAvailable(binding: Binding)
}
