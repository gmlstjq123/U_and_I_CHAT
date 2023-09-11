package com.chrome.chattingapp.push

data class PushNotice (
    val data : NoticeModel,
    val to : String
)