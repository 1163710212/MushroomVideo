package com.example.video.mymedia

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View

object VideoUtil {
    fun hideSystemUI(activity: Activity) {
        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        activity.window.decorView.systemUiVisibility = uiOptions
        /*if (Build.VERSION.SDK_INT < 16) {
            val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
            activity.window.setFlags(flag, flag)
        } else {
            val view = activity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
            view.systemUiVisibility = uiOptions
        }*/
    }

    fun showSystemUI(activity: Activity) {
        val view = activity.window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        view.systemUiVisibility = uiOptions

        /*if (Build.VERSION.SDK_INT < 16) {
            val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
            activity.window.clearFlags(flag)
        } else {
            val view = activity.window.decorView
            val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
            view.systemUiVisibility = uiOptions
        }*/
    }

    fun dp2px(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (scale * dp + 0.5f).toInt()
    }

}