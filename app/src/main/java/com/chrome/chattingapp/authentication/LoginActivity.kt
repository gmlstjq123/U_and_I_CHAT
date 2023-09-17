package com.chrome.chattingapp.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.PostDeviceTokenReq
import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val noAccountBtn = findViewById<TextView>(R.id.noAccount)
        noAccountBtn.setOnClickListener {
            val intent = Intent(this, JoinActivity::class.java)
            startActivity(intent)
        }

        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.email)
            val password = findViewById<TextInputEditText>(R.id.password)
            val uid = FirebaseAuthUtils.getUid()
            Log.d("uid", uid)
            val postLoginReq = PostLoginReq(uid, email.text.toString(), password.text.toString())

            if(email.text.toString().isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            else if(password.text.toString().isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            else {
                auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val response = loginUser(postLoginReq)
                                Log.d("LoginActivity", response.toString())
                                if (response.isSuccess) {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener(
                                        OnCompleteListener { task ->
                                            if (!task.isSuccessful) {
                                                Log.w("MyToken", "Fetching FCM registration token failed", task.exception)
                                                return@OnCompleteListener
                                            }
                                            val deviceToken = task.result
                                            val userInfo = UserInfo(uid, response.result?.userId,
                                                deviceToken, response.result?.accessToken, response.result?.refreshToken)
                                            Log.d("userInfo", userInfo.toString())
                                            FirebaseRef.userInfo.child(uid).setValue(userInfo)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val postDeviceTokenReq = PostDeviceTokenReq(uid, deviceToken)
                                                val response = saveDeviceToken(postDeviceTokenReq)
                                                Log.d("DeviceToken", response.toString())
                                                if (response.isSuccess) {
                                                    Log.d("DeviceToken", "디바이스 토큰 저장 완료")
                                                } else {
                                                    Log.d("DeviceToken", "디바이스 토큰 저장 실패")
                                                }
                                            }
                                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                            startActivity(intent)
                                        })
                                    Log.d("LoginActivity", "로그인 완료")
                                } else {
                                    // 로그인 실패 처리
                                    Log.d("LoginActivity", "로그인 실패")
                                    val message = response.message
                                    Log.d("JoinActivity", message)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Log.d("LoginActivity", "로그인 실패")
                            Toast.makeText(this@LoginActivity, "이메일 또는 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private suspend fun loginUser(postLoginReq: PostLoginReq): BaseResponse<PostLoginRes> {
        return RetrofitInstance.userApi.loginUser(postLoginReq)
    }

    private suspend fun saveDeviceToken(postDeviceTokenReq: PostDeviceTokenReq): BaseResponse<String> {
        return RetrofitInstance.userApi.saveDeviceToken(postDeviceTokenReq)
    }
}