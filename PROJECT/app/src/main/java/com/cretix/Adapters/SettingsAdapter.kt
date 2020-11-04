package com.cretix

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_settings_layout.view.*
import kotlinx.android.synthetic.main.item_source_layout.view.*
import java.util.zip.Inflater
import kotlin.concurrent.thread

data class SettingsItem(val title: String, val icon: Int, var action: () -> Unit)

class SettingsAdapter(val ctx: Context, val a: Activity) : BaseAdapter() {
    private  lateinit var inflater: LayoutInflater

    var settingsList: List<SettingsItem> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    init {
        inflater = a.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v: View = inflater.inflate(R.layout.item_settings_layout, null)
        v.icon.setImageResource(settingsList[position].icon)
        //v.title.text = settingsList[position].title
        v.setOnClickListener { settingsList[position].action() }
        return v
    }

    override fun getItem(position: Int): SettingsItem {
        return settingsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int = settingsList.size

}