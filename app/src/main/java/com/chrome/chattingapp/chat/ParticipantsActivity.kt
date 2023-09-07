package com.chrome.chattingapp.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.chrome.chattingapp.ListViewAdapter
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.UserProfile
import com.chrome.chattingapp.friend.UserDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParticipantsActivity : AppCompatActivity() {

    private var participantsProfileList : List<UserProfile>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_participants)

        val chatRoomId = intent.getStringExtra("chatRoomId")

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserList(chatRoomId!!)
            if (response.isSuccess) {
                participantsProfileList = response.result
                Log.d("participantsProfileList", participantsProfileList.toString())
                withContext(Dispatchers.Main) {
                    val adapter = ListViewAdapter(this@ParticipantsActivity, participantsProfileList ?: emptyList())
                    val listview = findViewById<ListView>(R.id.participantListView)
                    listview.adapter = adapter
                    adapter.notifyDataSetChanged()

                    listview.setOnItemClickListener { parent, view, position, id ->
                        val imgUrl = participantsProfileList!![position].imgUrl
                        val nickName = participantsProfileList!![position].nickName
                        val intent = Intent(this@ParticipantsActivity, UserDetailActivity::class.java)
                        intent.putExtra("imgUrl", imgUrl)
                        intent.putExtra("nickName", nickName)
                        startActivity(intent)
                    }
                }
            } else {
                participantsProfileList = emptyList() // 초기화 실패 시 빈 리스트로 설정
                Log.d("ParticipantActivity", "데이터 불러오기 실패")
                val message = response.message
                Log.d("ParticipantActivity", message)
            }
        }
    }

    private suspend fun getUserList(roomId: String): BaseResponse<List<UserProfile>> {
        return RetrofitInstance.chatApi.getUserList(roomId)
    }
}