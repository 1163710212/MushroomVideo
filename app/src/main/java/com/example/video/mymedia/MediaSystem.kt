package com.example.video.mymedia

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.util.Log
import android.view.Surface
import android.view.TextureView
import kotlin.concurrent.thread

class MediaSystem : TextureView.SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnPreparedListener {
    private var mPlayer: MediaPlayer? = MediaPlayer()
    private var afterPreparingListener: AfterPreparingListener? = null
    private var progressChangeListener: ProgressChangeListener? = null
    private var duration = 0

    companion object {
        var SURFACE: SurfaceTexture? = null
    }

    fun start() {
        mPlayer?.start()
    }

    fun pause() {
        mPlayer?.pause()
    }

    fun release() {
        mPlayer?.release()
        mPlayer = null
    }

    fun seekTo(percent: Int) {
        val time = (duration * percent / 100f).toInt()
        mPlayer?.seekTo(time)
    }

    fun getDuration() = mPlayer?.duration

    fun getCurrentPosition() = mPlayer?.currentPosition

    fun prepare(url: String) {
        if (mPlayer == null) {
            Log.d("mushroom", "mPlayer rebuild")
            mPlayer = MediaPlayer()
        }
        thread {
            mPlayer?.apply {
                reset()
                setSurface(Surface(SURFACE))
                setDataSource(url)
                //媒体装载完成回调
                setOnPreparedListener(this@MediaSystem)
                //播放缓存进度变化回调
                setOnBufferingUpdateListener(this@MediaSystem)
                //屏幕保持唤醒状态
                setScreenOnWhilePlaying(true)
                mPlayer?.prepareAsync()
            }
        }
    }

    interface AfterPreparingListener {
        fun afterPreparing(mediaPlayer: MediaPlayer)
    }

    interface ProgressChangeListener {
        fun onProgressChange(percent: Int)
    }

    fun setAfterLoadingListener(afterPreparingListener: AfterPreparingListener) {
        this.afterPreparingListener = afterPreparingListener
    }

    fun setProgressChangeListener(progressChangeListener: ProgressChangeListener) {
        this.progressChangeListener = progressChangeListener
    }

    override fun onPrepared(mp: MediaPlayer) {
        afterPreparingListener?.afterPreparing(mp)
        duration = mp.duration
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        progressChangeListener?.onProgressChange(percent)
    }

    /**
     * SurfaceTexture状态监听
     */
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        SURFACE = surface
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean { return false }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
}