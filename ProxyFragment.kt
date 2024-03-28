package uz.uzum.tezkor.courier.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import uz.uzum.tezkor.courier.base_screen.R

abstract class ProxyFragment : Fragment() {

    abstract fun createTargetFragment(): Fragment

    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FrameLayout(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            it.id = R.id.container
        }
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                createTargetFragment(),
            )
            .commit()
    }
}
