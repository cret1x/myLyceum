package com.cretix

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cretix.RoomComponents.SourceRoom.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlin.concurrent.thread


data class Group(val FullName: String, val url: String)

class MainScreenActivity : AppCompatActivity() {
    companion object{
        const val TAG = "FireBase"
    }
    private var navSelected = R.id.navigation_feed

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt("navSelected", navSelected)
        Log.d("MainScreenActivity", "onSaveInstanceState()")
    }


    override fun onBackPressed() {}

    private fun selectFragmentById(id: Int) {
        when (id) {
            R.id.navigation_feed -> {
                val fragment = FeedFragment()
                topAppBar.title = getString(R.string.title_feed)
                loadFragment(fragment)
            }
            R.id.navigation_favourite -> {
                val fragment = FavouriteFragment()
                topAppBar.title = getString(R.string.title_fav)
                loadFragment(fragment)
            }
            R.id.navigation_settings -> {
                val fragment = SettingsFragment()
                topAppBar.title = getString(R.string.title_settings)
                loadFragment(fragment)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainScreenActivity", "OnCreate()")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        val last_fragment_id = savedInstanceState?.getInt("navSelected") ?: R.id.navigation_feed
        selectFragmentById(last_fragment_id)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        navSelected = item.itemId
        when (item.itemId) {
            R.id.navigation_feed -> {
                val fragment = FeedFragment()
                topAppBar.title = getString(R.string.title_feed)
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favourite -> {
                val fragment = FavouriteFragment()
                topAppBar.title = getString(R.string.title_fav)
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                val fragment = SettingsFragment()
                topAppBar.title = getString(R.string.title_settings)
                loadFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
