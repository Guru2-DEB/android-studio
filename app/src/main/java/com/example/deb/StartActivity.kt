package com.example.deb

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        Handler().postDelayed(Runnable {
            val i = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(i)
            finish()
        }, 3000)

    }
}