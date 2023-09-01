package com.chrome.chattingapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserListFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

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
}