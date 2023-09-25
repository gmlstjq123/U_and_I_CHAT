package com.chrome.chattingapp.friend

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.chat.ChatRoom
import com.chrome.chattingapp.chat.ChatRoomActivity
import com.chrome.chattingapp.chat.dto.AddUserReq
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

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // Intent로부터 데이터를 가져옴
        val uid = intent.getStringExtra("uid")
        val imgUrl = intent.getStringExtra("imgUrl")
        val nickName = intent.getStringExtra("nickName")

        val profileImageView = findViewById<ImageView>(R.id.profileDetail)
        val nickNameTextView = findViewById<TextView>(R.id.nickNameDetail)
        val startChatBtn = findViewById<ImageView>(R.id.startChat)

        if(imgUrl != null && imgUrl != "null") {
            Glide.with(this)
                .load(imgUrl)
                .into(profileImageView)
        }

        nickNameTextView.text = nickName

        profileImageView.setOnClickListener {
            val intent = Intent(this, ProfileImageActivity::class.java)
            intent.putExtra("imgUrl", imgUrl)
            startActivity(intent)
        }

        startChatBtn.setOnClickListener {
            showDialog(uid!!)
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
                Log.w("ChatListFragment", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }

    private suspend fun createChatRoom(accessToken : String, roomName : String): BaseResponse<String> {
        return RetrofitInstance.chatApi.createChatRoom(accessToken, roomName)
    }

    private fun showDialog(uid : String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.chat_room_dialog, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("채팅방 생성하기")
        val alertDialog = builder.show()

        val createBtn = alertDialog.findViewById<Button>(R.id.create)
        createBtn.setOnClickListener {
            val roomName = alertDialog.findViewById<TextInputEditText>(R.id.roomName)
            val roomNameStr = roomName.text.toString()
            if(roomNameStr.isEmpty()) {
                Toast.makeText(this, "채팅방 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(roomNameStr.length > 12) {
                Toast.makeText(this, "채팅방 이름은 12글자 미만으로 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else {
                getAccessToken { accessToken ->
                    if (accessToken.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = createChatRoom(accessToken, roomNameStr)
                            if (response.isSuccess) {
                                Log.d("ChatListFragment", response.toString())
                                val roomId = response.result
                                val chatRoom = ChatRoom(roomId!!, roomNameStr)
                                Log.d("ChatRoom", chatRoom.toString())
                                // 본인의 채팅방
                                FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).child(roomId!!).setValue(chatRoom)
                                // 초대 받은 사람을 위한 채팅방
                                FirebaseRef.chatRoom.child(uid).child(roomId!!).setValue(chatRoom)
                                val intent = Intent(this@UserDetailActivity, ChatRoomActivity::class.java)
                                intent.putExtra("chatRoomId", roomId)
                                intent.putExtra("chatRoomName", roomNameStr)
                                intent.putExtra("invitedUid", uid)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val addUserReq = AddUserReq(uid, roomId)
                                    val response = addUser(addUserReq)
                                    if (response.isSuccess) {
                                        val invitedNickName = response.result
                                        FirebaseRef.chatRoom.child(uid).child(roomId).setValue(chatRoom)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@UserDetailActivity, invitedNickName + "님과 채팅을 시작합니다", Toast.LENGTH_SHORT).show()
                                        }
                                        startActivity(intent)
                                    } else {
                                        val message = response.message
                                        Log.d("UserDetailActivity", message)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(this@UserDetailActivity, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                            else {
                                Log.d("ChatListFragment", "채팅방 생성 실패")
                                val message = response.message
                                Log.d("ChatListFragment", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@UserDetailActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("ChatListFragment", "Invalid Token")
                    }
                }
                alertDialog.dismiss()
            }
        }
    }

    private suspend fun addUser(addUserReq: AddUserReq) : BaseResponse<String> {
        return RetrofitInstance.chatApi.addUser(addUserReq)
    }
}