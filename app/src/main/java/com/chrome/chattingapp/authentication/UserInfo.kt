package com.chrome.chattingapp.authentication

data class UserInfo (
    val uid : String? = null,
    val userId : Long? = null,
    val deviceToken : String? = null,
    val accessToken : String? = null,
    val refreshToken : String? = null,
)
