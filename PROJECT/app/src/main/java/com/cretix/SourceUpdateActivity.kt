package com.cretix

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.cretix.RoomComponents.SourceRoom.AppDatabase
import com.cretix.RoomComponents.SourceRoom.VKSourceEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.concurrent.thread

class SourceUpdateActivity : AppCompatActivity() {


    private val settingsPreferencesName = "settingsPrefs"
    private lateinit var settingsPrefs: SharedPreferences
    private val fireDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val roomDB: AppDatabase = MyApplication.instance.getDatabase()
    private lateinit var pb1: ProgressBar
    private lateinit var label1: TextView
    private lateinit var done: ImageView
    private lateinit var button_continue: Button
    private lateinit var vkSourcePrefs: SharedPreferences
    private val VK_SOURCE_PREFS_NAME = "vkSourcePrefs"
    companion object{
        const val TAG = "FireBase"
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SourceUpdateActivity", "onDestroy()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("SourceUpdateActivity", "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("SourceUpdateActivity", "onStop()")
    }

    override fun onStart() {
        super.onStart()
        Log.d("SourceUpdateActivity", "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d("SourceUpdateActivity", "onResume()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SourceUpdateActivity", "OnCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_update)

        vkSourcePrefs = getSharedPreferences(VK_SOURCE_PREFS_NAME, Context.MODE_PRIVATE)
        settingsPrefs = getSharedPreferences(settingsPreferencesName, Context.MODE_PRIVATE)
        label1 = findViewById(R.id.label1)
        pb1 = findViewById(R.id.progressBar)
        button_continue = findViewById(R.id.btn_continue)
        done = findViewById(R.id.img_tick)

        button_continue.isClickable = false
        button_continue.isEnabled = false

        fireDB.collection("meta")
            .get()
            .addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val version = task.result!!.documents[1].get("version") as Long
                    val deleted_id = task.result!!.documents[0].get("id") as String
                    val my_version = settingsPrefs.getLong("version", 0)
                    Log.v("VERSION", "DEVICE: $my_version | SERVER: $version")
                    if (version > my_version) {
                        settingsPrefs.edit().putLong("version", version).apply()
                        updateSources(deleted_id)
                    } else {
                        buttonActivate()
                    }
                }
                else {
                    Log.w(
                        MainScreenActivity.TAG,
                        "Error getting documents.",
                        task.exception
                    )
                }
            }

        button_continue.setOnClickListener {
            val intent = Intent(this, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun buttonActivate() {
        pb1.visibility = View.INVISIBLE
        label1.text = getText(R.string.content_up_to_date)
        button_continue.isClickable = true
        button_continue.isEnabled = true


        val drowable = done.drawable

        if (drowable is AnimatedVectorDrawableCompat) {
            val avd = drowable
            avd.start()
        } else if (drowable is AnimatedVectorDrawable) {
            val avd2 = drowable
            avd2.start()
        }
    }

    fun updateSources(del_id: String) {
        val vkSourceDao = roomDB.vkSourceDao()
        thread(start = true) {
            val sources = vkSourcePrefs.getString("sources_vk", "")!!
            val strs = sources.split(",").toTypedArray()
            val source_list = strs.toMutableList()
            Log.d("AAAAA", source_list.size.toString())
            if (source_list.size != 0) {
                for (source in source_list) {
                    if (source == "") {
                        continue
                    }
                    val title = vkSourceDao.getTitleByGid(source.toLong())
                    if (title == null) {
                        source_list.remove(source)
                        Log.d("AAAAAAAAAAAAA", source)
                    }
                }
                vkSourcePrefs.edit().putString("sources_vk", source_list.joinToString(",")).apply()
            }
        }
        Log.d("TO delete", del_id)
        vkSourceDao.deleteById(del_id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { Log.d("AAAAAAAAAAAAA","FFFFFFFFFFFFFFFFFF") }
            .doOnComplete {
                fireDB.collection("groups")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val disposable = Observable.fromIterable(task.result!!)
                                .map {
                                    val document = it
                                    if (vkSourceDao.isRowIsExist(document.id)) {
                                        val group = VKAPICalls.Group(document.data["gid"] as Long)
                                        vkSourceDao.update(
                                            id = document.id,
                                            title = document.data["name"] as String,
                                            isClosed = group.isClosed(),
                                            groupIcon = group.getIcon()
                                        )
                                    } else {
                                        vkSourceDao.insert(doc2VKsource(document))
                                    }
                                }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete {
                                    run {
                                        buttonActivate()
                                    }
                                }
                                .subscribe()
                        } else {
                            Log.w(
                                MainScreenActivity.TAG,
                                "Error getting documents.",
                                task.exception
                            )
                        }
                    }
            }
            .subscribe()

    }

    fun doc2VKsource(document: QueryDocumentSnapshot) : VKSourceEntity {
        val group = VKAPICalls.Group(document.data["gid"] as Long)
        val sourceEntity = VKSourceEntity(
            id = document.id,
            gid = document.data["gid"] as Long,
            isClosed = group.isClosed(),
            groupIcon = group.getIcon(),
            title = document.data["name"] as String,
            isSelected = false,
            isNotify = false
        )
        return sourceEntity
    }
}
