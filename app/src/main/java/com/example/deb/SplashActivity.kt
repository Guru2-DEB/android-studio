package com.example.deb

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 지연 없이 바로 MainActivity로 이동
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}