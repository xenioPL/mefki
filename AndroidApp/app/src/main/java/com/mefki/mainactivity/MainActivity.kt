package com.mefki.mainactivity

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.mefki.mainactivity.maps.MapsActivity
import androidx.fragment.app.Fragment
import com.mefki.mainactivity.alarms.AlarmsFragment
import kotlinx.android.synthetic.main.activity_main.*

class
MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_find_now -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_alarms -> {
                showFragment(AlarmsFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun showFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_main, fragment)
            .commit()
    }
}
