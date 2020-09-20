package com.cretix


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKScope
import com.vk.sdk.VKSdk
import com.vk.sdk.api.VKError


class MainActivity : AppCompatActivity(){
    private val settingsPreferencesName = "settingsPrefs"
    private val authorizationPreferencesName = "authPrefs"
    private lateinit var settingsPrefs: SharedPreferences
    private lateinit var authPrefs: SharedPreferences
    companion object {
        fun setNightMode(mode: String) {
            when(mode) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsPrefs = getSharedPreferences(settingsPreferencesName,Context.MODE_PRIVATE)
        authPrefs = getSharedPreferences(authorizationPreferencesName, Context.MODE_PRIVATE)
        setNightMode(settingsPrefs.getString("nightMode", "system")!!)
        val buttonStartApp = findViewById<Button>(R.id.btn_auth_ready)
        val buttonVKAuth = findViewById<Button>(R.id.start_vk_auth)
        val buttonFacebookAuth = findViewById<Button>(R.id.start_fb_auth)
        val buttonTwitterAuth = findViewById<Button>(R.id.start_twitter_auth)

        buttonStartApp.setOnClickListener { launchApp() }
        buttonVKAuth.setOnClickListener { startVkAuth() }
        buttonFacebookAuth.setOnClickListener { startFacebookAuth() }
        buttonTwitterAuth.setOnClickListener { startTwitterAuth() }


        if (authPrefs.getBoolean("isFirstLaunch", true)){
            buttonStartApp.isEnabled = false
            buttonStartApp.isClickable = false
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
            buttonStartApp.isEnabled = false
            buttonStartApp.isClickable = false
        }



    }



    private fun startVkAuth(){
        if(authPrefs.getBoolean("isVKAuth", false)){
            Toast.makeText(applicationContext, "Вы уже авторизованы через VK", Toast.LENGTH_SHORT).show()
        }
        else{
            VKSdk.login(this, VKScope.WALL, VKScope.FRIENDS)
        }
    }

    private fun startFacebookAuth(){
        if(authPrefs.getBoolean("isFacebookAuth", true)){
            Toast.makeText(applicationContext, "Вы уже авторизованы через Facebook", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(applicationContext, "Пока нельзя", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTwitterAuth(){
        if(authPrefs.getBoolean("isTwitterAuth", true)){
            Toast.makeText(applicationContext, "Вы уже авторизованы через Twitter", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(applicationContext, "Пока нельзя", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                override fun onResult(res: VKAccessToken) {
                    Toast.makeText(applicationContext, "Успешная авторизация", Toast.LENGTH_SHORT).show()
                    authPrefs.edit().putBoolean("isVKAuth", true).apply();
                    val buttonStartApp = findViewById<Button>(R.id.btn_auth_ready)
                    buttonStartApp.isEnabled = true
                    buttonStartApp.isClickable = true
                }
                override fun onError(error: VKError) {
                    Toast.makeText(applicationContext, "Ошибка во время авторизации", Toast.LENGTH_SHORT).show()
                }
            })
        ) {
            super.onActivityResult(requestCode, resultCode, data)

        }
    }



    private fun launchApp(){
        val intent = Intent(this, SourceUpdateActivity::class.java)
        startActivity(intent)
    }
}
