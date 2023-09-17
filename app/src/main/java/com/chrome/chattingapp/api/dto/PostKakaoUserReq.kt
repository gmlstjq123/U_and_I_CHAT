package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostKakaoUserReq(
    @SerializedName("uid")
    val uid : String,

    @SerializedName("deviceToken")
    val deviceToken : String,
)
