package com.chrome.chattingapp.authentication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.PostLoginReq
import com.chrome.chattingapp.api.dto.PostLoginRes
import com.chrome.chattingapp.api.dto.PostUserReq
import com.chrome.chattingapp.api.dto.PostUserRes
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class JoinActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        auth = Firebase.auth

        val joinBtn = findViewById<Button>(R.id.joinBtn)
        joinBtn.setOnClickListener {
            val nickname = findViewById<TextInputEditText>(R.id.nickname)
            val email = findViewById<TextInputEditText>(R.id.joinEmail)
            val password = findViewById<TextInputEditText>(R.id.joinPassword)
            val passwordChk = findViewById<TextInputEditText>(R.id.passwordChk)

            if(nickname.text.toString().isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            else if(email.text.toString().isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            else if(password.text.toString().isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            }

            else if(password.text.toString().length > 12 || password.text.toString().length < 6) {
                Toast.makeText(this, "비밀번호는 6~12자로만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }

            else if(password.text.toString() != passwordChk.text.toString()) {
                Toast.makeText(this, "비밀번호와 비밀번호 확인의 입력 값이 다릅니다.", Toast.LENGTH_SHORT).show()
            }

            else {
                val postUserReq = PostUserReq(nickname.text.toString(), email.text.toString(),
                    password.text.toString(), passwordChk.text.toString())

                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val response = createUser(postUserReq)
                                if (response.isSuccess) {
                                    // UI 업데이트는 Dispatchers.Main을 사용하여 메인 스레드에서 실행
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@JoinActivity, "가입을 환영합니다!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                                        startActivity(intent)
                                    }
                                } else {
                                    Log.d("JoinActivity", "회원가입 실패")
                                    val message = response.message
                                    Log.d("JoinActivity", message)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(this@JoinActivity, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            // UI 업데이트는 Dispatchers.Main을 사용하여 메인 스레드에서 실행
                            runOnUiThread {
                                Toast.makeText(this@JoinActivity, "이메일 형식이 잘못되었거나, 이미 존재하는 이메일입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    private suspend fun createUser(postUserReq: PostUserReq): BaseResponse<PostUserRes> {
        return RetrofitInstance.userApi.createUser(postUserReq)
    }
}