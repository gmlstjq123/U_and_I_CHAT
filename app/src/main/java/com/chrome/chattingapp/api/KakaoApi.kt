package com.chrome.chattingapp.api

import com.chrome.chattingapp.api.dto.PostKakaoLoginRes
import com.chrome.chattingapp.api.dto.PostKakaoUserReq
import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface KakaoApi {
    @POST("/oauth/kakao")
    suspend fun kakaoCallback(
        @Query("token") accessToken : String // 카카오 서버에서 보내준 access token으로 인증 토큰이다.
    ) : BaseResponse<PostKakaoLoginRes>

    @POST("/oauth/device-token")
    suspend fun saveUidAndToken(
        @Header("Authorization") accessToken : String,
        @Body postKakapUserReq: PostKakaoUserReq
    ): BaseResponse<String>
}