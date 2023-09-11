package com.chrome.chattingapp.api

import com.chrome.chattingapp.push.PushRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(ApiRepository.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val userApi = retrofit.create(UserApi::class.java)
        val myPageApi = retrofit.create(MyPageApi::class.java)
        val chatApi = retrofit.create(ChatApi::class.java)

        private val noticeRetrofit by lazy {
            Retrofit.Builder()
                .baseUrl(PushRepository.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val noticeApi = noticeRetrofit.create(NoticeApi::class.java)
    }
}