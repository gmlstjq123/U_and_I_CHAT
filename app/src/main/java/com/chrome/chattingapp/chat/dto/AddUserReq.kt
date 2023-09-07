package com.chrome.chattingapp.chat.dto

import com.google.gson.annotations.SerializedName

data class AddUserReq(
    @SerializedName("uid")
    val uid : String,

    @SerializedName("roomId")
    val roomId : String,
)
