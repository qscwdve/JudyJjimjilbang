package com.example.judyjjimjilbang

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.judyjjimjilbang.databinding.ActivityRankBinding
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class RankActivity : BaseActivity<ActivityRankBinding>(ActivityRankBinding::inflate) {
    var mScore = 0
    private lateinit var mDBHelper: RankDBHelper
    private lateinit var mDB: SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 데이터베이스 셋팅
        mDBHelper = RankDBHelper(this, "Rank.db", null, 1)
        mDB = mDBHelper.writableDatabase
        mDBHelper.onCreate(mDB)

        // 집모양을 누르면 초기화면으로 돌아간다.
        binding.home.setOnClickListener { finish() }

        // 게임 결과 점수 가져오기
        mScore = intent.getIntExtra("score", -1)

        // 현재 시간
        val nowTime = getTime()

        // 데이터베이스에 게임 결과를 저장한다.
        if(mScore != -1) mDBHelper.insertRankInfo(mDB, mScore, nowTime)

        // mDBHelper.selectRankOrder(mDB) : 데이터베이스에서 Rank 정보를 가져온다.
        val adapter = RankAdapter(mDBHelper.selectRankOrder(mDB), nowTime)
        binding.rankRecyclerView.adapter = adapter
        binding.rankRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getTime(): String {
        val pattern = "yyyy-MM-dd hh:mm:ss"
        val mNow = System.currentTimeMillis()
        val mDate = Date(mNow)
        return SimpleDateFormat(pattern).format(mDate)
    }
}