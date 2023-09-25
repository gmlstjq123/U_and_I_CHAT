package com.chrome.chattingapp.friend

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R
import com.github.chrisbanes.photoview.PhotoView


class ProfileImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_image)

        // Intent로부터 데이터를 가져옴
        val imgUrl = intent.getStringExtra("imgUrl")

        val profileImageView = findViewById<PhotoView>(R.id.profileImage)
        if(imgUrl != null && imgUrl != "null") {
            Glide.with(this)
                .load(imgUrl)
                .into(profileImageView)
        }
    }
}