package com.example.video

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.video.mymedia.MyVideoView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        myVideo.setUp("http://vjs.zencdn.net/v/oceans.mp4")
        myVideo.setScroller(myNestedScrollView)
        myVideo.setPosterImg("https://pic3.zhimg.com/v2-ddb6abbeebdb459bd2fb1cab0cca6d5b_1440w.jpg?source=172ae18b")
        val list = ArrayList<MyVideoView.DefinitionData>()
        list.apply {
            add(MyVideoView.DefinitionData("http://vjs.zencdn.net/v/oceans.mp4", "流畅"))
            add(MyVideoView.DefinitionData("http://vjs.zencdn.net/v/oceans.mp4", "高清"))
        }
        myVideo.setDefinitionData(list)



    }

    override fun onDestroy() {
        myVideo.release()
        super.onDestroy()
    }
}