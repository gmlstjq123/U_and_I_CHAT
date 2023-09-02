package com.chrome.chattingapp.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.PatchPasswordReq
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        val newPasswordBtn = findViewById<Button>(R.id.newPasswordBtn)
        newPasswordBtn.setOnClickListener {
            val oldPassword = findViewById<TextInputEditText>(R.id.oldPassword)
            val oldPasswordStr = oldPassword.text.toString()

            val newPassword = findViewById<TextInputEditText>(R.id.newPassword)
            val newPasswordStr = newPassword.text.toString()

            val newPasswordChk = findViewById<TextInputEditText>(R.id.newPasswordChk)
            val newPasswordChkStr = newPasswordChk.text.toString()

            if(oldPasswordStr.isEmpty()) {
                Toast.makeText(this, "현재 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(newPasswordStr.isEmpty()) {
                Toast.makeText(this, "새 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(newPasswordStr != newPasswordChkStr) {
                Toast.makeText(this, "비밀번호와 비밀번호 확인의 입력 값이 다릅니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                val patchPasswordReq = PatchPasswordReq(oldPasswordStr, newPasswordStr, newPasswordChkStr)
                getAccessToken { accessToken ->
                    if (accessToken.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = modifyPassword(accessToken, patchPasswordReq)

                            if (response.isSuccess) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@PasswordActivity, "비밀번호 변경이 완료되었습니다", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@PasswordActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }

                           else {
                                Log.d("PasswordActivity", "비밀번호 변경 실패")
                                val message = response.message
                                Log.d("PasswordActivity", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@PasswordActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("PasswordActivity", "Invalid Token")
                    }
                }

            }
        }
    }

    private suspend fun modifyPassword(accessToken : String, patchPasswordReq : PatchPasswordReq): BaseResponse<String> {
        return RetrofitInstance.myPageApi.modifyPassword(accessToken, patchPasswordReq)
    }

    private fun getAccessToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val accessToken = data?.accessToken ?: ""
                callback(accessToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("PasswordActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }
}