package com.example.ginggingi.mediaplayer

import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.example.ginggingi.mediaplayer.Models.PermissionModels
import com.example.ginggingi.mediaplayer.utils.PermissionChker
import kotlinx.android.synthetic.main.activity_media.*

class MediaActivity : AppCompatActivity() ,
        SurfaceHolder.Callback, View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, SeekBar.OnSeekBarChangeListener {
    private var ChkMediaInit: Boolean = false
    private var AddonCondition: Boolean = false

    lateinit var surfaceView: SurfaceView
    lateinit var surfaceHolder: SurfaceHolder
    lateinit var mediaPlayer: MediaPlayer
    lateinit var handler: Handler

    lateinit var seekRunnable: Runnable

    private lateinit var pChker: PermissionChker
    private lateinit var pModels: PermissionModels
    private lateinit var pList: Array<String>
    private val REQUEST_CODE = 1

    private var pressTime: Int = 0
    private var sec: Int = 3000
    var nowPosition: Int = 0
    var ChkisRotation = false
    var onSeeking = false
    var orientation: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        if (savedInstanceState != null) {
            nowPosition = savedInstanceState.getInt("CurrPos")
            ChkisRotation = true
        }

        init()
        PermissionInit()
        pChker.RequestPermission(
                this,
                pList,
                REQUEST_CODE,
                object: PermissionChker.RequestPermissionListener{
                    override fun onSuccess() {
                        AddonInit()
                        SurfaceInit()
                    }

                    override fun onFailed() {
                        Toast.makeText(applicationContext,"권한이 설정되지않았습니다.", Toast.LENGTH_SHORT).show()
                    }

                }
        )
    }

    private fun init() {
        orientation = windowManager.defaultDisplay.orientation
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt("CurrPos", mediaPlayer.currentPosition)
    }

    private fun SeekBarInit() {
        MediaProgressBar.max = mediaPlayer.duration/250
        MediaProgressBar.setOnSeekBarChangeListener(this)
        VideoDuration.setText(getTimes(mediaPlayer.duration))
        Log.i("mediaduration", getTimes(mediaPlayer.duration))
        seekRunnable = Runnable{
            if (mediaPlayer != null && mediaPlayer.isPlaying && !onSeeking) {
                MediaProgressBar.setProgress(mediaPlayer.currentPosition/250)
                CurrentPosition.setText(getTimes(mediaPlayer.currentPosition))
            }
            onSeeking = false
            handler.postDelayed( seekRunnable,250)
        }

        handler.postDelayed(seekRunnable, 250)
    }

    private fun PermissionInit() {
        pModels = PermissionModels()
        pList = arrayOf(
                pModels.readExStorage
        )
        pChker = PermissionChker()
    }

    private fun SurfaceInit() {
        mediaPlayer = MediaPlayer()
        surfaceView = SurfaceView
        surfaceView.setOnClickListener(this)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
    }

    private fun AddonInit() {
        playBtn.setOnClickListener(this)
        ScreenChanger.setOnClickListener(this)
        handler = Handler()
    }

    override fun onClick(v: View?) {
        when(v) {
            playBtn -> {
                Log.i("ClickChk", " playBtn:" + ChkMediaInit)
                if (ChkMediaInit)
                    if (mediaPlayer.isPlaying == true){
                        playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                        mediaPlayer.pause()
                    }else{
                        playBtn.setImageResource(R.drawable.ic_pause_white_24dp)
                        mediaPlayer.start()
                        hideAddons()
                    }
            }
            surfaceView -> {
                ChkUserTouch()
                if (!AddonCondition)
                    seekAddons()
            }
        }
    }

    fun ChkUserTouch() {
        if (pressTime == 0){
            sec = 3000
            pressTime = System.currentTimeMillis().toInt()
            handler.postDelayed(ChkUserTouchRunnable, 3000)
            Log.i("Sec1" , sec.toString())
        }else {
            sec = (System.currentTimeMillis() - pressTime).toInt()
            Log.i("Sec2" , sec.toString())
        }
    }

    fun seekAddons() {
        Addon.visibility = View.VISIBLE
        AddonCondition = true
    }

    fun hideAddons() {
        handler.postDelayed(hideRunnable,2000)
    }

    fun getTimes(i: Int): String {
        return String.format("%01d : %01d", (i / (1000 * 60)), (i % (1000 * 60) / 1000))
    }

    val hideRunnable = Runnable {
        kotlin.run {
            Addon.visibility = View.GONE
            AddonCondition = false
        }
    }

    val ChkUserTouchRunnable = Runnable {
        kotlin.run {
            if (sec >= 3000 && mediaPlayer.isPlaying) {
                Log.i("Sec3", sec.toString())
                pressTime = 0
                if(AddonCondition)
                    hideAddons()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pChker.RequestPermissionsResult(requestCode, pList, grantResults)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mediaPlayer.setDisplay(holder)
        try{
            mediaPlayer.setDataSource(this, Uri.parse("file:///storage/emulated/0/Download/Ramen.mp4"))
            mediaPlayer.prepare()
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnVideoSizeChangedListener(this)
            SetSurfaceViewSize()
        }catch (e: Exception){
            e.printStackTrace()
        }
        ChkMediaInit = true
    }

    private fun SetSurfaceViewSize() {

        val vW = mediaPlayer.videoWidth
        val vH = mediaPlayer.videoHeight

        val sW = windowManager.defaultDisplay.width

        SurfaceView.holder.setFixedSize(sW, ((vH.toFloat() / vW.toFloat()) * sW.toFloat()).toInt())
    }

    override fun onPrepared(mp: MediaPlayer?) {
        playBtn.setImageResource(R.drawable.ic_pause_white_24dp)
        if (ChkisRotation) {
            mediaPlayer.seekTo(nowPosition)
        }
        hideAddons()
        SeekBarInit()
    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
        SetSurfaceViewSize()
        mediaPlayer.start()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }
    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser){
            onSeeking = true
            mediaPlayer.seekTo(progress*250)
            Log.i("SeekTo",progress.toString())
        }
    }

}