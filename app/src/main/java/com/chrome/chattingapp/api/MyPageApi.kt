package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.PatchPasswordReq
import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.api.dto.PostUserRes
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface MyPageApi {
    @GET("/users")
    suspend fun getUserInfo(@Query("uid") uid : String): BaseResponse<GetUserRes>

    @POST("/users/log-out")
    suspend fun logoutUser(
        @Header("Authorization") accessToken : String
    ): BaseResponse<String>

    @PATCH("/users/nickname")
    suspend fun modifyUserName(
        @Header("Authorization") accessToken : String,
        @Query("nickName") nickName : String
    ) : BaseResponse<String>

    @PATCH("/users/password")
    suspend fun modifyPassword(
        @Header("Authorization") accessToken : String,
        @Body patchPasswordReq : PatchPasswordReq
    ): BaseResponse<String>

    @Multipart
    @PATCH("/users/profile")
    suspend fun modifyProfile(
        @Header("Authorization") accessToken : String,
        @Part image : MultipartBody.Part?
    ): BaseResponse<String>

    @PATCH("/users/noProfile")
    suspend fun modifyNoProfile(
        @Header("Authorization") accessToken : String
    ): BaseResponse<String>
}