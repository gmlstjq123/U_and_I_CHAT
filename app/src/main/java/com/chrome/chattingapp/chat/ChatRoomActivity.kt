package com.chrome.chattingapp.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.chrome.chattingapp.R

class ChatRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val roomName = findViewById<TextView>(R.id.chatRoomName)
        val nickNameList = findViewById<TextView>(R.id.nickNameList)
        val userCount = findViewById<TextView>(R.id.userCount)
        val sendBtn = findViewById<ImageView>(R.id.send)

        // Intent로부터 데이터를 가져옴
        roomName.text = intent.getStringExtra("chatRoomName")
        nickNameList.text = intent.getStringExtra("userList")
        val chatRoomId = intent.getStringExtra("chatRoomId")
    }
}