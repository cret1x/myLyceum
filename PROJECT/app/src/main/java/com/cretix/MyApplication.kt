package com.cretix

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.TypeConverter
import com.cretix.RoomComponents.SourceRoom.AppDatabase
import com.google.gson.Gson
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKAccessTokenTracker
import com.vk.sdk.VKSdk


class MyApplication : Application(){
    private val authorizationPreferencesName = "authPrefs"
    private lateinit var authPrefs: SharedPreferences
    private lateinit var database: AppDatabase

    companion object {
        lateinit var instance: MyApplication
    }

    var vkAccessTokenTracker: VKAccessTokenTracker = object : VKAccessTokenTracker() {
        override fun onVKAccessTokenChanged(oldToken: VKAccessToken?, newToken: VKAccessToken?) {
            if (newToken == null) {
                authPrefs.edit().putBoolean("isVKAuth", false).apply();
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this;
        authPrefs = getSharedPreferences(authorizationPreferencesName, Context.MODE_PRIVATE)
        vkAccessTokenTracker.startTracking()
        VKSdk.initialize(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "myLyceumDB"
        ).build()
    }

    fun getDatabase():AppDatabase = database
}