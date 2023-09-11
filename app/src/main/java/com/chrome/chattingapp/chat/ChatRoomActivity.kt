package com.chrome.chattingapp.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chrome.chattingapp.ListViewAdapter
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.friend.UserDetailActivity
import com.chrome.chattingapp.push.NoticeModel
import com.chrome.chattingapp.push.PushNotice
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatRoomActivity : AppCompatActivity() {

    lateinit var count : String // 채팅방 참여 인원수
    lateinit var messageAdapter : MessageAdapter
    lateinit var recyclerView : RecyclerView
    val messageList = mutableListOf<MessageModel>()
    var tokenList = listOf<String>() // 채팅 참여자의 토큰 목록
    val readerUidMap = mutableMapOf<String, Boolean>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        readerUidMap[FirebaseAuthUtils.getUid()] = true

        val roomName = findViewById<TextView>(R.id.chatRoomName)
        val nickNameList = findViewById<TextView>(R.id.nickNameList)
        val inviteBtn = findViewById<Button>(R.id.invite)
        val userCount = findViewById<TextView>(R.id.userCount)

        // Intent로부터 데이터를 가져옴
        val chatRoomName = intent.getStringExtra("chatRoomName")
        roomName.text = chatRoomName

        val roomId = intent.getStringExtra("chatRoomId")
        val chatRoomId = roomId

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

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserStrList(chatRoomId!!)
            Log.d("UserNickNameList", response.toString())
            if (response.isSuccess) {
                nickNameList.text = response.result.toString()
            } else {
                Log.d("UserNickNameList", "유저의 정보를 불러오지 못함")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val response = getTokenList(chatRoomId!!)
            if (response.isSuccess) {
                tokenList = response.result!!
                Log.d("TokenList", response.toString())
            } else {
                Log.d("UserNickNameList", "유저의 정보를 불러오지 못함")
            }
        }

        recyclerView = findViewById(R.id.messageRV)
        messageAdapter = MessageAdapter(this, messageList)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        getMessageList(chatRoomId!!)
        Log.d("MessageList", messageList.toString())

        inviteBtn.setOnClickListener {
            val intent = Intent(this, InviteActivity::class.java)
            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("chatRoomName", chatRoomName)
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
            if(contents.isEmpty()) {
                Toast.makeText(this, "메시지를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else {
                val sendTime = getSendTime()
                CoroutineScope(Dispatchers.IO).launch {
                    val response = getUserInfo(myUid)
                    if (response.isSuccess) {
                        myNickName = response.result?.nickName.toString()
                        myProfileUrl = response.result?.imgUrl.toString()
                        val readerUids = mutableMapOf<String, Boolean>()
                        messageModel = MessageModel(myUid, myNickName, myProfileUrl, contents,
                            sendTime, readerUids, 0)
                        Log.d("readerUid", readerUids.size.toString())
                        FirebaseRef.message.child(chatRoomId!!).push().setValue(messageModel)
                        val noticeModel = NoticeModel(myNickName, contents)
                        for(token in tokenList) { // 채팅방의 모든 유저에게 채팅 푸시알림을 전송
                            val pushNotice = PushNotice(noticeModel, token)
                            Log.d("Push", pushNotice.toString())
                            Log.d("Push", tokenList.toString())
                            createNotificationChannel()
                            pushNotification(pushNotice)
                        }
                    } else {
                        Log.d("ChatRoomActivity", "유저의 정보를 불러오지 못함")
                    }
                }
                message.text?.clear()
            }
        }
    }

    override fun onBackPressed() {
        readerUidMap.remove(FirebaseAuthUtils.getUid())
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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

    private fun getMessageList(chatRoomId: String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageList.clear()
                val newMessages = mutableListOf<MessageModel>() // 새로운 메시지를 저장할 리스트 생성

                for (dataModel in dataSnapshot.children) {
                    val messageModel = dataModel.getValue(MessageModel::class.java)
                    val messageId = dataModel.key
                    if (messageModel != null) {
                        if (messageId != null) {
                            // readerUidMap을 업데이트
                            FirebaseRef.message.child(chatRoomId!!).child(messageId).child("readerUids")
                                .updateChildren(readerUidMap as Map<String, Boolean>)
                                .addOnCompleteListener { readerUidTask ->
                                    if (readerUidTask.isSuccessful) {
                                        // readerUid 업데이트가 성공한 경우 unreadUserCount를 계산하여 업데이트합니다.
                                        val unreadUserCount = count.toInt() - messageModel.readerUids.size
                                        FirebaseRef.message.child(chatRoomId!!).child(messageId).child("unreadUserCount")
                                            .setValue(unreadUserCount)
                                    } else {
                                        Log.d("ChatRoomActivity", "reader UID를 업데이트하지 못함")
                                    }
                                }
                        }

                        if (messageModel.senderUid != FirebaseAuthUtils.getUid()) {
                            messageModel.viewType = MessageModel.VIEW_TYPE_YOU
                            if(!readerUidMap.containsKey(FirebaseAuthUtils.getUid())) {

                            }
                        }
                        newMessages.add(messageModel)
                    }
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

    private suspend fun getUserStrList(roomId: String): BaseResponse<String> {
        return RetrofitInstance.chatApi.getUserStrList(roomId)
    }

    private suspend fun getTokenList(roomId: String): BaseResponse<List<String>> {
        return RetrofitInstance.chatApi.getTokenList(roomId)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("test", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun pushNotification(notification: PushNotice) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.noticeApi.postNotification(notification)
    }
}