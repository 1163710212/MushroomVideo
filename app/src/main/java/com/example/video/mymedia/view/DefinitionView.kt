package com.example.video.mymedia.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.video.R
import com.example.video.mymedia.MyVideoView
import com.example.video.mymedia.VideoUtil

class DefinitionView(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {
    private val view: LinearLayout
    private var urlList: List<MyVideoView.DefinitionData>? = null
    private val tvList: ArrayList<TextView> = ArrayList()
    private var isShowChoose = false
    private var defaultHeight = VideoUtil.dp2px(context,50)
    private var expandHeight = 0
    private var video: MyVideoView? = null

    init {
        view = LayoutInflater.from(context)
            .inflate(R.layout.view_definition, this) as LinearLayout
    }

    fun setDefinitionData(list: List<MyVideoView.DefinitionData>, v: MyVideoView) {
        urlList = list
        video = v

        var eHeight = 0
        for (i in list.indices) {
            eHeight += 30
            val textView = TextView(context)
            textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, VideoUtil.dp2px(context,30))
            textView.gravity = Gravity.CENTER
            textView.text = list[i].title
            textView.setTextColor(Color.parseColor("#ffffff"))
            textView.setBackgroundColor(Color.parseColor("#22000000"))
            textView.visibility = View.GONE
            textView.setOnClickListener {
                for (tv in tvList) {
                    tv.visibility = View.GONE
                }
                val lp = layoutParams
                lp.height = defaultHeight
                layoutParams = lp

                video?.changeDefinition(list[i].url)
            }
            tvList.add(textView)
            view.addView(textView, 0)
        }
        expandHeight = VideoUtil.dp2px(context, eHeight) + defaultHeight

        view.findViewById<TextView>(R.id.chooseBtn).setOnClickListener {
            if (isShowChoose) {
                for (tv in tvList)
                    tv.visibility = View.GONE
                val lp = layoutParams
                lp.height = defaultHeight
                layoutParams = lp
                view.layoutParams = lp
            } else {
                val lp = layoutParams
                lp.height = expandHeight
                layoutParams = lp
                view.layoutParams = lp
                for (tv in tvList) {
                    tv.visibility = View.VISIBLE
                    tv.isClickable = true
                }
            }
            isShowChoose = !isShowChoose
        }
    }

}