package com.example.judyjjimjilbang

import android.app.Activity
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.judyjjimjilbang.model.RankData

class RankDBHelper(
    context: Activity,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    private val mRankTable = "rankTable"
    private val mRankCountId = "_ID"
    private val mRankScore = "SCORE"
    private val mRankDate = "DATE"

    override fun onCreate(db: SQLiteDatabase?) {
        val sql = "CREATE TABLE if not exists $mRankTable ($mRankCountId INTEGER PRIMARY KEY AUTOINCREMENT, $mRankScore INTEGER, $mRankDate TEXT);"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val sql: String = "drop table if exists $mRankTable"
        db?.execSQL(sql)
        onCreate(db)
    }

    fun insertRankInfo(db: SQLiteDatabase, score: Int, date: String){
        val cv = ContentValues()

        cv.put(mRankScore, score)
        cv.put(mRankDate, date)
        if(db.insert(mRankTable, null, cv).toInt() == -1){
            Log.d("database", "insert fail")
        } else Log.d("database", "insert success")
        cv.clear()
    }

    fun selectRankOrder(db: SQLiteDatabase) : ArrayList<RankData>{
        // 내임차순으로 정렬하여 제일 점수가 큰 것부터 가져온다.
        val query = "SELECT * FROM $mRankTable ORDER BY $mRankScore DESC;"
        val array = ArrayList<RankData>()
        val c = db.rawQuery(query,null)
        while(c.moveToNext()) {
            array.add(RankData(c.getInt(c.getColumnIndex(mRankScore)), c.getString(c.getColumnIndex(mRankDate))))
        }
        c.close()
        return array
    }
}