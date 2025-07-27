package com.example.deb

import android.widget.LinearLayout
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val studyNewListFragment = StudyNewListFragment()
    private val historyFragment = HistoryFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) 앱 시작 시 HomeFragment 보여주기 (최초 한 번만)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainContainer, homeFragment)
                .commit()
        }

        // 2) BottomNavigation 세팅
        findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
            .setOnItemSelectedListener { menuItem ->
                val selected = when (menuItem.itemId) {
                    R.id.Home    -> homeFragment
                    R.id.Study   -> studyNewListFragment
                    R.id.History -> historyFragment
                    else         -> homeFragment
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.mainContainer, selected)
                    .commit()
                true
            }

    }
}
