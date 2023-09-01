package com.chrome.chattingapp.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthUtils {
    companion object {
        private lateinit var auth : FirebaseAuth

        fun getUid() : String {
            auth = Firebase.auth
            return auth.currentUser?.uid.toString()
        }
    }
}