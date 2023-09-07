package com.chrome.chattingapp.chat

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageAdapter(private val context: Context, val items: MutableList<MessageModel>)
    : RecyclerView.Adapter<MessageAdapter.ItemViewHolder>() {
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val contents : TextView = view.findViewById(R.id.messageContentsArea)
        val dateTime : TextView = view.findViewById(R.id.dateTime)
        val profile : ImageView = view.findViewById(R.id.messageProfileArea)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_recycler_view_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.contents.text = item.contents
        holder.dateTime.text = item.sendTime

        if (item.senderProfileUrl != "null") {
            Glide.with(context)
                .load(item.senderProfileUrl)
                .into(holder.profile)
        } else {
            holder.profile.setImageResource(R.drawable.profile)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}