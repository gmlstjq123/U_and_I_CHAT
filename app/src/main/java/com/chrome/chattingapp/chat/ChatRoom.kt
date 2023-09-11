package com.chrome.chattingapp.chat

data class ChatRoom(
    val chatRoomId : String? = null,
    val roomName : String? = null,
    val unreadCount : Int? = 0,
    val lastMessage : String = ""
)
