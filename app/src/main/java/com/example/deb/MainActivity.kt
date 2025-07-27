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

        val headerBar = findViewById<LinearLayout>(R.id.headerBar)

        // 프래그먼트 단독 실행 (테스트용)
        supportFragmentManager.beginTransaction()
            .replace(R.id.mainContainer, StudyAiChatFragment())
            .commit()

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottomNavigationBar)
        bottomNavBar.setOnItemSelectedListener  {
            val selectedFragment: Fragment = when (it.itemId){
                R.id.Home -> homeFragment
                R.id.Study -> studyNewListFragment
                R.id.History -> historyFragment
                else -> homeFragment
            }

            supportFragmentManager.beginTransaction().replace(R.id.mainContainer, selectedFragment).commit()
            true
        }

        bottomNavBar.selectedItemId = R.id.Home

    }
}
