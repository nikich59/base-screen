package uz.uzum.tezkor.courier.common

interface MessageBottomSheetParent {

    fun onMessageBottomSheetPrimaryButtonClick(callbackTag: String) {}
    fun onMessageBottomSheetSecondaryButtonClick(callbackTag: String) {}
}
