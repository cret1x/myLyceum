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
import com.cretix.RoomComponents.SourceRoom.FacebookSourceEntity
import com.cretix.RoomComponents.SourceRoom.TwitterSourceEntity
import com.cretix.RoomComponents.SourceRoom.VKSourceEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.vk.sdk.api.methods.VKApiGroups
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotlin.concurrent.thread

class SourceUpdateActivity : AppCompatActivity() {


    private val settingsPreferencesName = "settingsPrefs"
    private lateinit var settingsPrefs: SharedPreferences
    val fireDB: FirebaseFirestore = FirebaseFirestore.getInstance()
    val roomDB: AppDatabase = MyApplication.instance.getDatabase()
    private lateinit var pb1: ProgressBar
    private lateinit var label1: TextView
    private lateinit var done: ImageView
    private lateinit var button_continue: Button
    companion object{
        const val TAG = "FireBase"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_update)

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
                    val version = task.result!!.documents.get(0).get("version") as Long
                    val my_version = settingsPrefs.getLong("version", 0)
                    Log.v("VERSION", "DEVICE: $my_version | SERVER: $version")
                    if (version > my_version) {
                        settingsPrefs.edit().putLong("version", version).apply()
                        updateSources()
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

    fun updateSources() {
        fireDB.collection("groups")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val vkSourceDao = roomDB.vkSourceDao()
                    val facebookSourceDao = roomDB.facebookSourceDao()
                    val twitterSourceDao = roomDB.twitterSourceDao()
                    val disposable = Observable.fromIterable(task.result!!)
                        .map {
                            val document = it
                            when ((document.data["type"] as Long).toInt()) {
                                0 -> {
                                        vkSourceDao.insert(doc2VKsource(document))
                                }
                                1 -> {
                                        facebookSourceDao.insert(doc2Facebooksource(document))
                                }
                                else -> {
                                        twitterSourceDao.insert(doc2Twittersource(document))
                                }
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

    fun doc2Twittersource(document: QueryDocumentSnapshot) : TwitterSourceEntity {
        val sourceEntity = TwitterSourceEntity(
            id = document.id,
            title = document.data["name"] as String,
            isSelected = false,
            isNotify = false
        )
        return sourceEntity
    }

    fun doc2Facebooksource(document: QueryDocumentSnapshot) :FacebookSourceEntity {
        val sourceEntity = FacebookSourceEntity(
            id = document.id,
            title = document.data["name"] as String,
            isSelected = false,
            isNotify = false
        )
        return sourceEntity
    }
}
