package com.chrome.chattingapp.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.chrome.chattingapp.R

class ChatRoomAdapter(private val context: Context, private val dataList : List<ChatRoom>) : BaseAdapter() {
    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.chat_listview_item, parent, false)
        }
        val listViewChatRoomName = convertView?.findViewById<TextView>(R.id.lvChatRoomName)
        val listViewUserList = convertView?.findViewById<TextView>(R.id.lvUserListArea)

        listViewChatRoomName!!.text = dataList[position].roomName
        listViewUserList!!.text = dataList[position].userList

        return convertView!!
    }

}