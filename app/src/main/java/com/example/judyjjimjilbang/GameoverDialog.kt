package com.example.judyjjimjilbang

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.judyjjimjilbang.databinding.DialogGameoverBinding

class GameoverDialog(val activity: GameActivity, val score: Int) : DialogFragment(){
    private lateinit var binding: DialogGameoverBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogGameoverBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scoreText = "당신의 점수는 ${score}점 입니다."
        binding.gameoverScore.text = scoreText

        binding.gameoverOk.setOnClickListener {
            // 확인 버튼을 누르면 게임 액티비티를 종료하고 랭크 액티비티로 연결한다.
            dismiss()
            activity.startRankActivity()
        }
    }

}