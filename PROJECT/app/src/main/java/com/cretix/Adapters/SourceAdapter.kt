package com.cretix

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_post_informal.view.*
import kotlinx.android.synthetic.main.item_source_layout.view.*
import kotlin.concurrent.thread


// id - fireBase id
data class SourceItem(val id: String, val title: String, val available: Boolean, var checked: Boolean, var isNotifications: Boolean, var icon: String, var gid: Long)

class SourceAdapter(val ctx: Context, val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val roomDB = MyApplication.instance.getDatabase()
    private val TYPE_EMPTY = 0
    private val TYPE_SOURCE = 1
    private val TYPE_NO_AUTH = 2
    private val ID_EMPTY = "empty"
    private val ID_AUTH = "no_auth"


    var sourceList: List<SourceItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var innerType: Int = 0

    override fun getItemViewType(position: Int): Int {
        if (sourceList[position].id == ID_EMPTY) {
            return TYPE_EMPTY
        } else if (sourceList[position].id == ID_AUTH) {
            return TYPE_NO_AUTH
        }
        return TYPE_SOURCE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType  == TYPE_SOURCE) {
            return SourceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_source_layout, parent, false))
        }
        return InformalPostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_post_informal, parent, false))
    }

    override fun getItemCount(): Int = sourceList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        if (sourceList[position].id != ID_EMPTY && sourceList[position].id != ID_AUTH) {
            (holder as SourceViewHolder).onBind(sourceList[position], position)
        } else if (sourceList[position].id == ID_EMPTY) {
            (holder as InformalPostViewHolder).onBind(activity.getString(R.string.empty_list))
        } else if (sourceList[position].id == ID_AUTH) {
            (holder as InformalPostViewHolder).onBind(activity.getString(R.string.not_auth))
        }
    }

    inner class InformalPostViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val textView: TextView = root.text
        fun onBind(message: String) {
            textView.text = message
        }
    }

    class ClosedDialogFragment(private val link: String) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
            builder.setMessage("Закрытая группа! \nВспупите в сообщество \n$link для просмотра содержимого")
                .setPositiveButton(getString(android.R.string.ok)
                ) { dialog, id ->

                }
            return builder.create()
        }
    }

    inner class SourceViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val view = root
        private val title = view.source_title
        private val img = view.source_img
        private val checkBox = view.source_checked
        private val notificationEnable = view.enableNotifications

        fun onBind(source: SourceItem, pos: Int) {

            if (sourceList[pos].isNotifications) {
                ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorPrimaryDark)));
            }

            if (!source.available) {
                checkBox.isClickable = false
                checkBox.isEnabled = false
                notificationEnable.isClickable = false
                ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.darker_gray)));

                view.setOnClickListener {
                    val i = ClosedDialogFragment(source.title)
                    i.show((ctx as AppCompatActivity).supportFragmentManager, "a")
                    //Snackbar.make(activity.findViewById(android.R.id.content), "Закрытая группа", Snackbar.LENGTH_SHORT).show()
                }
            }
            title.text = source.title
            checkBox.isChecked = source.checked
            Glide.with(view).load(source.icon).into(img)

            notificationEnable.setOnClickListener {
                sourceList[pos].isNotifications = !sourceList[pos].isNotifications
                if (sourceList[pos].isNotifications) {
                    ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorPrimaryDark)));
                } else {
                    ImageViewCompat.setImageTintList(notificationEnable, ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.black)));
                }
                thread(start = true) {
                    roomDB.vkSourceDao().updateNotificationState(sourceList[pos].id, sourceList[pos].isNotifications)
                }
            }

            checkBox.setOnClickListener {
                sourceList[pos].checked = checkBox.isChecked
                thread(start = true) {
                    roomDB.vkSourceDao().updateSelectedState(sourceList[pos].id, checkBox.isChecked)
                }
            }
        }
    }

}