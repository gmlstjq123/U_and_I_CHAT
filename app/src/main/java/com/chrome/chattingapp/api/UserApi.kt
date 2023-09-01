package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.api.dto.PostUserRes
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("/users")
    suspend fun createUser(@Body postUserReq: PostUserReq): BaseResponse<PostUserRes>

    @POST("/users/log-in")
    suspend fun loginUser(@Body postLoginReq: PostLoginReq): BaseResponse<PostLoginRes>
}