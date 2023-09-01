package com.chrome.chattingapp

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class MypageActivity : AppCompatActivity() {

    lateinit var profileImage : ImageView
    // Fragment로 대체하여 삭제 예정
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        profileImage = findViewById(R.id.profile)
        val profileBtn = findViewById<Button>(R.id.profileBtn)

        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImage.setImageURI(uri)
            }
        )

        profileBtn.setOnClickListener {
            getAction.launch("image/*")
        }
    }

    private fun uploadImage(uid: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference.child(uid + ".jpeg")
        profileImage.isDuplicateParentStateEnabled = true
        profileImage.buildDrawingCache()

        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()
        var uploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener{
        }.addOnSuccessListener { taskSnapshot ->
            Log.d("Image", Firebase.storage.reference.child(uid + ".jpeg").downloadUrl.toString())
        }
    }
}