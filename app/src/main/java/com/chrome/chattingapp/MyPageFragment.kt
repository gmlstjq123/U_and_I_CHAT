package com.chrome.chattingapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.authentication.LoginActivity
import com.chrome.chattingapp.mypage.NickNameActivity
import com.chrome.chattingapp.mypage.PasswordActivity
import com.chrome.chattingapp.mypage.ProfileActivity
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MyPageFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        val nickName = view.findViewById<Button>(R.id.nickNameBtn)
        nickName.setOnClickListener {
            val intent = Intent(requireActivity(), NickNameActivity::class.java)
            startActivity(intent)
        }

        val password = view.findViewById<Button>(R.id.passwordBtn)
        password.setOnClickListener {
            val intent = Intent(requireActivity(), PasswordActivity::class.java)
            startActivity(intent)
        }

        val profile = view.findViewById<Button>(R.id.profileBtn)
        profile.setOnClickListener {
            val intent = Intent(requireActivity(), ProfileActivity::class.java)
            startActivity(intent)
        }

        val logout = view.findViewById<Button>(R.id.logoutBtn)
        logout.setOnClickListener {
            val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.logout_dialog, null)
            val builder = AlertDialog.Builder(requireActivity())
                .setView(dialogView)
                .setTitle("로그아웃")
            val alertDialog = builder.show()
            val ok = alertDialog.findViewById<Button>(R.id.ok)
            ok.setOnClickListener {
                getAccessToken { accessToken ->
                    if (accessToken.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = logoutUser(accessToken)

                            if (response.isSuccess) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireActivity(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                Log.d("MyPageFragment", "로그아웃 실패")
                                val message = response.message
                                Log.d("PasswordActivity", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        requireActivity(), message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Log.e("MyPageFragment", "Invalid Token")
                    }
                }
            }

            val cancel = alertDialog.findViewById<Button>(R.id.cancel)
            cancel.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        val freind = view.findViewById<ImageView>(R.id.freind)
        freind.setOnClickListener {
            it.findNavController().navigate(R.id.action_myPageFragment_to_userListFragment)
        }

        val chat = view.findViewById<ImageView>(R.id.chat)
        chat.setOnClickListener {
            it.findNavController().navigate(R.id.action_myPageFragment_to_chatListFragment)
        }

        return view
    }

    private suspend fun logoutUser(accessToken : String): BaseResponse<String> {
        return RetrofitInstance.myPageApi.logoutUser(accessToken)
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