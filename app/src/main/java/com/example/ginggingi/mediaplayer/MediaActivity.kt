package com.example.ginggingi.mediaplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import com.example.ginggingi.mediaplayer.Interface.MediaControllerListener
import com.example.ginggingi.mediaplayer.Models.PermissionModels
import com.example.ginggingi.mediaplayer.utils.MediaController
import com.example.ginggingi.mediaplayer.utils.PermissionChker

import kotlinx.android.synthetic.main.activity_media.*

class MediaActivity : AppCompatActivity()
        , View.OnClickListener
        , SeekBar.OnSeekBarChangeListener {

    private lateinit var MC: MediaController

    private var AddonCondition: Boolean = false
    private lateinit var params: ViewGroup.MarginLayoutParams
    lateinit var h: Handler

    private lateinit var ChkUserTouchRunnable: Runnable


    private lateinit var pChker: PermissionChker
    private lateinit var pModels: PermissionModels
    private lateinit var pList: Array<String>
    private val REQUEST_CODE = 1

    private var sec: Int = 0
    var orientation: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)
        val position = if (savedInstanceState != null) savedInstanceState.getInt("Position", 0) else 0

        init()
        PermissionInit()
        pChker.RequestPermission(
                this,
                pList,
                REQUEST_CODE,
                object: PermissionChker.RequestPermissionListener{
                    override fun onGranted() {
                        MC = MediaController(applicationContext,
                                "/storage/emulated/0/Download/Ramen.mp4",
                                position,
                                SurfaceView,
                                MediaProgressBar,
                                object : MediaControllerListener {
                                    override fun onMediaInit() {
                                        SurfaceInit()
                                        AddonInit()
                                        SeekBarInit()
                                        SetSurfaceViewSize()
                                        hideAddons()

                                        h = object : Handler() {
                                            override fun handleMessage(msg: Message?) {
                                                when(msg!!.what) {
                                                    0 -> {
                                                        CurrentPosition.setText(getTimes(MC.position))
                                                    }
                                                }
                                            }
                                        }

                                        val NowTimeThread = NowTimeThread(h)
                                        NowTimeThread.isDaemon = true
                                        NowTimeThread.start()
                                    }

                                    override fun onPause() {
                                        playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp)
                                    }

                                    override fun onStart() {
                                        playBtn.setImageResource(R.drawable.ic_pause_white_24dp)
                                    }

                                    override fun onVideoSizeChanged() {
                                        SetSurfaceViewSize()
                                    }
                                })
                    }

                    override fun onDenied() {
                        Toast.makeText(applicationContext,"권한이 설정되지않았습니다.", Toast.LENGTH_SHORT).show()
                    }

                }
        )
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt("Position", MC.position)
        MC.releaseMedia()
    }

    private fun init() {
        orientation = windowManager.defaultDisplay.orientation
        h = Handler()
    }
    private fun SeekBarInit() {
        MediaProgressBar.setOnSeekBarChangeListener(this)
        VideoDuration.setText(getTimes(MC.duration))
        Log.i("mediaduration", getTimes(MC.duration))
    }
    private fun PermissionInit() {
        pModels = PermissionModels()
        pList = arrayOf(
                pModels.readExStorage
        )
        pChker = PermissionChker()
    }
    private fun SurfaceInit() {
        SurfaceView.setOnClickListener(this)
    }
    private fun AddonInit() {
        playBtn.setOnClickListener(this)
        ScreenChanger.setOnClickListener(this)
        TitleView.setText("Ramen")
        params = MediaProgressBar.layoutParams as ViewGroup.MarginLayoutParams
    }

    override fun onClick(v: View?) {
        when(v) {
            playBtn -> {
                if (MC.MediaPlaying) {
                    MC.PauseMedia()
                }else{
                    MC.StartMedia()
                }
            }
            SurfaceView -> {
                ChkUserTouch()
                if (!AddonCondition)
                    seekAddons()
            }
            ScreenChanger -> {
                ChangeScreenOrientation()
            }
        }
    }

    fun ChkUserTouch() {
        if (sec == 0){
            sec = 3000
            ChkUserRunnableStart()
            Log.i("Sec1" , sec.toString())
        }else {
            sec = 3000
            Log.i("Sec2" , sec.toString())
        }
    }

    fun seekAddons() {
        Addon.visibility = View.VISIBLE
        AddonCondition = true
        if (resources.configuration.orientation.equals(Configuration.ORIENTATION_LANDSCAPE)) {
            params.setMargins(0, 0, 0, 0)
            MediaProgressBar.thumb.alpha = 255
            MediaProgressBar.layoutParams = params
        }
    }
    fun hideAddons() {
        h.postDelayed(hideRunnable,500)

        params.setMargins(DPtoPx(-15) ,0,DPtoPx(-15),DPtoPx(-10))
        MediaProgressBar.thumb.alpha = 0
        MediaProgressBar.layoutParams = params
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

    fun ChkUserRunnableStart() {
        ChkUserTouchRunnable = Runnable {
            kotlin.run {
                if (sec < 100) {
                    Log.i("Sec3", sec.toString())
                    sec = 0
                    if (AddonCondition) {
                        hideAddons()
                    } else{
//                        if혼동 방지용
                    }
                } else {
                    sec -= 100
                    h.postDelayed(ChkUserTouchRunnable, 100)
                }
            }
        }
        h.postDelayed(ChkUserTouchRunnable, 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        pChker.RequestPermissionsResult(requestCode, pList, grantResults)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }
    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser){
            MC.SetSeek(progress)
            MC.position = progress
            Log.i("SeekTo",progress.toString())
        }
    }

    private fun ChangeScreenOrientation() {
        if(resources.configuration.orientation.equals(Configuration.ORIENTATION_PORTRAIT)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    private fun SetSurfaceViewSize() {

        val vW = MC.vWidth
        val vH = MC.vHeight

        val sW = windowManager.defaultDisplay.width

        SurfaceView.holder.setFixedSize(sW, ((vH.toFloat() / vW.toFloat()) * sW.toFloat()).toInt())
        if (resources.configuration.orientation.equals(Configuration.ORIENTATION_LANDSCAPE)) {
            SurfaceViewBG.layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT
        }else {
            SurfaceViewBG.layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT
        }
    }
    private fun DPtoPx(dp: Int): Int {
        val density = resources.displayMetrics.density

        return Math.round(dp.toFloat() * density)
    }
}

class NowTimeThread: Thread {
    var h: Handler
    constructor(h: Handler) {
        this.h = h
    }

    override fun run() {
        while (true) {

            h.sendEmptyMessage(0)

            try {
                Thread.sleep(50)
            }catch (ie: InterruptedException) {
                ie.printStackTrace()
            }
        }
    }
}