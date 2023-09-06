package com.chrome.chattingapp.friend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R

class ProfileImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_image)

        // Intent로부터 데이터를 가져옴
        val imgUrl = intent.getStringExtra("imgUrl")

        val profileImageView = findViewById<ImageView>(R.id.profileImage)
        if(imgUrl != null) {
            Glide.with(this)
                .load(imgUrl)
                .into(profileImageView)
        }
    }
}