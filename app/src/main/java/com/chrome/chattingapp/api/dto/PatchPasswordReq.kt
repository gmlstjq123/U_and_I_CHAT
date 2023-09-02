package com.chrome.chattingapp.api.dto

import com.google.gson.annotations.SerializedName

data class PatchPasswordReq(
    @SerializedName("exPassword")
    val exPassword : String,

    @SerializedName("newPassword")
    val newPassword : String,

    @SerializedName("newPasswordChk")
    val newPasswordChk : String
)
