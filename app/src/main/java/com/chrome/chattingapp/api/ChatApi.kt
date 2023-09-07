package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.chat.ChatRoom
import com.chrome.chattingapp.chat.dto.AddUserReq
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApi {
    @POST("/chat/room")
    suspend fun createChatRoom(
        @Header("Authorization") accessToken : String,
        @Query("roomName") roomName : String
    ) : BaseResponse<String>

    @POST("/chat/room/add")
    suspend fun addUser(@Body addUserReq : AddUserReq) : BaseResponse<String>

    @GET("/chat/userCount/{roomId}")
    suspend fun getUserCount(@Path("roomId") roomId : String) : BaseResponse<String>

    @GET("/chat/room/{roomId}")
    suspend fun getUserList(@Path("roomId") roomId : String) : BaseResponse<List<UserProfile>>
}