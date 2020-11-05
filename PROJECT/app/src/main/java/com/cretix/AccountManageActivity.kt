package com.cretix

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_account_manage.*

class AccountManageActivity : AppCompatActivity() {

    private lateinit var authPrefs: SharedPreferences
    private val authorizationPreferencesName = "authPrefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_manage)
        val intent = Intent(this, MainActivity::class.java)
        authPrefs = getSharedPreferences(authorizationPreferencesName, Context.MODE_PRIVATE)
        if (authPrefs.getBoolean("isVKAuth",false)) {
            button_vk.text = getString(R.string.action_logout)
        }
        if (authPrefs.getBoolean("isFacebookAuth",false)) {
            button_fb.text = getString(R.string.action_logout)
        }
        if (authPrefs.getBoolean("isTwitterAuth",false)) {
            button_tw.text = getString(R.string.action_logout)
        }


        button_vk.setOnClickListener {
            if (authPrefs.getBoolean("isVKAuth",false)) {
                authPrefs.edit().putBoolean("isVKAuth", false).apply()
            }
            startActivity(intent)
        }
        button_fb.setOnClickListener {
            if (authPrefs.getBoolean("isFacebookAuth",false)) {
                authPrefs.edit().putBoolean("isFacebookAuth", false).apply()
            }
            Snackbar.make(window.decorView.rootView, getString(R.string.unavailable), Snackbar.LENGTH_SHORT).show()
        }
        button_tw.setOnClickListener {
            if (authPrefs.getBoolean("isTwitterAuth",false)) {
                authPrefs.edit().putBoolean("isTwitterAuth", false).apply()
            }
            Snackbar.make(window.decorView.rootView, getString(R.string.unavailable), Snackbar.LENGTH_SHORT).show()
        }
    }
}
