package com.chrome.chattingapp.chat

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chrome.chattingapp.ListViewAdapter
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatRoomActivity : AppCompatActivity() {

    lateinit var count : String
    lateinit var messageAdapter : MessageAdapter
    lateinit var recyclerView : RecyclerView
    val messageList = mutableListOf<MessageModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val roomName = findViewById<TextView>(R.id.chatRoomName)
        val nickNameList = findViewById<TextView>(R.id.nickNameList)
        val inviteBtn = findViewById<Button>(R.id.invite)
        val userCount = findViewById<TextView>(R.id.userCount)

        // Intent로부터 데이터를 가져옴
        val chatRoomName = intent.getStringExtra("chatRoomName")
        roomName.text = chatRoomName

        val roomId = intent.getStringExtra("chatRoomId")
        val chatRoomId = roomId

        val userList = intent.getStringExtra("userList")
        nickNameList.text = userList

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserCount(chatRoomId!!)
            Log.d("userCount", response.toString())
            if (response.isSuccess) {
                count = response.result.toString()
                userCount.text = count
            } else {
                Log.d("UserListFragment", "유저의 정보를 불러오지 못함")
            }
        }

        recyclerView = findViewById<RecyclerView>(R.id.messageRV)
        messageAdapter = MessageAdapter(this, messageList)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        getMessageList(chatRoomId!!)
        Log.d("MessageList", messageList.toString())

        inviteBtn.setOnClickListener {
            val intent = Intent(this, InviteActivity::class.java)
            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("chatRoomName", chatRoomName)
            intent.putExtra("nickNameList", userList)
            startActivity(intent)
        }

        val participantBtn = findViewById<ImageView>(R.id.participants)
        participantBtn.setOnClickListener {
            val intent = Intent(this, ParticipantsActivity::class.java)
            intent.putExtra("chatRoomId", chatRoomId)
            startActivity(intent)
        }

        val message = findViewById<TextInputEditText>(R.id.message)
        val sendBtn = findViewById<ImageView>(R.id.send)

        lateinit var myNickName : String
        lateinit var myProfileUrl : String
        lateinit var messageModel: MessageModel
        val myUid = FirebaseAuthUtils.getUid()

        sendBtn.setOnClickListener {
            val contents = message.text.toString()
            val sendTime = getSendTime()
            CoroutineScope(Dispatchers.IO).launch {
                val response = getUserInfo(myUid)
                if (response.isSuccess) {
                    myNickName = response.result?.nickName.toString()
                    myProfileUrl = response.result?.imgUrl.toString()
                    messageModel = MessageModel(myUid, myNickName, myProfileUrl, contents, sendTime)
                    FirebaseRef.message.child(chatRoomId!!).push().setValue(messageModel)
                } else {
                    Log.d("ChatRoomActivity", "유저의 정보를 불러오지 못함")
                }
            }
            message.text?.clear()
        }
    }

    private suspend fun getUserCount(roomId: String) : BaseResponse<String> {
        return RetrofitInstance.chatApi.getUserCount(roomId)
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    //메시지 보낸 시각 정보 반환
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSendTime(): String {
        try {
            val localDateTime = LocalDateTime.now()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("M/d  h:mm  a")
            return localDateTime.format(dateTimeFormatter)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("시간 정보를 불러오지 못함")
        }
    }

    private fun getMessageList(chatRoomId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()
                val newMessages = mutableListOf<MessageModel>() // 새로운 메시지를 저장할 리스트 생성
                for (datamModel in dataSnapshot.children) {
                    val messageModel = datamModel.getValue(MessageModel::class.java)
                    if(messageModel!!.senderUid != FirebaseAuthUtils.getUid()) {
                        messageModel.viewType = MessageModel.VIEW_TYPE_YOU
                    }
                    newMessages.add(messageModel!!)
                }
                messageList.addAll(newMessages)
                messageAdapter.notifyDataSetChanged()
                Log.d("MessageList", messageList.toString())
                recyclerView.post {
                    recyclerView.scrollToPosition(recyclerView.adapter?.itemCount?.minus(1) ?: 0)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MyMessage", "onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.message.child(chatRoomId).addValueEventListener(postListener)
    }
}