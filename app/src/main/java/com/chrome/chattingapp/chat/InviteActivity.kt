package com.chrome.chattingapp.chat

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.chrome.chattingapp.ListViewAdapter
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.chat.dto.AddUserReq
import com.chrome.chattingapp.friend.UserDetailActivity
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InviteActivity : AppCompatActivity() {

    // userProfileList를 nullable로 선언
    private var userProfileList: List<UserProfile>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)

        val chatRoomId = intent.getStringExtra("chatRoomId")
        val chatRoomName = intent.getStringExtra("chatRoomName")
        val nickNameList = intent.getStringExtra("nickNameList")

        // userProfileList 초기화는 API 응답 이후에 수행
        getAccessToken { accessToken ->
            if (accessToken.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = getUsers(accessToken)
                    if (response.isSuccess) {
                        userProfileList = response.result
                        Log.d("UserProfileList", userProfileList.toString())
                        withContext(Dispatchers.Main) {
                            // UI 업데이트는 Main 스레드에서 수행
                            val adapter = ListViewAdapter(this@InviteActivity, userProfileList ?: emptyList())
                            val listview = findViewById<ListView>(R.id.inviteListView)
                            listview.adapter = adapter
                            adapter.notifyDataSetChanged()
                            Log.d("UserProfileList", userProfileList.toString())
                            listview.setOnItemClickListener { parent, view, position, id ->
                                val newNickNameList = nickNameList + ", " + userProfileList!![position].nickName
                                val chatRoom = ChatRoom(chatRoomId, chatRoomName, newNickNameList)
                                Log.d("ChatRoom", chatRoom.toString())
                                FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).child(chatRoomId!!).setValue(chatRoom)

                                val intent = Intent(this@InviteActivity, ChatRoomActivity::class.java)
                                intent.putExtra("chatRoomId", chatRoomId)
                                intent.putExtra("chatRoomName", chatRoomName)
                                intent.putExtra("userList", newNickNameList)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val addUserReq = AddUserReq(userProfileList!![position].uid, chatRoomId)
                                    val response = addUser(addUserReq)
                                    if (response.isSuccess) {
                                        val nickName = response.result
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@InviteActivity, nickName + " 님을 초대하였습니다.", Toast.LENGTH_SHORT).show()
                                        }
                                        startActivity(intent)
                                    } else {
                                        val message = response.message
                                        Log.d("InviteActivity", message)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@InviteActivity, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        userProfileList = emptyList() // 초기화 실패 시 빈 리스트로 설정
                        Log.d("UserListFragment", "데이터 불러오기 실패")
                        val message = response.message
                        Log.d("UserListFragment", message)
                    }
                }
            } else {
                Log.e("UserListFragment", "Invalid Token")
            }
        }
    }

    private suspend fun getUsers(accessToken: String): BaseResponse<List<UserProfile>> {
        return RetrofitInstance.userApi.getUsers(accessToken)
    }

    private suspend fun addUser(addUserReq: AddUserReq) : BaseResponse<String> {
        return RetrofitInstance.chatApi.addUser(addUserReq)
    }

    private fun getAccessToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val accessToken = data?.accessToken ?: ""
                callback(accessToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("UserListFragment", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }
}