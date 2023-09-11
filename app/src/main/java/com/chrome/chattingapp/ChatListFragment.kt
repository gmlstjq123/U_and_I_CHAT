package com.chrome.chattingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.navigation.findNavController
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.chat.ChatRoomAdapter
import com.chrome.chattingapp.chat.ChatRoom
import com.chrome.chattingapp.chat.ChatRoomActivity
import com.chrome.chattingapp.chat.MessageModel
import com.chrome.chattingapp.friend.UserDetailActivity
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : Fragment() {

    lateinit var listViewAdapter : ChatRoomAdapter
    lateinit var nickName : String
    val chatRoomList = mutableListOf<ChatRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat_list, container, false)

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserInfo(FirebaseAuthUtils.getUid())
            if (response.isSuccess) {
                nickName = response.result?.nickName.toString()
            } else {
                Log.d("ChatListFragment", "유저의 정보를 불러오지 못함")
            }
        }

        val listView = view.findViewById<ListView>(R.id.LVChatRoom)
        listViewAdapter = ChatRoomAdapter(requireActivity(), chatRoomList)
        listView.adapter = listViewAdapter

        getChatRoomList()

        listView.setOnItemClickListener { parent, view, position, id ->
            val chatRoomId = chatRoomList!![position].chatRoomId
            val chatRoomName = chatRoomList!![position].roomName
            val intent = Intent(requireActivity(), ChatRoomActivity::class.java)

            intent.putExtra("chatRoomId", chatRoomId)
            intent.putExtra("chatRoomName", chatRoomName)
            startActivity(intent)
        }

        val plusBtn = view.findViewById<ImageView>(R.id.plus)
        plusBtn.setOnClickListener {
            showDialog()
        }

        val freind = view.findViewById<ImageView>(R.id.freind)
        freind.setOnClickListener {
            it.findNavController().navigate(R.id.action_chatListFragment_to_userListFragment)
        }

        val mypage = view.findViewById<ImageView>(R.id.mypage)
        mypage.setOnClickListener {
            it.findNavController().navigate(R.id.action_chatListFragment_to_myPageFragment)
        }
        return view
    }

    private fun showDialog() {
        val dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.chat_room_dialog, null)
        val builder = AlertDialog.Builder(requireActivity())
            .setView(dialogView)
            .setTitle("채팅방 생성하기")
        val alertDialog = builder.show()

        val createBtn = alertDialog.findViewById<Button>(R.id.create)
        createBtn.setOnClickListener {
            val roomName = alertDialog.findViewById<TextInputEditText>(R.id.roomName)
            val roomNameStr = roomName.text.toString()
            if(roomNameStr.isEmpty()) {
                Toast.makeText(requireActivity(), "채팅방 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
            else if(roomNameStr.length > 12) {
                Toast.makeText(requireActivity(), "채팅방 이름은 12글자 미만으로 입력해주세요", Toast.LENGTH_SHORT).show()
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
                                FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).child(roomId!!).setValue(chatRoom)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireActivity(), "채팅방이 생성되었습니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                Log.d("ChatListFragment", "채팅방 생성 실패")
                                val message = response.message
                                Log.d("ChatListFragment", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
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

    private suspend fun createChatRoom(accessToken : String, roomName : String): BaseResponse<String> {
        return RetrofitInstance.chatApi.createChatRoom(accessToken, roomName)
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

    private fun getChatRoomList() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatRoomList.clear()
                for (dataModel in dataSnapshot.children) {
                    val chatRoom = dataModel.getValue(ChatRoom::class.java)
                    if(chatRoom != null) {
                        chatRoomList.add(chatRoom)
                        getUnreadMessageCount(chatRoom.chatRoomId!!)
                    }
                }

                listViewAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databseError: DatabaseError) {
                Log.w("MyMessage", "onCancelled", databseError.toException())
            }
        }
        FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).addValueEventListener(postListener)
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    private fun getUnreadMessageCount(chatRoomId : String) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0
                var lastMessage = ""
                for (datamModel in dataSnapshot.children) {
                    val uidList = datamModel.child("readerUids").getValue(object : GenericTypeIndicator<MutableMap<String, Boolean>>() {})

                    if (uidList != null) {
                        Log.d("readerUids", uidList.toString())
                        if (!uidList.containsKey(FirebaseAuthUtils.getUid())) {
                            // readerUid에 내 uid가 없으면
                            count++
                        }
                    }
                    val lastDataModel = datamModel.getValue(MessageModel::class.java)
                    if(lastDataModel != null) {
                        lastMessage = lastDataModel.contents
                    }
                }
                FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).child(chatRoomId).child("unreadCount").setValue(count)
                FirebaseRef.chatRoom.child(FirebaseAuthUtils.getUid()).child(chatRoomId).child("lastMessage").setValue(lastMessage)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MyMessage", "onCancelled", databaseError.toException())
            }
        }
        FirebaseRef.message.child(chatRoomId).addValueEventListener(postListener)
    }
}