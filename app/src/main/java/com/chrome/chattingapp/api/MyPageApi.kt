package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.api.dto.PostUserRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MyPageApi {
    @GET("/users")
    suspend fun getUserInfo(@Query("uid") uid : String): BaseResponse<GetUserRes>
}