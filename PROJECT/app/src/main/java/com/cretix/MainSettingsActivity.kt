package com.cretix

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_settings.*

class MainSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_settings)
        en_hint.setOnClickListener {
            val i = Intent(this, UIGuideActivity::class.java).apply {
                putExtra("lupa", "pupa")
            }
            startActivity(i)
        }
    }
}
