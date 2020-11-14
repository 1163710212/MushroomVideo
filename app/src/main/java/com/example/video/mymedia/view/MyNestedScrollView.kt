package com.example.video.mymedia.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class MyNestedScrollView(context: Context, attributeSet: AttributeSet) :
    NestedScrollView(context, attributeSet){
    var isVideoTouched = false
    private var onScrollChangeListener: OnScrollChangeListener? = null
    interface OnScrollChangeListener {
        fun onScrollChange(left: Int, top: Int, oldLeft: Int, oldTop: Int)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollChangeListener?.onScrollChange(l, t, oldl, oldt)
    }

    fun setOnScrollChangeListener(onScrollChangeListener: OnScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isVideoTouched) return false
        return super.onInterceptTouchEvent(ev)
    }


}