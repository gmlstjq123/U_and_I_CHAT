package com.chrome.chattingapp.mypage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.chrome.chattingapp.IntroActivity
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WithdrawalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdrawal)
        auth = Firebase.auth

        val withdrawal = findViewById<Button>(R.id.check)
        withdrawal.setOnClickListener {
            val agreement = findViewById<TextInputEditText>(R.id.agreement)
            val agreementStr = agreement.text.toString()

            getAccessToken { accessToken ->
                if (accessToken.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = deleteUser(accessToken, agreementStr)
                        if (response.isSuccess) {
                            FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).removeValue()
                            FirebaseAuth.getInstance().currentUser?.delete()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WithdrawalActivity, "회원 탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@WithdrawalActivity, IntroActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        else {
                            Log.d("WithdrawalActivity", "회원 탈퇴 실패")
                            val message = response.message
                            Log.d("WithdrawalActivity", message)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@WithdrawalActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("WithdrawalActivity", "Invalid Token")
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
                Log.w("WithdrawalActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }

    private suspend fun deleteUser(accessToken : String, agreement : String) : BaseResponse<String> {
        return RetrofitInstance.myPageApi.deleteUser(accessToken, agreement)
    }
}