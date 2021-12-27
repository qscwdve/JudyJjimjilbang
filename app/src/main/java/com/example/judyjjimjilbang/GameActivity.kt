package com.example.judyjjimjilbang

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.judyjjimjilbang.databinding.ActivityGameBinding
import com.example.judyjjimjilbang.model.ItemPoint
import com.example.judyjjimjilbang.model.SnackToProgress
import com.example.judyjjimjilbang.model.XYPosition
import java.util.*
import kotlin.collections.ArrayList

class GameActivity : BaseActivity<ActivityGameBinding>(ActivityGameBinding::inflate) {
    private var mCustomerNum = 100   // 쥬디의 찜질방 손님 최대 명수

    // 쥬디의 찜질방에 오신 손님이 선택하게 될 서비스
    // 각각의 손님이 선택할 서비스를 결정하거나 몇 개의 서비스를 받았는지 혹은 3개의 서비스를 다 받은 뒤, 계산 등을 관리하는 서비스 관리 RunItem 클래스
    // 각각의 손님(최대100명)에 대한 서비스 관리 목록이 들어간 배열 -> mCustomerRunControl[4] : 4번 손님에 대한 서비스 관리 클래스
    private val mCustomerRunControl = Array<RunItem>(mCustomerNum) { RunItem() }

    // 각 요소들의 위치정보를 저장한다. [0]얼음방, [1]불꽃방, [2]마사지, [3]라이트, [4]의자1 , [5]의자2, [6]의자3, [7] 물 [8] 달걀 [9] 쓰레기통
    private val mItemPosition = Array<ItemPoint>(10) { ItemPoint() }

    // 각 요소들에 손님이 있는지 없는지를 확인하는 배열이다. 손님이 있으면 손님번호가, 없으면 -1이다.
    private val mItemExistCheck = Array<Int>(7) {-1}

    // 손님이 계신 곳을 나타내는 배열이다. 손님이 어디 계신지 모르면 -1이다. ex) 3번손님이 계신 곳 = mCustomPosition[3]
    // 손님이 계신 곳은 mItemPosition 와 같이 [0]얼음방, [1]불꽃방, [2]마사지, [3]라이트, [4]의자1 , [5]의자2, [6]의자3 이다.
    private val mCustomPosition = Array<Int>(mCustomerNum) {-1}

    // 각 요소(서비스 : 얼음방, 불꽃방, 마사지, 라이트)에서 손님이 다가갔을 때, 서비스를 받을 수 있다는 표시인 하얀색 테두리가 있는 그림으로 바뀌거나,
    // 손님이 서비스를 받고 있는 게이지를 나타내는 프로그레스바를 컨트롤하는 클래스 = EvenItemControl.kt 를 담고 있는 배열이다.
    lateinit var mItemControlList: ArrayList<EventItemControl>

    // 각 요소(의자1, 의자2, 의자3)에 손님이 다가갔을때, 앉을 수 있다는 표시를 해주는 것을 컨트롤하는 클래스를 담은 배열
    lateinit var mStandControlList: ArrayList<EventStandControl>

    // 쥬디가 사용자가 선택한 곳으로 움직이는 애니메이션 관련 변수
    private lateinit var mJudyAnimation: JudyAnimation
    // 쥬디가 이동해야할 경로가 담겨있는 큐이다.
    private val mJudyclickQueue: Queue<Int> = LinkedList<Int>()
    // 쥬디가 물 혹은 달걀을 집고 손님에게 가져다줄 때, 쥬디가 왼손/오른손에 들은 간식(물, 달걀) 정보 관리하는 변수
    private val snackControl = SnackControl()

    private var mCustomerFinishCount = 0     // 서비스를 다 받고 계산까지 마친 손님 수
    private var mCustomerStartCount = 0      // 쥬디의 찜질방에 오신 손님 수

    // 손님이 마사지 서비스를 원할 경우, 마사지를 받기 전에 물 혹은 달걀을 요청할 것이다.
    // 마사지 기계는 1대이므로 이 마사지 기계에 앉은 손님의 프로그레스바의 정보는 1개만 있으면 된다.
    private var snackToProgress : SnackToProgress? = null

    // 손님의 요구를 시간내에 충족시키지 못할 경우 바로 게임이 끝난다.
    // 게임이 끝났는지 안끝났는지 판별하는 변수
    var mfinish = false    // false : 게임 안끝남 , true : 게임 끝남

    // 손님이 있을 수 있는 위치를 저장한 배열
    // 0 : 얼음방, 1 : 불꽃방, 2 : 마사지, 3 : 라이트, 4 : 의자1 , 5 : 의자2 , 6 : 의자3
    val mCustomerExistPosition = Array<XYPosition>(7) { XYPosition(0F, 0F) }

    lateinit var handler : Handler
    private val nPL = 10000
    private val nBL = 20000
    private val nRL = 30000
    private val nProL = 40000
    private val nIL = 50000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = Handler(Looper.getMainLooper())
        mItemControlList = createItemControlList()
        mStandControlList = createStandControlList()

        // 각 요소들의 위치정보를 가져온다.
        getItemPosition(binding.iceRoom, 0)
        getItemPosition(binding.fireRoom, 1)
        getItemPosition(binding.masa, 2)
        getItemPosition(binding.light, 3)
        getItemPosition(binding.stand1, 4)
        getItemPosition(binding.stand2, 5)
        getItemPosition(binding.stand3, 6)
        getItemPosition(binding.water, 7)
        getItemPosition(binding.egg, 8)
        getItemPosition(binding.waste, 9)

        getJudyInitPosition()

        // 쥬디가 움직일 수 있는 곳을 클릭했을 때, 쥬디가 갈 경로정보를 저장하고 한곳씩 움직이는 애니메이션을 작동한다.
        binding.water.setOnClickListener {
            if (indexToCheckBox(4).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(4)
            }
        }
        binding.waste.setOnClickListener {
            if (indexToCheckBox(6).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(6)
            }
        }
        binding.egg.setOnClickListener {
            if (indexToCheckBox(5).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(5)
            }
        }
        binding.fireRoom.setOnClickListener {
            if (indexToCheckBox(1).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(1)
            }
        }
        binding.iceRoom.setOnClickListener {
            if (indexToCheckBox(0).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(0)
            }
        }
        binding.masa.setOnClickListener {
            if (indexToCheckBox(2).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(2)
            }
        }
        binding.light.setOnClickListener {
            if (indexToCheckBox(3).visibility != ImageView.VISIBLE) {
                judyAnimationAdd(3)
            }
        }

        gameStart()
    }

    // 각 요소의 위치를 설정한다.
    private fun getItemPosition(view: View, index: Int){
        val viewTreeObserver = view.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                mItemPosition[index].x = view.x
                mItemPosition[index].y = view.y
                mItemPosition[index].width = view.measuredWidth
                mItemPosition[index].height = view.measuredHeight

                when(index){
                    in 0..1 -> {
                        // 얼음방, 불꽃방
                        mItemControlList[index].setItemPosition(view.x, view.y, view.measuredWidth, view.measuredHeight)
                        mCustomerExistPosition[index].x = if(index == 1) view.x else view.x + view.measuredWidth/2
                        mCustomerExistPosition[index].y = view.y + view.measuredHeight/2
                    }
                    in 2..3 -> {
                        // 마사지, 라이트
                        mItemControlList[index].setItemPosition(view.x, view.y, view.measuredWidth, view.measuredHeight)
                        mCustomerExistPosition[index].x = view.x
                        mCustomerExistPosition[index].y = view.y
                    }
                    in 4..6 -> {
                        // 의자일 경우 - 의자1, 의자2, 의자3
                        mStandControlList[index - 4].setPosition(view.x, view.y, view.measuredWidth, view.measuredHeight)
                        mCustomerExistPosition[index].x = view.x - view.measuredWidth/3
                        mCustomerExistPosition[index].y = view.y - view.measuredHeight * 1.5F
                    }
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    // 쥬디의 초기위치 가져오기
    private fun getJudyInitPosition(){
        val viewTreeObserver = binding.judy.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                mJudyAnimation = JudyAnimation(binding.judy, mJudyclickQueue, this@GameActivity)
                mJudyAnimation.setJudyInitPosition(binding.judy.x, binding.judy.y)
                binding.judy.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    // 게임을 시작한다.
    private fun gameStart(){
        mCustomerStartCount = 0
        Thread(){
            while(mCustomerStartCount < mCustomerNum){
                if(mfinish){
                    // 게임 종료
                    break
                }
                var position = -1
                // 손님은 6초마다 한번씩 오는데 남은 의자가 있으면 가게로 손님이 들어오고 의자가 없으면 다시 8초뒤를 기다린다.
                Handler(Looper.getMainLooper()).post {
                    // 손님이 올 수 있는지 체크한다. 의자1, 의자2, 의자3 에 손님이 있는지 없는지 확인한다.
                    // 비어있으면 -1이므로 position 변수에 앞으로 오시는 손님의 초기위치를 정한다. (의자1, 의자2, 의자3 중 하나)
                    if(mItemExistCheck[4] == -1) position = 0
                    else if(mItemExistCheck[5] == -1) position = 1
                    else if(mItemExistCheck[6] == -1) position = 2
                    if(position != -1) {
                        // 손님이 올 수 있으면(남은 의자가 있으면) 손님을 가게로 들인다.
                        createCustomer(binding.MainParent, mCustomerStartCount, position)
                        mCustomerStartCount++
                    }
                }
                Thread.sleep(6000)
            }
        }.start()
    }

    private fun createCustomer(parent: ViewGroup, num: Int, position: Int) {
        // position : 손님이 앉을 수 있는 의자 번호이다. 0 : stand1 , 1 : stand2 , 2 : stand3
        // num : 손님 번호이다.
        val itemParent = LinearLayout(this)
        itemParent.id = nPL + num
        itemParent.orientation = LinearLayout.HORIZONTAL
        itemParent.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val characterBody = ImageView(this)
        characterBody.id = nBL + num
        characterBody.layoutParams = LinearLayout.LayoutParams(180, 270)
        characterBody.setImageResource(R.drawable.charactersit)

        // 받을 서비스 선택 (얼음방, 불꽃방, 마사지, 라이트)
        val number = mCustomerRunControl[num].nextRun(-1)
        // 선택한 서비스의 이미지 리소스 (얼음방, 불꽃방, 마사지, 라이트, 계산)
        val res = mCustomerRunControl[num].indexToResource(number)

        // characterImage : 받을 서비스를 적용할 이미지뷰
        val characterImage = ImageView(this)
        characterImage.id = nIL + num
        characterImage.setImageResource(res)
        val characterImageLayoutParams = RelativeLayout.LayoutParams(80, 80)
        characterImageLayoutParams.topMargin = 30
        characterImageLayoutParams.marginStart = 25
        characterImage.layoutParams = characterImageLayoutParams
        characterImage.setTag(number)  // 받을 서비스 저장

        val characterTimer = ImageView(this)
        characterTimer.id = nProL + num
        characterTimer.setImageResource(R.drawable.question)
        characterTimer.setTag(0)
        val layoutParams = RelativeLayout.LayoutParams(130, 160)
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        characterTimer.layoutParams = layoutParams

        setCustomerOnTouchListener(itemParent, characterBody, characterImage, characterTimer, num)


        val itemRelativeLayout = RelativeLayout(this)
        itemRelativeLayout.id = nRL + num
        itemRelativeLayout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        itemRelativeLayout.addView(characterTimer)
        itemRelativeLayout.addView(characterImage)

        itemParent.addView(characterBody)
        itemParent.addView(itemRelativeLayout)

        parent.addView(itemParent)

        // 캐릭터 위치 초기화 새로 오신 손님은 의자에만 앉아있을 수 있다. 의자는 4, 5, 6 중 하나이다.
        mCustomPosition[num] = position + 4
        mItemExistCheck[position + 4] = num
        itemParent.x = mCustomerExistPosition[position + 4].x
        itemParent.y = mCustomerExistPosition[position + 4].y
        customerTimerThreadStart(num, characterTimer)
    }

    private fun customerTimerThreadStart(num : Int, runItemValueView : ImageView){
        // num : 손님 번호
        val thread1 = Thread(){
            for(i in 1..4){
                // 8초에 한번씩 손님이 기다릴 수 있는 인내심이 떨어진다.
                // 총 3번 참을 수 있다. 4번이 되면 손님의 인내심이 바닥나고 게임은 종료된다.
                for(time in 1..80){
                    Thread.sleep(100)
                    if(mCustomPosition[num] == -1 || mfinish){
                        // 손님이 제시간내에 서비스를 다 받아서 가게를 나간 상태, 이미 손님 캐릭터도 지워졌다.
                        break
                    }
                }
                if(mCustomPosition[num] == -1 || mfinish){
                    // 손님이 제시간내에 서비스를 다 받아서 가게를 나간 상태, 이미 손님 캐릭터도 지워졌다.
                    break
                } else {
                    handler.post {
                        val questionImage = when(i){
                            1 -> R.drawable.questionone
                            2 -> R.drawable.questiontwo
                            3 -> R.drawable.questionthree
                            else -> R.drawable.question
                        }
                        runItemValueView.setImageResource(questionImage)
                        runItemValueView.setTag(i)    // 손님의 인내심 게이지가 몇단계인지 저장
                        if(mCustomPosition[num] == 2){
                            // 손님이 마사지 서비스를 받고 있을 경우 gameActivity 의 question 을 제어해야 한다.
                            binding.masaQuestion.setImageResource(questionImage)
                        }
                    }
                }

                if(mfinish) {
                    // 이미 다른 손님의 욕구를 충족시키지 못해 게임이 끝난 상태
                    break
                } else if(i == 4){
                    // 이 손님(num)의 욕구를 시간내에 충족시키지 못해 타임 오버로 게임이 종료되었다.
                    mfinish = true
                    handler.post {
                        runItemValueView.setImageResource(R.drawable.question_finish)
                        // Toast.makeText(this, "the game is over!", Toast.LENGTH_SHORT).show()
                        gameFinish()
                    }
                    break
                }
            }
        }
        thread1.start()
        // Log.d("thread", "thread state  :  ${thread1.state}")
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setCustomerOnTouchListener(character: View, bodyImage: ImageView, itemImage: ImageView, timerView: ImageView, num: Int){
        // num = 손님번호
        character.setOnTouchListener(View.OnTouchListener { v, event ->

            val parentWidth = (v.parent as ViewGroup).width // 부모 View 의 Width
            val parentHeight = (v.parent as ViewGroup).height // 부모 View 의 Height

            // itemImage 의 태그에는 받을 서비스의 번호가 들어있다.
            // 받을 서비스 : [-1]계산 , [0]얼음방, [1]불꽃방, [2]마사지, [3]라이트
            // 받을 서비스가 계산이면 손님은 움직이지 못한다.
            if (itemImage.getTag() != -1) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    // 뷰 누름, 손님을 이동시킬때 손님은 일어서있다.
                    bodyImage.setImageResource(R.drawable.characterstand)
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    // 뷰 이동 중, 손님도 같이 움직여야 한다.
                    v.x = v.x + event.x - v.width / 2
                    v.y = v.y + event.y - v.height / 2

                    // 각 요소(서비스)들의 상태 확인 (각 요소 : 0 - 얼음방, 1 - 불꽃방, 2 - 마사지, 3 - 라이트)
                    // 손님이 현재 받을 수 있는 서비스에 다가가면 그 서비스는 하얀색테두리가 둘러진 그림(clickImage)으로 바뀌여야 한다.
                    // 혹은 손님이 서비스 영역에 없는데도 clickImage 일 경우 originImage 로 바꿔야 한다.
                    for (i in 0..3) {
                        if (mItemExistCheck[i] == -1 && mItemControlList[i].checkItemIn(v.x, v.y)) {
                            // 현재 서비스 받고 있는 손님이 없고, 서비스 영역 안에 손님이 들어가 있음
                            if (!mItemControlList[i].isChangeImage) {
                                // 손님이 서비스 영역 안에 들어가 있는데 originImage 일 경우 -> clickImage 로 바꾼다.
                                mItemControlList[i].changeImage(2)
                            }
                        } else if (mItemControlList[i].isChangeImage) {
                            // 손님이 서비스 영역 안에 뷰가 안들어가 있는데 clickImage 일 경우 -> originImage 로 바꾼다.
                            mItemControlList[i].changeImage(1)
                        }
                    }
                    // 손님이 의자 영역에 있으면 의자는 clickImage 로 바뀌어야 하고,
                    // 손님이 의자 영역에 있지 않으면 originImage 로 바뀌어야 한다.
                    for (i in 0..2) {
                        if (mItemExistCheck[i + 4] == -1 && mStandControlList[i].checkItemIn(ItemPoint(v.x, v.y, v.measuredWidth, v.measuredHeight))) {
                            // 의자에 앉아있는 손님은 없고, 의자 영역 안에 손님이 들어가 있음
                            if (!mStandControlList[i].isChangeImage) {
                                // 손님이 의자 영역 안에 들어가 있는데 originImage 일 경우 -> clickImage 로 바꾼다
                                mStandControlList[i].changeImage(2)
                            }
                        } else if (mStandControlList[i].isChangeImage) {
                            // 서비스 영역 안에 손님이 없는데 clickImage 일 경우 -> originImage
                            mStandControlList[i].changeImage(1)
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    // 뷰에서 손을 뗌
                    if (v.x < 0) {
                        v.setX(0F)
                    } else if (v.x + v.width > parentWidth) {
                        v.x = (parentWidth - v.width).toFloat()
                    }
                    if (v.y < 0) {
                        v.setY(0F)
                    } else if (v.y + v.height > parentHeight) {
                        v.y = (parentHeight - v.height).toFloat()
                    }

                    if(mCustomPosition[num] in 0..3 && mItemControlList[mCustomPosition[num]].checkItemIn(v.x, v.y)){
                        // 손님의 원래 위치가 서비스 영역(얼음방, 불꽃방, 마사지, 라이트)이었고, 이동 후에도 그 서비스 영역이었을 경우
                        // 쥬디가 그 서비스 영역으로 이동하라는 것으로 인식한다. (쥬디는 서비스 영역(얼음방, 불꽃방, 마사지, 라이트) 을 클릭할 경우 그쪽으로 이동해야하기 때문)
                        if (indexToCheckBox(mCustomPosition[num]).visibility != ImageView.VISIBLE) {
                            judyAnimationAdd(mCustomPosition[num])
                        }
                    } else {
                        var flag = false   // 손님이 위치를 이동할 수 있을 경우 : true, 위치를 이동할 수 없을 경우 : false
                        // 뷰에서 손을 땠을 때 clickImage -> originImage 로 바꾸기
                        // 손님이 서비스(얼음방, 불꽃방, 마사지, 라이트)를 받을 경우 progressbar 실행한다.
                        for (i in 0..3) {
                            if (mItemControlList[i].checkItemIn(v.x, v.y)) {
                                // 서비스 영역 안에 손님이 들어가 있음 -> 다른 서비스 검사할 필요 없음. (break)
                                if (mItemControlList[i].isChangeImage) {
                                    // 손님이 서비스를 받는다!
                                    mItemControlList[i].changeImage(1)  // originImage 로 바꾼다.
                                    if (!mItemControlList[i].lock && itemImage.getTag() == i) {
                                        // 손님이 서비스를 받을 거기 때문에 이전에 위치했던 장소는 비어야 한다.
                                        mItemExistCheck[mCustomPosition[num]] = -1
                                        // 손님의 현재 위치를 받을 서비스 위치로 한다.
                                        mCustomPosition[num] = i
                                        // 서비스 영역에 손님이 있다는 표시를 해준다. (num) -> 없으면 -1이다.
                                        mItemExistCheck[i] = num
                                        flag = true  // 손님은 위치를 이동했다는 표시이다.
                                        if(i == 2){
                                            // 만약 손님이 마사지 서비스를 받게 되는 경우 손님은 물 혹은 달걀을 요청한다.
                                            character.visibility = View.GONE
                                            binding.masa.setImageResource(R.drawable.masaquestion)
                                            binding.masaStandQuestion.visibility = View.VISIBLE
                                            binding.masaQuestion.setImageResource(when(timerView.getTag()){
                                                1 -> R.drawable.questionone
                                                2 -> R.drawable.questiontwo
                                                3 -> R.drawable.questionthree
                                                else -> R.drawable.question
                                            })
                                            if(Random().nextInt(2) == 0) {
                                                // water = 0
                                                binding.masaQuestionValue.setImageResource(R.drawable.wateritem)
                                                binding.masaQuestionValue.setTag(0)
                                            }
                                            else {
                                                // egg = 1
                                                binding.masaQuestionValue.setImageResource(R.drawable.eggitem)
                                                binding.masaQuestionValue.setTag(1)
                                            }
                                            // 나중에 마사지 서비스를 받기 전 물 혹은 달걀 서비스를 받으면 실행될 마사지의 진행바(프로그래스바)의 정보이다.
                                            // 마사지 기계는 1대이므로 단일 변수로 저장시켜도 된다.
                                            snackToProgress = SnackToProgress(character, itemImage, num, i)
                                        }
                                        else {
                                            // 얼음방, 불꽃방, 라이트 서비스를 받게 될 경우, 바로 서비스를 받는다.
                                            // 서비스를 받는 동안 게이지가 줄어드는 표시(progressbar)를 설정한다.
                                            mItemControlList[i].progressThread(character, itemImage, num, i)
                                        }
                                    }
                                }
                                break
                            }
                        }
                        // 손님이 의자 영역에 있었던 경우, 의자에 앉아있는 손님이 없었던 경우엔 의자에 앉혀야 한다.
                        if (!flag) {
                            for (k in 0..2) {
                                if (mStandControlList[k].isChangeImage && mItemExistCheck[k + 4] == -1) {
                                    // 손님이 의자 영역에 있었고, 의자에 앉아있는 손님이 없었던 경우
                                    // 손님을 의자에 앉힌다.
                                    mStandControlList[k].changeImage(1)  // originImage 로 바꾸기
                                    character.x = mCustomerExistPosition[k + 4].x
                                    character.y = mCustomerExistPosition[k + 4].y
                                    bodyImage.setImageResource(R.drawable.charactersit)

                                    mItemExistCheck[k + 4] = num  // 이 의자에 손님이 앉았으니 손님 번호를 넣어줌
                                    // 손님이 이 의자에 앉을 것이므로 이전에 있었던 곳엔 이제 아무도 없으니 -1로 공백으로 둔다.
                                    mItemExistCheck[mCustomPosition[num]] = -1
                                    // 손님의 현재 위치를 이 의자위치로 바꾼다.
                                    mCustomPosition[num] = k + 4
                                    flag = true
                                    break
                                }
                            }
                        }
                        // 손님은 서비스 영역(얼음방, 불꽃방, 마사지, 라이트) 혹은 의자(1,2,3) 에만 갈 수 있다.
                        // 따라서 손님이 어중간한 장소에 놓여지거나 이미 서비스 영역 혹은 의자(1,2,3)에 먼저 온 손님이 있어
                        // 위치를 옮기지 못하는 경우, 이전에 있던 위치로 되돌아간다.
                        if (!flag) {
                            if (mCustomPosition[num] >= 4) {
                                // 손님이 이전에 있었던 곳이 의자였던 경우 앉아있는 모습으로 바꾼다.
                                bodyImage.setImageResource(R.drawable.charactersit)
                            }
                            if(mCustomPosition[num] != -1){
                                // 현재 손님이 원래 있던 곳으로 되돌아간다.
                                character.x = mCustomerExistPosition[mCustomPosition[num]].x
                                character.y = mCustomerExistPosition[mCustomPosition[num]].y
                            }
                        }
                    }
                }
            } else {
                // 손님이 계산을 기다리고 있는 경우 위치를 움직이지 못한다.
                if (event.action == MotionEvent.ACTION_UP) {
                    for (i in 0..3) {
                        if (mItemControlList[i].checkItemIn(v.x, v.y)) {
                            // 만약 손님이 있는 곳이 서비스 기구들(얼음방, 불꽃방, 마사지, 라이트) 영역에 있고,
                            // 손님이 계산을 기다리고 있을 경우 손님이 계신곳은 곳 서비스 기구들이고 서비스 기구들은
                            // 쥬디가 움직일 수 있는 영역 중 한 곳을 클릭한 셈이니 쥬디가 갈 경로로 추가해준다.
                            if (indexToCheckBox(i).visibility != ImageView.VISIBLE) {
                                judyAnimationAdd(i)
                            }
                            break
                        }
                    }
                }
            }
            true
        })
    }

    // 각각의 손님이 받고있던 서비스를 전부 다 받고 다음 서비스를 선택할 때 실행되는 함수
    fun nextRunItem(now: Int, num: Int, itemImageView: ImageView) {
        // now 는 현재 Run 한 것(현재 받은 서비스), num 은 character (-1 ~ 3 중 하나 : 계산, 얼음방, 불꽃방, 마사지, 라이트)
        val number = mCustomerRunControl[num].nextRun(now)
        val res = mCustomerRunControl[num].indexToResource(number)
        itemImageView.setImageResource(res)
        itemImageView.setTag(number)  // 받을 서비스 번호 저장
    }

    private fun createItemControlList() : ArrayList<EventItemControl>{
        // [0] 얼음방, [1] 불꽃방 , [2] 마사지 , [3] 라이트
        val array = ArrayList<EventItemControl>()
        array.add(EventItemControl(R.drawable.iceroom, R.drawable.iceroomclick, this))
        array.add(EventItemControl(R.drawable.fire, R.drawable.fireclick, this))
        array.add(EventItemControl(R.drawable.masa, R.drawable.masaclick, this))
        array.add(EventItemControl(R.drawable.light, R.drawable.lightclick, this))

        array[0].setView(binding.iceRoom, binding.progressIceRoom, R.drawable.iceroom, handler)
        array[1].setView(binding.fireRoom, binding.progressFireRoom, R.drawable.fire, handler)
        array[2].setView(binding.masa, binding.progressMasa, R.drawable.msasrun, handler)
        array[3].setView(binding.light, binding.progressLight, R.drawable.lightrun, handler)
        return array
    }

    private fun createStandControlList() : ArrayList<EventStandControl>{
        val array = ArrayList<EventStandControl>()
        array.add(EventStandControl(binding.stand1))   // 의자1
        array.add(EventStandControl(binding.stand2))   // 의자2
        array.add(EventStandControl(binding.stand3))   // 의자3
        return array
    }

    fun indexToCheckBox(version: Int): ImageView {
        return when (version) {
            0 -> binding.checkBoxIceRoom
            1 -> binding.checkBoxFireRoom
            2 -> binding.checkBoxMasa
            3 -> binding.checkBoxLight
            4 -> binding.checkBoxWater
            5 -> binding.checkBoxEgg
            6 -> binding.checkBoxWaste
            else -> binding.checkBoxIceRoom
        }
    }

    private fun judyAnimationAdd(version: Int) {
        // 쥬디가 움직여야 할 곳을 표시한다. (체크표시)
        indexToCheckBox(version).visibility = ImageView.VISIBLE
        // 쥬디가 움직여야하는 경로를 담고있는 큐에 경로를 추가한다.
        mJudyclickQueue.add(version)
        // 쥬디가 움직이지 않고 있었는데 새로운 경로를 추가한 경우
        if (mJudyclickQueue.size == 1 && !mJudyAnimation.animationFlag) {
            // 쥬디가 직접 움직이도록 해준다.
            judyAnimationStart(version)
        }
    }

    fun judyAnimationStart(version: Int) {
        // version : 0. iceRoom 1. FireRoom 2. masa 3. light 4. water 5. egg 6. waste
        // mItemPosition : [0]얼음방, [1]불꽃방, [2]마사지, [3]라이트, [4]의자1 , [5]의자2, [6]의자3, [7] 물 [8] 달걀 [9] 쓰레기통

        when(version){
            0 -> mJudyAnimation.setPositionValue(version, mItemPosition[version].x + mItemPosition[version].width/2, mItemPosition[version].y + mItemPosition[version].height/2)
            1 -> mJudyAnimation.setPositionValue(version, mItemPosition[version].x + mItemPosition[version].width/3, mItemPosition[version].y + mItemPosition[version].height/2)
            2 -> mJudyAnimation.setPositionValue(version, mItemPosition[version].x + mItemPosition[version].width/2, mItemPosition[version].y + mItemPosition[version].height/2)
            3 -> mJudyAnimation.setPositionValue(version, mItemPosition[version].x - mItemPosition[version].width/2, mItemPosition[version].y + mItemPosition[version].height/2)
            else -> {
                mJudyAnimation.setPositionValue(version, mItemPosition[version + 3].x - mItemPosition[version + 3].width, mItemPosition[version + 3].y)
            }
        }
        mJudyAnimation.startAnimation()
    }

    fun getCustomerMoneyOrSnackCheck(runNum: Int) {
        if(runNum == 2 && binding.masaStandQuestion.visibility == View.VISIBLE){
            // 마사지 기계로 갔다면,
            if(binding.masaQuestionValue.getTag() == 0){
                // 손님이 물(water)을 요청했을 경우
                if (snackControl.getWaterCount() >= 1) {
                    // 왼손 혹은 오른손 중에 물이 있으므로 손님에게 물을 건네줄 경우
                    if(snackControl.getRightValue() == 0){
                        // 오른손에 water 있음 (water = 0)
                        // 손님에게 물을 줬으니 오른손은 비워있고 쥬디가 가지고 있는 총 물의 개수에서 1만큼 줄어든다.
                        snackControl.subRightWater()
                        binding.judyRight.setImageResource(0)    // 쥬디의 오른손은 빈손이 된다.
                    } else if(snackControl.getLeftValue() == 0){
                        // 왼손에 water 있음
                        // 손님에게 물을 줬으니 오른손은 비워있고 쥬디가 가지고 있는 총 물의 개수에서 1만큼 줄어든다.
                        snackControl.subLeftWater()
                        binding.judyLeft.setImageResource(0)    // 쥬디의 왼손은 빈손이 된다.
                    }
                    // 마사지 기계에 있는 손님이 물 서비스를 받았으니 서비스 요청 창은 사라진다.
                    binding.masaStandQuestion.visibility = View.GONE
                    // 손님이 물 서비스를 받고 난 뒤, 마사지 서비스를 받는다. 이 때 진행바(프로그레스바)도 같이 실행된다.
                    mItemControlList[snackToProgress!!.service].progressThread(snackToProgress!!.character, snackToProgress!!.itemImageView, snackToProgress!!.num, snackToProgress!!.service)
                }
            }
            else if(binding.masaQuestionValue.getTag() == 1){
                // 손님이 달걀(egg)을 요청했을 경우
                if (snackControl.getEggCount() >= 1) {
                    // 왼손 혹은 오른손 중에 달걀(egg)이 있으므로 손님에게 달걀을 건네줄 경우
                    if(snackControl.getRightValue() == 1){
                        // 오른손에 egg 있음 (egg = 1)
                        // 손님에게 달걀을 줬으니 오른손은 비워있고 쥬디가 가지고 있는 총 달걀의 개수에서 1만큼 줄어든다.
                        snackControl.subRightEgg()
                        binding.judyRight.setImageResource(0)    // 쥬디의 오른손은 빈손이 된다.
                    } else if(snackControl.getLeftValue() == 1){
                        // 왼손에 egg 있음
                        // 손님에게 달걀을 줬으니 오른손은 비워있고 쥬디가 가지고 있는 총 달걀의 개수에서 1만큼 줄어든다.
                        snackControl.subLeftEgg()
                        binding.judyLeft.setImageResource(0)    // 쥬디의 왼손은 빈손이 된다.
                    }
                    // 마사지 기계에 있는 손님이 달걀 서비스를 받았으니 서비스 요청 창은 사라진다.
                    binding.masaStandQuestion.visibility = View.GONE
                    // 손님이 달걀 서비스를 받고 난 뒤, 마사지 서비스를 받는다. 이 때 진행바(프로그레스바)도 같이 실행된다.
                    mItemControlList[snackToProgress!!.service].progressThread(snackToProgress!!.character, snackToProgress!!.itemImageView, snackToProgress!!.num, snackToProgress!!.service)
                }
            }
        }
        else {
            val num = mItemExistCheck[runNum]  // 서비스 영역에 손님이 있을경우, num = 손님 번호
            //Log.d("number", "기능 번호 : $runNum   숫자 : $num  position : ${mPositionChacterCheck[num]}")
            if (num != -1) {
                // 서비스 영역에 손님이 있을경우
                val itemImage = findViewById<ImageView>(nIL + num)   // 그 서비스 영역에 있는 손님이 요청한 서비스의 그림 view
                val itemImageTagValue = itemImage.getTag()
                if (itemImageTagValue == -1) {
                    // 원래 손님이 계산을 요청했던 경우 계산을 해주었으니 게임 점수가 올라가고
                    // 손님은 계산을 끝마쳤으니 사라질 것이다.
                    val item = findViewById<RelativeLayout>(nRL + num)
                    val parent = findViewById<LinearLayout>(nPL + num)
                    mItemExistCheck[runNum] = -1        // 손님이 갔으니 이 서비스 영역에는 손님이 없을 것이다.
                    mCustomPosition[num] = -1    // 손님이 갔으니 손님의 현재 위치는 -1이다.
                    item.removeAllViews()       // 손님 사라짐
                    parent.removeAllViews()     // 손님 사라짐
                    mCustomerFinishCount++     // 계산을 마친 손님의 수
                    // 손님의 욕구를 충족시켜주고 계산까지 마쳤으니 점수가 올라간다.
                    val score = "${mCustomerFinishCount*10} 점"
                    binding.score.text = score
                }
            }
        }
    }

    fun snackClickControl(clickNum: Int) {
        /* clickNum : 4. water , 5. egg , 6. waste
        * (water) 쥬디가 물에 다가간 경우, 비워있는 손에 물을 집는다.
        * (egg) 쥬디가 달걀에 다가간 경우, 비워있는 손에 달걀을 집는다.
        * (waste) 쥬디가 쓰레기통에 다가간 경우, 들고있던 물건(물, 달걀)을 전부 버린다.
        * */
        if (clickNum == 6) {
            snackControl.clear()
            binding.judyRight.setImageResource(0)
            binding.judyLeft.setImageResource(0)
        }
        if (snackControl.count() < 2) {
            if (clickNum == 4) {
                if (snackControl.getRightCheck()) {
                    // 오른손 뭐 들었으므로 왼손으로 물을 집는다.
                    snackControl.addLeftWater()
                    binding.judyLeft.setTag(4)
                    binding.judyLeft.setImageResource(R.drawable.wateritem)
                } else {
                    // 오른손이 비었으니 오른손으로 물을 집는다.
                    snackControl.addRightWater()
                    binding.judyRight.setTag(4)
                    binding.judyRight.setImageResource(R.drawable.wateritem)
                }
            } else if (clickNum == 5) {
                if (snackControl.getRightCheck()) {
                    // 오른손 뭐 들었으므로 왼손으로 달걀을 집는다.
                    binding.judyLeft.setTag(5)
                    snackControl.addLeftEgg()
                    binding.judyLeft.setImageResource(R.drawable.eggitem)
                } else {
                    // 오른손이 비었으니 오른손으로 달걀을 집는다.
                    binding.judyRight.setTag(5)
                    snackControl.addRightEgg()
                    binding.judyRight.setImageResource(R.drawable.eggitem)
                }
            }
        }
    }

    private fun gameFinish(){
        // 게임 종료 다이얼로그를 띄운다.
        val dialog = GameoverDialog(this, mCustomerFinishCount*10)
        dialog.show(supportFragmentManager, "gameover")
    }

    fun startRankActivity() {
        val intent = Intent(this, RankActivity::class.java).apply {
            putExtra("score", mCustomerFinishCount*10)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        mfinish = false
        super.onDestroy()
    }
}