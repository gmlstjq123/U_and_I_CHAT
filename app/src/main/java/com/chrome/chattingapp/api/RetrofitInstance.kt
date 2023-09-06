package com.chrome.chattingapp.api

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
    }
}