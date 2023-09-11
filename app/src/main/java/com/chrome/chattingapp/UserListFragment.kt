package com.chrome.chattingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.UserProfile
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

class UserListFragment : Fragment() {

    // userProfileList를 nullable로 선언
    private var userProfileList: List<UserProfile>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
        lateinit var nickname: String
        val myProfile = view.findViewById<ImageView>(R.id.profileArea)
        val myNickName = view.findViewById<TextView>(R.id.nickNameArea)

        // 내 프로필
        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserInfo(FirebaseAuthUtils.getUid())
            if (response.isSuccess) {
                nickname = response.result?.nickName.toString()
                withContext(Dispatchers.Main) {
                    myNickName.text = nickname
                }
                if (response.result?.imgUrl != null) {
                    val profileUrl = response.result?.imgUrl
                    val profileUri = Uri.parse(profileUrl)
                    withContext(Dispatchers.Main) {
                        Glide.with(requireActivity())
                            .load(profileUri)
                            .into(myProfile)
                    }
                }
            } else {
                Log.d("UserListFragment", "유저의 정보를 불러오지 못함")
            }
        }

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
                            val adapter = ListViewAdapter(requireContext(), userProfileList ?: emptyList())
                            val listview = view.findViewById<ListView>(R.id.freindListView)
                            listview.adapter = adapter
                            adapter.notifyDataSetChanged()
                            Log.d("UserProfileList", userProfileList.toString())
                            listview.setOnItemClickListener { parent, view, position, id ->
                                val imgUrl = userProfileList!![position].imgUrl
                                val nickName = userProfileList!![position].nickName
                                val intent = Intent(requireActivity(), UserDetailActivity::class.java)
                                intent.putExtra("imgUrl", imgUrl)
                                intent.putExtra("nickName", nickName)
                                startActivity(intent)
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

        val chat = view.findViewById<ImageView>(R.id.chat)
        chat.setOnClickListener {
            it.findNavController().navigate(R.id.action_userListFragment_to_chatListFragment)
        }

        val mypage = view.findViewById<ImageView>(R.id.mypage)
        mypage.setOnClickListener {
            it.findNavController().navigate(R.id.action_userListFragment_to_myPageFragment)
        }
        return view
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    private suspend fun getUsers(accessToken: String): BaseResponse<List<UserProfile>> {
        return RetrofitInstance.userApi.getUsers(accessToken)
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

