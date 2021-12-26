package com.example.judyjjimjilbang

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi

class EventItemControl(val originImage : Int, val clickImage: Int, val activity: GameActivity) {
    private val CLICK_IMAGE = 2
    private val ORIGIN_IMAGE = 1
    private var runImage = originImage
    var X = 0f
    var Y = 0f
    var width = 0F
    var height = 0F
    var isChangeImage = false
    lateinit var view: ImageView
    lateinit var progress : ProgressBar
    lateinit var handler : Handler
    var lock : Boolean = false

    fun setItemPosition(X:Float, Y:Float, width:Int, height:Int){
        this.X = X
        this.Y = Y
        this.width = width/2F
        this.height = height/2F
    }

    fun setView(view:ImageView, progress : ProgressBar, runImage : Int, handler: Handler){
        this.view = view
        this.progress = progress
        this.runImage = runImage
        this.handler = handler
    }

    //  손님이 이 서비스 영역에 있는지 없는지 판별해준다.
    fun checkItemIn(vX: Float, vY:Float) : Boolean{
        return (vX >= this.X - width && vX <= this.X + width && vY >= this.Y - height && vY <= this.Y + height)
    }

    // 손님이 이 서비스 영역에 있을 때, 앉을 수 있으면 clickImage, 앉을 수 없으면 originImage
    fun changeImage(version:Int){
        if(version == ORIGIN_IMAGE){
            view.setImageResource(originImage)
            isChangeImage = false
        } else {
            view.setImageResource(clickImage)
            isChangeImage = true
        }
    }

    // 손님이 서비스 받는 게이지를 실행해준다.
    @RequiresApi(Build.VERSION_CODES.N)
    fun progressThread(character: View, itemImageView: ImageView, num : Int, now : Int){
        // now : 현재 받을 서비스 번호 , num : 손님 번호
        character.visibility = View.GONE
        progress.setProgress(100, false)
        progress.visibility = View.VISIBLE
        view.setImageResource(runImage)
        lock = true
        Thread() {
            // 0.1초마다 5씩 줄어든다.
            var progressState = 95
            for(i in 1..20){
                try {
                    progress.setProgress(progressState, true)
                    progressState -= 5
                    Thread.sleep(100)
                } catch (E:Exception){
                    Log.d("error", E.toString())
                }
            }
            // 손님이 서비스를 다 받으면 다음엔 어떤 서비스를 받을지 선택해야한다.
            handler.post {
                character.x = activity.mCustomerExistPosition[now].x
                character.y = activity.mCustomerExistPosition[now].y
                character.visibility = View.VISIBLE
                progress.visibility = View.GONE    // 게이지는 사라진다.
                view.setImageResource(originImage)

                // 다음 서비스 선택
                activity.nextRunItem(now, num, itemImageView)
            }
            lock = false
        }.start()
    }
}