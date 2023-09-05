package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class UserProfile (
    @SerializedName("imgUrl")
    val imgUrl : String,

    @SerializedName("nickName")
    val nickName : String
)
