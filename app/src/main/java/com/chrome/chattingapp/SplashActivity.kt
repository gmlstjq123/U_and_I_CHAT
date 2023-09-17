package com.chrome.chattingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.PostReissueReq
import com.chrome.chattingapp.authentication.LoginActivity
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val uid = FirebaseAuthUtils.getUid()
        Log.d("uid", uid)

        if(uid == null) { // 신규 유저 -> 회원가입 페이지로 전환
            Handler().postDelayed({
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }, 2000)
        }

        else { // 기존 유저
            getAccessToken { accessToken -> // 액세스 토큰 이용
                if(accessToken == null) { // 회원가입만 하고, 로그인은 한번도 한 적 없는 유저 -> 로그인 페이지로 전환
                    Handler().postDelayed({
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }, 2000)
                }
                if (accessToken.isNotEmpty()) { // 일반적인 유저
                    CoroutineScope(Dispatchers.IO).launch {
                        val expirationChk = checkExpiration(accessToken) // 액세스 토큰의 만료 여부 확인
                        if(expirationChk.isSuccess) { // 액세스 토큰의 만료 여부와 무관하게 항상 재발급
                            getRefreshToken { refreshToken ->
                                if (refreshToken.isNotEmpty()) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val postReissueReq = PostReissueReq(FirebaseAuthUtils.getUid(), refreshToken)
                                        val response = reissueToken(postReissueReq) // 리프레시 토큰으로 액세스 토큰 재발급
                                        if(response.isSuccess) {
                                            val newAccessToken = response.result // 파이어베이스의 기존 accessToekn을 newAccessToken으로 바꿈
                                            FirebaseRef.userInfo.child(uid).child("accessToken").setValue(newAccessToken)
                                            // [만약 모든 API에 재발급 로직을 추가한다면, 여기에서 newAccessToken을 이용하여 API를 호출하면 된다]
                                            withContext(Dispatchers.Main) {
                                                Handler().postDelayed({
                                                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                                    finish() }, 2000)
                                            }
                                        }
                                        else { // 리프레시 토큰이 만료되었거나 유효하지 않은 경우 -> 로그인 페이지로 전환
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(this@SplashActivity, "인증 토큰이 만료되어 로그인 후 이용 가능합니다", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                                                startActivity(intent)
                                            }
                                        }
                                    }
                                }
                                else { // access token은 Not Empty이면서, refresh token만 Empty 한 경우(거의 일어나지 않을 상황) -> 회원가입 페이지로 전환
                                    Log.e("SplashActivity", "Invalid Token")
                                    Handler().postDelayed({
                                        startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                                        finish() }, 2000)
                                }
                            }
                        }
                        else { // 유효하지 않은 토큰(ex. 위변조된 토큰, Redis 블랙토큰)의 접근 -> 회원가입 페이지로 전환
                            Log.d("SplashActivity", "Invalid User")
                            val message = expirationChk.message
                            Log.d("SplashActivity", message)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@SplashActivity, message, Toast.LENGTH_SHORT).show()
                                Handler().postDelayed({
                                    startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                                    finish()
                                }, 2000)
                            }
                        }
                    }
                }

                else {
                    Handler().postDelayed({
                        startActivity(Intent(this, IntroActivity::class.java))
                        finish()
                    }, 2000)
                }
            }
        }
    }

    private fun getAccessToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val accessToken = data?.accessToken ?: ""

                callback(accessToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("NickNameActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }

    private fun getRefreshToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val refreshToken = data?.refreshToken ?: ""
                callback(refreshToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("NickNameActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }

    private suspend fun checkExpiration(accessToken : String): BaseResponse<Boolean> {
        return RetrofitInstance.userApi.checkExpiration(accessToken)
    }

    private suspend fun reissueToken(postReissueReq : PostReissueReq): BaseResponse<String> {
        return RetrofitInstance.userApi.reissueToken(postReissueReq)
    }
}