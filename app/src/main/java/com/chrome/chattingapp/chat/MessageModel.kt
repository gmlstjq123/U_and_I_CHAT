package com.chrome.chattingapp.chat

data class MessageModel (
    val senderUid : String = "",
    val senderNickName : String = "",
    val senderProfileUrl : String = "",
    val contents : String = "",
    val sendTime : String = "",
    val readerUids: MutableMap<String, Boolean> = mutableMapOf(), // 메시지 읽은 유저의 UID
    var unreadUserCount : Int = 0, // 메시지를 읽지 않은 사람의 수
    val imageUrl : String? = "null",
    var viewType: Int = VIEW_TYPE_ME
) {
    companion object {
        const val VIEW_TYPE_YOU = 0
        const val VIEW_TYPE_ME = 1
        const val VIEW_TYPE_YOU_IMAGE = 3
        const val VIEW_TYPE_ME_IMAGE = 4
    }
}