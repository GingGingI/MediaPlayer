package com.example.ginggingi.mediaplayer.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.SeekBar
import com.example.ginggingi.mediaplayer.Interface.MediaControllerListener
import java.io.File

class MediaController: AppCompatActivity,
        SurfaceHolder.Callback,
        MediaPlayer.OnPreparedListener {

    private var context: Context

//    Values
    var position: Int = 0
    var duration: Int = 0
    var MediaPlaying: Boolean = false

    var vWidth: Int = 0
    var vHeight: Int = 0

    private var filePath: String
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var h: Handler

//    Views
    private var Seekbar: SeekBar
    private var Surface: SurfaceView
    private lateinit var SurfaceHolder: SurfaceHolder

//    Runnable
    private lateinit var SeekRun: Runnable

//    Callback
    private var MCListener: MediaControllerListener

    constructor(context: Context,
                filePath: String,
                Surface: SurfaceView,
                Seekbar: SeekBar,
                MCListener: MediaControllerListener) {
        this.context = context
        this.filePath = filePath
        this.Surface = Surface
        this.Seekbar = Seekbar
//        Callback
        this.MCListener = MCListener
//        Initialize MediaPlayer
        initialize()
    }

    constructor(context: Context,
                filePath: String,
                position: Int,
                Surface: SurfaceView,
                Seekbar: SeekBar,
                MCListener: MediaControllerListener) {
        this.context = context
        this.filePath = filePath
        this.position = position
        this.Surface = Surface
        this.Seekbar = Seekbar
//        Callback
        this.MCListener = MCListener
//        Initialize MediaPlayer
        initialize()
    }

    private fun initialize() {
//      Handler Init
        h = Handler()

//        Media&Surface Init
        mediaPlayer = MediaPlayer()
        SurfaceHolder = Surface.holder
        SurfaceHolder.addCallback(this)
    }

    fun StartMedia() {
        mediaPlayer!!.start()
        MediaPlaying = true
        MCListener.onStart()
    }

    fun PauseMedia() {
        mediaPlayer!!.pause()
        MediaPlaying = false
        MCListener.onPause()
    }

    fun releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    fun SetSeek(position: Int) {
        mediaPlayer!!.seekTo(position)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }
    override fun surfaceDestroyed(holder: SurfaceHolder?) {

    }
    override fun surfaceCreated(holder: SurfaceHolder?) {
        mediaPlayer!!.setDisplay(holder)
        try {
            if (File(filePath).exists()) {
                mediaPlayer!!.setDataSource(
                        context,
                        Uri.parse(File(filePath).path)
                )
                mediaPlayer!!.setOnPreparedListener(this)
                mediaPlayer!!.prepare()
            }
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

//  mediaPlayer가 실행될 준비가 되었을 시.
    override fun onPrepared(mp: MediaPlayer?) {
        if (position != 0)
            mediaPlayer!!.seekTo(position)

        duration = mediaPlayer!!.duration

        vWidth = mediaPlayer!!.videoWidth
        vHeight = mediaPlayer!!.videoHeight

        Seekbar.max = duration
        MCListener.onMediaInit()
        SeekbarRun()
        StartMedia()
    }

//    받은 Seekbar 를 MediaPlayer에맞게 SetProgress하는 메서드
    private fun SeekbarRun() {
        SeekRun = Runnable {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying) {
                    position = mediaPlayer!!.currentPosition
                    Seekbar.setProgress(position)
                }
            }
            h.postDelayed(SeekRun, 50)
        }
        h.post(SeekRun)
    }



}