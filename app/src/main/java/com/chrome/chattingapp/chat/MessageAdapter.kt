package com.chrome.chattingapp.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chrome.chattingapp.R

class MessageAdapter(private val context: Context, val items: MutableList<MessageModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder1(private val view: View) : RecyclerView.ViewHolder(view) {
        val contents : TextView = view.findViewById(R.id.messageContentsArea)
        val dateTime : TextView = view.findViewById(R.id.dateTime)
        val nickName : TextView = view.findViewById(R.id.messageNickName)
        val profile : ImageView = view.findViewById(R.id.messageProfileArea)
        val unreadUserContainer : FrameLayout = view.findViewById(R.id.unreadUserCountContainer)
        val unreadUserCount : TextView = view.findViewById(R.id.unreadUserCountTextView)
    }

    class ItemViewHolder2(private val view: View) : RecyclerView.ViewHolder(view) {
        val contents : TextView = view.findViewById(R.id.messageContentsArea2)
        val dateTime : TextView = view.findViewById(R.id.dateTime2)
        val nickName : TextView = view.findViewById(R.id.messageNickName2)
        val profile : ImageView = view.findViewById(R.id.messageProfileArea2)
        val unreadUserContainer : FrameLayout = view.findViewById(R.id.unreadUserCountContainer2)
        val unreadUserCount : TextView = view.findViewById(R.id.unreadUserCountTextView2)
    }

    class ItemViewHolder3(private val view: View) : RecyclerView.ViewHolder(view) {
        val image : ImageView = view.findViewById(R.id.imageContentsArea)
        val dateTime : TextView = view.findViewById(R.id.imageDateTime)
        val nickName : TextView = view.findViewById(R.id.imageNickName)
        val profile : ImageView = view.findViewById(R.id.imageProfileArea)
        val unreadUserContainer : FrameLayout = view.findViewById(R.id.imageUnreadUserCountContainer)
        val unreadUserCount : TextView = view.findViewById(R.id.imageUnreadUserCountTextView)
    }

    class ItemViewHolder4(private val view: View) : RecyclerView.ViewHolder(view) {
        val image : ImageView = view.findViewById(R.id.imageContentsArea2)
        val dateTime : TextView = view.findViewById(R.id.imageDateTime2)
        val nickName : TextView = view.findViewById(R.id.imageNickName2)
        val profile : ImageView = view.findViewById(R.id.imageProfileArea2)
        val unreadUserContainer : FrameLayout = view.findViewById(R.id.imageUnreadUserCountContainer2)
        val unreadUserCount : TextView = view.findViewById(R.id.imageUnreadUserCountTextView2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterLayout : View?
        return when(viewType) {
            MessageModel.VIEW_TYPE_ME -> {
                adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.message_recycler_view_item, parent, false)
                ItemViewHolder1(adapterLayout)
            }
            MessageModel.VIEW_TYPE_YOU -> {
                adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.message_recycler_view_item2, parent, false)
                ItemViewHolder2(adapterLayout)
            }
            MessageModel.VIEW_TYPE_ME_IMAGE -> {
                adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.image_recycler_view_item, parent, false)
                ItemViewHolder3(adapterLayout)
            }
            MessageModel.VIEW_TYPE_YOU_IMAGE -> {
                adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.image_recycler_view_item2, parent, false)
                ItemViewHolder4(adapterLayout)
            }
            else -> throw RuntimeException("Invalid View Type Error")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (item.viewType) {
            MessageModel.VIEW_TYPE_ME -> {
                (holder as ItemViewHolder1).contents.text = item.contents
                holder.dateTime.text = item.sendTime
                holder.nickName.text = item.senderNickName
                holder.unreadUserCount.text = item.unreadUserCount.toString()

                if (item.unreadUserCount.toString().toInt() > 0) {
                    holder.unreadUserContainer!!.visibility = View.VISIBLE
                    holder.unreadUserCount.visibility = View.VISIBLE
                } else {
                    holder.unreadUserContainer!!.visibility = View.GONE
                    holder.unreadUserCount.visibility = View.GONE
                }

                if (item.senderProfileUrl != "null") {
                    Glide.with(context)
                        .load(item.senderProfileUrl)
                        .into(holder.profile)
                } else {
                    holder.profile.setImageResource(R.drawable.profile)
                }
                holder.profile.setOnClickListener {
                    profileClickListener.onClick(it, position)
                }
            }

            MessageModel.VIEW_TYPE_YOU -> {
                (holder as ItemViewHolder2).contents.text = item.contents
                holder.dateTime.text = item.sendTime
                holder.nickName.text = item.senderNickName
                holder.unreadUserCount.text = item.unreadUserCount.toString()

                if (item.unreadUserCount.toString().toInt() > 0) {
                    holder.unreadUserContainer!!.visibility = View.VISIBLE
                    holder.unreadUserCount.visibility = View.VISIBLE
                } else {
                    holder.unreadUserContainer!!.visibility = View.GONE
                    holder.unreadUserCount.visibility = View.GONE
                }

                if (item.senderProfileUrl != "null") {
                    Glide.with(context)
                        .load(item.senderProfileUrl)
                        .into(holder.profile)
                } else {
                    holder.profile.setImageResource(R.drawable.profile)
                }
                holder.profile.setOnClickListener {
                    profileClickListener.onClick(it, position)
                }
            }

            MessageModel.VIEW_TYPE_ME_IMAGE -> {
                (holder as ItemViewHolder3).dateTime.text = item.sendTime
                holder.nickName.text = item.senderNickName
                holder.unreadUserCount.text = item.unreadUserCount.toString()

                if(item.imageUrl != "null") {
                    Glide.with(context)
                        .load(item.imageUrl)
                        .into(holder.image)
                    holder.image.clipToOutline = true
                } else {
                    holder.profile.setImageResource(R.drawable.broken_image)
                }
                if (item.unreadUserCount.toString().toInt() > 0) {
                    holder.unreadUserContainer!!.visibility = View.VISIBLE
                    holder.unreadUserCount.visibility = View.VISIBLE
                } else {
                    holder.unreadUserContainer!!.visibility = View.GONE
                    holder.unreadUserCount.visibility = View.GONE
                }

                if (item.senderProfileUrl != "null") {
                    Glide.with(context)
                        .load(item.senderProfileUrl)
                        .into(holder.profile)
                } else {
                    holder.profile.setImageResource(R.drawable.profile)
                }
                holder.profile.setOnClickListener {
                    profileClickListener.onClick(it, position)
                }
                holder.image.setOnClickListener {
                    imageClickListener.onClick(it, position)
                }
            }

            MessageModel.VIEW_TYPE_YOU_IMAGE -> {
                (holder as ItemViewHolder4).dateTime.text = item.sendTime
                holder.nickName.text = item.senderNickName
                holder.unreadUserCount.text = item.unreadUserCount.toString()

                if (item.unreadUserCount.toString().toInt() > 0) {
                    holder.unreadUserContainer!!.visibility = View.VISIBLE
                    holder.unreadUserCount.visibility = View.VISIBLE
                } else {
                    holder.unreadUserContainer!!.visibility = View.GONE
                    holder.unreadUserCount.visibility = View.GONE
                }

                if (item.senderProfileUrl != "null") {
                    Glide.with(context)
                        .load(item.senderProfileUrl)
                        .into(holder.profile)
                } else {
                    holder.profile.setImageResource(R.drawable.profile)
                }

                if(item.imageUrl != "null") {
                    Glide.with(context)
                        .load(item.imageUrl)
                        .into(holder.image)
                    holder.image.clipToOutline = true
                } else {
                    holder.profile.setImageResource(R.drawable.broken_image)
                }
                holder.profile.setOnClickListener {
                    profileClickListener.onClick(it, position)
                }
                holder.image.setOnClickListener {
                    imageClickListener.onClick(it, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    interface  OnItemClickListener {
        fun onClick(view : View, position : Int)
    }

    fun setProfileClickListener(onItemClickListener: OnItemClickListener) {
        this.profileClickListener = onItemClickListener
    }

    fun setImageClickListener(onItemClickListener: OnItemClickListener) {
        this.imageClickListener = onItemClickListener
    }

    private lateinit var profileClickListener : OnItemClickListener
    private lateinit var imageClickListener : OnItemClickListener
}