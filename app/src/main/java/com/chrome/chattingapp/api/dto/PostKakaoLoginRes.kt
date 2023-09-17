package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostKakaoLoginRes (
    @SerializedName("userId")
    val userId : Long,

    @SerializedName("email")
    val email : String,

    @SerializedName("accessToken")
    val accessToken : String,

    @SerializedName("refreshToken")
    val refreshToken : String
)