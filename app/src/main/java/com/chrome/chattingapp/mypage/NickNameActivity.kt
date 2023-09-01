package com.chrome.chattingapp.mypage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

            }
        }
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }
}