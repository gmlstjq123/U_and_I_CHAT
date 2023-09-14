package com.chrome.chattingapp.friend

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R
import com.chrome.chattingapp.mypage.ProfileActivity

class MyProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        // Intent로부터 데이터를 가져옴
        val imgUrl = intent.getStringExtra("imgUrl")
        val nickName = intent.getStringExtra("nickName")

        val profileImageView = findViewById<ImageView>(R.id.myProfileDetail)
        val nickNameTextView = findViewById<TextView>(R.id.myNickNameDetail)
        val toMyPageBtn = findViewById<ImageView>(R.id.modifyProfile)

        if(imgUrl != null) {
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

        toMyPageBtn.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}