package com.example.judyjjimjilbang

class SnackControl {
    private var egg = 0           // 쥬디가 집은 달걀 개수
    private var water = 0         // 쥬디가 집은 물 개수
    private var rightSnack = false    // 오른손에 물 혹은 달걀을 가지고 있는지 없는지 판별하는 변수
    private var leftSnack = false     // 왼손에 물 혹은 달걀을 가지고 있는지 없는지 판별하는 변수
    private var rightSnackValue = -1     // 오른손에 든 간식 : 물(0), 달걀(1)
    private var leftSnackValue = -1      // 왼손에 든 간식 : 물(0), 달걀(1)

    fun addRightEgg(){
        // 오른손에 달걀을 집은 경우
        this.rightSnack = true
        this.egg++
        this.rightSnackValue = 1
    }
    fun addRightWater(){
        // 오른손에 물을 집은 경우
        this.rightSnack = true
        this.water++
        this.rightSnackValue = 0
    }
    fun addLeftEgg(){
        // 왼손에 달걀을 집은 경우
        this.leftSnack = true
        this.egg++
        this.leftSnackValue = 1
    }
    fun addLeftWater(){
        // 왼손에 물을 집은 경우
        this.leftSnack = true
        this.water++
        this.leftSnackValue = 0
    }
    fun subLeftEgg(){
        // 왼손에서 달걀을 손님에게 주거나 쓰레기통에 버린 경우
        this.leftSnack = false
        this.leftSnackValue = -1
        this.egg--
    }
    fun subLeftWater(){
        // 왼손에서 물을 손님에게 주거나 쓰레기통에 버린 경우
        this.leftSnack = false
        this.leftSnackValue = -1
        this.water--
    }
    fun subRightWater(){
        // 오른손에서 물을 손님에게 준 버린 경우
        this.rightSnack = false
        this.rightSnackValue = -1
        this.water--
    }
    fun subRightEgg(){
        // 오른손에서 달걀을 손님에게 준 경우
        this.rightSnack = false
        this.rightSnackValue = -1
        this.egg--
    }
    fun getRightCheck() : Boolean = this.rightSnack
    fun getLeftCheck() : Boolean = this.leftSnack
    fun getEggCount() : Int = this.egg
    fun getWaterCount() : Int = this.water
    fun getRightValue() : Int = this.rightSnackValue
    fun getLeftValue() : Int = this.leftSnackValue

    // 쥬디가 쓰레기통에 들고있던 간식을 버린 경우
    fun clear(){
        this.rightSnack = false
        this.leftSnack = false
        this.egg = 0
        this.water = 0
        this.rightSnackValue = -1
        this.leftSnackValue = -1
    }

    fun count() : Int = this.egg + this.water
}