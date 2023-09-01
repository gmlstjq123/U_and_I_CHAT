package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class GetUserRes(
    @SerializedName("imgUrl")
    val imgUrl : String,

    @SerializedName("nickName")
    val nickName : String
)
