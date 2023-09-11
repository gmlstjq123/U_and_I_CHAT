package com.chrome.chattingapp.api

import com.chrome.chattingapp.push.PushNotice
import com.chrome.chattingapp.push.PushRepository
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NoticeApi {
    @Headers("Authorization: key=${PushRepository.SERVER_KEY}", "Content-Type:${PushRepository.CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotice) : retrofit2.Response<ResponseBody>
}