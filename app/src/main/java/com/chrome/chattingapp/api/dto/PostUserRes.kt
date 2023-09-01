package com.chrome.chattingapp.api.dto

import android.R.id
import com.google.gson.annotations.SerializedName


data class PostUserRes (
    @SerializedName("userId")
    val userId : Long,

    @SerializedName("nickName")
    val nickName : String
)