package com.cretix

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_source_layout.view.*
import kotlin.concurrent.thread

// id - fireBase id
data class SourceItem(val id: String, val title: String, val available: Boolean, var checked: Boolean, var isNotifications: Boolean, var icon: String)

class SourceAdapter(val ctx: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val roomDB = MyApplication.instance.getDatabase()
    private val TYPE_EMPTY = 0
    private val TYPE_SOURCE = 1
    private val TYPE_NO_AUTH = 2


    var sourceList: List<SourceItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var innerType: Int = 0
        set(value) {
            field = value
        }

    override fun getItemViewType(position: Int): Int {
        if (sourceList[position].id == "empty") {
            return TYPE_EMPTY
        } else if (sourceList[position].id == "no_auth") {
            return TYPE_NO_AUTH
        }
        return TYPE_SOURCE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType  == TYPE_SOURCE) {
            return SourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_source_layout, parent, false))
        } else if (viewType == TYPE_EMPTY){
            return EmptySourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_empty_list, parent, false))
        } else {
            return EmptySourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_not_auth_list, parent, false))
        }

    }

    override fun getItemCount(): Int = sourceList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if (sourceList[position].id != "empty" && sourceList[position].id != "no_auth") {
            (holder as SourceViewHolder).onBind(sourceList[position], position)
        }
    }

    inner class EmptySourceViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        fun onBind() {}
    }

    inner class SourceViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val view = root
        private val title = view.source_title
        private val img = view.source_img
        private val checkBox = view.source_checked
        private val notificationEnable = view.enableNotifications

        fun onBind(source: SourceItem, pos: Int) {

            Log.d("SOURCE:", pos.toString() + " -> " + source.toString())
            if (sourceList[pos].isNotifications) {
                ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorPrimaryDark)));
            }

            if (!source.available) {
                Log.d("SOURCE", "PRIVATE")
                checkBox.isClickable = false
                checkBox.isEnabled = false
                notificationEnable.isClickable = false
                ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.darker_gray)));

                view.setOnClickListener {
                    Toast.makeText(ctx,"Закрытая группа", Toast.LENGTH_SHORT).show()
                }
            }
            title.text = source.title
            checkBox.isChecked = source.checked
            Glide.with(view).load(source.icon).into(img)

            notificationEnable.setOnClickListener {
                sourceList[pos].isNotifications = !sourceList[pos].isNotifications
                Log.d("SOURCE", sourceList[pos].title + " -> " + sourceList[pos].isNotifications.toString())
                if (sourceList[pos].isNotifications) {
                    ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorPrimaryDark)));
                } else {
                    ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.black)));
                }
                thread(start = true) {
                    when(innerType) {
                        0 -> roomDB.vkSourceDao().updateNotificationState(sourceList[pos].id, sourceList[pos].isNotifications)
                        //1 -> roomDB.facebookSourceDao().updateNotificationState(sourceList[pos].id, checkBox.isChecked)
                        //else -> roomDB.twitterSourceDao().updateNotificationState(sourceList[pos].id, checkBox.isChecked)
                    }

                }
            }

            checkBox.setOnClickListener {
                sourceList[pos].checked = checkBox.isChecked
                thread(start = true) {
                    when(innerType) {
                        0 -> roomDB.vkSourceDao().updateSelectedState(sourceList[pos].id, checkBox.isChecked)
                        1 -> roomDB.facebookSourceDao().updateSelectedState(sourceList[pos].id, checkBox.isChecked)
                        else -> roomDB.twitterSourceDao().updateSelectedState(sourceList[pos].id, checkBox.isChecked)
                    }

                }
            }
        }
    }

}