package com.cretix

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.cretix.ui.main.SectionsPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers


class SourceSelectActivity : AppCompatActivity() {
    private val VK_SOURCE_PREFS_NAME = "vkSourcePrefs"
    private lateinit var vkSourcePrefs: SharedPreferences
    private val roomDB = MyApplication.instance.getDatabase()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_select)

        vkSourcePrefs = getSharedPreferences(VK_SOURCE_PREFS_NAME, Context.MODE_PRIVATE)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener {
            val disposable = roomDB.vkSourceDao().getSelected()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { it.printStackTrace() }
                .doOnNext {
                    // save selected sources
                    val source_list = mutableListOf<Long>()
                    for (s_e in it) {
                        source_list.add(s_e.gid)
                    }
                    vkSourcePrefs.edit().putString("sources_vk", source_list.joinToString(",")).apply()

                    // get selected notifications
                    val disp = roomDB.vkSourceDao().getNotify()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {list->
                            for (id in list) {
                                FirebaseMessaging.getInstance().subscribeToTopic(id.toString())
                            }

                            // get not selected notifications
                            val disp = roomDB.vkSourceDao().getNotNotify()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext {list_->
                                    for (id in list_) {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(id.toString())
                                    }
                                    kotlin.run { finish() }
                                }
                                .doOnError { it.printStackTrace() }
                                .subscribe()
                        }
                        .doOnError { it.printStackTrace() }
                        .subscribe()
                }
                .subscribe()
        }
    }
}