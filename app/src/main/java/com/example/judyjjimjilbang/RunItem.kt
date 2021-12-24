package com.example.judyjjimjilbang

import java.util.*

class RunItem {
    private var iceRoom = false  // 0
    private var fireRoom = false   // 1
    private var masa = false      // 2
    private var light = false     // 3
    private var count = 0         // 손님이 받은 서비스 개수 : 서비스는 3번 받는다.

    // 손님이 다음 서비스 받을 것을 선택한다. -> 숫자로 리턴받는다. (0,1,2,3)
    fun nextRun(now : Int) : Int{
        if(now != -1) {
            // 현재 받은 서비스는 서비스를 받았다고 표시한다.
            valueChange(now, true)
        }
        if(count < 3) {
            val random = Random()
            var number = random.nextInt(4)
            while (indexToCheck(number)) number = random.nextInt(4)
            valueChange(number, true)
            count++
            return number
        }
        return -1
    }

    // 서비스를 받았는지 안받았는지 설정한다.
    fun valueChange(num : Int, value : Boolean){
        when(num){
            0 -> iceRoom = value
            1 -> fireRoom = value
            2 -> masa = value
            3 -> light = value
        }
    }

    // 이미 받은 서비스는 받을 수 없으므로 이미 받은 서비스인지 확인한다.
    fun indexToCheck(number : Int) : Boolean{
        return when(number) {
            0 -> iceRoom
            1 -> fireRoom
            2 -> masa
            3 -> light
            else -> false
        }
    }

    // 선택한 서비스(0,1,2,3)의 이미지를 리턴한다.
    fun indexToResource(number : Int) : Int {
        return when(number){
            0 -> R.drawable.iceitem
            1 -> R.drawable.fireitem
            2 -> R.drawable.masaitem
            3 -> R.drawable.lightitem
            else -> R.drawable.money
        }
    }
}