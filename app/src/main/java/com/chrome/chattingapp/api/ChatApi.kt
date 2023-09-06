package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.chat.ChatRoom
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

//    @GET("/chat/room/{roomId}")
//    suspend fun getUserList(
//        @Path("roomId") roomId : String
//    ) : BaseResponse<List<GetUserRes>>


//    @GET("chat/room")
//    suspend fun getChatRoomList(
//        @Header("Authorization") accessToken : String
//    ) : BaseResponse<List<ChatRoom>>
}