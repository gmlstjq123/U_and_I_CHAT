package com.chrome.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.PostDeviceTokenReq
import com.chrome.chattingapp.api.dto.PostKakaoLoginRes
import com.chrome.chattingapp.api.dto.PostKakaoUserReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostNaverLoginRes
import com.chrome.chattingapp.api.dto.PostNaverUserReq
import com.chrome.chattingapp.authentication.JoinActivity
import com.chrome.chattingapp.authentication.LoginActivity
import com.chrome.chattingapp.authentication.UserInfo
import com.chrome.chattingapp.databinding.ActivityIntroBinding
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var binding : ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_intro)

        auth = Firebase.auth

        /** Naver Login Module Initialize */
        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret, "U & I TALK")

        val joinBtn = findViewById<Button>(R.id.join)
        joinBtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        val loginBtn = findViewById<Button>(R.id.login)
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "기타 에러", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (token != null) {
                Log.d("accessToken", token.accessToken)
                CoroutineScope(Dispatchers.IO).launch {
                    val response = kakaoCallback(token.accessToken)
                    Log.d("IntroActivity", response.toString())
                    if (response.isSuccess) {
                        Log.d("email", response.result!!.email)
                        if(FirebaseAuthUtils.getUid() == null) {
                            auth.createUserWithEmailAndPassword(response.result!!.email, "abc123")
                        }
                        FirebaseMessaging.getInstance().token.addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    Log.w("MyToken", "Fetching FCM registration token failed", task.exception)
                                    return@OnCompleteListener
                                }
                                val uid = FirebaseAuthUtils.getUid()
                                val deviceToken = task.result
                                val userInfo = UserInfo(uid, response.result?.userId,
                                    deviceToken, response.result?.accessToken, response.result?.refreshToken)
                                Log.d("userInfo", userInfo.toString())
                                FirebaseRef.userInfo.child(uid).setValue(userInfo)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val postKakaoUserReq = PostKakaoUserReq(uid, deviceToken)
                                    val saveRes = saveUidAndToken(response.result?.accessToken!!, postKakaoUserReq)
                                    Log.d("UidToken", saveRes.toString())
                                    if (saveRes.isSuccess) {
                                        Log.d("UidToken", "UID와 디바이스 토큰 저장 완료")
                                    } else {
                                        Log.d("UidToken", "UID와 디바이스 토큰 저장 실패")
                                    }
                                }
                                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                finish()
                            })
                        Log.d("IntroActivity", "로그인 완료")
                    } else {
                        // 로그인 실패 처리
                        Log.d("IntroActivity", "로그인 실패")
                        val message = response.message
                        Log.d("IntroActivity", message)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@IntroActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        val kakaoLoginBtn = findViewById<ImageView>(R.id.kakao_login)
        kakaoLoginBtn.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                val token = NaverIdLoginSDK.getAccessToken().toString()
                Log.d("naverToken", token)
                if(token != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = naverCallback(token)
                        Log.d("IntroActivity", response.toString())
                        if (response.isSuccess) {
                            Log.d("email", response.result!!.email)
                            Log.d("uid", FirebaseAuthUtils.getUid())
                            if(FirebaseAuthUtils.getUid() == null) {
                                auth.createUserWithEmailAndPassword(response.result!!.email, "abc123")
                            }
                            FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        Log.w("MyToken", "Fetching FCM registration token failed", task.exception)
                                        return@OnCompleteListener
                                    }
                                    val uid = FirebaseAuthUtils.getUid()
                                    val deviceToken = task.result
                                    val userInfo = UserInfo(uid, response.result?.userId,
                                        deviceToken, response.result?.accessToken, response.result?.refreshToken)
                                    Log.d("userInfo", userInfo.toString())
                                    FirebaseRef.userInfo.child(uid).setValue(userInfo)

                                    CoroutineScope(Dispatchers.IO).launch {
                                        val postNaverUserReq = PostNaverUserReq(uid, deviceToken)
                                        val saveRes = saveNaverUidAndToken(response.result?.accessToken!!, postNaverUserReq)
                                        Log.d("UidToken", saveRes.toString())
                                        if (saveRes.isSuccess) {
                                            Log.d("UidToken", "UID와 디바이스 토큰 저장 완료")
                                        } else {
                                            Log.d("UidToken", "UID와 디바이스 토큰 저장 실패")
                                        }
                                    }
                                    val intent = Intent(this@IntroActivity, MainActivity::class.java)
                                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                    finish()
                                })
                            Log.d("IntroActivity", "로그인 완료")
                        } else {
                            // 로그인 실패 처리
                            Log.d("IntroActivity", "로그인 실패")
                            val message = response.message
                            Log.d("IntroActivity", message)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@IntroActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                else {
                    Toast.makeText(this@IntroActivity, "접근이 거부 됨", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                Log.d("naverToken", errorCode)
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d("naverToken", errorDescription.toString())
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        binding.buttonOAuthLoginImg.setOAuthLogin(oauthLoginCallback = oauthLoginCallback)
    }

    private suspend fun kakaoCallback(accessToken: String): BaseResponse<PostKakaoLoginRes> {
        return RetrofitInstance.kakaoApi.kakaoCallback(accessToken)
    }

    private suspend fun naverCallback(accessToken: String): BaseResponse<PostNaverLoginRes> {
        return RetrofitInstance.naverApi.naverCallback(accessToken)
    }

    private suspend fun saveUidAndToken(accessToken: String, postKakaoUserReq: PostKakaoUserReq): BaseResponse<String> {
        return RetrofitInstance.kakaoApi.saveUidAndToken(accessToken, postKakaoUserReq)
    }

    private suspend fun saveNaverUidAndToken(accessToken: String, postNaverUserReq: PostNaverUserReq): BaseResponse<String> {
        return RetrofitInstance.naverApi.saveUidAndToken(accessToken, postNaverUserReq)
    }
}

