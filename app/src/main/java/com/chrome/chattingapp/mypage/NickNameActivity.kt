package com.chrome.chattingapp.mypage

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
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NickNameActivity : AppCompatActivity() {

    lateinit var nickName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nick_name)

        val oldNickName = findViewById<TextView>(R.id.oldNickName)

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserInfo(FirebaseAuthUtils.getUid())
            if (response.isSuccess) {
                this@NickNameActivity.nickName = response.result?.nickName.toString()
                runOnUiThread {
                    oldNickName.text = nickName
                }
            } else {
                Log.d("NickNameActivity", "유저의 정보를 불러오지 못함")
            }
        }

        val newNickNameBtn = findViewById<Button>(R.id.newNickNameBtn)

        newNickNameBtn.setOnClickListener {
            val newNickName = findViewById<TextInputEditText>(R.id.newNickName)
            val newNickNameStr = newNickName.text.toString()
            if(newNickNameStr.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else {
                getAccessToken { accessToken ->
                    if (accessToken.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = modifyUserName(accessToken, newNickNameStr)
                            if (response.isSuccess) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@NickNameActivity, "닉네임 변경이 완료되었습니다", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@NickNameActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                            else {
                                Log.d("NickNameActivity", "닉네임 변경 실패")
                                val message = response.message
                                Log.d("NickNameActivity", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@NickNameActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("NickNameActivity", "Invalid Token")
                    }
                }

            }
        }
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    private suspend fun modifyUserName(accessToken : String, nickName : String) : BaseResponse<String> {
        return RetrofitInstance.myPageApi.modifyUserName(accessToken, nickName)
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
}