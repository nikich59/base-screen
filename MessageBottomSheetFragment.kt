package uz.uzum.tezkor.courier.common

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import kotlinx.parcelize.Parcelize
import uz.uzum.tezkor.courier.base_screen.R
import uz.uzum.tezkor.courier.base_screen.databinding.MessageBottomSheetBinding
import uz.uzum.tezkor.uikit.drawable.ConvexTopDrawable
import uz.uzum.tezkor.uikit.view.PrimaryButton2

@Parcelize
data class MessageBottomSheetData(
    val title: String,
    val message: CharSequence,
    val primaryButtonLabel: String?,
    val secondaryButtonLabel: String?,
    val callbackTag: String?,
) : Parcelable

class MessageBottomSheetFragment internal constructor() :
    BaseBottomSheetFragment<NoViewModel, MessageBottomSheetBinding>() {

    companion object {
        fun getInstance(
            title: String,
            message: CharSequence,
            primaryButtonLabel: String? = null,
            secondaryButtonLabel: String? = null,
            callbackTag: String? = null,
        ): DialogFragment {
            val fragment = MessageBottomSheetFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(
                    DATA_BUNDLE_KEY,
                    MessageBottomSheetData(
                        title = title,
                        message = message,
                        primaryButtonLabel = primaryButtonLabel,
                        secondaryButtonLabel = secondaryButtonLabel,
                        callbackTag = callbackTag,
                    ),
                )
            }

            return fragment
        }
    }

    private val data: MessageBottomSheetData by lazy {
        @Suppress("DEPRECATION")
        requireArguments().getParcelable(DATA_BUNDLE_KEY)!!
    }

    override fun viewModelKClass() = NoViewModel::class

    override fun createViewModel() = NoViewModel()

    override fun isFullScreenBottomSheet() = false

    override fun inflateBinding(inflater: LayoutInflater, parentView: ViewGroup?): MessageBottomSheetBinding {
        return MessageBottomSheetBinding.inflate(
            inflater,
            parentView,
            false,
        )
    }

    override fun initializeUi(binding: MessageBottomSheetBinding, savedInstanceState: Bundle?) {
        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.contentContainer.background = ConvexTopDrawable(
            fillColor = requireContext().getColor(R.color.neutral_00),
            bumpHeight = requireContext().resources.getDimension(R.dimen.convex_top_drawable_bump_height),
        )

        binding.title.text = data.title
        binding.message.text = data.message

        binding.primaryButton.setPredefinedPrimary()

        val primaryButtonLabel = data.primaryButtonLabel
        binding.primaryButton.isVisible = primaryButtonLabel != null
        if (primaryButtonLabel != null) {
            binding.primaryButton.setContent(
                PrimaryButton2.Content.Text(primaryButtonLabel),
            )
            binding.primaryButton.setOnClickListener {
                data.callbackTag?.also { callbackTag ->
                    getParent().onMessageBottomSheetPrimaryButtonClick(callbackTag)
                }
                dismiss()
            }
        }

        val secondaryButtonLabel = data.secondaryButtonLabel
        binding.secondaryButton.isVisible = secondaryButtonLabel != null
        if (secondaryButtonLabel != null) {
            binding.secondaryButton.text = secondaryButtonLabel
            binding.secondaryButton.setOnClickListener {
                data.callbackTag?.also { callbackTag ->
                    getParent().onMessageBottomSheetSecondaryButtonClick(callbackTag)
                }
                dismiss()
            }
        }
    }

    override fun observeData() {

    }

    private fun getParent(): MessageBottomSheetParent {
        return (parentFragment ?: requireActivity()) as MessageBottomSheetParent
    }
}

private const val DATA_BUNDLE_KEY = "data"
