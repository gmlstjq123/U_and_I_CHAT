package com.chrome.chattingapp.chat

data class MessageModel (
    val senderUid : String = "",
    val senderNickName : String = "",
    val senderProfileUrl : String = "",
    val contents : String = "",
    val sendTime : String = "",
    var viewType: Int = VIEW_TYPE_ME
) {
    companion object {
        const val VIEW_TYPE_YOU = 0
        const val VIEW_TYPE_ME = 1
    }
}