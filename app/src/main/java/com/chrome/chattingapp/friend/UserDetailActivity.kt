package com.chrome.chattingapp.friend

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R

class UserDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)

        // Intent로부터 데이터를 가져옴
        val imgUrl = intent.getStringExtra("imgUrl")
        val nickName = intent.getStringExtra("nickName")

        val profileImageView = findViewById<ImageView>(R.id.profileDetail)
        val nickNameTextView = findViewById<TextView>(R.id.nickNameDetail)

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
    }
}