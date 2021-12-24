package com.example.judyjjimjilbang

import android.os.Build
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.util.*

class JudyAnimation(val character: View, val orderQueue : Queue<Int>, val activity: GameActivity) : Animation.AnimationListener {
    var positionX : Float = 0F
    var positionY : Float = 0F
    var version = 0
    var judyInitX : Float = 0F
    var judyInitY : Float = 0F

    fun setPositionValue(version: Int, X: Float, Y: Float){
        //version : 0. iceRoom 1. FireRoom 2. masa 3. light 4. water 5. egg 6. waste
        this.positionX = X
        this.positionY = Y
        this.version = version
    }

    fun setJudyInitPosition(X: Float, Y: Float){
        this.judyInitX = X
        this.judyInitY = Y
    }

    fun startAnimation(){
        // 현재 쥬디위치에서 이동할 곳(positionX, positonY)까지 애니메이션으로 이동한다.
        val X = positionX - character.x
        val Y = positionY - character.y
        val adni = TranslateAnimation(0f, X, 0f, Y).apply {
            duration = 2000
        }
        adni.setAnimationListener(this)
        character.startAnimation(adni)
    }
    override fun onAnimationStart(animation: Animation?) {

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAnimationEnd(animation: Animation?) {
        // 쥬디가 이동해야할 곳으로 애니메이션이 완료하면, 위치를 이동해야할 곳으로 바꾼다.
        character.x = positionX
        character.y = positionY
        // 쥬디가 이동을 완료했으니 체크박스는 없어져야 한다.
        activity.indexToCheckBox(version).visibility = ImageView.GONE
        if(this.version <= 3){
            // 쥬디가  0. iceRoom 1. FireRoom 2. masa 3. light 중 한 영역에 다가갔을 경우
            activity.getCustomerMoneyOrSnackCheck(this.version)
        }
        else {
            // 쥬디가 4. water , 5. egg , 6. waste 에 다가갔을 경우
            activity.snackClickControl(this.version)
        }
        // 쥬디가 이동을 완료했으니 이동해야할 경로도 지워준다.
        orderQueue.remove()
        if(orderQueue.size != 0){
            // 앞으로 쥬디가 이동해야할 경로가 아직 남아있으면 그쪽으로 이동한다.
            activity.judyAnimationStart(orderQueue.element())
        } else {
            // 쥬디가 가야할 곳이 더이상 없으면 초기 위치로 돌아간다.
            val X = judyInitX - character.x
            val Y = judyInitY - character.y
            val adni = TranslateAnimation(0f, X, 0f, Y).apply {
                duration = 2000
            }
            adni.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    // 초기위치로 이동하는 애니메이션이 끝나면 초기위치로 이동한다.
                    character.x = judyInitX
                    character.y = judyInitY
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            character.startAnimation(adni)
        }
    }

    override fun onAnimationRepeat(animation: Animation?) {
    }

}