package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostUserReq (
    @SerializedName("nickName")
    val nickName : String,

    @SerializedName("email")
    val email : String,

    @SerializedName("password")
    val password : String,

    @SerializedName("passwordChk")
    val passwordChk : String,
)