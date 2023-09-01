package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PostLoginReq (
    @SerializedName("uid")
    val uid : String,

    @SerializedName("email")
    val email : String,

    @SerializedName("password")
    val password : String
)