package com.example.judyjjimjilbang

import android.os.Handler
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.judyjjimjilbang.model.ItemPoint

class EventStandControl(val standView: ImageView) {
    val originImage : Int = R.drawable.stand
    val clickImage: Int = R.drawable.standclick
    private val CLICK_IMAGE = 2
    private val ORIGIN_IMAGE = 1
    var X = 0f
    var Y = 0f
    var width = 0F
    var height = 0F
    var isChangeImage = false

    fun setPosition(X:Float, Y:Float, width:Int, height:Int){
        this.X = X
        this.Y = Y
        this.width = width/2F
        this.height = height/2F
    }

    // 손님이 이 의자 영역에 있는지 없는지 판별해준다.
    fun checkItemIn(customerPosition: ItemPoint) : Boolean{
        // customerPosition : 손님의 현재 위치정보 - x, y, width, height
        return ((this.X >= customerPosition.x && this.X <= customerPosition.x + customerPosition.width)
                && (this.Y >= customerPosition.y && this.Y <= customerPosition.y + customerPosition.height))
        //return (vX >= this.X - width && vX <= this.X + width && vY >= this.Y - height && vY <= this.Y + height)
    }

    // 손님이 의자 영역에 있을 때, 앉을 수 있으면 clickImage, 앉을 수 없으면 originImage
    fun changeImage(version:Int){
        if(version == ORIGIN_IMAGE){
            standView.setImageResource(originImage)
            isChangeImage = false
        } else {
            standView.setImageResource(clickImage)
            isChangeImage = true
        }
    }

}