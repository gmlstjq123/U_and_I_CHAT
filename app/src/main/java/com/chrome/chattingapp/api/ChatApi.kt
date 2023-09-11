package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.chat.ChatRoom
import com.chrome.chattingapp.chat.dto.AddUserReq
import com.chrome.chattingapp.push.PushNotice
import com.chrome.chattingapp.push.PushRepository
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.Param
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
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

    @GET("/chat/userList/{roomId}")
    suspend fun getUserStrList(@Path("roomId") roomId : String) : BaseResponse<String>

    @GET("/chat/tokenList/{roomId}")
    suspend fun getTokenList(@Path("roomId") roomId : String) : BaseResponse<List<String>>
}