package com.chrome.chattingapp.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
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
import com.chrome.chattingapp.friend.MyProfileActivity
import com.chrome.chattingapp.friend.ProfileImageActivity
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatRoomActivity : AppCompatActivity() {

    lateinit var messageAdapter : MessageAdapter
    lateinit var recyclerView : RecyclerView
    lateinit var chatRoomId : String
    val messageList = mutableListOf<MessageModel>()
    var tokenList = listOf<String>() // 채팅 참여자의 토큰 목록
    val connectedUids = mutableMapOf<String, Boolean>() // 현재 채팅방에 입장한 유저의 UID 맵

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
        chatRoomId = roomId!!

        connectedUids[FirebaseAuthUtils.getUid()] = true
        FirebaseRef.connected.child(chatRoomId!!).push().setValue(connectedUids)

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserCount(chatRoomId!!)
            Log.d("userCount", response.toString())
            if (response.isSuccess) {
                val count = response.result.toString()
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

        getAccessToken { accessToken ->
            if (accessToken.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = getTokenList(accessToken, chatRoomId!!)
                    if (response.isSuccess) {
                        tokenList = response.result!!
                        Log.d("TokenList", response.toString())
                    } else {
                        Log.d("TokenList", "유저의 정보를 불러오지 못함")
                    }
                }
            } else {
                Log.e("TokenList", "Invalid Token")
            }
        }

        recyclerView = findViewById(R.id.messageRV)
        messageAdapter = MessageAdapter(this, messageList)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        messageAdapter.setProfileClickListener(object : MessageAdapter.OnItemClickListener {
            override fun onClick(view: View, position: Int) {
                val uid = messageList!![position].senderUid
                val imgUrl = messageList!![position].senderProfileUrl
                val nickName = messageList!![position].senderNickName

                var intent : Intent? = null
                if(uid == FirebaseAuthUtils.getUid()) {
                    intent = Intent(this@ChatRoomActivity, MyProfileActivity::class.java)
                    intent.putExtra("imgUrl", imgUrl)
                    intent.putExtra("nickName", nickName)
                }
                else {
                    intent = Intent(this@ChatRoomActivity, UserDetailActivity::class.java)
                    intent.putExtra("uid", uid)
                    intent.putExtra("imgUrl", imgUrl)
                    intent.putExtra("nickName", nickName)
                }
                startActivity(intent)
            }
        })

        messageAdapter.setImageClickListener(object : MessageAdapter.OnItemClickListener {
            override fun onClick(view: View, position: Int) {
                val imgUrl = messageList!![position].imageUrl
                val intent = Intent(this@ChatRoomActivity, ProfileImageActivity::class.java)
                intent.putExtra("imgUrl", imgUrl)
                startActivity(intent)
            }
        })

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
        val sendImageBtn = findViewById<ImageView>(R.id.send_image)

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

                        val connectedUidsRef = FirebaseRef.connected.child(chatRoomId!!)
                        val connectedUidsList: MutableList<String> = mutableListOf() // 현재 접속 중인 유저의 UID 목록

                        connectedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (childSnapshot in dataSnapshot.children) { // 난수 값
                                    val originalString = childSnapshot.value.toString()
                                    Log.d("originalString", originalString)
                                    // originalString이 {sRuRu1YVJMSj4csAUPOmaFfKfWJ2=true}와 같이 나오므로,
                                    // 앞에서 1글자, 뒤에서 6글자를 제거해 UID만 추출
                                    val modifiedString = originalString.substring(1, originalString.length - 6)
                                    Log.d("modifiedString", modifiedString)
                                    connectedUidsList.add(modifiedString)
                                    Log.d("connectedUidsList", connectedUidsList.toString())
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    val response = getUserUidList(chatRoomId!!) // 채팅방에 참여한 모든 유저의 UID 목록
                                    Log.d("userUidList", response.toString())
                                    if (response.isSuccess) {
                                        val uidList = response.result!!
                                        Log.d("userUidList", response.result.toString())
                                        Log.d("userUidList", uidList.toString())

                                        val difference = uidList.subtract(connectedUidsList) // 차집합 List
                                        Log.d("difference", connectedUidsList.toString())
                                        Log.d("difference", difference.toString())
                                        val deviceTokenList = mutableListOf<String>()

                                        val addList = difference.map { uid ->
                                            async(Dispatchers.IO) {
                                                val response = getDeviceTokenByUid(uid)
                                                if (response.isSuccess) {
                                                    deviceTokenList.add(response.result.toString())
                                                } else {
                                                    Log.d("ChatRoomActivity", "디바이스 토큰 정보를 불러오지 못함")
                                                }
                                            }
                                        }

                                        // 모든 작업이 완료될 때까지 대기
                                        addList.awaitAll()

                                        Log.d("deviceTokenList", deviceTokenList.toString())
                                        val noticeModel = NoticeModel(myNickName, contents)
                                        for (token in deviceTokenList) {
                                            val pushNotice = PushNotice(noticeModel, token)
                                            Log.d("Push", pushNotice.toString())
                                            Log.d("Push", tokenList.toString())
                                            createNotificationChannel()
                                            pushNotification(pushNotice)
                                        }
                                    } else {
                                        Log.d("userUidList", "유저의 정보를 불러오지 못함")
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // 처리 중 오류 발생 시 처리
                            }
                        })
                    } else {
                        Log.d("ChatRoomActivity", "유저의 정보를 불러오지 못함")
                    }
                }
                message.text?.clear()
            }
        }

        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    val response = uploadImage(uri)
                    if (response.isSuccess) {
                        val selectedImageUri = response.result!!
                        Log.d("selectedImageUri", selectedImageUri)
                        val sendTime = getSendTime()
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = getUserInfo(myUid)
                            if (response.isSuccess) {
                                myNickName = response.result?.nickName.toString()
                                myProfileUrl = response.result?.imgUrl.toString()
                                val readerUids = mutableMapOf<String, Boolean>()
                                Log.d("selectedImageUriChk", selectedImageUri)
                                messageModel = MessageModel(myUid, myNickName, myProfileUrl, "이미지",
                                    sendTime, readerUids, 0, selectedImageUri)
                                Log.d("readerUid", readerUids.size.toString())
                                FirebaseRef.message.child(chatRoomId!!).push().setValue(messageModel)

                                val connectedUidsRef = FirebaseRef.connected.child(chatRoomId!!)
                                val connectedUidsList: MutableList<String> = mutableListOf() // 현재 접속 중인 유저의 UID 목록

                                connectedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        for (childSnapshot in dataSnapshot.children) { // 난수 값
                                            val originalString = childSnapshot.value.toString()
                                            Log.d("originalString", originalString)
                                            // originalString이 {sRuRu1YVJMSj4csAUPOmaFfKfWJ2=true}와 같이 나오므로,
                                            // 앞에서 1글자, 뒤에서 6글자를 제거해 UID만 추출
                                            val modifiedString = originalString.substring(1, originalString.length - 6)
                                            Log.d("modifiedString", modifiedString)
                                            connectedUidsList.add(modifiedString)
                                            Log.d("connectedUidsList", connectedUidsList.toString())
                                        }

                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = getUserUidList(chatRoomId!!) // 채팅방에 참여한 모든 유저의 UID 목록
                                            Log.d("userUidList", response.toString())
                                            if (response.isSuccess) {
                                                val uidList = response.result!!
                                                Log.d("userUidList", response.result.toString())
                                                Log.d("userUidList", uidList.toString())

                                                val difference = uidList.subtract(connectedUidsList) // 차집합 List
                                                Log.d("difference", connectedUidsList.toString())
                                                Log.d("difference", difference.toString())
                                                val deviceTokenList = mutableListOf<String>()

                                                val addList = difference.map { uid ->
                                                    async(Dispatchers.IO) {
                                                        val response = getDeviceTokenByUid(uid)
                                                        if (response.isSuccess) {
                                                            deviceTokenList.add(response.result.toString())
                                                        } else {
                                                            Log.d("ChatRoomActivity", "디바이스 토큰 정보를 불러오지 못함")
                                                        }
                                                    }
                                                }

                                                // 모든 작업이 완료될 때까지 대기
                                                addList.awaitAll()

                                                Log.d("deviceTokenList", deviceTokenList.toString())
                                                val noticeModel = NoticeModel(myNickName, "이미지 파일")
                                                for (token in deviceTokenList) {
                                                    val pushNotice = PushNotice(noticeModel, token)
                                                    Log.d("Push", pushNotice.toString())
                                                    Log.d("Push", tokenList.toString())
                                                    createNotificationChannel()
                                                    pushNotification(pushNotice)
                                                }
                                            } else {
                                                Log.d("userUidList", "유저의 정보를 불러오지 못함")
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // 처리 중 오류 발생 시 처리
                                    }
                                })
                            } else {
                                Log.d("ChatRoomActivity", "유저의 정보를 불러오지 못함")
                            }
                        }
                        message.text?.clear()
                    } else {
                        Log.d("ChatRoomActivity", "이미지 정보를 불러오지 못함")
                    }
                }
            }
        )

        sendImageBtn.setOnClickListener {
            getAction.launch("image/*")
        }
    }

    override fun onPause() {
        super.onPause()
        val uid = FirebaseAuthUtils.getUid() // 현재 사용자의 UID
        connectedUids.remove(FirebaseAuthUtils.getUid())
        val connectedUidsRef = FirebaseRef.connected.child(chatRoomId)

        connectedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // 각 난수 키에 대한 데이터를 가져옵니다.
                    val randomKeyData = childSnapshot.child(uid)
                    if (randomKeyData.exists()) {
                        connectedUidsRef.child(childSnapshot.key!!).child(uid).removeValue()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 처리 중 오류 발생 시 처리
            }
        })
    }

    override fun onRestart() {
        super.onRestart()
        connectedUids[FirebaseAuthUtils.getUid()] = true
        FirebaseRef.connected.child(chatRoomId!!).push().setValue(connectedUids)
    }

    override fun onBackPressed() {
        val uid = FirebaseAuthUtils.getUid() // 현재 사용자의 UID
        connectedUids.remove(FirebaseAuthUtils.getUid())
        val connectedUidsRef = FirebaseRef.connected.child(chatRoomId)

        connectedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // 각 난수 키에 대한 데이터를 가져옵니다.
                    val randomKeyData = childSnapshot.child(uid)
                    if (randomKeyData.exists()) {
                        connectedUidsRef.child(childSnapshot.key!!).child(uid).removeValue()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private suspend fun getUserCount(roomId: String) : BaseResponse<String> {
        return RetrofitInstance.chatApi.getUserCount(roomId)
    }

    private suspend fun uploadImage(uri: Uri?) : BaseResponse<String> {
        val imagePath = getImagePathFromUri(uri!!)
        val imageFile = imagePath?.let { File(it) }
        Log.d("ProfileActivity", "path : " + imagePath.toString())
        Log.d("ProfileActivity", "file : " + imageFile.toString())

        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile?.name, requestFile)

        return RetrofitInstance.chatApi.uploadImage(imagePart)
    }

    private fun getImagePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val imagePath = cursor?.getString(columnIndex!!)
        cursor?.close()
        return imagePath
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    private suspend fun getDeviceTokenByUid(uid: String): BaseResponse<String> {
        return RetrofitInstance.userApi.getDeviceTokenByUid(uid)
    }

    //메시지 보낸 시각 정보 반환
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getSendTime(): String {
        try {
            val localDateTime = LocalDateTime.now()
            val dateTimeFormatter = DateTimeFormatter.ofPattern("M/d  a h:mm")
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
                            CoroutineScope(Dispatchers.IO).launch {
                                val response = getUserInfo(messageModel.senderUid)
                                if (response.isSuccess) {
                                    if(response.result!!.imgUrl != null) {
                                        FirebaseRef.message.child(chatRoomId!!).child(messageId).child("senderProfileUrl")
                                            .setValue(response.result!!.imgUrl)
                                    }
                                    else {
                                        FirebaseRef.message.child(chatRoomId!!).child(messageId).child("senderProfileUrl")
                                            .setValue("null")
                                    }
                                    FirebaseRef.message.child(chatRoomId!!).child(messageId).child("senderNickName")
                                        .setValue(response.result!!.nickName)
                                } else {
                                    Log.d("NickNameActivity", "유저의 정보를 불러오지 못함")
                                }
                            }
                            // connectedUids로 업데이트
                            FirebaseRef.message.child(chatRoomId!!).child(messageId).child("readerUids")
                                .updateChildren(connectedUids as Map<String, Boolean>)
                                .addOnCompleteListener { readerUidTask ->
                                    if (readerUidTask.isSuccessful) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val response = getUserUidList(chatRoomId!!)
                                            Log.d("userUidList", response.toString())
                                            if (response.isSuccess) {
                                                val newUidList = response.result!!
                                                Log.d("newUidList", response.result.toString())
                                                Log.d("newUidList", newUidList.toString())

                                                val keysList: List<String> = ArrayList<String>(messageModel.readerUids.keys)
                                                Log.d("keysList", keysList.toString())
                                                val intersection = newUidList.intersect(keysList)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    val response = getUserCount(chatRoomId!!)
                                                    Log.d("userCount", response.toString())
                                                    if (response.isSuccess) {
                                                        val count = response.result.toString()
                                                        val unreadUserCount = count.toInt() - intersection.size
                                                        Log.d("unreadUserCount", "1. " + count)
                                                        Log.d("unreadUserCount", "2. " + intersection.size.toString())
                                                        FirebaseRef.message.child(chatRoomId!!).child(messageId).child("unreadUserCount")
                                                            .setValue(unreadUserCount)
                                                    } else {
                                                        Log.d("UserListFragment", "유저의 정보를 불러오지 못함")
                                                    }
                                                }
                                            } else {
                                                Log.d("newUidList", "유저의 정보를 불러오지 못함")
                                            }
                                        }
                                    }

                                    else {
                                        Log.d("ChatRoomActivity", "reader UID를 업데이트하지 못함")
                                    }
                                }
                        }

                        if (messageModel.senderUid != FirebaseAuthUtils.getUid() && messageModel.imageUrl == "null") {
                            messageModel.viewType = MessageModel.VIEW_TYPE_YOU
                        }
                        else if (messageModel.senderUid == FirebaseAuthUtils.getUid() && messageModel.imageUrl != "null") {
                            messageModel.viewType = MessageModel.VIEW_TYPE_ME_IMAGE
                        }
                        else if(messageModel.senderUid != FirebaseAuthUtils.getUid() && messageModel.imageUrl != "null") {
                            messageModel.viewType = MessageModel.VIEW_TYPE_YOU_IMAGE
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

    private suspend fun getTokenList(accessToken : String, roomId: String): BaseResponse<List<String>> {
        return RetrofitInstance.chatApi.getTokenList(accessToken, roomId)
    }

    private suspend fun getUserUidList(roomId: String): BaseResponse<List<String>> {
        return RetrofitInstance.chatApi.getUserUidList(roomId)
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

    private fun getAccessToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val accessToken = data?.accessToken ?: ""
                callback(accessToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ChatRoomActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
    }
}