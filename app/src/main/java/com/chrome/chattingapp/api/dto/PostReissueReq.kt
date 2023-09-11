package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostReissueReq(
    @SerializedName("uid")
    val uid : String,

    @SerializedName("refreshToken")
    val refreshToken : String
)
