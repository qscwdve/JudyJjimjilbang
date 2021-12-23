package com.example.judyjjimjilbang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.judyjjimjilbang.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 게임 시작 버튼
        binding.startGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

    }
}