package com.example.video.mymedia

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.video.R
import com.example.video.mymedia.view.DefinitionView
import com.example.video.mymedia.view.MyNestedScrollView
import java.util.*

class MyVideoView(context: Context, attributeSet: AttributeSet)
    : ConstraintLayout(context, attributeSet), View.OnClickListener{
    val controlBtn: ImageView
    val posterImg: ImageView
    val videoShower: TextureView
    val loadingView: ProgressBar
    private val pauseBtn: ImageView
    var scrollView: MyNestedScrollView? = null
    private val videoBottomBar: View
    private val seekBar: SeekBar
    private val currentTime: TextView
    private val totalTime: TextView
    private val screenControllerBtn: View
    private val screenControllerImg: ImageView
    private val videoTopBar: View
    private val videoBackBtn: View
    private val definition: DefinitionView

    val mediaSystem: MediaSystem
    private val mAudioManager: AudioManager

    var state = STATE_PREPARING
    var screenState = SCREEN_NORMAL

    var isShowWidget = false
    private var isFirstLayout = true
    private var videoUrl: String? = null
    private var videoTimer: Timer? = null
    private var progressTimer: Timer? = null
    private var dismissTask: DismissTask? = null
    private var progressTask: ProgressTask? = null
    private var progressLock = Object()

    private var normalX = 0f
    private var normalY = 0f
    private var fullY = 0f
    private var fullHeight = 0
    private var fullWidth = 0
    var normalHeight = 0
    var normalWidth = 0
    var tinyHeight = 200
    var tinyWidth = 300
    private var currentProgress: Int? = 0

    companion object {
        const val STATE_PREPARING = 1
        const val STATE_PLAYING = 2
        const val STATE_PAUSE = 3

        const val SCREEN_NORMAL = 1
        const val SCREEN_TINY = 2
        const val SCREEN_FULL = 3
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_video, this)
        controlBtn = view.findViewById(R.id.videoControlBtn)
        posterImg = view.findViewById(R.id.posterImg)
        videoShower = view.findViewById(R.id.videoShower)
        loadingView = view.findViewById(R.id.videoLoading)
        pauseBtn = view.findViewById(R.id.videoPauseBtn)
        videoBottomBar = view.findViewById(R.id.videoBottomBar)
        seekBar = view.findViewById(R.id.videoSeekBar)
        currentTime = view.findViewById(R.id.videoCurrentTime)
        totalTime = view.findViewById(R.id.videoTotalTime)
        screenControllerBtn = view.findViewById(R.id.screenControllerBtn)
        screenControllerImg = view.findViewById(R.id.screenControllerImg)
        videoTopBar = view.findViewById(R.id.videoTopBar)
        videoBackBtn = view.findViewById(R.id.videoBackBtn)
        definition = view.findViewById(R.id.videoDefinition)


        mediaSystem = MediaSystem()
        mAudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        view.viewTreeObserver.addOnGlobalLayoutListener {
            if (isFirstLayout) {
                //获取默认布局宽高和位置
                isFirstLayout = false
                normalX = view.x
                normalY = view.y
                normalHeight = view.measuredHeight
                normalWidth = view.measuredWidth

                //获取全屏时宽高
                val decorView = (context as Activity).window.decorView
                fullHeight = decorView.measuredWidth
                fullWidth = decorView.measuredHeight
            }
        }
        initListener()
    }


    fun setUp(videoUrl: String) {
        this.videoUrl = videoUrl
    }

    fun setPosterImg(url: String) {
        Glide.with(context)
            .applyDefaultRequestOptions(RequestOptions())
            .load(url)
            .into(posterImg)
    }

    fun release() {
        cancelProgressTask()
        mediaSystem.release()
    }

    private fun setNormalScreen() {
        val lp = layoutParams
        lp.width = normalWidth
        lp.height = normalHeight
        layoutParams = lp
        rotation = 0f
        x = normalX
        y = normalY

        changWidgetUI()
        screenControllerImg.setBackgroundResource(R.drawable.icon_video_magnify)
        VideoUtil.showSystemUI(context as Activity)
        screenState = SCREEN_NORMAL
    }

    private fun setFullScreen() {
        val lp = layoutParams
        lp.width = fullWidth
        lp.height = fullHeight
        layoutParams = lp
        rotation = 90f
        val translation = (lp.height - lp.width) / 2f
        x = translation
        y = -translation + fullY

        changWidgetUI()
        screenControllerImg.setBackgroundResource(R.drawable.icon_video_shrink)
        VideoUtil.hideSystemUI(context as Activity)
        screenState = SCREEN_FULL
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        videoShower.surfaceTextureListener = mediaSystem
        controlBtn.setOnClickListener(this)
        pauseBtn.setOnClickListener(this)
        videoShower.setOnTouchListener(object : OnTouchListener {
            private var startX = 0f
            private var startY = 0f
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when {
                    event.action == MotionEvent.ACTION_DOWN && state != STATE_PREPARING -> {
                        startX = event.x
                        startY = event.y
                        scrollView?.isVideoTouched = true
                        return true
                    }
                    event.action == MotionEvent.ACTION_MOVE
                            && screenState == SCREEN_TINY -> {
                        x += (event.x - startX)
                        y += (event.y- startY)
                    }
                    event.action ==  MotionEvent.ACTION_UP && screenState != SCREEN_TINY
                            && state != STATE_PREPARING -> {
                        cancelDismissTask()
                        changWidgetUI()
                    }
                }
                return false
            }
        })
        screenControllerBtn.setOnClickListener {
            Log.d("", screenState.toString())
            if (screenState == SCREEN_NORMAL) {
                setFullScreen()
            } else {
                setNormalScreen()
            }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaSystem.seekTo(seekBar.progress)
            }
        })
        mediaSystem.setProgressChangeListener(object : MediaSystem.ProgressChangeListener {
            override fun onProgressChange(percent: Int) {
                seekBar.secondaryProgress = percent
            }
        })
        videoTopBar.setOnClickListener {
            setNormalScreen()
        }
    }

    fun setScroller(scrollView: MyNestedScrollView) {
        this.scrollView = scrollView
        scrollView.setOnScrollChangeListener(object :
            MyNestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(left: Int, top: Int, oldLeft: Int, oldTop: Int) {
                //获取全屏时的位置
                fullY = top.toFloat()
                if (top > normalHeight && state == STATE_PLAYING) {
                    val lp =  layoutParams
                    lp.width = tinyWidth
                    lp.height = tinyHeight
                    if (y == normalY) {
                        x = (fullHeight - tinyWidth).toFloat()
                        y = top.toFloat()
                    } else {
                        y += (top - oldTop)
                    }
                    layoutParams = lp
                    changeUIVisible(View.VISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE)
                    screenState = SCREEN_TINY
                } else {
                    val lp = layoutParams
                    lp.width = normalWidth
                    lp.height = normalHeight
                    y = normalY
                    x = normalX
                    layoutParams = lp
                    if (state != STATE_PLAYING) {
                        changeUIVisible(View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE)
                    } else {
                        changeUIVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE)
                    }
                    screenState = SCREEN_NORMAL
                }
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.videoControlBtn -> {
                val volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.5
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume.toInt(),0)
                when(state) {
                    STATE_PLAYING -> {
                        mediaSystem.pause()
                        state = STATE_PAUSE
                        controlBtn.setImageResource(R.drawable.icon_play)
                        cancelDismissTask()
                    }
                    STATE_PAUSE -> {
                        mediaSystem.start()
                        state = STATE_PLAYING
                        controlBtn.setImageResource(R.drawable.icon_pausel)
                        cancelDismissTask()
                        changWidgetUI()
                    }
                    STATE_PREPARING -> {
                        videoUrl?.let { mediaSystem.prepare(it) }
                        loadingView.visibility = View.VISIBLE
                        controlBtn.visibility = View.INVISIBLE
                        videoShower.isClickable = false
                        mediaSystem.setAfterLoadingListener(object : MediaSystem.AfterPreparingListener {
                            override fun afterPreparing(mediaPlayer: MediaPlayer) {
                                mediaSystem.start()
                                state = STATE_PLAYING
                                loadingView.visibility = View.GONE
                                posterImg.visibility = View.GONE
                                controlBtn.setImageResource(R.drawable.icon_pausel)
                                videoShower.isClickable = true

                                //设置视频总时长，现在只处理小于一个小时的视频
                                mediaPlayer.duration
                                val duration = mediaPlayer.duration
                                totalTime.text = transformTime(duration)

                                //改变清晰度继续从上次位置播放
                                currentProgress?.let { mediaSystem.seekTo(it) }

                                //监听播放进度变化
                                startProgressTask()
                            }
                        })
                    }
                }
            }
            R.id.videoPauseBtn -> {
                mediaSystem.pause()
                state = STATE_PAUSE
                changeUIVisible(View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE)
                controlBtn.setImageResource(R.drawable.icon_play)
                setNormalScreen()
            }
        }
    }


    inner class DismissTask : TimerTask() {
        override fun run() {
            isShowWidget = false
            post {
                changeUIVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE)
            }
        }
    }

    inner class ProgressTask : TimerTask() {
        override fun run() {
            post {
                val duration = mediaSystem.getDuration()

                val position = mediaSystem.getCurrentPosition()

                currentTime.text = position?.let { transformTime(it) }
                position?.let { it ->
                    duration?.let { _ ->
                        currentProgress = 100 * it / duration
                        currentProgress?.let { it1 -> seekBar.progress = it1 }
                    }
                }
            }
        }
    }

    class DefinitionData(val url: String, val title: String)


    private fun startProgressTask() {
        synchronized(progressLock) {
            if (progressTask == null) progressTask = ProgressTask()
            if (progressTimer == null) progressTimer = Timer()
            progressTimer?.schedule(progressTask, 0, 100)
        }

    }

    private fun cancelProgressTask() {
        synchronized(progressLock) {
            progressTask?.cancel()
            progressTimer?.cancel()
            progressTask = null
            progressTimer = null
        }
    }

    private fun cancelDismissTask() {
        dismissTask?.cancel()
        videoTimer?.cancel()
    }

    private fun changWidgetUI() {
        if (!isShowWidget) {
            dismissTask = DismissTask()
            videoTimer = Timer()
            changeUIVisible(View.INVISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE)
            isShowWidget = true
            videoTimer?.schedule(dismissTask, 3000)
        } else {
            isShowWidget = false
            changeUIVisible(View.INVISIBLE, View.INVISIBLE, View.INVISIBLE, View.INVISIBLE)
        }
    }

    fun changeUIVisible(v1: Int, v2: Int, v3: Int, v4: Int) {
        pauseBtn.visibility = v1
        controlBtn.visibility = v2
        videoBottomBar.visibility = v3
        definition.visibility = v3
        if (screenState == SCREEN_NORMAL) {
            videoTopBar.visibility = View.INVISIBLE
        } else {
            videoTopBar.visibility = v4
        }
    }

    fun setDefinitionData(list: List<DefinitionData>) {
        definition.setDefinitionData(list, this)
    }

    fun changeDefinition(url: String) {
        cancelProgressTask()
        mediaSystem.apply {
            state = STATE_PREPARING
            release()
            prepare(url)
        }
    }

    /**
     *传入时间的单位是毫秒
     */
    private fun transformTime(time: Int): String {
        val min = time / 60000
        val sec = (time / 1000) % 60
        return when {
            min < 10 && sec < 10 -> {
                "0$min:0$sec"
            }
            min >= 10 && sec >= 10 -> {
                "$min:$sec"
            }
            min <= 10 && sec >= 10 -> {
                "0$min:$sec"
            }
            else -> {
                "$min:0$sec"
            }
        }
    }
}