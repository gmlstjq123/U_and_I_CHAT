package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.PostKakaoUserReq
import com.chrome.chattingapp.api.dto.PostNaverLoginRes
import com.chrome.chattingapp.api.dto.PostNaverUserReq
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface NaverApi {
    @POST("/oauth/naver")
    suspend fun naverCallback(
        @Query("token") accessToken : String // 네이버 서버에서 보내준 access token으로, 인증 토큰이다.
    ) : BaseResponse<PostNaverLoginRes>

    @POST("/oauth/device-token")
    suspend fun saveUidAndToken(
        @Header("Authorization") accessToken : String,
        @Body postKakapUserReq: PostNaverUserReq
    ): BaseResponse<String>
}