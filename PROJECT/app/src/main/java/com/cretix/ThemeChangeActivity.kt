package com.cretix

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.android.synthetic.main.activity_theme_change.*

class ThemeChangeActivity : AppCompatActivity() {
    private val settingsPreferencesName = "settingsPrefs"
    private lateinit var settingsPrefs: SharedPreferences

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ThemeChangeActivity", "onDestroy()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("ThemeChangeActivity", "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("ThemeChangeActivity", "onStop()")
    }

    override fun onStart() {
        super.onStart()
        Log.d("ThemeChangeActivity", "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d("ThemeChangeActivity", "onResume()")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ThemeChangeActivity", "onCreate()")
        settingsPrefs = getSharedPreferences(settingsPreferencesName, Context.MODE_PRIVATE)
        val mode = settingsPrefs.getString("nightMode", "system")!!
        setContentView(R.layout.activity_theme_change)

        when(mode) {
            "light" ->  {
                theme_light.strokeColor = resources.getColor(R.color.colorPrimaryDark)
                theme_light.strokeWidth = resources.getDimension(R.dimen.selected_width).toInt()
            }
            "dark" -> {
                theme_dark.strokeColor = resources.getColor(R.color.colorPrimaryDark)
                theme_dark.strokeWidth = resources.getDimension(R.dimen.selected_width).toInt()
            }
            "system" -> 3
        }

        theme_dark.setOnClickListener {
            settingsPrefs.edit().putString("nightMode", "dark").apply()
            theme_dark.strokeColor = resources.getColor(R.color.colorPrimaryDark)
            theme_dark.strokeWidth = resources.getDimension(R.dimen.selected_width).toInt()
            theme_light.strokeColor = resources.getColor(R.color.colorPrimary)
            theme_light.strokeWidth = resources.getDimension(R.dimen.not_selected_width).toInt()
            setNightMode("dark")
        }

        theme_light.setOnClickListener {
            settingsPrefs.edit().putString("nightMode", "light").apply()

            theme_light.strokeColor = resources.getColor(R.color.colorPrimaryDark)
            theme_light.strokeWidth = resources.getDimension(R.dimen.selected_width).toInt()
            theme_dark.strokeColor = resources.getColor(R.color.colorPrimary)
            theme_dark.strokeWidth = resources.getDimension(R.dimen.not_selected_width).toInt()
            setNightMode("light")
        }
/*
        theme_system.setOnClickListener {
            settingsPrefs.edit().putString("nightMode", "light").apply()

            theme_light.strokeColor = resources.getColor(R.color.colorPrimaryDark)
            theme_light.strokeWidth = resources.getDimension(R.dimen.selected_width).toInt()
            theme_dark.strokeColor = resources.getColor(R.color.colorPrimary)
            theme_dark.strokeWidth = resources.getDimension(R.dimen.not_selected_width).toInt()
            setNightMode("light")
        }*/
    }

    private fun setNightMode(mode: String) {
        when(mode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
