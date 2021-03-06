package com.cretix


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(){
    private val settingsPreferencesName = "settingsPrefs"
    private val authorizationPreferencesName = "authPrefs"
    private lateinit var settingsPrefs: SharedPreferences
    private lateinit var authPrefs: SharedPreferences
    private var wasFirstLaunch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsPrefs = getSharedPreferences(settingsPreferencesName,Context.MODE_PRIVATE)
        authPrefs = getSharedPreferences(authorizationPreferencesName, Context.MODE_PRIVATE)

        setNightMode(settingsPrefs.getString("nightMode", "system")!!)

        setContentView(R.layout.activity_main)

        btn_auth_ready.setOnClickListener { launchApp() }
        start_vk_auth.setOnClickListener { startVkAuth() }
        start_fb_auth.setOnClickListener { startFacebookAuth() }
        start_twitter_auth.setOnClickListener { startTwitterAuth() }


        if( authPrefs.getBoolean("isVKAuth", false)) {
            start_vk_auth.isClickable = false
            start_vk_auth.isEnabled = false;
        }

        if (authPrefs.getBoolean("isFirstLaunch", true)){
            btn_auth_ready.isEnabled = false
            btn_auth_ready.isClickable = false
            wasFirstLaunch = true
            authPrefs.edit().apply {
                putBoolean("isFirstLaunch", false)
                putBoolean("isVKAuth", false)
                putBoolean("isFacebookAuth", false)
                putBoolean("isTwitterAuth", false)
            }.apply()
        }
        if( authPrefs.getBoolean("isVKAuth", false)        ||
            authPrefs.getBoolean("isFacebookAuth", false)  ||
            authPrefs.getBoolean("isTwitterAuth", false)) {
            launchApp();
        } else {
            btn_auth_ready.isEnabled = false
            btn_auth_ready.isClickable = false
        }



    }

    private fun startVkAuth(){
        if(authPrefs.getBoolean("isVKAuth", false)){
            Snackbar.make(window.decorView.rootView, getString(R.string.vk_auth_ready), Snackbar.LENGTH_SHORT).show()
        }
        else{
            VKSdk.login(this, VKScope.WALL, VKScope.FRIENDS)
        }
    }

    private fun startFacebookAuth(){
        if(authPrefs.getBoolean("isFacebookAuth", true)){
            Snackbar.make(window.decorView.rootView, getString(R.string.fb_auth_ready), Snackbar.LENGTH_SHORT).show()
        }
        else{
            Snackbar.make(window.decorView.rootView, getString(R.string.unavailable), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startTwitterAuth(){
        if(authPrefs.getBoolean("isTwitterAuth", true)){
            Snackbar.make(window.decorView.rootView, getString(R.string.tw_auth_ready), Snackbar.LENGTH_SHORT).show()
        }
        else{
            Snackbar.make(window.decorView.rootView, getString(R.string.unavailable), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                override fun onResult(res: VKAccessToken) {
                    Snackbar.make(window.decorView.rootView, getString(R.string.auth_success), Snackbar.LENGTH_SHORT).show()
                    authPrefs.edit().putBoolean("isVKAuth", true).apply();
                    val buttonStartApp = findViewById<Button>(R.id.btn_auth_ready)
                    buttonStartApp.isEnabled = true
                    buttonStartApp.isClickable = true
                    settingsPrefs.edit().putLong("version", 0).apply()
                }
                override fun onError(error: VKError) {
                    Snackbar.make(window.decorView.rootView, getString(R.string.auth_error), Snackbar.LENGTH_SHORT).show()
                }
            })
        ) {
            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    private fun setNightMode(mode: String) {
        when(mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun launchApp(){
        val intent: Intent = if (wasFirstLaunch) {
            Intent(this, UIGuideActivity::class.java)
        } else {
            Intent(this, SourceUpdateActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
