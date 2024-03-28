package uz.uzum.tezkor.courier.common

import androidx.viewbinding.ViewBinding

/**
 * Base Fragment class for screens without [BaseScreenData]
 */
abstract class BaseFragmentNoScreenData<ViewModel : androidx.lifecycle.ViewModel, Binding : ViewBinding> :
    BaseFragment<ViewModel, Binding, NoScreenData>()
