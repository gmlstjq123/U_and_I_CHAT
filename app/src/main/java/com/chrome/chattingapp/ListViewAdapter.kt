package com.chrome.chattingapp

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.chrome.chattingapp.api.dto.UserProfile

class ListViewAdapter(private val context: Context, private val dataList : List<UserProfile>) : BaseAdapter() {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, parent, false)
        }
        val listViewProfile = convertView?.findViewById<ImageView>(R.id.listProfileArea)
        val listViewText = convertView?.findViewById<TextView>(R.id.listNickNameArea)
        val imgUrl = dataList[position].imgUrl
        if(imgUrl != "null") {
            val imgUri = Uri.parse(imgUrl)
            Glide.with(context)
                .load(imgUri)
                .into(listViewProfile!!)
        } else {
            listViewProfile!!.setImageResource(R.drawable.profile)
        }

        listViewText!!.text=dataList[position].nickName
        return convertView!!
    }

}