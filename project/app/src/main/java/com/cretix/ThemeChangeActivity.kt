package com.cretix

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import kotlinx.android.synthetic.main.activity_theme_change.*

class ThemeChangeActivity : AppCompatActivity() {
    private val settingsPreferencesName = "settingsPrefs"
    private lateinit var settingsPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_change)

        settingsPrefs = getSharedPreferences(settingsPreferencesName, Context.MODE_PRIVATE)
        val mode = settingsPrefs.getString("nightMode", "system")
        when(mode) {
            "light" -> theme_light.isChecked = true
            "dark" -> theme_dark.isChecked = true
            "system" -> theme_system.isChecked = true
        }
        theme_light.setOnClickListener { onRadioButtonClicked(it) }
        theme_dark.setOnClickListener { onRadioButtonClicked(it) }
        theme_system.setOnClickListener { onRadioButtonClicked(it) }

    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.getId()) {
                R.id.theme_light ->
                    if (checked) {
                        settingsPrefs.edit().putString("nightMode", "light").apply()
                        MainActivity.setNightMode("light")
                    }
                R.id.theme_dark ->
                    if (checked) {
                        settingsPrefs.edit().putString("nightMode", "dark").apply()
                        MainActivity.setNightMode("dark")
                    }
                R.id.theme_system ->
                    if (checked) {
                        settingsPrefs.edit().putString("nightMode", "system").apply()
                        MainActivity.setNightMode("system")
                    }
            }
        }
    }

}
