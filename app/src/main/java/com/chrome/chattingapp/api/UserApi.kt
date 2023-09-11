package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.PostDeviceTokenReq
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostReissueReq
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.api.dto.PostUserRes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserApi {
    @POST("/users")
    suspend fun createUser(@Body postUserReq: PostUserReq): BaseResponse<PostUserRes>

    @POST("/users/log-in")
    suspend fun loginUser(@Body postLoginReq: PostLoginReq): BaseResponse<PostLoginRes>

    @GET("/users/list-up")
    suspend fun getUsers(
        @Header("Authorization") accessToken : String
    ) : BaseResponse<List<UserProfile>>

    @GET("/users/check-token")
    suspend fun checkExpiration(
        @Header("Authorization") accessToken : String
    ) : BaseResponse<Boolean>

    @POST("/users/reissue-token")
    suspend fun reissueToken(@Body postReissueReq: PostReissueReq) : BaseResponse<String>

    @POST("users/device-token")
    suspend fun saveDeviceToken(@Body postDeviceTokenReq : PostDeviceTokenReq) : BaseResponse<String>
}