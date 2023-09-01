package com.chrome.chattingapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.findNavController
import com.chrome.chattingapp.mypage.NickNameActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyPageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyPageFragment : Fragment() {

    lateinit var profileImage : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_page, container, false)

        val nickName = view.findViewById<Button>(R.id.nickNameBtn)
        nickName.setOnClickListener {
            val intent = Intent(requireActivity(), NickNameActivity::class.java)
            startActivity(intent)
        }

        val freind = view.findViewById<ImageView>(R.id.freind)
        freind.setOnClickListener {
            it.findNavController().navigate(R.id.action_myPageFragment_to_userListFragment)
        }

        val chat = view.findViewById<ImageView>(R.id.chat)
        chat.setOnClickListener {
            it.findNavController().navigate(R.id.action_myPageFragment_to_chatListFragment)
        }

        //        profileImage = view.findViewById<ImageView>(R.id.profile)
//
//        val profileBtn = view.findViewById<Button>(R.id.profileBtn)
//
//        val getAction = registerForActivityResult(
//            ActivityResultContracts.GetContent(),
//            ActivityResultCallback { uri ->
//                profileImage.setImageURI(uri)
//            }
//        )
//
//        profileBtn.setOnClickListener {
//            getAction.launch("image/*")
//        }
        return view
    }

//    private fun uploadImage(uid: String) {
//        val storage = Firebase.storage
//        val storageRef = storage.reference.child(uid + ".jpeg")
//        profileImage.isDuplicateParentStateEnabled = true
//        profileImage.buildDrawingCache()
//
//        val bitmap = (profileImage.drawable as BitmapDrawable).bitmap
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//
//        val data = baos.toByteArray()
//        var uploadTask = storageRef.putBytes(data)
//        uploadTask.addOnFailureListener{
//        }.addOnSuccessListener { taskSnapshot ->
//            Log.d("Image", Firebase.storage.reference.child(uid + ".jpeg").downloadUrl.toString())
//        }
//    }
}