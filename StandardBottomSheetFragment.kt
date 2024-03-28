package uz.uzum.tezkor.courier.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import uz.uzum.tezkor.courier.base_screen.R
import uz.uzum.tezkor.courier.base_screen.databinding.StandardBottomSheetFragmentBinding
import uz.uzum.tezkor.uikit.draw.standardTopShadow
import uz.uzum.tezkor.uikit.drawable.ConvexTopDrawable
import uz.uzum.tezkor.uikit.recycler.TezkorRecyclerItem
import kotlin.math.roundToInt

abstract class StandardBottomSheetFragment<ViewModel : androidx.lifecycle.ViewModel> :
    BaseBottomSheetFragment<
        ViewModel,
        StandardBottomSheetFragmentBinding,
        >() {

    override fun isFullScreenBottomSheet() = false

    override fun inflateBinding(
        inflater: LayoutInflater,
        parentView: ViewGroup?,
    ): StandardBottomSheetFragmentBinding {
        return StandardBottomSheetFragmentBinding.inflate(
            inflater,
            parentView,
            false,
        )
    }

    protected abstract fun getBottomSheetTitle(): String

    @CallSuper
    override fun initializeUi(
        binding: StandardBottomSheetFragmentBinding,
        savedInstanceState: Bundle?,
    ) {
        binding.header.setTitle(getBottomSheetTitle())
        binding.header.setOnCloseClick {
            dismiss()
        }
        binding.header.isClickable = true

        val bottomButtonContainerBackground = ConvexTopDrawable(
            requireContext().getColor(R.color.neutral_00),
            requireContext().resources.getDimension(R.dimen.convex_top_drawable_bump_height),
            shadow = standardTopShadow(requireContext()),
        )
        binding.bottomContentContainer.background = bottomButtonContainerBackground

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    updateHeaderOnScroll(binding)
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        onScrollSettled(binding)
                    }
                }
            },
        )

        binding.contentContainer.afterMeasured = {
            binding.recyclerView.updatePadding(
                top = (binding.header.getExpandedHeight() - binding.recyclerViewContainer.paddingTop).roundToInt(),
                bottom = (binding.bottomContentContainer.measuredHeight - bottomButtonContainerBackground.bumpHeight)
                    .roundToInt()
                    .coerceAtLeast(0),
            )
            binding.recyclerView.scrollToPosition(0)
        }
    }

    protected fun setBottomContent(view: View?, layoutParams: FrameLayout.LayoutParams? = null) {
        requireBinding().bottomContentContainer.removeAllViews()
        if (view == null) {
            requireBinding().bottomContentContainer.visibility = View.GONE
        } else {
            requireBinding().bottomContentContainer.visibility = View.VISIBLE

            @Suppress("NAME_SHADOWING")
            val layoutParams = layoutParams
                ?: LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            layoutParams.width = LayoutParams.MATCH_PARENT
            layoutParams.height = LayoutParams.WRAP_CONTENT
            requireBinding().bottomContentContainer.addView(view, layoutParams)
        }
    }

    protected fun getBottomContent(): View? {
        return requireBinding().bottomContentContainer.children.singleOrNull()
    }

    protected fun setRecyclerViewItems(items: List<TezkorRecyclerItem<*>>) {
        requireBinding().recyclerView.adapter.submitList(items)
    }

    private fun updateHeaderOnScroll(binding: StandardBottomSheetFragmentBinding) {
        if (binding.header.height > 0) {
            val firstView = binding.recyclerView.layoutManager?.findViewByPosition(0)
            if (firstView == null) {
                binding.header.setCollapsed()
            } else {
                binding.header.updateBottomOffset((binding.recyclerView.paddingTop - firstView.y).roundToInt())
            }
        }
    }

    private fun onScrollSettled(binding: StandardBottomSheetFragmentBinding) {
        val expandedProgress = binding.header.getExpandedProgress()
        if (expandedProgress > 0f && expandedProgress < 1f) {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }
}
