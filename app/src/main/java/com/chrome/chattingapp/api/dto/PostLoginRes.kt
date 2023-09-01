package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostLoginRes (
    @SerializedName("userId")
    val userId : Long,

    @SerializedName("accessToken")
    val accessToken : String,

    @SerializedName("refreshToken")
    val refreshToken : String
)