package com.chrome.chattingapp.mypage

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.chrome.chattingapp.MainActivity
import com.chrome.chattingapp.R
import com.chrome.chattingapp.api.BaseResponse
import com.chrome.chattingapp.api.RetrofitInstance
import com.chrome.chattingapp.api.dto.GetUserRes
import com.chrome.chattingapp.api.dto.PatchPasswordReq
import com.chrome.chattingapp.utils.FirebaseAuthUtils
import com.chrome.chattingapp.utils.FirebaseRef
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    lateinit var profileUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val newProfileBtn = findViewById<Button>(R.id.newProfileBtn)
        val profileImage = findViewById<ImageView>(R.id.profile)
        val noProfileBtn = findViewById<Button>(R.id.noProfileBtn)

        CoroutineScope(Dispatchers.IO).launch {
            val response = getUserInfo(FirebaseAuthUtils.getUid())
            if (response.isSuccess) {
                if(response.result?.imgUrl != null) {
                    this@ProfileActivity.profileUrl = response.result?.imgUrl
                    val profileUri = Uri.parse(profileUrl)
                    runOnUiThread {
                        Glide.with(this@ProfileActivity)
                            .load(profileUri)
                            .into(profileImage)
                    }
                }

            } else {
                Log.d("ProfileActivity", "유저의 정보를 불러오지 못함")
            }
        }

        val getAction = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                profileImage.setImageURI(uri)
                selectedImageUri = uri
                Log.d("imageUri", selectedImageUri.toString())
            }
        )

        profileImage.setOnClickListener {
            getAction.launch("image/*")
        }

        noProfileBtn.setOnClickListener {
            getAccessToken { accessToken ->
                if (accessToken.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val response = modifyNoProfile(accessToken)
                        if (response.isSuccess) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ProfileActivity, "기본 프로필이 적용됩니다", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            Log.d("ProfileActivity", "프로필 변경 실패")
                            val message = response.message
                            Log.d("ProfileActivity", message)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("ProfileActivity", "Invalid Token")
                }
            }
        }

        newProfileBtn.setOnClickListener {
            if (selectedImageUri != null) {
                getAccessToken { accessToken ->
                    if (accessToken.isNotEmpty()) {
                        Log.d("ProfileActivity", selectedImageUri.toString())
                        CoroutineScope(Dispatchers.IO).launch {
                            val response = modifyProfile(accessToken, selectedImageUri)
                            Log.d("ProfileActivity", response.result.toString())
                            if (response.isSuccess) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@ProfileActivity, "프로필 변경이 완료되었습니다", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                Log.d("ProfileActivity", "프로필 변경 실패")
                                val message = response.message
                                Log.d("ProfileActivity", message)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("ProfileActivity", "Invalid Token")
                    }
                }
            } else {
                Toast.makeText(this@ProfileActivity, "새로운 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getUserInfo(uid: String): BaseResponse<GetUserRes> {
        return RetrofitInstance.myPageApi.getUserInfo(uid)
    }

    private suspend fun modifyProfile(accessToken : String, uri: Uri?): BaseResponse<String> {
        // Create a RequestBody from the image file
        val imagePath = getImagePathFromUri(uri!!)
        val imageFile = imagePath?.let { File(it) }
        Log.d("ProfileActivity", "path : " + imagePath.toString())
        Log.d("ProfileActivity", "file : " + imageFile.toString())

        val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile!!)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile?.name, requestFile)

        return RetrofitInstance.myPageApi.modifyProfile(accessToken, imagePart)
    }

    private suspend fun modifyNoProfile(accessToken : String) : BaseResponse<String> {
        return RetrofitInstance.myPageApi.modifyNoProfile(accessToken)
    }

    private fun getAccessToken(callback: (String) -> Unit) {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val data = dataSnapshot.getValue(com.chrome.chattingapp.authentication.UserInfo::class.java)
                val accessToken = data?.accessToken ?: ""
                callback(accessToken)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("ProfileActivity", "onCancelled", databaseError.toException())
            }
        }

        FirebaseRef.userInfo.child(FirebaseAuthUtils.getUid()).addListenerForSingleValueEvent(postListener)
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
}