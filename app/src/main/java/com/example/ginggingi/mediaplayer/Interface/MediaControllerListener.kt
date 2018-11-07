package com.example.ginggingi.mediaplayer.Interface

interface MediaControllerListener {
    fun onMediaInit()
    fun onPause()
    fun onStart()
    fun onVideoSizeChanged()
}